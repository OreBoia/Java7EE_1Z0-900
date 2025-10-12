# Quiz Avanzato su Servizi REST con JAX-RS - Domande Miste con Codice

Questo quiz avanzato copre i concetti dei Servizi REST con JAX-RS in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Principi Fondamentali di REST

### 🔵 Domanda 1

Quale dei seguenti **NON** è un principio fondamentale dell'architettura REST?

- a) Stateless (Senza stato)
- b) Client-Server Architecture
- c) Session Management
- d) Uniform Interface

---

### 🟢 Domanda 2

Quali dei seguenti metodi HTTP sono **idempotenti**? (Seleziona tutti)

- a) `GET`
- b) `POST`
- c) `PUT`
- d) `DELETE`
- e) `PATCH`
- f) `HEAD`

---

### 💻 Domanda 3

Osserva questo scenario REST:

```
Client invia: GET /api/utenti/123
Server risponde: 200 OK
{
  "id": 123,
  "nome": "Mario",
  "email": "mario@example.com",
  "_links": {
    "self": "/api/utenti/123",
    "ordini": "/api/utenti/123/ordini",
    "modifica": "/api/utenti/123"
  }
}
```

Quale principio REST viene implementato attraverso la sezione `_links`?

- a) Stateless
- b) Cacheable
- c) HATEOAS (Hypermedia as the Engine of Application State)
- d) Layered System

---

## 2. JAX-RS Annotations e Configurazione

### 💻 Domanda 4

Analizza questa configurazione JAX-RS:

```java
@ApplicationPath("/api")
public class RestConfiguration extends Application {
    // Classe vuota
}

@Path("/prodotti")
public class ProdottoResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Prodotto getProdotto(@PathParam("id") int id) {
        return new Prodotto(id, "Laptop", 999.99);
    }
}
```

Qual è l'URI completo per accedere al prodotto con ID 5?

- a) `/prodotti/5`
- b) `/api/prodotti/5`
- c) `/RestConfiguration/prodotti/5`
- d) `/Application/api/prodotti/5`

---

### 🟢 Domanda 5

Quali delle seguenti sono **annotazioni JAX-RS valide** per mappare metodi HTTP? (Seleziona tutte)

- a) `@GET`
- b) `@POST`
- c) `@UPDATE`
- d) `@DELETE`
- e) `@PATCH`
- f) `@CREATE`

---

### 💻 Domanda 6

Osserva questa risorsa JAX-RS:

```java
@Path("/ordini")
public class OrdineResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creaOrdine(Ordine ordine) {
        Ordine nuovoOrdine = salvaOrdine(ordine);
        
        return Response.status(Response.Status.CREATED)
                       .entity(nuovoOrdine)
                       .header("Location", "/api/ordini/" + nuovoOrdine.getId())
                       .build();
    }
}
```

Quale codice di stato HTTP viene restituito in caso di successo?

- a) 200 OK
- b) 201 Created
- c) 202 Accepted
- d) 204 No Content

---

## 3. Content Negotiation e MediaType

### 💻 Domanda 7

Analizza questo metodo con content negotiation:

```java
@Path("/utenti")
public class UtenteResource {

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Utente getUtente(@PathParam("id") int id) {
        return new Utente(id, "Alice", "alice@example.com");
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String creaUtente(Utente utente) {
        salvaUtente(utente);
        return "Utente creato con successo";
    }
}
```

Se il client invia la richiesta `GET /api/utenti/1` con header `Accept: application/xml`, in che formato sarà la risposta?

- a) JSON
- b) XML
- c) Text Plain
- d) Errore 406 Not Acceptable

---

### 🔵 Domanda 8

Quale costante `MediaType` rappresenta il tipo MIME `"application/json"`?

- a) `MediaType.JSON`
- b) `MediaType.APPLICATION_JSON`
- c) `MediaType.JSON_TYPE`
- d) `MediaType.APP_JSON`

---

### 💻 Domanda 9

Osserva questa configurazione per upload di file:

```java
@Path("/files")
public class FileResource {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String uploadFile(@FormParam("file") InputStream fileStream,
                           @FormParam("filename") String filename) {
        // Logica di salvataggio file
        salvaFile(fileStream, filename);
        return "File " + filename + " caricato con successo";
    }
}
```

Quale tipo di richiesta HTTP dovrebbe inviare il client?

- a) GET con query parameters
- b) POST con JSON payload
- c) POST con multipart form data
- d) PUT con binary data

---

## 4. Parameter Injection

### 💻 Domanda 10

Analizza questo metodo con diverse iniezioni di parametri:

```java
@Path("/ricerca")
public class RicercaResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ricercaProdotti(
        @QueryParam("categoria") String categoria,
        @QueryParam("prezzo_min") @DefaultValue("0") double prezzoMin,
        @QueryParam("prezzo_max") Double prezzoMax,
        @QueryParam("ordina") @DefaultValue("nome") String ordinaPer,
        @HeaderParam("User-Agent") String userAgent,
        @Context UriInfo uriInfo) {
        
        // Logica di ricerca
        List<Prodotto> risultati = eseguiRicerca(categoria, prezzoMin, prezzoMax, ordinaPer);
        
        return Response.ok(risultati)
                       .header("X-Total-Count", risultati.size())
                       .header("X-User-Agent", userAgent)
                       .build();
    }
}
```

Se il client invia `GET /api/ricerca?categoria=elettronica&prezzo_max=500`, quale valore avrà `prezzoMin`?

- a) `null`
- b) `0`
- c) `0.0`
- d) Si verifica un errore

---

### 🟢 Domanda 11

Quali delle seguenti sono **annotazioni JAX-RS per parameter injection** valide? (Seleziona tutte)

- a) `@PathParam`
- b) `@QueryParam`
- c) `@RequestParam`
- d) `@FormParam`
- e) `@HeaderParam`
- f) `@SessionParam`

---

### 💻 Domanda 12

Osserva questo uso di path parameters:

```java
@Path("/utenti")
public class UtenteResource {

    @GET
    @Path("/{userId}/ordini/{ordineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ordine getOrdineUtente(@PathParam("userId") int userId,
                                  @PathParam("ordineId") int ordineId,
                                  @Context SecurityContext securityContext) {
        
        verificaAutorizzazione(userId, securityContext);
        return trovaOrdine(userId, ordineId);
    }
}
```

Per accedere all'ordine 789 dell'utente 123, quale URI dovrebbe usare il client?

- a) `/api/utenti/123/ordini/789`
- b) `/api/utenti?userId=123&ordineId=789`
- c) `/api/utenti/123?ordineId=789`
- d) `/api/ordini/789?userId=123`

---

## 5. Response Building e Status Codes

### 💻 Domanda 13

Analizza questo metodo che gestisce diverse condizioni:

```java
@Path("/prodotti")
public class ProdottoResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProdotto(@PathParam("id") int id) {
        
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorMessage("ID deve essere positivo"))
                          .build();
        }
        
        Prodotto prodotto = trovaProdotto(id);
        
        if (prodotto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(new ErrorMessage("Prodotto non trovato"))
                          .build();
        }
        
        if (!prodotto.isDisponibile()) {
            return Response.status(Response.Status.GONE)
                          .entity(new ErrorMessage("Prodotto non più disponibile"))
                          .build();
        }
        
        return Response.ok(prodotto)
                       .header("Last-Modified", prodotto.getUltimaModifica())
                       .header("ETag", prodotto.getVersione())
                       .build();
    }
}
```

Quale codice di stato viene restituito se viene richiesto un prodotto con `id = -5`?

- a) 404 Not Found
- b) 400 Bad Request
- c) 410 Gone
- d) 200 OK

---

### 🔵 Domanda 14

Quale metodo `Response.Status` rappresenta il codice HTTP 201?

- a) `Response.Status.OK`
- b) `Response.Status.CREATED`
- c) `Response.Status.ACCEPTED`
- d) `Response.Status.NO_CONTENT`

---

### 💻 Domanda 15

Osserva questo metodo per aggiornamento condizionale:

```java
@PUT
@Path("/{id}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response aggiornaProdotto(@PathParam("id") int id,
                                Prodotto prodotto,
                                @HeaderParam("If-Match") String ifMatch) {
    
    Prodotto esistente = trovaProdotto(id);
    
    if (esistente == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    if (ifMatch != null && !ifMatch.equals(esistente.getETag())) {
        return Response.status(Response.Status.PRECONDITION_FAILED)
                      .entity(new ErrorMessage("Versione del prodotto obsoleta"))
                      .build();
    }
    
    Prodotto aggiornato = aggiornaEPersisti(id, prodotto);
    
    return Response.ok(aggiornato)
                   .header("ETag", aggiornato.getETag())
                   .build();
}
```

Quale codice di stato viene restituito se l'header `If-Match` non corrisponde all'ETag del prodotto esistente?

- a) 409 Conflict
- b) 412 Precondition Failed
- c) 428 Precondition Required
- d) 422 Unprocessable Entity

---

## 6. Exception Handling

### 💻 Domanda 16

Analizza questa gestione delle eccezioni:

```java
// Eccezione personalizzata
public class ProdottoNotFoundException extends Exception {
    private int prodottoId;
    
    public ProdottoNotFoundException(int id) {
        super("Prodotto con ID " + id + " non trovato");
        this.prodottoId = id;
    }
    
    public int getProdottoId() { return prodottoId; }
}

// Exception Mapper
@Provider
public class ProdottoNotFoundExceptionMapper 
    implements ExceptionMapper<ProdottoNotFoundException> {
    
    @Override
    public Response toResponse(ProdottoNotFoundException exception) {
        ErrorDetail error = new ErrorDetail(
            "PRODUCT_NOT_FOUND",
            exception.getMessage(),
            exception.getProdottoId()
        );
        
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

// Risorsa che usa l'eccezione
@Path("/prodotti")
public class ProdottoResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Prodotto getProdotto(@PathParam("id") int id) 
            throws ProdottoNotFoundException {
        
        Prodotto p = trovaProdotto(id);
        if (p == null) {
            throw new ProdottoNotFoundException(id);
        }
        return p;
    }
}
```

Cosa succede quando viene richiesto un prodotto inesistente?

- a) Viene lanciata un'eccezione 500 Internal Server Error
- b) L'ExceptionMapper cattura l'eccezione e restituisce 404 con dettagli JSON
- c) Il metodo restituisce null
- d) Si verifica un errore di compilazione

---

### 🟢 Domanda 17

Quali delle seguenti sono **WebApplicationException** predefinite in JAX-RS? (Seleziona tutte)

- a) `NotFoundException`
- b) `BadRequestException`
- c) `ValidationException`
- d) `InternalServerErrorException`
- e) `ForbiddenException`

---

### 💻 Domanda 18

Osserva questo uso diretto di WebApplicationException:

```java
@Path("/admin")
public class AdminResource {

    @DELETE
    @Path("/utenti/{id}")
    public Response eliminaUtente(@PathParam("id") int id,
                                 @Context SecurityContext security) {
        
        if (!security.isUserInRole("admin")) {
            throw new ForbiddenException("Operazione riservata agli amministratori");
        }
        
        Utente utente = trovaUtente(id);
        if (utente == null) {
            throw new NotFoundException("Utente " + id + " non trovato");
        }
        
        if (utente.hasOrdiniAttivi()) {
            throw new BadRequestException("Impossibile eliminare utente con ordini attivi");
        }
        
        eliminaUtente(utente);
        return Response.noContent().build();
    }
}
```

Quale codice di stato viene restituito se un utente non-admin tenta di eliminare un utente?

- a) 401 Unauthorized
- b) 403 Forbidden
- c) 400 Bad Request
- d) 404 Not Found

---

## 7. JAX-RS 2.0 Client API

### 💻 Domanda 19

Analizza questo client JAX-RS 2.0:

```java
public class ProdottoClient {
    
    public List<Prodotto> cercaProdotti(String categoria, double prezzoMax) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client
                .target("http://api.negozio.com/api/prodotti")
                .queryParam("categoria", categoria)
                .queryParam("prezzo_max", prezzoMax);
            
            return target
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Prodotto>>() {});
        }
    }
    
    public Prodotto creaProdotto(Prodotto nuovo) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("http://api.negozio.com/api/prodotti");
            
            Response response = target
                .request()
                .post(Entity.json(nuovo));
            
            if (response.getStatus() == 201) {
                Prodotto creato = response.readEntity(Prodotto.class);
                response.close();
                return creato;
            } else {
                String error = response.readEntity(String.class);
                response.close();
                throw new RuntimeException("Errore creazione: " + error);
            }
        }
    }
}
```

Perché si usa `GenericType<List<Prodotto>>` invece di `List<Prodotto>.class`?

- a) Per performance migliori
- b) Per preservare l'informazione sui tipi generici a runtime
- c) È obbligatorio per tutte le collezioni
- d) Per compatibilità con versioni precedenti

---

### 🔵 Domanda 20

Nel codice precedente, cosa fa `Entity.json(nuovo)`?

- a) Converte l'oggetto in una stringa JSON
- b) Crea un Entity con Content-Type application/json e serializza l'oggetto
- c) Valida che l'oggetto sia un JSON valido
- d) Comprime l'oggetto in formato JSON

---

### 💻 Domanda 21

Osserva questo client con gestione degli errori:

```java
public class UtenteClient {
    
    public Utente getUtente(int id) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client
                .target("http://api.example.com/utenti/{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();
            
            try {
                switch (response.getStatus()) {
                    case 200:
                        return response.readEntity(Utente.class);
                    case 404:
                        return null;
                    case 403:
                        throw new SecurityException("Accesso negato");
                    default:
                        throw new RuntimeException("Errore HTTP: " + response.getStatus());
                }
            } finally {
                response.close();
            }
        }
    }
}
```

Cosa fa il metodo `resolveTemplate("id", id)`?

- a) Sostituisce il placeholder `{id}` nell'URI con il valore fornito
- b) Aggiunge un query parameter `id`
- c) Imposta un header `id`
- d) Crea un nuovo template per l'URI

---

## 8. Sub-Resource Locators

### 💻 Domanda 22

Analizza questo esempio di sub-resource locators:

```java
@Path("/utenti")
public class UtenteResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtente(@PathParam("id") int id) {
        return trovaUtente(id);
    }
    
    @Path("/{id}/ordini")
    public OrdineSubResource getOrdiniUtente(@PathParam("id") int userId) {
        Utente utente = trovaUtente(userId);
        if (utente == null) {
            throw new NotFoundException("Utente non trovato");
        }
        return new OrdineSubResource(userId);
    }
}

// Sub-resource class
public class OrdineSubResource {
    private int userId;
    
    public OrdineSubResource(int userId) {
        this.userId = userId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getOrdini() {
        return trovaOrdiniUtente(userId);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response creaOrdine(Ordine ordine) {
        ordine.setUserId(userId);
        Ordine nuovo = salvaOrdine(ordine);
        return Response.status(201).entity(nuovo).build();
    }
    
    @GET
    @Path("/{ordineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ordine getOrdine(@PathParam("ordineId") int ordineId) {
        return trovaOrdine(userId, ordineId);
    }
}
```

Quale delle seguenti **NON** è una caratteristica del metodo `getOrdiniUtente()`?

- a) È annotato solo con `@Path`, senza metodi HTTP
- b) Restituisce un'istanza di una classe sub-resource
- c) Viene automaticamente registrato come Provider JAX-RS
- d) Permette di creare API gerarchiche

---

### 🔵 Domanda 23

Per accedere a un ordine specifico dell'utente, quale URI completo dovrebbe essere utilizzato?

- a) `/api/utenti/{userId}/ordini/{ordineId}`
- b) `/api/ordini/{ordineId}?userId={userId}`
- c) `/api/utenti/{userId}?ordineId={ordineId}`
- d) `/api/OrdineSubResource/{ordineId}`

---

## 9. Filtri e Interceptors

### 💻 Domanda 24

Analizza questo filtro per logging delle richieste:

```java
@Provider
@Priority(Priorities.USER)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.info("REQUEST: " + requestContext.getMethod() + " " + 
                   requestContext.getUriInfo().getPath() + 
                   " from " + requestContext.getHeaderString("User-Agent"));
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        logger.info("RESPONSE: " + responseContext.getStatus() + 
                   " for " + requestContext.getMethod() + " " + 
                   requestContext.getUriInfo().getPath());
    }
}
```

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Token di autenticazione richiesto")
                        .build()
            );
        }
        
        String token = authHeader.substring("Bearer ".length());
        if (!isValidToken(token)) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Token non valido")
                        .build()
            );
        }
    }
    
    private boolean isValidToken(String token) {
        // Logica di validazione token
        return token != null && token.length() > 10;
    }
}
```

Quale filtro viene eseguito **per primo** in base alle priorità?

- a) LoggingFilter (Priority: USER)
- b) AuthenticationFilter (Priority: AUTHENTICATION)
- c) Entrambi hanno la stessa priorità
- d) L'ordine è casuale

---

### 🔵 Domanda 25

Cosa fa il metodo `requestContext.abortWith(response)` nel filtro di autenticazione?

- a) Registra un errore nel log
- b) Interrompe il processing della richiesta e restituisce la response specificata
- c) Imposta un timeout per la richiesta
- d) Reindirizza la richiesta a un altro endpoint

---

### 💻 Domanda 26

Osserva questo interceptor per compressione:

```java
@Provider
@Compress
public class GzipWriterInterceptor implements WriterInterceptor {
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) 
            throws IOException, WebApplicationException {
        
        // Ottiene l'OutputStream originale
        OutputStream originalStream = context.getOutputStream();
        
        // Crea un GZIPOutputStream per compressione
        GZIPOutputStream gzipStream = new GZIPOutputStream(originalStream);
        
        // Imposta il nuovo stream nel context
        context.setOutputStream(gzipStream);
        
        try {
            // Procede con la serializzazione normale
            context.proceed();
        } finally {
            gzipStream.finish();
        }
        
        // Aggiunge header per indicare la compressione
        context.getHeaders().add("Content-Encoding", "gzip");
    }
}

// Annotazione personalizzata per attivare la compressione
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Compress {
}

// Uso dell'interceptor
@Path("/files")
public class FileResource {
    
    @GET
    @Path("/large-data")
    @Compress
    @Produces(MediaType.APPLICATION_JSON)
    public List<LargeObject> getLargeData() {
        return generateLargeDataSet();
    }
}
```

Quale annotazione è necessaria perché l'interceptor venga applicato al metodo `getLargeData()`?

- a) `@Provider`
- b) `@Compress`
- c) `@NameBinding`
- d) `@Interceptor`

---

## 10. Best Practices e Performance

### 💻 Domanda 27

Analizza queste due implementazioni per la paginazione:

```java
// Approccio A: Paginazione semplice
@Path("/prodotti")
public class ProdottoResourceA {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Prodotto> getProdotti(@QueryParam("page") @DefaultValue("1") int page,
                                     @QueryParam("size") @DefaultValue("20") int size) {
        return prodottoService.findProdotti(page, size);
    }
}
```

```java
// Approccio B: Paginazione con metadati
@Path("/prodotti")
public class ProdottoResourceB {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProdotti(@QueryParam("page") @DefaultValue("1") int page,
                               @QueryParam("size") @DefaultValue("20") int size,
                               @Context UriInfo uriInfo) {
        
        PaginatedResult<Prodotto> result = prodottoService.findProdottiPaginated(page, size);
        
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        
        ResponseBuilder response = Response.ok(result.getContent())
            .header("X-Total-Count", result.getTotalElements())
            .header("X-Page-Count", result.getTotalPages())
            .header("X-Current-Page", page);
        
        // Link per navigazione
        if (result.hasNext()) {
            URI nextUri = uriBuilder.clone()
                .queryParam("page", page + 1)
                .queryParam("size", size)
                .build();
            response.header("Link", "<" + nextUri + ">; rel=\"next\"");
        }
        
        if (result.hasPrevious()) {
            URI prevUri = uriBuilder.clone()
                .queryParam("page", page - 1)
                .queryParam("size", size)
                .build();
            response.header("Link", "<" + prevUri + ">; rel=\"prev\"");
        }
        
        return response.build();
    }
}
```

Quale approccio è **migliore** per un'API REST ben progettata?

- a) Approccio A: più semplice e veloce
- b) Approccio B: fornisce metadati utili per la navigazione
- c) Sono equivalenti in termini di usabilità
- d) Approccio A: consuma meno risorse

---

### 🟢 Domanda 28

Quali delle seguenti sono **best practices** per servizi REST con JAX-RS? (Seleziona tutte)

- a) Usare nomi di risorse al plurale (`/utenti` invece di `/utente`)
- b) Restituire sempre codici di stato HTTP appropriati
- c) Implementare content negotiation quando necessario
- d) Mettere la logica di business direttamente nei metodi della risorsa
- e) Utilizzare versioning dell'API per evoluzione backward-compatible

---

### 💻 Domanda 29

Osserva questa implementazione per caching condizionale:

```java
@Path("/prodotti")
public class ProdottoResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProdotto(@PathParam("id") int id,
                               @Context Request request) {
        
        Prodotto prodotto = prodottoService.findById(id);
        if (prodotto == null) {
            throw new NotFoundException();
        }
        
        // Crea EntityTag basato su versione prodotto
        EntityTag etag = new EntityTag(prodotto.getVersione());
        
        // Controlla se il client ha una versione aggiornata
        ResponseBuilder builder = request.evaluatePreconditions(etag);
        
        if (builder != null) {
            // Client ha versione aggiornata, restituisce 304 Not Modified
            return builder.build();
        }
        
        // Costruisce risposta completa con ETag
        return Response.ok(prodotto)
                       .tag(etag)
                       .lastModified(prodotto.getUltimaModifica())
                       .cacheControl(buildCacheControl())
                       .build();
    }
    
    private CacheControl buildCacheControl() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(3600); // 1 ora
        cc.setPrivate(false); // Può essere cached da proxy
        return cc;
    }
}
```

Quale vantaggio principale offre questa implementazione?

- a) Migliore sicurezza dei dati
- b) Riduzione del traffico di rete attraverso caching HTTP
- c) Maggiore velocità di elaborazione server-side
- d) Supporto automatico per multiple versioni API

---

### 🔵 Domanda 30

Nel codice precedente, quando viene restituito il codice di stato 304 Not Modified?

- a) Quando il prodotto non esiste
- b) Quando il client non fornisce l'header If-None-Match
- c) Quando l'ETag del client corrisponde all'ETag corrente del prodotto
- d) Quando si verifica un errore nella cache

---

---

## Risposte Corrette

### 1. **c)** Session Management

REST è stateless, quindi la gestione delle sessioni contraddice i principi REST.

### 2. **a, c, d, f)** GET, PUT, DELETE, HEAD

POST e PATCH non sono idempotenti perché possono avere effetti diversi se ripetuti.

### 3. **c)** HATEOAS (Hypermedia as the Engine of Application State)

La sezione `_links` fornisce hypermedia per guidare la navigazione del client.

### 4. **b)** `/api/prodotti/5`

`@ApplicationPath("/api")` definisce il prefisso base per tutti i servizi REST.

### 5. **a, b, d, e)** GET, POST, DELETE, PATCH

`@UPDATE` e `@CREATE` non esistono in JAX-RS.

### 6. **b)** 201 Created

`Response.Status.CREATED` corrisponde al codice HTTP 201.

### 7. **b)** XML

Il metodo produce sia JSON che XML, il client richiede XML tramite Accept header.

### 8. **b)** `MediaType.APPLICATION_JSON`

È la costante standard per il tipo MIME application/json.

### 9. **c)** POST con multipart form data

`@Consumes(MediaType.MULTIPART_FORM_DATA)` richiede questo tipo di richiesta.

### 10. **c)** `0.0`

`@DefaultValue("0")` imposta il valore di default per il parametro double.

### 11. **a, b, d, e)** PathParam, QueryParam, FormParam, HeaderParam

`@RequestParam` e `@SessionParam` non esistono in JAX-RS.

### 12. **a)** `/api/utenti/123/ordini/789`

Il path template viene risolto sostituendo i placeholder con i valori.

### 13. **b)** 400 Bad Request

La prima condizione (`id <= 0`) restituisce BAD_REQUEST.

### 14. **b)** `Response.Status.CREATED`

CREATED rappresenta il codice HTTP 201.

### 15. **b)** 412 Precondition Failed

Quando If-Match non corrisponde all'ETag, si restituisce PRECONDITION_FAILED.

### 16. **b)** L'ExceptionMapper cattura l'eccezione e restituisce 404 con dettagli JSON

Il mapper personalizzato gestisce l'eccezione e crea una risposta strutturata.

### 17. **a, b, d, e)** NotFoundException, BadRequestException, InternalServerErrorException, ForbiddenException

`ValidationException` non è una WebApplicationException standard di JAX-RS.

### 18. **b)** 403 Forbidden

`ForbiddenException` genera un codice di stato 403.

### 19. **b)** Per preservare l'informazione sui tipi generici a runtime

GenericType è necessario per deserializzare correttamente i tipi generici come `List<T>`.

### 20. **b)** Crea un Entity con Content-Type application/json e serializza l'oggetto

Entity.json() imposta il Content-Type e prepara l'oggetto per la serializzazione.

### 21. **a)** Sostituisce il placeholder `{id}` nell'URI con il valore fornito

resolveTemplate sostituisce i template parameters nell'URI.

### 22. **c)** Viene automaticamente registrato come Provider JAX-RS

I sub-resource non sono Provider, sono istanze create dai locator methods.

### 23. **a)** `/api/utenti/{userId}/ordini/{ordineId}`

L'URI segue la struttura gerarchica definita dai path annotations.

### 24. **b)** AuthenticationFilter (Priority: AUTHENTICATION)

AUTHENTICATION ha priorità più alta (valore numerico più basso) rispetto a USER.

### 25. **b)** Interrompe il processing della richiesta e restituisce la response specificata

abortWith ferma la catena di processing e restituisce immediatamente la risposta.

### 26. **b)** `@Compress`

L'annotazione @Compress (name binding) attiva l'interceptor per il metodo specifico.

### 27. **b)** Approccio B: fornisce metadati utili per la navigazione

L'approccio B segue le best practice REST fornendo header e link per la navigazione.

### 28. **a, b, c, e)** Nomi al plurale, codici di stato appropriati, content negotiation, versioning API

La logica di business non dovrebbe stare nelle risorse REST.

### 29. **b)** Riduzione del traffico di rete attraverso caching HTTP

Il caching condizionale evita trasferimenti non necessari quando il contenuto non è cambiato.

### 30. **c)** Quando l'ETag del client corrisponde all'ETag corrente del prodotto

304 Not Modified viene restituito quando `evaluatePreconditions` determina che il client ha la versione corrente.