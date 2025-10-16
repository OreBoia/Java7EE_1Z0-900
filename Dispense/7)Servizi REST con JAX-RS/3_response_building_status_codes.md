# Response Building e Gestione degli Status Codes in JAX-RS

## Introduzione Teorica

In JAX-RS, oltre a restituire direttamente oggetti POJO che vengono automaticamente serializzati, è spesso necessario avere un controllo più fine sulla risposta HTTP. La classe `Response` permette di costruire risposte personalizzate con codici di stato, header e contenuto specifici.

### Contesto Architetturale

Il **Response Building** rappresenta uno degli aspetti fondamentali nell'implementazione di servizi REST professionali. Mentre i framework come JAX-RS offrono meccanismi automatici di serializzazione (ritornando direttamente oggetti Java che vengono convertiti in JSON/XML), i servizi REST enterprise richiedono spesso un controllo granulare su:

- **Codici di stato HTTP**: Per comunicare precisamente l'esito dell'operazione
- **Header HTTP**: Per fornire metadati aggiuntivi (cache, sicurezza, CORS, etc.)
- **Formato della risposta**: Per supportare diversi tipi di contenuto
- **Gestione errori**: Per fornire informazioni strutturate sui fallimenti

### Vantaggi del Response Esplicito

1. **Conformità REST**: Rispetto rigoroso dei principi architetturali REST
2. **Interoperabilità**: Comportamento prevedibile per client diversi
3. **Debugging**: Informazioni dettagliate per il troubleshooting
4. **Performance**: Controllo su cache e ottimizzazioni
5. **Sicurezza**: Gestione appropriata di header di sicurezza

## Costruzione di Risposte con Response Builder

### Response Builder Pattern

#### Teoria del Pattern Builder

Il **Builder Pattern** è un design pattern creazionale che permette di costruire oggetti complessi passo dopo passo. In JAX-RS, questo pattern è implementato attraverso la classe `Response.ResponseBuilder`, che offre un'API fluida per comporre risposte HTTP complete.

**Caratteristiche del Pattern:**
- **Immutabilità**: Ogni chiamata restituisce un nuovo builder
- **Flessibilità**: Permette di costruire oggetti con configurazioni diverse
- **Leggibilità**: Il codice risulta auto-documentante e intuitivo
- **Validazione**: Controlli di consistenza durante la costruzione

#### Meccanismo Interno

Quando si utilizza `Response.status().entity().header().build()`, internamente JAX-RS:

1. **Crea un ResponseBuilder** con lo stato specificato
2. **Accumula configurazioni** (entity, headers, media type)
3. **Valida la coerenza** delle impostazioni
4. **Costruisce l'oggetto** Response finale immutabile

JAX-RS utilizza il pattern Builder per costruire oggetti `Response` in modo fluido e leggibile:

```java
@Path("/prodotti")
public class ProdottoResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creaProdotto(Prodotto prodotto) {
        // Validazione input
        if (prodotto.getNome() == null || prodotto.getNome().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorMessage("Nome prodotto obbligatorio"))
                          .build();
        }
        
        // Salvataggio
        Prodotto salvato = prodottoService.salva(prodotto);
        
        // Risposta di successo con 201 Created
        return Response.status(Response.Status.CREATED)
                      .entity(salvato)
                      .header("Location", "/api/prodotti/" + salvato.getId())
                      .header("X-Created-At", Instant.now().toString())
                      .build();
    }
}
```

### Metodi Response.status()

#### Teoria dei Codici di Stato HTTP

I **codici di stato HTTP** rappresentano il meccanismo standardizzato per comunicare l'esito di una richiesta HTTP. Sono definiti nella RFC 7231 e seguono una semantica precisa che deve essere rispettata per garantire l'interoperabilità.

#### Classificazione dei Codici di Stato

La numerazione dei codici di stato segue una logica semantica:

**Codici 1xx (Informativi)**: Comunicazioni di protocollo (raramente usati in REST)

**Codici 2xx (Successo)**: L'operazione è stata completata con successo

**Codici 3xx (Redirection)**: Il client deve eseguire azioni aggiuntive

**Codici 4xx (Errori del Client)**: La richiesta contiene errori o è malformata

**Codici 5xx (Errori del Server)**: Il server ha fallito nell'elaborare una richiesta valida

La classe `Response.Status` fornisce tutte le costanti per i codici di stato HTTP standard:

```java
// Codici 2xx - Successo
Response.Status.OK                    // 200 - Richiesta elaborata con successo
Response.Status.CREATED               // 201 - Nuova risorsa creata (POST/PUT)
Response.Status.ACCEPTED              // 202 - Richiesta accettata ma elaborazione asincrona
Response.Status.NO_CONTENT            // 204 - Successo ma nessun contenuto da restituire

// Codici 3xx - Redirection  
Response.Status.MOVED_PERMANENTLY     // 301 - Risorsa spostata permanentemente
Response.Status.NOT_MODIFIED          // 304 - Contenuto non modificato (cache valida)

// Codici 4xx - Client Error
Response.Status.BAD_REQUEST           // 400 - Richiesta malformata o parametri invalidi
Response.Status.UNAUTHORIZED          // 401 - Autenticazione richiesta o fallita
Response.Status.FORBIDDEN             // 403 - Accesso negato (autorizzazione insufficiente)
Response.Status.NOT_FOUND             // 404 - Risorsa non trovata
Response.Status.CONFLICT              // 409 - Conflitto con lo stato attuale della risorsa
Response.Status.PRECONDITION_FAILED   // 412 - Condizione preliminare non soddisfatta

// Codici 5xx - Server Error
Response.Status.INTERNAL_SERVER_ERROR // 500 - Errore interno del server
Response.Status.SERVICE_UNAVAILABLE   // 503 - Servizio temporaneamente non disponibile
```

#### Semantica REST e Status Codes

Ogni operazione REST dovrebbe utilizzare il codice di stato più appropriato:

- **GET**: 200 (successo), 404 (non trovato), 304 (non modificato)
- **POST**: 201 (creato), 200 (elaborato), 409 (conflitto)
- **PUT**: 200 (aggiornato), 201 (creato), 204 (aggiornato senza contenuto)
- **DELETE**: 204 (cancellato), 404 (non trovato), 409 (impossibile cancellare)

### Esempio Completo: CRUD con Response Builder

#### Analisi Teorica dell'Implementazione CRUD

L'esempio seguente dimostra l'implementazione completa delle operazioni CRUD (Create, Read, Update, Delete) utilizzando il Response Builder di JAX-RS. Ogni metodo illustra specifici aspetti teorici:

**Principi Implementati:**

1. **Validazione Input**: Controllo dei parametri prima dell'elaborazione
2. **Gestione Errori**: Codici di stato appropriati per ogni situazione
3. **Concorrenza Ottimistica**: Utilizzo di ETag per prevenire conflitti
4. **Caching HTTP**: Header per ottimizzare le performance
5. **Idempotenza**: Comportamento prevedibile per operazioni ripetute

#### Dettaglio delle Operazioni

**GET (Lettura):**
- Validazione dell'ID (BusinessLogic)
- Gestione risorsa non trovata (404)
- Header di cache (ETag, Last-Modified)
- Risposta ottimizzata per client

**PUT (Aggiornamento):**
- Controllo esistenza risorsa
- Gestione concorrenza con If-Match header
- Aggiornamento atomico
- Risposta con nuova versione

**DELETE (Cancellazione):**
- Verifica possibilità di cancellazione
- Risposta 204 (No Content) per successo
- Gestione riferimenti in uso

```java
@Path("/prodotti")
@Produces(MediaType.APPLICATION_JSON)
public class ProdottoResource {

    @Inject
    private ProdottoService service;

    @GET
    @Path("/{id}")
    public Response getProdotto(@PathParam("id") int id) {
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorMessage("ID deve essere positivo"))
                          .build();
        }

        Prodotto prodotto = service.findById(id);
        
        if (prodotto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(new ErrorMessage("Prodotto non trovato"))
                          .build();
        }

        return Response.ok(prodotto)
                      .header("Last-Modified", prodotto.getUltimaModifica())
                      .header("ETag", "\"" + prodotto.getVersione() + "\"")
                      .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiornaProdotto(@PathParam("id") int id, 
                                    Prodotto prodotto,
                                    @HeaderParam("If-Match") String ifMatch) {
        
        Prodotto esistente = service.findById(id);
        
        if (esistente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Controllo concorrenza ottimistica
        if (ifMatch != null && !ifMatch.equals("\"" + esistente.getVersione() + "\"")) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                          .entity(new ErrorMessage("Versione del prodotto obsoleta"))
                          .build();
        }
        
        Prodotto aggiornato = service.aggiorna(id, prodotto);
        
        return Response.ok(aggiornato)
                      .header("ETag", "\"" + aggiornato.getVersione() + "\"")
                      .build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminaProdotto(@PathParam("id") int id) {
        boolean eliminato = service.elimina(id);
        
        if (!eliminato) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.noContent().build(); // 204 No Content
    }
}
```

## Header HTTP Personalizzati

### Teoria degli Header HTTP

Gli **Header HTTP** sono metadati che accompagnano le richieste e risposte HTTP, fornendo informazioni aggiuntive sul contenuto, sulla cache, sulla sicurezza e su altri aspetti del protocollo. In JAX-RS, il controllo granulare degli header è fondamentale per:

#### Categorie di Header

1. **Header Standard**: Definiti dalle specifiche HTTP (Content-Type, Cache-Control, etc.)
2. **Header Personalizzati**: Specifici dell'applicazione (convenzionalmente prefissati con "X-")
3. **Header di Sicurezza**: Per proteggere da vulnerabilità comuni
4. **Header CORS**: Per gestire le richieste cross-origin
5. **Header di Cache**: Per ottimizzare le performance

#### Benefici degli Header Personalizzati

- **Metadati Applicativi**: Informazioni specifiche del dominio
- **Debug e Monitoring**: Tracciamento delle performance
- **Integrazione**: Comunicazione tra servizi
- **Versionamento**: Gestione di versioni multiple dell'API

### Aggiunta di Header Personalizzati

```java
@GET
@Path("/stats")
public Response getStatistiche() {
    StatisticheProdotti stats = service.calcolaStatistiche();
    
    return Response.ok(stats)
                  .header("X-Total-Products", stats.getTotale())
                  .header("X-Cache-Status", "HIT")
                  .header("X-Processing-Time", "150ms")
                  .header("Access-Control-Allow-Origin", "*") // CORS
                  .build();
}
```

### Header di Sicurezza

#### Teoria della Sicurezza Web mediante Header

Gli **header di sicurezza** rappresentano un meccanismo di difesa fondamentale contro molte vulnerabilità web comuni. Implementare correttamente questi header è essenziale per applicazioni enterprise:

**Header di Sicurezza Essenziali:**

- **Cache-Control**: Previene la memorizzazione di dati sensibili
- **X-Content-Type-Options**: Previene attacchi MIME-sniffing
- **X-Frame-Options**: Protegge da attacchi clickjacking
- **X-XSS-Protection**: Attiva la protezione XSS del browser
- **Strict-Transport-Security**: Forza l'uso di HTTPS
- **Content-Security-Policy**: Previene attacchi di injection

#### Strategie di Sicurezza

1. **Defense in Depth**: Multipli livelli di protezione
2. **Least Privilege**: Accesso minimo necessario
3. **Security by Design**: Sicurezza integrata nell'architettura

```java
@GET
@Path("/sensitive-data")
public Response getDatiSensibili() {
    // ... logica business
    
    return Response.ok(dati)
                  .header("Cache-Control", "no-store, no-cache, must-revalidate")
                  .header("Pragma", "no-cache")
                  .header("X-Content-Type-Options", "nosniff")
                  .header("X-Frame-Options", "DENY")
                  .build();
}
```

## Response con Diversi Formati

### Teoria del Content Negotiation

Il **Content Negotiation** è un meccanismo HTTP che permette al client e al server di concordare il formato più appropriato per i dati scambiati.

#### Principi del Content Negotiation

1. **Flessibilità**: Supporto di multiple rappresentazioni della stessa risorsa
2. **Interoperabilità**: Compatibilità con client diversi (web, mobile, API)
3. **Evoluzione**: Aggiunta di nuovi formati senza breaking changes
4. **Performance**: Ottimizzazione basata sulle capacità del client

#### Meccanismi di Negoziazione

**Header-Based**: Utilizza header HTTP (Accept, Accept-Language, Accept-Encoding)

**URL-Based**: Specifica il formato nell'URL (/api/prodotti.json)

**Parameter-Based**: Usa query parameters (?format=json)

#### Vantaggi dell'Implementazione JAX-RS

- **Automatico**: Binding automatico basato su @Produces
- **Personalizzabile**: Controllo fine del processo di negoziazione  
- **Estensibile**: Supporto per formati custom
- **Performante**: Selezione ottimizzata del formato

### Content Negotiation Avanzata

```java
@GET
@Path("/{id}")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
public Response getProdotto(@PathParam("id") int id, 
                           @HeaderParam("Accept") String acceptHeader) {
    
    Prodotto prodotto = service.findById(id);
    
    if (prodotto == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    // Logica personalizzata basata su Accept header
    if (acceptHeader != null && acceptHeader.contains("text/plain")) {
        String testoSemplice = prodotto.getNome() + " - €" + prodotto.getPrezzo();
        return Response.ok(testoSemplice, MediaType.TEXT_PLAIN).build();
    }
    
    // Default: JSON o XML automatico
    return Response.ok(prodotto).build();
}
```

## Gestione di File e Streaming

### Teoria della Gestione File in REST

La gestione di **file e contenuti binari** in servizi REST presenta sfide specifiche relative a:

#### Problematiche dei File in REST

1. **Memoria**: File grandi possono causare OutOfMemory
2. **Performance**: Trasferimenti lenti impattano l'esperienza utente
3. **Sicurezza**: Validazione e controllo dei contenuti
4. **Concorrenza**: Gestione di accessi multipli simultanei
5. **Transazioni**: Coordinamento tra dati e file

#### Strategie di Implementazione

**Buffering**: Caricamento completo in memoria (piccoli file)

**Streaming**: Trasferimento incrementale (file grandi)

**Chunked Transfer**: Invio in blocchi per ottimizzare la rete

**Content-Disposition**: Controllo del comportamento del browser

#### Considerazioni Architetturali

- **Separazione**: File storage separato dal database
- **CDN Integration**: Distribuzione geografica per performance
- **Caching**: Strategie di cache per contenuti statici
- **Versioning**: Gestione di versioni multiple dei file

### Download di File

```java
@GET
@Path("/export/{formato}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public Response esportaProdotti(@PathParam("formato") String formato) {
    
    if (!"csv".equals(formato) && !"excel".equals(formato)) {
        return Response.status(Response.Status.BAD_REQUEST)
                      .entity("Formato non supportato")
                      .build();
    }
    
    byte[] fileData = service.esportaProdotti(formato);
    String filename = "prodotti." + formato;
    String mediaType = "csv".equals(formato) ? "text/csv" : 
                      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
    return Response.ok(fileData, mediaType)
                  .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                  .header("Content-Length", fileData.length)
                  .build();
}
```

### Streaming di Contenuto

#### Teoria dello Streaming HTTP

Lo **streaming** è una tecnica fondamentale per gestire grandi volumi di dati senza compromettere le performance del server. I vantaggi includono:

**Vantaggi dello Streaming:**

1. **Memoria Costante**: Utilizzo fisso indipendentemente dalla dimensione dei dati
2. **Responsiveness**: Il client riceve dati immediatamente
3. **Scalabilità**: Supporto per migliaia di richieste concorrenti
4. **Interruzione**: Possibilità di terminare il trasferimento prematuramente

#### Implementazione StreamingOutput

L'interfaccia `StreamingOutput` di JAX-RS permette di:

- **Controllo Fine**: Gestione diretta dell'OutputStream
- **Flush Strategico**: Ottimizzazione del buffering di rete
- **Gestione Errori**: Handling di eccezioni durante il trasferimento
- **Resource Management**: Chiusura appropriata delle risorse

#### Pattern di Streaming Comuni

1. **Database Streaming**: Risultati query grandi processati incrementalmente
2. **File Streaming**: Trasferimento di file senza caricamento completo
3. **Real-time Data**: Streaming di eventi o dati live
4. **Batch Processing**: Esportazione di grandi dataset

```java
@GET
@Path("/large-report")
@Produces(MediaType.APPLICATION_JSON)
public Response getLargeReport() {
    
    StreamingOutput stream = new StreamingOutput() {
        @Override
        public void write(OutputStream output) throws IOException {
            try (PrintWriter writer = new PrintWriter(output)) {
                writer.println("{\"data\": [");
                
                // Simula processamento di grandi quantità di dati
                for (int i = 0; i < 1000000; i++) {
                    writer.println("  {\"id\": " + i + ", \"value\": \"data" + i + "\"}");
                    if (i < 999999) writer.println(",");
                    
                    // Flush periodico per evitare accumulo memoria
                    if (i % 1000 == 0) {
                        writer.flush();
                    }
                }
                
                writer.println("]}");
            }
        }
    };
    
    return Response.ok(stream)
                  .header("Content-Type", MediaType.APPLICATION_JSON)
                  .build();
}
```

## Response Condizionali

### Redirect e Forward

```java
@GET
@Path("/prodotto-del-giorno")
public Response getProdottoDelGiorno() {
    int idProdottoDelGiorno = service.calcolaIdProdottoDelGiorno();
    
    // Redirect temporaneo
    URI location = URI.create("/api/prodotti/" + idProdottoDelGiorno);
    return Response.temporaryRedirect(location).build(); // 307
}

@GET
@Path("/legacy/{id}")
public Response legacyEndpoint(@PathParam("id") int id) {
    // Redirect permanente per API deprecate
    URI newLocation = URI.create("/api/v2/prodotti/" + id);
    return Response.status(Response.Status.MOVED_PERMANENTLY)
                  .location(newLocation)
                  .build(); // 301
}
```

## Best Practices per Response Building

### Teoria delle Best Practices

L'implementazione di **best practices** nel Response Building è cruciale per creare API REST professionali, maintainabili e interoperabili. Queste pratiche derivano da:

#### Principi Fondamentali

1. **Consistenza**: Comportamento uniforme in tutta l'API
2. **Prevedibilità**: Risposte che rispettano gli standard HTTP
3. **Usabilità**: Informazioni utili per gli sviluppatori client
4. **Robustezza**: Gestione appropriata di errori e casi edge
5. **Performance**: Ottimizzazione delle risposte per efficienza

#### Impatto delle Best Practices

- **Developer Experience**: API più facili da integrare
- **Debugging**: Troubleshooting semplificato
- **Scalabilità**: Performance ottimizzate per alta concorrenza
- **Manutenibilità**: Codice più pulito e gestibile
- **Conformità**: Rispetto degli standard industriali

### 1. Utilizzo Consistente dei Codici di Stato

#### Importanza della Consistenza

La **consistenza nei codici di stato** è fondamentale perché:

- I client possono implementare logica di retry e error handling
- Gli strumenti di monitoring possono categorizzare correttamente gli errori
- La documentazione API diventa più chiara e utilizzabile
- Si rispettano le aspettative degli sviluppatori REST

```java
// ✅ CORRETTO: Codici di stato appropriati
@POST
public Response creaRisorsa(Risorsa risorsa) {
    if (!validaInput(risorsa)) {
        return Response.status(400).build(); // Bad Request
    }
    
    try {
        Risorsa creata = service.crea(risorsa);
        return Response.status(201).entity(creata).build(); // Created
    } catch (ConflictException e) {
        return Response.status(409).build(); // Conflict
    }
}

// ❌ SBAGLIATO: Sempre 200 OK
@POST
public Response creaRisorsaMale(Risorsa risorsa) {
    return Response.ok("Operazione completata").build(); // Sempre 200!
}
```

### 2. Header Informativi

```java
@GET
@Path("/search")
public Response cercaProdotti(@QueryParam("q") String query,
                             @QueryParam("page") @DefaultValue("0") int page,
                             @QueryParam("size") @DefaultValue("20") int size) {
    
    RisultatoPaginato<Prodotto> risultato = service.cerca(query, page, size);
    
    return Response.ok(risultato.getContenuto())
                  .header("X-Total-Count", risultato.getTotaleElementi())
                  .header("X-Page-Number", page)
                  .header("X-Page-Size", size)
                  .header("X-Total-Pages", risultato.getTotalePagine())
                  .build();
}
```

### 3. Gestione Errori Strutturata

#### Teoria della Gestione Errori

La **gestione errori strutturata** rappresenta un aspetto critico delle API REST professionali. Una strategia ben definita permette:

**Benefici della Strutturazione:**

1. **Debugging Semplificato**: Informazioni dettagliate per identificare problemi
2. **Automazione**: Client possono gestire errori programmaticamente
3. **Logging Strutturato**: Correlazione e analisi degli errori
4. **User Experience**: Messaggi informativi per gli utenti finali
5. **Compliance**: Rispetto degli standard di sicurezza (non esporre dettagli interni)

#### Componenti di una Risposta di Errore

- **Codice Errore**: Identificatore univoco per categoria di errore
- **Messaggio**: Descrizione human-readable del problema
- **Dettagli**: Informazioni specifiche per il debugging
- **Timestamp**: Momento dell'errore per correlazione nei log
- **Trace ID**: Identificatore per tracciare richieste distribuite

#### Pattern di Error Handling

1. **Exception Mapping**: Trasformazione automatica eccezioni → Response
2. **Error Codes**: Sistemi di codici consistenti per categorizzazione
3. **Contextual Information**: Informazioni specifiche del dominio
4. **Internationalization**: Supporto multi-lingua per messaggi

```java
public class ErrorResponse {
    private String codiceErrore;
    private String messaggio;
    private String dettagli;
    private Instant timestamp;
    
    // costruttori, getter, setter...
}

@PUT
@Path("/{id}")
public Response aggiorna(@PathParam("id") int id, Prodotto prodotto) {
    try {
        Prodotto aggiornato = service.aggiorna(id, prodotto);
        return Response.ok(aggiornato).build();
        
    } catch (ValidationException e) {
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR", 
            "Dati non validi", 
            e.getMessage(),
            Instant.now()
        );
        return Response.status(400).entity(error).build();
        
    } catch (NotFoundException e) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND", 
            "Risorsa non trovata", 
            "Prodotto con ID " + id + " non esiste",
            Instant.now()
        );
        return Response.status(404).entity(error).build();
    }
}
```

## Considerazioni Architetturali Avanzate

### Integrazione con Microservizi

Nell'architettura a microservizi, il Response Building assume importanza critica:

- **Correlation IDs**: Tracciamento di richieste attraverso servizi multipli
- **Circuit Breaker Pattern**: Gestione di fallimenti a cascata
- **Bulkhead Pattern**: Isolamento di risorse per resilienza
- **Timeout Management**: Gestione appropriata dei timeout di rete

### Performance e Ottimizzazione

#### Strategie di Ottimizzazione

1. **Response Compression**: Utilizzo di gzip per ridurre il payload
2. **Partial Responses**: Filtraggio campi per ridurre dati trasferiti  
3. **Connection Pooling**: Riutilizzo connessioni per efficienza
4. **Async Processing**: Elaborazione asincrona per operazioni lunghe

#### Monitoring e Osservabilità

- **Metrics**: Tempo di risposta, throughput, error rate
- **Logging**: Structured logging per analisi automatizzata
- **Tracing**: Distribuzione delle richieste per performance tuning
- **Health Checks**: Endpoint per monitoring dello stato del servizio

### Sicurezza Avanzata

#### Protezione dei Dati

- **Data Minimization**: Esporre solo dati necessari
- **Sensitive Data Masking**: Mascheramento informazioni sensibili
- **Audit Logging**: Tracciamento accessi per compliance
- **Rate Limiting**: Protezione da attacchi DoS

## Glossario

| Termine | Definizione |
|---------|-------------|
| **Response Builder** | Pattern per costruire oggetti Response in modo fluido |
| **Status Code** | Codice numerico HTTP che indica l'esito dell'operazione |
| **Header HTTP** | Metadati associati alla richiesta o risposta HTTP |
| **Content-Disposition** | Header che specifica come il browser deve gestire il contenuto |
| **StreamingOutput** | Interfaccia per scrivere contenuto in streaming |
| **ETag** | Header per il controllo della cache e concorrenza ottimistica |
| **Location** | Header che specifica l'URI di una risorsa creata o spostata |
| **Content Negotiation** | Processo di selezione del formato più appropriato per la risposta |
| **CORS** | Cross-Origin Resource Sharing - meccanismo per gestire richieste cross-domain |
| **Circuit Breaker** | Pattern per gestire fallimenti in sistemi distribuiti |
| **Correlation ID** | Identificatore univoco per tracciare richieste attraverso servizi multipli |
