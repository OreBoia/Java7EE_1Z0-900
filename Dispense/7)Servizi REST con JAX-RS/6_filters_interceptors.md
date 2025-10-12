# Filtri e Interceptors in JAX-RS 2.0 - Architettura delle Cross-Cutting Concerns

## Introduzione Teorica

I Filtri e gli Interceptors rappresentano l'implementazione del **Cross-Cutting Concerns Pattern** in JAX-RS 2.0, una delle innovazioni architetturali più significative per la gestione di preoccupazioni trasversali nelle applicazioni REST.

### Problemi Architetturali Risolti

1. **Separation of Concerns**: Separano la logica di business dalle preoccupazioni tecniche (autenticazione, logging, ecc.)
2. **Code Duplication**: Evitano la duplicazione di codice trasversale in ogni endpoint
3. **Cross-Cutting Concerns**: Gestiscono aspetti che "attraversano" tutti i livelli dell'applicazione
4. **Non-Invasive Processing**: Permettono modifiche senza alterare il codice esistente delle risorse

### Vantaggi Architetturali

I Filtri e gli Interceptors sono una delle novità più importanti di JAX-RS 2.0 dal punto di vista architetturale. Implementano il **Chain of Responsibility Pattern** e l'**Aspect-Oriented Programming (AOP)**, permettendo di:

- **Intercettare e modificare** richieste/risposte HTTP in modo non invasivo
- **Implementare funzionalità trasversali** come autenticazione, logging, compressione e caching
- **Creare pipeline modulari** di elaborazione configurabili dinamicamente
- **Garantire la separazione delle responsabilità** tra logica di business e concerns tecnici

## Concetti Fondamentali - Teoria dei Layer di Elaborazione

### Differenza Teorica tra Filtri e Interceptors

La distinzione tra Filtri e Interceptors riflette due livelli diversi di astrazione nel modello di elaborazione HTTP:

**Teoria dei Livelli di Elaborazione**:
1. **Metadata Layer** (Filtri): Gestisce metadati HTTP (header, URI, status code)
2. **Content Layer** (Interceptors): Gestisce il contenuto delle entity durante marshalling/unmarshalling

- **Filtri**: Implementano il **Decorator Pattern** a livello di **metadati** HTTP
  - Operano su header, URI, parametri, status code
  - Eseguiti prima/dopo il processing principale
  - Possono interrompere completamente la catena di elaborazione (`abortWith()`)
  - Ideali per: autenticazione, autorizzazione, logging, CORS, caching

- **Interceptors**: Implementano il **Interceptor Pattern** a livello di **contenuto** 
  - Operano sui dati durante serializzazione/deserializzazione
  - Integrati nel processo di marshalling delle entity
  - Non possono interrompere ma solo modificare il flusso dei dati
  - Ideali per: compressione, crittografia, trasformazione dati, validazione contenuto

### Architettura Simmetrica Server vs Client

**Principio di Simmetria Architetturale**: JAX-RS implementa una architettura simmetrica dove gli stessi pattern sono applicabili sia lato server che lato client, facilitando il riuso di codice e la comprensione concettuale.

**Teoria della Dualità Client-Server**:

| Tipo | Server (Container) | Client | Responsabilità Teorica |
|------|---------|---------|---------|
| **Request Filter** | `ContainerRequestFilter` | `ClientRequestFilter` | **Pre-processing**: Modifiche prima dell'elaborazione |
| **Response Filter** | `ContainerResponseFilter` | `ClientResponseFilter` | **Post-processing**: Modifiche dopo l'elaborazione |
| **Reader Interceptor** | `ReaderInterceptor` | `ReaderInterceptor` | **Input Processing**: Unmarshalling delle entity in ingresso |
| **Writer Interceptor** | `WriterInterceptor` | `WriterInterceptor` | **Output Processing**: Marshalling delle entity in uscita |

**Flusso di Elaborazione Simmetrico**:
- **Server**: Request Filter → Reader Interceptor → Resource Method → Writer Interceptor → Response Filter
- **Client**: Request Filter → Writer Interceptor → HTTP Call → Reader Interceptor → Response Filter

## Request e Response Filters - Implementazione del Chain Pattern

### Request Filter Base - Teoria dell'Authentication Gate Pattern

**Pattern Implementato**: Il Request Filter implementa l'**Authentication Gate Pattern**, fungendo da punto di controllo obbligatorio per tutte le richieste.

**Principi Teorici**:

- **Early Validation**: Validazione delle credenziali prima di qualsiasi elaborazione costosa
- **Fail-Fast Security**: Interruzione immediata per richieste non autorizzate
- **Context Enrichment**: Arricchimento del contesto con informazioni utente per i livelli successivi
- **Stateless Authentication**: Validazione basata su token senza stato del server

```java
/* AUTHENTICATION GATE FILTER
 * Teoria: Implementa il Gateway Pattern per il controllo degli accessi
 * Posizionato strategicamente come primo controllo nella catena
 */
@Provider  // Registrazione automatica nel JAX-RS container
@Priority(Priorities.AUTHENTICATION)  // Priorità alta: eseguito per primo
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        /* HEADER EXTRACTION PATTERN:
         * Teoria: Estrazione delle credenziali dal protocollo HTTP standard
         */
        String authHeader = requestContext.getHeaderString("Authorization");
        
        /* FAIL-FAST VALIDATION PATTERN:
         * Teoria: Validazione immediata per ridurre il costo computazionale
         * delle richieste non autorizzate
         */
        if (authHeader == null) {
            /* EARLY TERMINATION PATTERN:
             * Teoria: abortWith() interrompe completamente la catena di processing
             * implementando il Circuit Breaker pattern per la sicurezza
             */
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Token di autenticazione richiesto")
                        .build()
            );
            return; // Terminazione immediata, nessun processing successivo
        }
        
        /* PROTOCOL COMPLIANCE VALIDATION:
         * Teoria: Verifica conformità al protocollo Bearer Token (RFC 6750)
         */
        if (!authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Formato token non valido")
                        .build()
            );
            return;
        }
        
        /* TOKEN EXTRACTION:
         * Teoria: Parsing del token dalla convenzione "Bearer <token>"
         */
        String token = authHeader.substring("Bearer ".length());
        
        try {
            /* TOKEN VALIDATION PATTERN:
             * Teoria: Validazione crittografica del token (JWT, OAuth, etc.)
             * con gestione delle eccezioni per token corrotti o scaduti
             */
            Claims claims = validateToken(token);
            
            /* CONTEXT ENRICHMENT PATTERN:
             * Teoria: Arricchimento del contesto della richiesta con metadati utente
             * Questi dati saranno disponibili ai filtri e resource method successivi
             */
            requestContext.setProperty("userId", claims.getSubject());
            requestContext.setProperty("userRoles", claims.get("roles"));
            
            // Qui il processing continua normalmente (nessun abortWith())
            
        } catch (InvalidTokenException e) {
            /* EXCEPTION-BASED SECURITY:
             * Teoria: La gestione delle eccezioni diventa parte del controllo di flusso
             * per garantire che token invalidi non passino mai la validazione
             */
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Token non valido o scaduto")
                        .build()
            );
        }
    }
    
    private Claims validateToken(String token) throws InvalidTokenException {
        // Logica di validazione JWT token
        // Throw InvalidTokenException se non valido
        return null; // placeholder
    }
}
```

### Response Filter per CORS - Cross-Origin Security Pattern

**Teoria del CORS (Cross-Origin Resource Sharing)**:
Il CORS risolve il problema della **Same-Origin Policy** dei browser, permettendo accesso controllato da domini diversi.

**Pattern Implementato**: **Header Decoration Pattern** per la modifica delle risposte HTTP senza alterare la logica business.

```java
/* CORS RESPONSE DECORATOR:
 * Teoria: Implementa il Decorator Pattern per aggiungere header CORS
 * a tutte le risposte HTTP senza modificare la logica delle risorse
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)  // Priorità per decorazione header
public class CorsFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        /* UNIVERSAL CORS HEADERS:
         * Teoria: Aggiunta sistematica di header CORS per conformità browser
         * Implementa il "Permissive CORS" pattern (attenzione alla sicurezza)
         */
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
                                       "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
                                       "Content-Type, Authorization, X-Requested-With");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
        
        /* PREFLIGHT REQUEST HANDLING:
         * Teoria: Gestione delle richieste OPTIONS per CORS preflight
         * secondo le specifiche W3C CORS
         */
        if ("OPTIONS".equals(requestContext.getMethod())) {
            responseContext.setStatus(Response.Status.OK.getStatusCode());
            responseContext.setEntity(null);  // Risposta vuota per preflight
        }
    }
}
```

### Filtro Combinato Request/Response per Logging

```java
@Provider
@Priority(Priorities.USER)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        String requestId = UUID.randomUUID().toString();
        requestContext.setProperty("requestId", requestId);
        
        logger.info("REQUEST [{}] {} {} from {} - User-Agent: {}", 
                   requestId,
                   requestContext.getMethod(),
                   requestContext.getUriInfo().getPath(),
                   getClientIP(requestContext),
                   requestContext.getHeaderString("User-Agent"));
        
        // Log dei parametri query
        MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
        if (!params.isEmpty()) {
            logger.debug("REQUEST [{}] Query Parameters: {}", requestId, params);
        }
        
        // Timestamp per calcolare durata
        requestContext.setProperty("startTime", System.currentTimeMillis());
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        String requestId = (String) requestContext.getProperty("requestId");
        Long startTime = (Long) requestContext.getProperty("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        logger.info("RESPONSE [{}] {} - {} bytes in {}ms", 
                   requestId,
                   responseContext.getStatus(),
                   getEntitySize(responseContext),
                   duration);
        
        // Warning per risposte lente
        if (duration > 2000) {
            logger.warn("SLOW RESPONSE [{}] took {}ms", requestId, duration);
        }
    }
    
    private String getClientIP(ContainerRequestContext context) {
        // Implementazione per estrarre IP client
        return "unknown"; // placeholder
    }
    
    private long getEntitySize(ContainerResponseContext context) {
        Object entity = context.getEntity();
        if (entity == null) return 0;
        
        if (entity instanceof String) {
            return ((String) entity).length();
        }
        
        return -1; // Non determinabile
    }
}
```

## Priorità e Ordinamento - Teoria dell'Execution Chain

### Sistema di Priorità - Chain of Responsibility Ordinato

**Principio Teorico**: L'ordinamento dei filtri implementa una **Chain of Responsibility ordinata** dove ogni anello ha una priorità specifica che determina la sua posizione nella catena.

**Teoria delle Priorità**:
- **Valori numerici bassi = Priorità alta**: Implementa il principio di "importanza inversa"
- **Determinismo**: Garantisce un ordinamento prevedibile e riproducibile
- **Separazione delle responsabilità**: Ogni fascia di priorità ha uno scopo architetturale specifico

JAX-RS utilizza l'annotazione `@Priority` per determinare l'ordine di esecuzione secondo il **Ordered Chain Pattern**.

```java
/* PRIORITY CONSTANTS - Architectural Layers
 * Teoria: Ogni costante rappresenta un layer architetturale specifico
 * con responsabilità ben definite nel processing HTTP
 */
public final class Priorities {
    /* SECURITY LAYER: Prima linea di difesa */
    public static final int AUTHENTICATION = 1000;      // Identificazione utente
    public static final int AUTHORIZATION = 2000;       // Controllo permessi
    
    /* PROTOCOL LAYER: Gestione protocollo HTTP */
    public static final int HEADER_DECORATOR = 3000;    // Manipolazione header
    
    /* DATA LAYER: Trasformazione contenuto */
    public static final int ENTITY_CODER = 4000;        // Encoding/decoding entity
    
    /* APPLICATION LAYER: Logica applicativa */
    public static final int USER = 5000;                // Filtri definiti dall'utente
    
    /* TEORICA GERARCHIA DI ESECUZIONE:
     * 1. Security (1000-2999): Controlli di sicurezza obbligatori
     * 2. Protocol (3000-3999): Manipolazione protocollo HTTP
     * 3. Data (4000-4999): Trasformazione dei dati
     * 4. Application (5000+): Logica specifica dell'applicazione
     */
}

// Esempi di utilizzo
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter { /*...*/ }

@Provider
@Priority(Priorities.AUTHORIZATION) 
public class RoleFilter implements ContainerRequestFilter { /*...*/ }

@Provider
@Priority(Priorities.USER + 100) // Priorità personalizzata
public class CustomFilter implements ContainerRequestFilter { /*...*/ }
```

### Catena di Execution

```java
// REQUEST FILTERS (ordine: priorità crescente)
// 1. AuthenticationFilter (AUTHENTICATION = 1000)
// 2. AuthorizationFilter (AUTHORIZATION = 2000)  
// 3. LoggingFilter (USER = 5000)

// --- RESOURCE METHOD EXECUTION ---

// RESPONSE FILTERS (ordine: priorità decrescente)
// 1. LoggingFilter (USER = 5000)
// 2. AuthorizationFilter (AUTHORIZATION = 2000)
// 3. AuthenticationFilter (AUTHENTICATION = 1000)
```

## Name Binding - Selective Filter Application Pattern

**Teoria del Name Binding**: Implementa il **Selective Decoration Pattern**, permettendo l'applicazione mirata di filtri/interceptors solo agli endpoint che ne hanno bisogno, evitando l'overhead di processing globale.

**Principi Architetturali**:

- **Granular Control**: Controllo fine sulla applicazione dei filtri
- **Performance Optimization**: Evita elaborazione non necessaria su endpoint che non la richiedono  
- **Separation of Concerns**: Ogni annotazione rappresenta una preoccupazione specifica
- **Declarative Programming**: Configurazione dichiarativa tramite annotazioni

Il Name Binding permette di applicare filtri/interceptors solo a specifici endpoint tramite il **Annotation-Driven Configuration Pattern**.

### Definizione di Name Binding Annotation - Custom Meta-Annotations

```java
/* SECURITY NAME BINDING:
 * Teoria: Meta-annotazione per controllo accesso granulare
 * Implementa il Role-Based Access Control (RBAC) pattern
 */
@NameBinding  // Meta-annotazione JAX-RS per binding selettivo
@Target({ElementType.TYPE, ElementType.METHOD})  // Applicabile a classi e metodi
@Retention(RetentionPolicy.RUNTIME)  // Disponibile a runtime per reflection
public @interface Secured {
    String[] roles() default {};  // Ruoli richiesti, array vuoto = solo autenticazione
}

/* CACHING NAME BINDING:
 * Teoria: Meta-annotazione per controllo cache HTTP
 * Implementa il Caching Strategy pattern con configurazione dichiarativa
 */
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {
    int maxAge() default 3600;  // TTL cache in secondi
}

/* RATE LIMITING NAME BINDING:
 * Teoria: Meta-annotazione per throttling delle richieste
 * Implementa il Rate Limiting pattern per protezione DoS
 */
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int requestsPerMinute() default 100;
}
```

### Filtro con Name Binding

```java
@Provider
@Secured
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {
    
    @Context
    private ResourceInfo resourceInfo;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        // Ottiene l'annotazione dal metodo o dalla classe
        Secured secured = resourceInfo.getResourceMethod().getAnnotation(Secured.class);
        if (secured == null) {
            secured = resourceInfo.getResourceClass().getAnnotation(Secured.class);
        }
        
        String[] requiredRoles = secured.roles();
        
        // Estrae ruoli dell'utente dal token (impostato da AuthenticationFilter)
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) requestContext.getProperty("userRoles");
        
        if (userRoles == null) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build()
            );
            return;
        }
        
        // Verifica che l'utente abbia almeno uno dei ruoli richiesti
        boolean hasRole = requiredRoles.length == 0 || // Nessun ruolo specifico richiesto
                         Arrays.stream(requiredRoles)
                               .anyMatch(userRoles::contains);
        
        if (!hasRole) {
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity("Ruoli richiesti: " + Arrays.toString(requiredRoles))
                        .build()
            );
        }
    }
}
```

### Utilizzo nei Resource Methods

```java
@Path("/admin")
public class AdminResource {
    
    // Applica SecurityFilter con ruolo admin
    @GET
    @Path("/users")
    @Secured(roles = {"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    // Applica SecurityFilter senza ruoli specifici (solo autenticazione)
    @GET
    @Path("/profile") 
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public User getProfile(@Context SecurityContext security) {
        String userId = (String) security.getUserPrincipal().getName();
        return userService.findById(userId);
    }
}

@Path("/products")
@Cached(maxAge = 1800) // 30 minuti di cache per tutta la classe
public class ProductResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts() {
        // Eredita @Cached dalla classe
        return productService.findAll();
    }
    
    @GET
    @Path("/{id}")
    @Cached(maxAge = 3600) // Override: 1 ora per questo metodo specifico
    @Produces(MediaType.APPLICATION_JSON)
    public Product getProduct(@PathParam("id") int id) {
        return productService.findById(id);
    }
    
    @POST
    @Secured(roles = {"admin", "editor"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(Product product) {
        // Non eredita @Cached (non appropriato per POST)
        Product created = productService.create(product);
        return Response.status(201).entity(created).build();
    }
}
```

## Reader e Writer Interceptors - Content Processing Layer

**Teoria degli Interceptors**: Operano al **Content Processing Layer**, intercettando il flusso di serializzazione/deserializzazione delle entity HTTP. Implementano il **Interceptor Pattern** integrato nel ciclo di vita delle entity.

**Differenza Fondamentale dai Filtri**:
- **Filtri**: Operano sui metadati (header, URI, status) - **Control Flow**
- **Interceptors**: Operano sui contenuti (entity body) - **Data Flow**

**Timing di Esecuzione**:
- **WriterInterceptor**: Durante la serializzazione (Java Object → Stream HTTP)
- **ReaderInterceptor**: Durante la deserializzazione (Stream HTTP → Java Object)

### Interceptor per Compressione - Transparent Data Transformation

```java
/* COMPRESSION WRITER INTERCEPTOR:
 * Teoria: Implementa il Transparent Proxy Pattern per compressione automatica
 * Il client riceve dati compressi senza sapere che l'elaborazione è avvenuta
 */
@Provider
@Compress // Name binding per attivazione selettiva
public class GzipWriterInterceptor implements WriterInterceptor {
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) 
            throws IOException, WebApplicationException {
        
        /* CONTENT NEGOTIATION PATTERN:
         * Teoria: Verifica capacità del client prima della trasformazione
         * Implementa il principio di "graceful degradation"
         */
        MultivaluedMap<String, Object> headers = context.getHeaders();
        String acceptEncoding = (String) headers.getFirst("Accept-Encoding");
        
        if (acceptEncoding == null || !acceptEncoding.contains("gzip")) {
            /* TRANSPARENT BYPASS:
             * Teoria: Se il client non supporta gzip, l'interceptor diventa
             * completamente trasparente, passando il controllo al prossimo nella catena
             */
            context.proceed();
            return;
        }
        
        /* STREAM SUBSTITUTION PATTERN:
         * Teoria: Sostituisce lo stream di output con uno decorato (Decorator Pattern)
         * senza che gli altri componenti se ne accorgano
         */
        OutputStream originalStream = context.getOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream(originalStream);
        
        // Sostituzione trasparente dello stream
        context.setOutputStream(gzipStream);
        
        try {
            /* DELEGATED PROCESSING:
             * Teoria: proceed() continua la catena di serializzazione
             * ma ora i dati vengono automaticamente compressi
             */
            context.proceed();
        } finally {
            /* RESOURCE CLEANUP PATTERN:
             * Teoria: Garantisce pulizia delle risorse anche in caso di eccezioni
             */
            gzipStream.finish(); // Finalizza la compressione
            gzipStream.close();
        }
        
        // Aggiunge header per indicare la compressione
        headers.putSingle("Content-Encoding", "gzip");
        headers.remove("Content-Length"); // Non più accurato dopo compressione
    }
}

// Annotazione per attivare la compressione
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Compress {
}
```

### Interceptor per Crittografia

```java
@Provider
@Encrypted
public class EncryptionWriterInterceptor implements WriterInterceptor {
    
    @Inject
    private EncryptionService encryptionService;
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) 
            throws IOException, WebApplicationException {
        
        // Intercetta l'output prima della serializzazione
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream originalStream = context.getOutputStream();
        
        // Reindirizza temporaneamente l'output
        context.setOutputStream(baos);
        
        try {
            // Procede con la serializzazione normale
            context.proceed();
            
            // Ottiene i dati serializzati
            byte[] originalData = baos.toByteArray();
            
            // Cripta i dati
            byte[] encryptedData = encryptionService.encrypt(originalData);
            
            // Scrive i dati criptati nello stream originale
            originalStream.write(encryptedData);
            
            // Aggiunge header per indicare la crittografia
            context.getHeaders().putSingle("X-Content-Encrypted", "AES-256");
            
        } finally {
            baos.close();
        }
    }
}

@Provider
@Encrypted
public class DecryptionReaderInterceptor implements ReaderInterceptor {
    
    @Inject
    private EncryptionService encryptionService;
    
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) 
            throws IOException, WebApplicationException {
        
        // Verifica se il contenuto è criptato
        String encrypted = context.getHeaders().getFirst("X-Content-Encrypted");
        
        if (!"AES-256".equals(encrypted)) {
            // Non criptato, procede normalmente
            return context.proceed();
        }
        
        // Legge tutti i dati criptati
        InputStream originalStream = context.getInputStream();
        byte[] encryptedData = originalStream.readAllBytes();
        
        // Decripta i dati
        byte[] decryptedData = encryptionService.decrypt(encryptedData);
        
        // Sostituisce lo stream con i dati decriptati
        ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
        context.setInputStream(bais);
        
        // Procede con la deserializzazione
        return context.proceed();
    }
}
```

## Filtri Lato Client

### Client Request Filter

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ClientAuthenticationFilter implements ClientRequestFilter {
    
    private final String apiKey;
    
    public ClientAuthenticationFilter(String apiKey) {
        this.apiKey = apiKey;
    }
    
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        
        // Aggiunge automaticamente l'header di autenticazione
        requestContext.getHeaders().add("Authorization", "Bearer " + apiKey);
        
        // Aggiunge header personalizzati
        requestContext.getHeaders().add("X-Client-Version", "1.0");
        requestContext.getHeaders().add("X-Request-ID", UUID.randomUUID().toString());
    }
}

// Utilizzo nel client
public class ApiClient {
    
    public void chiamaApiConFiltri() {
        
        Client client = ClientBuilder.newClient();
        
        // Registra il filtro sul client
        client.register(new ClientAuthenticationFilter("my-api-key"));
        
        try {
            String result = client
                .target("https://api.example.com/data")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
            
            System.out.println("Risultato: " + result);
            
        } finally {
            client.close();
        }
    }
}
```

### Client Response Filter per Retry Logic

```java
@Provider
@Priority(Priorities.USER)
public class RetryFilter implements ClientRequestFilter, ClientResponseFilter {
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Inizializza contatore retry
        requestContext.setProperty("retryCount", 0);
    }
    
    @Override
    public void filter(ClientRequestContext requestContext, 
                      ClientResponseContext responseContext) throws IOException {
        
        int status = responseContext.getStatus();
        Integer retryCount = (Integer) requestContext.getProperty("retryCount");
        
        // Retry per errori 5xx o timeout
        if ((status >= 500 || status == 408) && retryCount < MAX_RETRIES) {
            
            try {
                // Attende prima del retry
                Thread.sleep(RETRY_DELAY_MS * (retryCount + 1));
                
                // Incrementa contatore
                requestContext.setProperty("retryCount", retryCount + 1);
                
                // Esegue nuovamente la richiesta
                // Nota: questo è un esempio semplificato
                // Una implementazione reale richiederebbe logica più complessa
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

## Best Practices - Principi di Robustezza e Performance

### 1. Gestione delle Eccezioni nei Filtri - Defensive Programming Pattern

**Teoria della Fault Tolerance**: I filtri devono implementare strategie di gestione degli errori che non compromettano l'intero sistema.

**Principi Applicati**:

- **Graceful Degradation**: Il sistema continua a funzionare anche se un filtro non critico fallisce
- **Fail-Fast vs Fail-Safe**: Bilanciamento tra interruzione immediata e continuazione con funzionalità ridotta
- **Error Isolation**: Gli errori in un filtro non devono propagarsi ad altri componenti
- **Logging Strategy**: Registrazione appropriata per debugging senza compromettere le performance

```java
@Provider
@Priority(Priorities.USER)
public class RobustFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RobustFilter.class);
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        try {
            // Logica del filtro che potrebbe fallire
            performFilterLogic(requestContext);
            
        } catch (Exception e) {
            logger.error("Errore nel filtro", e);
            
            // Decide se interrompere o continuare
            if (isCriticalError(e)) {
                requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Errore interno del sistema")
                            .build()
                );
            } else {
                // Log dell'errore ma continua il processing
                logger.warn("Errore non critico nel filtro, continuo", e);
            }
        }
    }
    
    private void performFilterLogic(ContainerRequestContext context) {
        // Implementazione che potrebbe lanciare eccezioni
    }
    
    private boolean isCriticalError(Exception e) {
        return e instanceof SecurityException || 
               e instanceof DatabaseConnectionException;
    }
}
```

### 2. Performance e Ottimizzazione

```java
@Provider
@Priority(Priorities.USER)
public class OptimizedFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final String SKIP_PROCESSING = "SKIP_PROCESSING";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        // Evita processing per richieste OPTIONS (CORS preflight)
        if ("OPTIONS".equals(requestContext.getMethod())) {
            requestContext.setProperty(SKIP_PROCESSING, true);
            return;
        }
        
        // Evita processing per risorse statiche
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/static/") || path.endsWith(".js") || path.endsWith(".css")) {
            requestContext.setProperty(SKIP_PROCESSING, true);
            return;
        }
        
        // Processing normale solo se necessario
        performExpensiveOperation(requestContext);
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        // Skip se marcato nel request filter
        if (Boolean.TRUE.equals(requestContext.getProperty(SKIP_PROCESSING))) {
            return;
        }
        
        // Response processing normale
        performResponseProcessing(responseContext);
    }
    
    private void performExpensiveOperation(ContainerRequestContext context) {
        // Operazione costosa solo quando necessaria
    }
    
    private void performResponseProcessing(ContainerResponseContext context) {
        // Processing della risposta
    }
}
```

## Glossario - Terminologia Teorica e Architetturale

| Termine | Definizione Teorica |
|---------|-------------|
| **ContainerRequestFilter** | Implementazione del Gate Pattern per richieste server-side, opera sui metadati HTTP in ingresso implementando cross-cutting concerns |
| **ContainerResponseFilter** | Implementazione del Decorator Pattern per risposte server-side, modifica metadati HTTP in uscita senza alterare la logica business |
| **ClientRequestFilter** | Filtro client-side che implementa il Interceptor Pattern per richieste HTTP uscenti, utile per autenticazione e logging automatici |
| **ClientResponseFilter** | Filtro client-side per elaborazione trasparente di risposte HTTP, implementa pattern come retry logic e error handling |
| **WriterInterceptor** | Implementa l'Interceptor Pattern durante la serializzazione (marshalling), opera sul flusso di dati dal model verso HTTP |
| **ReaderInterceptor** | Implementa l'Interceptor Pattern durante la deserializzazione (unmarshalling), opera sul flusso di dati da HTTP verso model |
| **@Priority** | Annotazione che implementa l'Ordered Chain Pattern, definisce priorità numerica per l'ordinamento deterministico della catena |
| **@NameBinding** | Meta-annotazione che implementa il Selective Decoration Pattern per binding granulare di filtri/interceptors |
| **@Provider** | Marker annotation per registrazione automatica nel JAX-RS container, implementa il Service Provider Pattern |
| **abortWith()** | Implementa il Circuit Breaker Pattern, interrompendo immediatamente la catena di processing con una risposta alternativa |
| **proceed()** | Implementa la continuazione nella Chain of Responsibility, delegando al prossimo elemento nella catena di elaborazione |
| **Cross-Cutting Concerns** | Aspetti trasversali che "attraversano" tutti i livelli applicativi (logging, sicurezza, caching, monitoraggio) |
| **Chain of Responsibility** | Design Pattern implementato dai filtri per creare catene di elaborazione modulari e configurabili |
| **Aspect-Oriented Programming** | Paradigma implementato da filtri/interceptors per separare concerns trasversali dalla logica business |
