# Exception Handling in JAX-RS

## Introduzione Teorica

La gestione delle eccezioni in JAX-RS è un aspetto fondamentale per creare API robuste e user-friendly. JAX-RS fornisce diversi meccanismi per intercettare, mappare e gestire le eccezioni in modo centralizzato ed elegante.

### Contesto Architetturale dell'Exception Handling

L'**Exception Handling** in contesti enterprise rappresenta uno degli aspetti più critici dello sviluppo di API REST. Una gestione appropriata delle eccezioni determina:

#### Impatto sulla Qualità del Software

1. **Robustezza**: Capacità dell'applicazione di gestire situazioni impreviste
2. **Usabilità**: Feedback chiari e utili per gli sviluppatori client
3. **Sicurezza**: Prevenzione dell'esposizione di informazioni sensibili
4. **Manutenibilità**: Codice più pulito e centralizzazione della logica di errore
5. **Observability**: Tracciamento e monitoraggio degli errori in produzione

#### Principi Fondamentali

**Separation of Concerns**: Separazione tra logica business e gestione errori

**Fail Fast**: Rilevamento rapido e gestione immediata degli errori

**Graceful Degradation**: Comportamento elegante in presenza di errori

**Information Hiding**: Protezione di dettagli interni del sistema

**Consistency**: Formato uniforme delle risposte di errore

### Architettura JAX-RS per Exception Handling

JAX-RS implementa un sistema a **due livelli** per la gestione delle eccezioni:

1. **WebApplicationException**: Eccezioni specifiche HTTP con mapping automatico
2. **ExceptionMapper**: Interceptor per trasformazione personalizzata delle eccezioni

Questo approccio permette sia gestione rapida (eccezioni predefinite) che controllo granulare (mapper personalizzati).

## WebApplicationException

### Teoria delle WebApplicationException

`WebApplicationException` rappresenta il **primo livello** del sistema di gestione eccezioni JAX-RS. È progettata seguendo il principio della **convenzione over configurazione**.

#### Meccanismo di Funzionamento

Quando viene lanciata una `WebApplicationException`, JAX-RS:

1. **Intercetta** automaticamente l'eccezione durante il processing della richiesta
2. **Estrae** il codice di stato HTTP dall'eccezione
3. **Costruisce** una Response HTTP appropriata
4. **Interrompe** il normale flusso di elaborazione
5. **Ritorna** la risposta al client senza ulteriore processing

#### Vantaggi del Modello WebApplicationException

**Semplicità**: Mapping diretto eccezione → HTTP status senza configurazione

**Performance**: Elaborazione ottimizzata senza overhead di reflection

**Type Safety**: Controllo a compile-time del tipo di eccezione

**Integrazione**: Funziona seamlessly con il resto dell'architettura JAX-RS

#### Pattern di Utilizzo

Le `WebApplicationException` seguono il pattern **Exception as Flow Control**, dove le eccezioni non rappresentano errori di sistema ma condizioni business normali (es. risorsa non trovata).

`WebApplicationException` è la classe base per tutte le eccezioni specifiche di JAX-RS. Quando viene lanciata, JAX-RS automaticamente la converte in una risposta HTTP appropriata.

### Eccezioni Predefinite

JAX-RS 2.0 fornisce diverse sottoclassi di `WebApplicationException` per i casi più comuni:

```java
import javax.ws.rs.*;

@Path("/prodotti")
public class ProdottoResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Prodotto getProdotto(@PathParam("id") int id) {
        
        // 400 Bad Request
        if (id <= 0) {
            throw new BadRequestException("ID deve essere positivo");
        }
        
        Prodotto prodotto = prodottoService.findById(id);
        
        // 404 Not Found
        if (prodotto == null) {
            throw new NotFoundException("Prodotto con ID " + id + " non trovato");
        }
        
        return prodotto;
    }
    
    @DELETE
    @Path("/{id}")
    public void eliminaProdotto(@PathParam("id") int id, 
                               @Context SecurityContext security) {
        
        // 401 Unauthorized
        if (security.getUserPrincipal() == null) {
            throw new NotAuthorizedException("Autenticazione richiesta");
        }
        
        // 403 Forbidden  
        if (!security.isUserInRole("admin")) {
            throw new ForbiddenException("Operazione riservata agli amministratori");
        }
        
        boolean eliminato = prodottoService.elimina(id);
        if (!eliminato) {
            throw new NotFoundException("Prodotto non trovato");
        }
    }
}
```

### Eccezioni WebApplicationException Comuni

#### Categorizzazione delle Eccezioni HTTP

Le eccezioni JAX-RS seguono la **semantica HTTP standard**, mappando condizioni specifiche a codici di stato appropriati:

**Errori Client (4xx)**: Problemi nella richiesta del client

**Errori Server (5xx)**: Problemi nell'elaborazione lato server

#### Analisi delle Eccezioni Principali

| Eccezione | Codice HTTP | Uso Tipico | Contesto Teorico |
|-----------|-------------|------------|------------------|
| `BadRequestException` | 400 | Parametri non validi, dati malformati | **Input Validation**: Controllo integrità dati in ingresso |
| `NotAuthorizedException` | 401 | Utente non autenticato | **Authentication**: Verifica identità dell'utente |
| `ForbiddenException` | 403 | Utente autenticato ma senza permessi | **Authorization**: Controllo permessi per risorsa |
| `NotFoundException` | 404 | Risorsa non esistente | **Resource Location**: Mapping URL → Business Entity |
| `NotAllowedException` | 405 | Metodo HTTP non supportato | **HTTP Semantics**: Verifica operazioni consentite |
| `NotAcceptableException` | 406 | Formato richiesto non supportato | **Content Negotiation**: Matching formato client/server |
| `ClientErrorException` | 4xx | Errori client generici | **Generic Client Errors**: Fallback per errori 4xx |
| `InternalServerErrorException` | 500 | Errori interni del server | **System Failures**: Errori infrastruttura/applicazione |
| `ServiceUnavailableException` | 503 | Servizio temporaneamente non disponibile | **Load Management**: Gestione overload temporaneo |
| `ServerErrorException` | 5xx | Errori server generici | **Generic Server Errors**: Fallback per errori 5xx |

#### Strategia di Selezione

La scelta della corretta eccezione deve considerare:

1. **Semantica HTTP**: Rispetto del significato standard dei codici
2. **Client Expectation**: Comportamento atteso dai client REST
3. **Debugging**: Facilità di identificazione del problema
4. **Security**: Non esposizione di informazioni sensibili

## ExceptionMapper

### Teoria dell'Exception Mapping

Per una gestione centralizzata e personalizzata delle eccezioni, JAX-RS fornisce l'interfaccia `ExceptionMapper<T>`. Permette di mappare specifici tipi di eccezione a risposte HTTP customizzate.

#### Principi Architetturali dell'Exception Mapping

L'**Exception Mapping** rappresenta l'implementazione del pattern **Interceptor** applicato alla gestione errori. I vantaggi includono:

**Centralizzazione**: Un punto unico per gestire ogni tipo di eccezione

**Separation of Concerns**: Logica business separata dalla gestione errori

**Consistency**: Formato uniforme delle risposte di errore

**Reusability**: Riutilizzo della logica di mapping in tutta l'applicazione

**Testability**: Testing isolato della logica di error handling

#### Meccanismo di Risoluzione

JAX-RS utilizza un sistema di **type resolution** per selezionare l'ExceptionMapper appropriato:

1. **Exact Match**: Cerca mapper per il tipo esatto dell'eccezione
2. **Inheritance Chain**: Risale la gerarchia delle classi
3. **Interface Matching**: Considera le interfacce implementate
4. **Generic Fallback**: Utilizza mapper generici come ultimo resort

#### Provider Registration

L'annotazione `@Provider` registra automaticamente l'ExceptionMapper nel **JAX-RS Provider Registry**, rendendo la classe disponibile per l'intercepting delle eccezioni.

#### Vantaggi vs WebApplicationException

| Aspetto | WebApplicationException | ExceptionMapper |
|---------|------------------------|-----------------|
| **Semplicità** | Alta (immediata) | Media (richiede configurazione) |
| **Flessibilità** | Bassa | Alta (controllo completo) |
| **Riutilizzabilità** | Bassa | Alta (centralizzata) |
| **Testing** | Difficile | Facile (separata) |
| **Manutenibilità** | Media | Alta |

### Esempio Base di ExceptionMapper

```java
// Eccezione business personalizzata
public class ProdottoNotFoundException extends Exception {
    private final int prodottoId;
    
    public ProdottoNotFoundException(int id) {
        super("Prodotto con ID " + id + " non trovato");
        this.prodottoId = id;
    }
    
    public int getProdottoId() {
        return prodottoId;
    }
}

// Classe per rappresentare errori in formato JSON
public class ErrorDetail {
    private String codice;
    private String messaggio;
    private String dettagli;
    private Instant timestamp;
    private Object datiAggiuntivi;
    
    // Costruttori
    public ErrorDetail(String codice, String messaggio) {
        this.codice = codice;
        this.messaggio = messaggio;
        this.timestamp = Instant.now();
    }
    
    public ErrorDetail(String codice, String messaggio, Object datiAggiuntivi) {
        this(codice, messaggio);
        this.datiAggiuntivi = datiAggiuntivi;
    }
    
    // Getter e setter...
}

// ExceptionMapper per gestire ProdottoNotFoundException
@Provider
public class ProdottoNotFoundExceptionMapper 
    implements ExceptionMapper<ProdottoNotFoundException> {
    
    @Override
    public Response toResponse(ProdottoNotFoundException exception) {
        
        ErrorDetail errorDetail = new ErrorDetail(
            "PRODUCT_NOT_FOUND",
            exception.getMessage(),
            Map.of("prodottoId", exception.getProdottoId())
        );
        
        return Response.status(Response.Status.NOT_FOUND)
                      .entity(errorDetail)
                      .type(MediaType.APPLICATION_JSON)
                      .build();
    }
}
```

### ExceptionMapper per Eccezioni di Sistema

#### Teoria della Gestione Eccezioni di Sistema

Le **eccezioni di sistema** rappresentano errori che originano da layer infrastrutturali (database, rete, filesystem) piuttosto che dalla logica business. La loro gestione richiede considerazioni specifiche:

**Logging Appropriato**: Tracciamento dettagliato per debugging operativo

**Security**: Prevenzione dell'esposizione di dettagli interni del sistema

**Recovery**: Possibilità di retry automatico per errori transienti

**Monitoring**: Integration con sistemi di alerting e monitoraggio

#### Pattern di Gestione per Layer

1. **Validation Layer**: Errori di input e constrains
2. **Business Layer**: Violazioni regole business
3. **Persistence Layer**: Errori database e transazioni
4. **Network Layer**: Timeout e connettività
5. **System Layer**: Memoria, filesystem, risorse

#### Strategia di Fallback

Il **GenericExceptionMapper** implementa il pattern **Catch-All** per gestire eccezioni non previste, garantendo che l'applicazione non esponga stack trace o dettagli interni.

```java
// Mapper per eccezioni di validazione
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);
    
    @Override
    public Response toResponse(ValidationException exception) {
        
        logger.warn("Errore di validazione: {}", exception.getMessage());
        
        ErrorDetail error = new ErrorDetail(
            "VALIDATION_ERROR",
            "I dati forniti non sono validi",
            exception.getMessage()
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                      .entity(error)
                      .build();
    }
}

// Mapper per eccezioni generiche non gestite
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    @Override
    public Response toResponse(Throwable exception) {
        
        // Log dell'errore per debug
        logger.error("Errore non gestito: ", exception);
        
        // Non esporre dettagli interni in produzione
        String messaggio = "Si è verificato un errore interno";
        String dettagli = null;
        
        // In development, mostra più dettagli
        if (isDevelopmentMode()) {
            dettagli = exception.getMessage();
        }
        
        ErrorDetail error = new ErrorDetail(
            "INTERNAL_ERROR",
            messaggio,
            dettagli
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                      .entity(error)
                      .build();
    }
    
    private boolean isDevelopmentMode() {
        // Logica per determinare se siamo in development
        return "development".equals(System.getProperty("app.environment"));
    }
}
```

## Gestione di Eccezioni Multiple

### Teoria delle Exception Hierarchies

La gestione di **eccezioni multiple** richiede un design architetturale che bilanci **specificità** e **riutilizzo**. JAX-RS supporta gerarchie di eccezioni attraverso il meccanismo di **type inheritance resolution**.

#### Principi di Design delle Exception Hierarchies

**Single Responsibility**: Ogni eccezione ha una responsabilità specifica

**Open/Closed Principle**: Estensibile per nuovi errori senza modificare esistenti

**Liskov Substitution**: Eccezioni derivate sostituibili con quelle base

**Dependency Inversion**: Dipendenza da astrazioni, non da implementazioni concrete

#### Strategie di Mapping Resolution

JAX-RS risolve i mapper seguendo questo ordine di priorità:

1. **Exact Type Match**: Mapper specifico per il tipo esatto
2. **Subclass Match**: Mapper per la superclasse più specifica
3. **Interface Match**: Mapper per interfacce implementate
4. **Generic Match**: Mapper per `Throwable` o `Exception`

#### Vantaggi della Hierarchy Approach

**Code Reuse**: Logica comune condivisa tra eccezioni correlate

**Maintainability**: Modifiche centralizzate per famiglie di errori

**Consistency**: Comportamento uniforme per errori dello stesso dominio

**Extensibility**: Aggiunta di nuove eccezioni senza impatti

#### Pattern di Specializzazione

Il pattern comune prevede:

- **Abstract Base Exception**: Definisce struttura e comportamento comune
- **Domain Specific Exceptions**: Implementazioni concrete per domini business
- **Generic Mapper**: Gestione base per tutte le eccezioni del dominio
- **Specialized Mappers**: Override per casi specifici che richiedono trattamento particolare

### Hierarchy di ExceptionMapper

```java
// Exception base per errori business
public abstract class BusinessException extends Exception {
    protected final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// Eccezioni specifiche
public class InsufficientStockException extends BusinessException {
    private final int quantitaRichiesta;
    private final int quantitaDisponibile;
    
    public InsufficientStockException(int richiesta, int disponibile) {
        super("INSUFFICIENT_STOCK", 
              String.format("Quantità richiesta: %d, disponibile: %d", richiesta, disponibile));
        this.quantitaRichiesta = richiesta;
        this.quantitaDisponibile = disponibile;
    }
    
    // getter...
}

public class PaymentFailedException extends BusinessException {
    private final String transactionId;
    
    public PaymentFailedException(String transactionId, String reason) {
        super("PAYMENT_FAILED", "Pagamento fallito: " + reason);
        this.transactionId = transactionId;
    }
    
    // getter...
}

// Mapper generico per tutte le BusinessException
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {
    
    @Override
    public Response toResponse(BusinessException exception) {
        
        ErrorDetail error = new ErrorDetail(
            exception.getErrorCode(),
            exception.getMessage()
        );
        
        // Codici di stato basati sul tipo di errore business
        Response.Status status = mapToHttpStatus(exception.getErrorCode());
        
        return Response.status(status)
                      .entity(error)
                      .build();
    }
    
    private Response.Status mapToHttpStatus(String errorCode) {
        switch (errorCode) {
            case "INSUFFICIENT_STOCK":
            case "PAYMENT_FAILED":
                return Response.Status.CONFLICT; // 409
            case "INVALID_OPERATION":
                return Response.Status.BAD_REQUEST; // 400
            default:
                return Response.Status.INTERNAL_SERVER_ERROR; // 500
        }
    }
}

// Mapper specializzato per eccezioni specifiche
@Provider
public class InsufficientStockExceptionMapper implements ExceptionMapper<InsufficientStockException> {
    
    @Override
    public Response toResponse(InsufficientStockException exception) {
        
        Map<String, Object> details = Map.of(
            "quantitaRichiesta", exception.getQuantitaRichiesta(),
            "quantitaDisponibile", exception.getQuantitaDisponibile()
        );
        
        ErrorDetail error = new ErrorDetail(
            "INSUFFICIENT_STOCK",
            exception.getMessage(),
            details
        );
        
        return Response.status(Response.Status.CONFLICT)
                      .entity(error)
                      .header("Retry-After", "3600") // Riprova tra 1 ora
                      .build();
    }
}
```

## Utilizzo Pratico negli Endpoint

### Teoria dell'Integration Exception Handling

L'**integrazione** dell'exception handling negli endpoint REST richiede la considerazione di diversi aspetti architetturali:

#### Layer Responsibility

**Presentation Layer** (JAX-RS Endpoints):
- Validazione parametri HTTP
- Gestione autenticazione/autorizzazione
- Trasformazione eccezioni business → HTTP

**Business Layer** (Service Classes):
- Logica di dominio
- Validazione business rules
- Coordinamento transazioni

**Persistence Layer** (Repository/DAO):
- Gestione database
- Constraint violations
- Transaction management

#### Exception Flow Design

Il flusso delle eccezioni deve seguire il principio **Let it Crash** con **Graceful Recovery**:

1. **Early Validation**: Controlli immediati sui parametri
2. **Business Processing**: Esecuzione logica con possibili eccezioni
3. **Exception Propagation**: Le eccezioni salgono attraverso i layer
4. **Centralized Handling**: ExceptionMapper gestisce la trasformazione
5. **Client Response**: Risposta HTTP strutturata

#### Performance Considerations

- **Fast Path**: Validazioni rapide per errori comuni
- **Exception Cost**: Le eccezioni hanno overhead, non usare per flow control normale
- **Resource Cleanup**: Gestione appropriata di risorse in caso di errore

### Esempio Completo: Gestione Ordine

```java
@Path("/ordini")
public class OrdineResource {
    
    @Inject
    private OrdineService ordineService;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creaOrdine(NuovoOrdineRequest request) 
            throws ValidationException, InsufficientStockException, PaymentFailedException {
        
        // Validazione - potrebbe lanciare ValidationException
        validaRichiestaOrdine(request);
        
        // Creazione ordine - potrebbe lanciare varie BusinessException
        Ordine ordine = ordineService.creaOrdine(request);
        
        // Se tutto va bene, ritorna 201 Created
        return Response.status(Response.Status.CREATED)
                      .entity(ordine)
                      .header("Location", "/api/ordini/" + ordine.getId())
                      .build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Ordine getOrdine(@PathParam("id") int id) {
        
        if (id <= 0) {
            throw new BadRequestException("ID ordine deve essere positivo");
        }
        
        Ordine ordine = ordineService.findById(id);
        
        if (ordine == null) {
            throw new NotFoundException("Ordine " + id + " non trovato");
        }
        
        return ordine;
    }
    
    @PUT
    @Path("/{id}/stato")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiornaStato(@PathParam("id") int id, 
                                 StatoOrdineRequest request,
                                 @Context SecurityContext security) {
        
        // Controllo autorizzazione
        if (!security.isUserInRole("operator")) {
            throw new ForbiddenException("Solo gli operatori possono modificare lo stato degli ordini");
        }
        
        try {
            ordineService.aggiornaStato(id, request.getNuovoStato());
            return Response.noContent().build();
            
        } catch (IllegalStateTransitionException e) {
            // Transizione di stato non valida
            throw new ClientErrorException("Transizione di stato non valida: " + e.getMessage(), 
                                         Response.Status.CONFLICT);
        }
    }
    
    private void validaRichiestaOrdine(NuovoOrdineRequest request) throws ValidationException {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("L'ordine deve contenere almeno un item");
        }
        
        if (request.getIndirizzoDiSpedizione() == null) {
            throw new ValidationException("Indirizzo di spedizione obbligatorio");
        }
    }
}
```

## Best Practices per Exception Handling

### Teoria delle Best Practices

L'implementazione di **best practices** nell'Exception Handling è cruciale per creare API robuste, sicure e mantenibili. Queste pratiche derivano da anni di esperienza nella costruzione di sistemi distribuiti enterprise.

#### Principi Fondamentali

**Consistency**: Formato uniforme per tutti i tipi di errore

**Security**: Protezione delle informazioni sensibili

**Usability**: Informazioni utili per debugging e integrazione

**Performance**: Gestione efficiente senza overhead significativo

**Observability**: Tracciabilità per monitoring e troubleshooting

#### Impatto delle Best Practices

- **Developer Experience**: API più facili da integrare e debuggare
- **System Reliability**: Comportamento prevedibile in situazioni di errore
- **Security Posture**: Riduzione della superficie di attacco
- **Operational Excellence**: Facilitazione del monitoring e alerting

### 1. Struttura Consistente degli Errori

#### Teoria della Standardizzazione Errori

La **standardizzazione** del formato errori segue il principio **RFC 7807 (Problem Details for HTTP APIs)**, che definisce una struttura comune per rappresentare errori HTTP in formato JSON.

**Benefici della Standardizzazione:**

- **Predictability**: Client possono implementare parsing uniforme
- **Tooling**: Strumenti automatici di error handling
- **Documentation**: Documentazione API più chiara
- **Evolution**: Estendibilità senza breaking changes

#### Componenti Standard

- **type**: URI che identifica il tipo di problema
- **title**: Descrizione breve human-readable
- **status**: Codice di stato HTTP  
- **detail**: Spiegazione specifica dell'istanza
- **instance**: URI che identifica l'occurrence specifica

```java
// Classe standard per tutti gli errori API
public class ApiErrorResponse {
    private final String type;           // Tipo di errore
    private final String title;          // Titolo breve
    private final int status;            // Codice HTTP
    private final String detail;         // Dettagli specifici
    private final String instance;       // URI dell'istanza che ha causato l'errore
    private final Instant timestamp;     // Momento dell'errore
    private final Map<String, Object> extensions; // Dati aggiuntivi
    
    // Costruttore builder pattern...
    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }
}

@Provider
public class StandardExceptionMapper implements ExceptionMapper<Exception> {
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        
        ApiErrorResponse error = ApiErrorResponse.builder()
            .type("about:blank")
            .title("Internal Server Error")
            .status(500)
            .detail("Si è verificato un errore interno")
            .instance(uriInfo.getRequestUri().toString())
            .build();
        
        return Response.status(500)
                      .entity(error)
                      .type(MediaType.APPLICATION_JSON)
                      .build();
    }
}
```

### 2. Logging Appropriato

#### Teoria del Structured Logging

Il **logging appropriato** nell'exception handling è fondamentale per l'**observability** dei sistemi enterprise. Una strategia efficace deve bilanciare:

**Information Richness**: Dati sufficienti per il debugging

**Performance Impact**: Overhead minimale sul sistema

**Security Compliance**: Non esposizione di informazioni sensibili

**Operational Usability**: Formato facilmente parsabile da sistemi di monitoring

#### Principi del Contextual Logging

**Correlation**: Ogni log entry deve essere correlabile ad una specifica richiesta

**Structured Format**: Uso di campi strutturati per facilità di parsing

**Level Appropriateness**: Selezione corretta del livello di log

**Contextual Information**: Inclusione di metadati utili per troubleshooting

#### MDC (Mapped Diagnostic Context)

Il **MDC** è un meccanismo thread-local per aggiungere informazioni contestuali ai log. Vantaggi:

- **Request Tracing**: Tracciamento di richieste attraverso layer multipli
- **User Context**: Associazione errori a utenti specifici
- **Performance Monitoring**: Correlazione con metriche di performance
- **Security Auditing**: Tracciabilità per audit di sicurezza

#### Log Level Strategy

- **TRACE/DEBUG**: Dettagli implementativi (solo development)
- **INFO**: Errori client (4xx) e eventi business normali
- **WARN**: Situazioni anomale ma gestibili
- **ERROR**: Errori server (5xx) e eccezioni non gestite
- **FATAL**: Errori che compromettono l'intero sistema

```java
@Provider
public class LoggingExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingExceptionMapper.class);
    
    @Context
    private HttpServletRequest request;
    
    @Override
    public Response toResponse(Exception exception) {
        
        // Log con contesto della richiesta
        MDC.put("requestId", generateRequestId());
        MDC.put("userId", getCurrentUserId());
        MDC.put("endpoint", request.getRequestURI());
        
        try {
            if (exception instanceof WebApplicationException) {
                WebApplicationException wae = (WebApplicationException) exception;
                int status = wae.getResponse().getStatus();
                
                if (status >= 400 && status < 500) {
                    // Errori client: log a livello INFO
                    logger.info("Client error {}: {}", status, exception.getMessage());
                } else {
                    // Errori server: log a livello ERROR
                    logger.error("Server error {}: ", status, exception);
                }
                
                return wae.getResponse();
                
            } else {
                // Eccezione non gestita: sempre ERROR
                logger.error("Unhandled exception: ", exception);
                
                return Response.status(500)
                              .entity(new ErrorDetail("INTERNAL_ERROR", "Errore interno"))
                              .build();
            }
        } finally {
            MDC.clear();
        }
    }
    
    private String generateRequestId() {
        // Genera ID univoco per la richiesta
        return UUID.randomUUID().toString();
    }
    
    private String getCurrentUserId() {
        // Estrae user ID dal security context
        return "anonymous"; // placeholder
    }
}
```

### 3. Sicurezza e Privacy

#### Teoria della Security-First Exception Handling

La **sicurezza** nell'exception handling rappresenta un aspetto critico spesso trascurato. Gli errori possono involontariamente esporre:

**Information Disclosure**: Dettagli interni del sistema

**System Architecture**: Tecnologie e configurazioni utilizzate

**Business Logic**: Regole e processi interni

**Data Exposure**: Informazioni sui dati memorizzati

#### Principi di Secure Exception Handling

**Principle of Least Information**: Esporre solo informazioni necessarie

**Environment Awareness**: Comportamento diverso per development/production

**Sanitization**: Rimozione di dati sensibili dai messaggi di errore

**Audit Trail**: Logging sicuro per investigation post-incident

#### Common Security Anti-Patterns

❌ **Stack Trace Exposure**: Mai esporre stack trace completi in production

❌ **Database Schema Leaks**: Non mostrare constraint violations SQL

❌ **Path Disclosure**: Evitare esposizione di percorsi filesystem

❌ **Configuration Leaks**: Non includere parametri di configurazione

#### Privacy Considerations

- **GDPR Compliance**: Gestione appropriata di dati personali negli errori
- **Data Minimization**: Logging solo di informazioni necessarie
- **Right to be Forgotten**: Capacità di rimuovere dati dai log
- **Consent Management**: Rispetto delle preferenze utente per tracking

```java
@Provider
public class SecureExceptionMapper implements ExceptionMapper<Exception> {
    
    @Context
    private Application application;
    
    @Override
    public Response toResponse(Exception exception) {
        
        boolean isProduction = isProductionEnvironment();
        
        if (exception instanceof SecurityException) {
            // Non esporre dettagli di sicurezza
            return Response.status(Response.Status.FORBIDDEN)
                          .entity(new ErrorDetail("ACCESS_DENIED", "Accesso negato"))
                          .build();
        }
        
        if (exception instanceof SQLException) {
            // Non esporre dettagli del database
            String message = isProduction ? 
                "Errore interno" : 
                "Database error: " + exception.getMessage();
                
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorDetail("DATABASE_ERROR", message))
                          .build();
        }
        
        // Altri tipi di eccezione...
        return handleGenericException(exception, isProduction);
    }
    
    private boolean isProductionEnvironment() {
        // Determina se siamo in produzione
        return !"development".equals(System.getProperty("app.env"));
    }
}
```

## Considerazioni Architetturali Avanzate

### Exception Handling in Microservices

In architetture **microservizi**, l'exception handling deve considerare:

#### Distributed Error Propagation

- **Circuit Breaker**: Prevenzione cascading failures
- **Bulkhead Pattern**: Isolamento dei fallimenti
- **Timeout Management**: Gestione appropriata dei timeout
- **Retry Strategies**: Politiche di retry intelligenti

#### Cross-Service Error Correlation

- **Distributed Tracing**: Tracciamento errori attraverso servizi multipli
- **Correlation IDs**: Identificatori univoci per richieste distribuite
- **Error Aggregation**: Consolidamento errori per analisi
- **Service Health Monitoring**: Monitoraggio stato dei servizi dependencies

### Performance e Scalabilità

#### Exception Handling Performance

- **Exception Cost**: Le eccezioni hanno overhead, evitare per flow control normale
- **Stack Trace Generation**: Impatto CPU della generazione stack trace
- **Memory Allocation**: Gestione memoria durante picchi di errori
- **Thread Contention**: Evitare contention su logger e shared resources

#### Optimization Strategies

- **Exception Pools**: Riutilizzo istanze per eccezioni comuni
- **Lazy Stack Traces**: Generazione stack trace solo quando necessario
- **Async Logging**: Logging asincrono per ridurre latenza
- **Batched Error Reporting**: Aggregazione errori per efficienza

### Compliance e Governance

#### Regulatory Compliance

- **PCI DSS**: Protezione dati pagamento negli errori
- **HIPAA**: Gestione informazioni sanitarie
- **SOX**: Audit trail per compliance finanziaria
- **ISO 27001**: Gestione sicurezza informazioni

## Glossario

| Termine | Definizione |
|---------|-------------|
| **WebApplicationException** | Eccezione base di JAX-RS che viene automaticamente mappata a risposte HTTP |
| **ExceptionMapper** | Interfaccia per mappare eccezioni Java a risposte HTTP personalizzate |
| **@Provider** | Annotazione che registra una classe come provider JAX-RS |
| **Business Exception** | Eccezione che rappresenta un errore di logica business |
| **Error Detail** | Classe che struttura le informazioni di errore per le API |
| **Exception Hierarchy** | Gerarchia di eccezioni per gestire diversi tipi di errore |
| **MDC** | Mapped Diagnostic Context per il logging con contesto |
| **Circuit Breaker** | Pattern per gestire fallimenti in sistemi distribuiti |
| **Correlation ID** | Identificatore univoco per tracciare richieste attraverso servizi |
| **Structured Logging** | Formato di logging con campi strutturati per parsing automatico |
| **RFC 7807** | Standard per rappresentazione errori HTTP in formato JSON |
