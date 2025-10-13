# Best Practices e Performance per Servizi REST con JAX-RS

Questa dispensa copre le best practices, tecniche di ottimizzazione e pattern avanzati per lo sviluppo di servizi REST robusti e performanti con JAX-RS.

## Paginazione

La paginazione è essenziale per API che gestiscono grandi dataset. Esistono diversi approcci per implementarla efficacemente.

### Paginazione Offset-Based

**Descrizione dell'esempio**: Implementazione completa di un endpoint REST per la paginazione offset-based di prodotti. L'esempio mostra come gestire parametri di paginazione, validazione input, costruzione di metadati di risposta e link di navigazione conformi allo standard RFC 5988. Include header personalizzati per fornire informazioni complete sulla paginazione al client.

```java
@Path("/prodotti")
public class ProdottoResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProdotti(@QueryParam("page") @DefaultValue("0") int page,
                               @QueryParam("size") @DefaultValue("20") int size,
                               @QueryParam("sort") @DefaultValue("id") String sortField,
                               @QueryParam("direction") @DefaultValue("ASC") String sortDirection,
                               @Context UriInfo uriInfo) {
        
        // Validazione parametri
        if (page < 0) {
            throw new BadRequestException("Il numero di pagina deve essere >= 0");
        }
        
        if (size < 1 || size > 100) {
            throw new BadRequestException("La dimensione pagina deve essere tra 1 e 100");
        }
        
        // Ricerca paginata
        PaginatedResult<Prodotto> result = prodottoService.findPaginated(
            page, size, sortField, sortDirection
        );
        
        // Costruzione risposta con metadati
        return buildPaginatedResponse(result, page, size, uriInfo);
    }
    
    private Response buildPaginatedResponse(PaginatedResult<Prodotto> result, 
                                          int page, int size, UriInfo uriInfo) {
        
        ResponseBuilder responseBuilder = Response.ok(result.getContent());
        
        // Header con metadati paginazione
        responseBuilder.header("X-Total-Count", result.getTotalElements());
        responseBuilder.header("X-Page-Count", result.getTotalPages());
        responseBuilder.header("X-Current-Page", page);
        responseBuilder.header("X-Page-Size", size);
        
        // Link navigation (RFC 5988)
        StringBuilder linkHeader = new StringBuilder();
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        
        // First page
        URI firstUri = uriBuilder.clone()
            .queryParam("page", 0)
            .queryParam("size", size)
            .build();
        linkHeader.append("<").append(firstUri).append(">; rel=\"first\"");
        
        // Previous page
        if (page > 0) {
            URI prevUri = uriBuilder.clone()
                .queryParam("page", page - 1)
                .queryParam("size", size)
                .build();
            linkHeader.append(", <").append(prevUri).append(">; rel=\"prev\"");
        }
        
        // Next page
        if (page < result.getTotalPages() - 1) {
            URI nextUri = uriBuilder.clone()
                .queryParam("page", page + 1)
                .queryParam("size", size)
                .build();
            linkHeader.append(", <").append(nextUri).append(">; rel=\"next\"");
        }
        
        // Last page
        URI lastUri = uriBuilder.clone()
            .queryParam("page", result.getTotalPages() - 1)
            .queryParam("size", size)
            .build();
        linkHeader.append(", <").append(lastUri).append(">; rel=\"last\"");
        
        responseBuilder.header("Link", linkHeader.toString());
        
        return responseBuilder.build();
    }
}
```

### Paginazione Cursor-Based per Performance

**Descrizione dell'esempio**: Implementazione di paginazione cursor-based per ottimizzare le performance su dataset molto grandi. Questo approccio evita i problemi di performance dell'offset-based quando si naviga verso pagine profonde. L'esempio mostra l'uso di cursori `after` e `before`, la costruzione di una risposta strutturata con metadati di paginazione e link di navigazione.

```java
@Path("/eventi")
public class EventoResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventi(@QueryParam("after") String cursorAfter,
                             @QueryParam("before") String cursorBefore,
                             @QueryParam("limit") @DefaultValue("20") int limit,
                             @Context UriInfo uriInfo) {
        
        if (limit < 1 || limit > 100) {
            throw new BadRequestException("Limit deve essere tra 1 e 100");
        }
        
        // Non permettere entrambi i cursor
        if (cursorAfter != null && cursorBefore != null) {
            throw new BadRequestException("Specificare solo 'after' o 'before', non entrambi");
        }
        
        CursorPage<Evento> page = eventoService.findByCursor(
            cursorAfter, cursorBefore, limit
        );
        
        return buildCursorResponse(page, limit, uriInfo);
    }
    
    private Response buildCursorResponse(CursorPage<Evento> page, 
                                       int limit, UriInfo uriInfo) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", page.getItems());
        response.put("pageInfo", Map.of(
            "hasNextPage", page.hasNext(),
            "hasPreviousPage", page.hasPrevious(),
            "startCursor", page.getStartCursor(),
            "endCursor", page.getEndCursor()
        ));
        
        ResponseBuilder responseBuilder = Response.ok(response);
        
        // Link header per navigazione
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        List<String> links = new ArrayList<>();
        
        if (page.hasNext()) {
            URI nextUri = uriBuilder.clone()
                .queryParam("after", page.getEndCursor())
                .queryParam("limit", limit)
                .build();
            links.add("<" + nextUri + ">; rel=\"next\"");
        }
        
        if (page.hasPrevious()) {
            URI prevUri = uriBuilder.clone()
                .queryParam("before", page.getStartCursor())
                .queryParam("limit", limit)
                .build();
            links.add("<" + prevUri + ">; rel=\"prev\"");
        }
        
        if (!links.isEmpty()) {
            responseBuilder.header("Link", String.join(", ", links));
        }
        
        return responseBuilder.build();
    }
}
```

## Caching HTTP

### Cache Control Headers

**Descrizione dell'esempio**: Implementazione completa del caching HTTP con ETag e controlli di concorrenza ottimistica. L'esempio mostra come generare ETag basati su contenuto e versione, gestire conditional requests per risposte 304 Not Modified, implementare cache control headers appropriati, e gestire aggiornamenti con controlli di concorrenza per evitare conflitti.

```java
@Path("/articoli")
public class ArticoloResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticolo(@PathParam("id") int id,
                               @Context Request request) {
        
        Articolo articolo = articoloService.findById(id);
        if (articolo == null) {
            throw new NotFoundException("Articolo non trovato");
        }
        
        // ETag basato su hash del contenuto + versione
        String etag = generateETag(articolo);
        EntityTag entityTag = new EntityTag(etag);
        
        // Controllo conditional request
        ResponseBuilder builder = request.evaluatePreconditions(
            articolo.getUltimaModifica(), entityTag
        );
        
        if (builder != null) {
            // Client ha versione aggiornata, return 304 Not Modified
            return builder.build();
        }
        
        // Cache control per articoli pubblici
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3600); // 1 ora
        cacheControl.setPrivate(false); // Cacheable da proxy pubblici
        cacheControl.setNoStore(false);
        
        return Response.ok(articolo)
                      .cacheControl(cacheControl)
                      .tag(entityTag)
                      .lastModified(articolo.getUltimaModifica())
                      .build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response aggiornaArticolo(@PathParam("id") int id,
                                   Articolo articolo,
                                   @HeaderParam("If-Match") String ifMatchHeader,
                                   @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince) {
        
        Articolo esistente = articoloService.findById(id);
        if (esistente == null) {
            throw new NotFoundException("Articolo non trovato");
        }
        
        // Controllo concorrenza ottimistica
        if (ifMatchHeader != null) {
            String currentETag = generateETag(esistente);
            String clientETag = ifMatchHeader.replaceAll("\"", "");
            
            if (!currentETag.equals(clientETag)) {
                return Response.status(Response.Status.PRECONDITION_FAILED)
                              .entity(Map.of("error", "Articolo modificato da altro utente"))
                              .build();
            }
        }
        
        Articolo aggiornato = articoloService.aggiorna(id, articolo);
        
        // Invalida cache esistenti
        return Response.ok(aggiornato)
                      .tag(new EntityTag(generateETag(aggiornato)))
                      .lastModified(aggiornato.getUltimaModifica())
                      .header("Cache-Control", "no-cache") // Forza revalidation
                      .build();
    }
    
    private String generateETag(Articolo articolo) {
        String content = articolo.getId() + "|" + 
                        articolo.getVersione() + "|" + 
                        articolo.getUltimaModifica().toEpochMilli();
        
        return DigestUtils.md5Hex(content);
    }
}
```

### Cache con Vary Header

**Descrizione dell'esempio**: Implementazione di caching avanzato con header Vary per gestire content negotiation. L'esempio mostra come gestire cache differenziate basate su Accept e Accept-Language headers, generare ETag che includono formato e lingua, e utilizzare l'header Vary per informare i proxy di cache che la risposta varia in base a specifici header della richiesta.

```java
@GET
@Path("/contenuto")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Response getContenuto(@HeaderParam("Accept") String accept,
                           @HeaderParam("Accept-Language") String acceptLanguage,
                           @Context Request request) {
    
    // Determina formato e lingua
    String formato = accept.contains("xml") ? "xml" : "json";
    String lingua = parseLanguage(acceptLanguage);
    
    Contenuto contenuto = contenutoService.findByFormato(formato, lingua);
    
    // ETag che include formato e lingua
    String etagValue = contenuto.getId() + "-" + formato + "-" + lingua;
    EntityTag etag = new EntityTag(etagValue);
    
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
        return builder.build();
    }
    
    CacheControl cc = new CacheControl();
    cc.setMaxAge(1800); // 30 minuti
    
    return Response.ok(contenuto)
                  .cacheControl(cc)
                  .tag(etag)
                  .header("Vary", "Accept, Accept-Language") // Importante!
                  .build();
}
```

## Ottimizzazioni delle Performance

### Lazy Loading e Projection

**Descrizione dell'esempio**: Implementazione di ottimizzazione delle performance attraverso projection e lazy loading. L'esempio mostra come permettere ai client di specificare quali campi recuperare tramite query parameter `fields`, come ottimizzare le query database basandosi sui campi richiesti, e come implementare la serializzazione selettiva per ridurre il payload di risposta e migliorare le performance di rete.

```java
@Path("/utenti")
public class UtenteResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUtenti(@QueryParam("fields") String fieldsParam,
                             @QueryParam("embed") String embedParam,
                             @QueryParam("q") String searchQuery) {
        
        // Parse dei campi richiesti
        Set<String> fields = parseFields(fieldsParam);
        Set<String> embeds = parseEmbeds(embedParam);
        
        // Query ottimizzata basata sui campi richiesti
        List<Utente> utenti = utenteService.findWithProjection(
            searchQuery, fields, embeds
        );
        
        // Serializzazione selettiva
        if (!fields.isEmpty()) {
            List<Map<String, Object>> projected = utenti.stream()
                .map(u -> projectFields(u, fields))
                .collect(Collectors.toList());
            
            return Response.ok(projected).build();
        }
        
        return Response.ok(utenti).build();
    }
    
    private Set<String> parseFields(String fieldsParam) {
        if (fieldsParam == null) return Collections.emptySet();
        
        return Arrays.stream(fieldsParam.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
    }
    
    private Map<String, Object> projectFields(Utente utente, Set<String> fields) {
        Map<String, Object> result = new HashMap<>();
        
        if (fields.contains("id")) result.put("id", utente.getId());
        if (fields.contains("nome")) result.put("nome", utente.getNome());
        if (fields.contains("email")) result.put("email", utente.getEmail());
        // ... altri campi
        
        return result;
    }
}
```

### Bulk Operations

**Descrizione dell'esempio**: Implementazione di operazioni bulk per ottimizzare performance quando si lavora con multiple entità. L'esempio mostra come gestire la creazione e aggiornamento batch di prodotti, implementare validazione batch con raccolta di tutti gli errori, gestire risposte parziali con status code Multi-Status (207), e fornire feedback dettagliato sui risultati delle operazioni bulk.

```java
@Path("/prodotti")
public class ProdottoResource {
    
    @POST
    @Path("/bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProdottiBulk(List<NuovoProdottoRequest> requests) {
        
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Lista prodotti vuota");
        }
        
        if (requests.size() > 100) {
            throw new BadRequestException("Massimo 100 prodotti per operazione bulk");
        }
        
        // Validazione batch
        List<String> validationErrors = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            NuovoProdottoRequest req = requests.get(i);
            List<String> errors = validaProdotto(req);
            errors.forEach(err -> validationErrors.add("Item " + i + ": " + err));
        }
        
        if (!validationErrors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(Map.of("errors", validationErrors))
                          .build();
        }
        
        // Operazione bulk ottimizzata
        BulkResult<Prodotto> result = prodottoService.createBulk(requests);
        
        // Risposta dettagliata
        Map<String, Object> response = Map.of(
            "created", result.getCreated(),
            "failed", result.getFailed(),
            "totalRequested", requests.size(),
            "successCount", result.getSuccessCount(),
            "failureCount", result.getFailureCount()
        );
        
        int status = result.getFailureCount() == 0 ? 201 : 207; // Multi-Status
        
        return Response.status(status).entity(response).build();
    }
    
    @PATCH
    @Path("/bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProdottiBulk(List<AggiornamentoProdottoRequest> requests) {
        
        // Validazione e processing simile al create
        BulkResult<Prodotto> result = prodottoService.updateBulk(requests);
        
        // Invalida cache correlate
        cacheService.invalidatePattern("prodotti:*");
        
        return Response.ok(buildBulkResponse(result)).build();
    }
}
```

### Streaming per Grandi Dataset

**Descrizione dell'esempio**: Implementazione di streaming output per l'esportazione efficiente di grandi dataset. L'esempio mostra come utilizzare StreamingOutput per evitare l'accumulo di memoria quando si esportano migliaia di record, implementare paginazione interna per il processing batch, gestire il flush periodico per ottimizzare l'uso della memoria, e fornire file di download con header appropriati.

```java
@Path("/export")
public class ExportResource {
    
    @GET
    @Path("/utenti.csv")
    @Produces("text/csv")
    public Response exportUtentiCsv(@QueryParam("filtro") String filtro) {
        
        StreamingOutput stream = output -> {
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                
                // Header CSV
                writer.println("ID,Nome,Email,DataRegistrazione");
                
                // Streaming dei dati con paginazione interna
                int page = 0;
                int pageSize = 1000;
                boolean hasMore = true;
                
                while (hasMore) {
                    List<Utente> batch = utenteService.findBatch(filtro, page, pageSize);
                    
                    for (Utente utente : batch) {
                        writer.printf("%d,%s,%s,%s%n",
                            utente.getId(),
                            escapeCsv(utente.getNome()),
                            escapeCsv(utente.getEmail()),
                            utente.getDataRegistrazione().toString()
                        );
                        
                        // Flush periodico per evitare accumulo memoria
                        if (page % 10 == 0) {
                            writer.flush();
                        }
                    }
                    
                    hasMore = batch.size() == pageSize;
                    page++;
                }
            }
        };
        
        String filename = "utenti_" + LocalDate.now() + ".csv";
        
        return Response.ok(stream)
                      .header("Content-Disposition", 
                             "attachment; filename=\"" + filename + "\"")
                      .build();
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
```

## Versioning delle API

### URI Versioning

**Descrizione dell'esempio**: Implementazione di versioning delle API tramite URI path. Questo approccio mantiene versioni separate delle risorse attraverso path diversi (/api/v1/, /api/v2/), permettendo l'evoluzione dell'API mantenendo backward compatibility. Ogni versione ha le proprie classi resource e DTO, consentendo modifiche indipendenti tra le versioni.

```java
@Path("/api/v1/prodotti")
public class ProdottoV1Resource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProdottoV1DTO getProdotto(@PathParam("id") int id) {
        Prodotto prodotto = prodottoService.findById(id);
        return mapToV1DTO(prodotto);
    }
}

@Path("/api/v2/prodotti") 
public class ProdottoV2Resource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProdottoV2DTO getProdotto(@PathParam("id") int id) {
        Prodotto prodotto = prodottoService.findById(id);
        return mapToV2DTO(prodotto);
    }
}
```

### Header-based Versioning

**Descrizione dell'esempio**: Implementazione di versioning tramite header personalizzato API-Version. Questo approccio mantiene URL puliti mentre permette al client di specificare la versione desiderata tramite header. L'esempio mostra la gestione di multiple versioni in una singola classe resource, mapping dinamico ai DTO appropriati, e informazioni sulle versioni supportate nella risposta.

```java
@Path("/prodotti")
public class ProdottoVersionedResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProdotto(@PathParam("id") int id,
                               @HeaderParam("API-Version") @DefaultValue("1.0") String version) {
        
        Prodotto prodotto = prodottoService.findById(id);
        if (prodotto == null) {
            throw new NotFoundException("Prodotto non trovato");
        }
        
        Object dto = mapToVersionedDTO(prodotto, version);
        
        return Response.ok(dto)
                      .header("API-Version", version)
                      .header("Supported-Versions", "1.0, 1.1, 2.0")
                      .build();
    }
    
    private Object mapToVersionedDTO(Prodotto prodotto, String version) {
        switch (version) {
            case "1.0":
                return mapToV1DTO(prodotto);
            case "1.1":
                return mapToV1_1DTO(prodotto);
            case "2.0":
                return mapToV2DTO(prodotto);
            default:
                throw new BadRequestException("Versione API non supportata: " + version);
        }
    }
}
```

### Content-Type Versioning

**Descrizione dell'esempio**: Implementazione di versioning tramite Content-Type utilizzando media type personalizzati. Questo approccio sfrutta il content negotiation HTTP standard, permettendo ai client di specificare la versione desiderata tramite l'header Accept. L'esempio mostra l'uso di vendor-specific media types (application/vnd.api.v1+json) e fallback alla versione più recente.

```java
@Path("/prodotti")
public class ProdottoContentTypeVersioned {
    
    @GET
    @Path("/{id}")
    @Produces({"application/vnd.api.v1+json", "application/vnd.api.v2+json"})
    public Response getProdotto(@PathParam("id") int id,
                               @HeaderParam("Accept") String accept) {
        
        Prodotto prodotto = prodottoService.findById(id);
        if (prodotto == null) {
            throw new NotFoundException("Prodotto non trovato");
        }
        
        if (accept.contains("v1+json")) {
            return Response.ok(mapToV1DTO(prodotto))
                          .type("application/vnd.api.v1+json")
                          .build();
        } else if (accept.contains("v2+json")) {
            return Response.ok(mapToV2DTO(prodotto))
                          .type("application/vnd.api.v2+json")
                          .build();
        }
        
        // Default alla versione più recente
        return Response.ok(mapToV2DTO(prodotto))
                      .type("application/vnd.api.v2+json")
                      .build();
    }
}
```

## Rate Limiting

### Implementazione con Filter

**Descrizione dell'esempio**: Implementazione completa di rate limiting tramite JAX-RS Filter. L'esempio mostra come creare un filtro riusabile che intercetta le richieste, identifica i client tramite API key, JWT o IP address, applica limiti configurabili per finestra temporale, gestisce risposte 429 Too Many Requests con header informativi, e include un'annotazione custom per configurare i limiti.

```java
@Provider
@RateLimited
@Priority(Priorities.AUTHORIZATION + 100)
public class RateLimitFilter implements ContainerRequestFilter {
    
    @Inject
    private RateLimitService rateLimitService;
    
    @Context
    private ResourceInfo resourceInfo;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        // Estrae configurazione rate limit
        RateLimited annotation = resourceInfo.getResourceMethod()
                                           .getAnnotation(RateLimited.class);
        
        if (annotation == null) {
            annotation = resourceInfo.getResourceClass()
                                   .getAnnotation(RateLimited.class);
        }
        
        if (annotation == null) {
            return; // Nessun rate limiting configurato
        }
        
        // Identifica il client
        String clientId = extractClientId(requestContext);
        String key = buildRateLimitKey(clientId, resourceInfo);
        
        // Verifica rate limit
        RateLimitResult result = rateLimitService.checkRateLimit(
            key, 
            annotation.requestsPerWindow(), 
            annotation.windowSizeSeconds()
        );
        
        if (!result.isAllowed()) {
            
            // Calcola retry-after
            long retryAfter = result.getResetTimeSeconds() - System.currentTimeMillis() / 1000;
            
            Response response = Response.status(429) // Too Many Requests
                .entity(Map.of(
                    "error", "Rate limit exceeded",
                    "limit", annotation.requestsPerWindow(),
                    "remaining", result.getRemaining(),
                    "resetTime", result.getResetTimeSeconds()
                ))
                .header("X-RateLimit-Limit", annotation.requestsPerWindow())
                .header("X-RateLimit-Remaining", result.getRemaining())
                .header("X-RateLimit-Reset", result.getResetTimeSeconds())
                .header("Retry-After", retryAfter)
                .build();
            
            requestContext.abortWith(response);
            return;
        }
        
        // Aggiunge header informativi alla risposta
        requestContext.setProperty("rateLimitResult", result);
    }
    
    private String extractClientId(ContainerRequestContext context) {
        // Priorità: API Key > JWT Subject > IP Address
        
        String apiKey = context.getHeaderString("X-API-Key");
        if (apiKey != null) {
            return "api:" + apiKey;
        }
        
        String authorization = context.getHeaderString("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String subject = extractJwtSubject(authorization.substring(7));
                return "user:" + subject;
            } catch (Exception e) {
                // Fall back to IP
            }
        }
        
        return "ip:" + getClientIP(context);
    }
    
    private String buildRateLimitKey(String clientId, ResourceInfo resourceInfo) {
        return String.format("%s:%s:%s", 
                           clientId,
                           resourceInfo.getResourceClass().getSimpleName(),
                           resourceInfo.getResourceMethod().getName());
    }
}

// Annotazione per configurare rate limiting
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int requestsPerWindow() default 100;
    int windowSizeSeconds() default 3600; // 1 ora
}
```

### Uso del Rate Limiting

**Descrizione dell'esempio**: Applicazione pratica del rate limiting a diversi endpoint con configurazioni differenziate. L'esempio mostra come applicare limiti default a livello di classe (1000 req/ora), override per operazioni specifiche come POST con limiti più restrittivi (10 req/minuto), e limiti personalizzati per operazioni costose come le ricerche (100 req/minuto).

```java
@Path("/api/prodotti")
@RateLimited(requestsPerWindow = 1000, windowSizeSeconds = 3600) // Default per tutta la classe
public class ProdottoResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Prodotto> getProdotti() {
        // Eredita rate limit dalla classe: 1000 req/ora
        return prodottoService.findAll();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RateLimited(requestsPerWindow = 10, windowSizeSeconds = 60) // Override: 10 req/minuto
    public Response creaProdotto(Prodotto prodotto) {
        // Limite più restrittivo per operazioni di scrittura
        Prodotto created = prodottoService.create(prodotto);
        return Response.status(201).entity(created).build();
    }
    
    @GET
    @Path("/search")
    @RateLimited(requestsPerWindow = 100, windowSizeSeconds = 60) // 100 ricerche/minuto
    public List<Prodotto> cercaProdotti(@QueryParam("q") String query) {
        // Ricerca è operazione costosa, limite più basso
        return prodottoService.search(query);
    }
}
```

## Monitoring e Metriche

### Filter per Metriche

**Descrizione dell'esempio**: Implementazione di un filtro per il monitoraggio e la raccolta di metriche delle API REST. L'esempio mostra come intercettare richieste e risposte per misurare performance, contare richieste per endpoint e metodo HTTP, registrare tempi di risposta, categorizzare per status code, e integrare con sistemi di monitoraggio per analisi delle performance e troubleshooting.

```java
@Provider
@Priority(Priorities.USER)
public class MetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    @Inject
    private MetricsService metricsService;
    
    private static final String START_TIME = "request.startTime";
    private static final String ENDPOINT = "request.endpoint";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        requestContext.setProperty(START_TIME, System.currentTimeMillis());
        
        String endpoint = requestContext.getMethod() + " " + 
                         requestContext.getUriInfo().getPath();
        requestContext.setProperty(ENDPOINT, endpoint);
        
        // Conta richieste in ingresso
        metricsService.incrementCounter("http.requests.total", 
                                      "method", requestContext.getMethod(),
                                      "endpoint", endpoint);
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        Long startTime = (Long) requestContext.getProperty(START_TIME);
        String endpoint = (String) requestContext.getProperty(ENDPOINT);
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Registra durata risposta
            metricsService.recordTimer("http.requests.duration", 
                                     duration,
                                     "method", requestContext.getMethod(),
                                     "endpoint", endpoint,
                                     "status", String.valueOf(responseContext.getStatus()));
            
            // Conta per status code
            metricsService.incrementCounter("http.responses.total",
                                          "method", requestContext.getMethod(),
                                          "endpoint", endpoint,
                                          "status", String.valueOf(responseContext.getStatus()));
        }
    }
}
```

## Glossario

| Termine | Definizione |
|---------|-------------|
| **Offset-based Pagination** | Paginazione usando numero pagina e dimensione |
| **Cursor-based Pagination** | Paginazione usando cursori per performance migliori |
| **ETag** | Header per controllo cache e concorrenza ottimistica |
| **Cache-Control** | Header per controllare il comportamento della cache |
| **Conditional Request** | Richiesta che include header per cache validation |
| **Vary Header** | Header che specifica quali header influenzano la cache |
| **Projection** | Selezione di specifici campi da restituire |
| **Bulk Operation** | Operazione su multipli elementi in una singola richiesta |
| **Streaming Output** | Invio di dati in streaming per gestire grandi dataset |
| **Rate Limiting** | Limitazione del numero di richieste per prevenire abusi |
| **Content Negotiation** | Processo di selezione del formato di risposta appropriato |
