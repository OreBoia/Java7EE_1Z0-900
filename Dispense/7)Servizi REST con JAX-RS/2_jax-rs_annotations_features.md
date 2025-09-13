# JAX-RS 2.0: Annotazioni e Funzionalità Principali

JAX-RS (Java API for RESTful Web Services) è una specifica di Java EE che, tramite un set di annotazioni e interfacce, semplifica enormemente lo sviluppo di servizi web RESTful. La versione 2.0, introdotta in Java EE 7, ha aggiunto importanti funzionalità come un'API client standard, filtri e interceptor.

## Annotazioni Fondamentali

Le annotazioni sono il cuore di JAX-RS. Permettono di mappare classi e metodi Java a URI e metodi HTTP, trasformando un POJO (Plain Old Java Object) in una risorsa RESTful.

### Annotazioni di Routing

| Annotazione | Descrizione                                                                                             |
| :---------- | :------------------------------------------------------------------------------------------------------ |
| `@Path`     | Mappa una classe (risorsa root) o un metodo a un percorso URI. Può contenere template (es. `/{id}`).     |
| `@GET`      | Mappa un metodo al verbo HTTP GET (recupero dati).                                                      |
| `@POST`     | Mappa un metodo al verbo HTTP POST (creazione di una nuova risorsa).                                    |
| `@PUT`      | Mappa un metodo al verbo HTTP PUT (aggiornamento/sostituzione completa di una risorsa).                 |
| `@DELETE`   | Mappa un metodo al verbo HTTP DELETE (rimozione di una risorsa).                                        |
| `@PATCH`    | Mappa un metodo al verbo HTTP PATCH (aggiornamento parziale di una risorsa).                            |
| `@HEAD`     | Mappa un metodo al verbo HTTP HEAD (recupero dei soli metadati).                                        |
| `@OPTIONS`  | Mappa un metodo al verbo HTTP OPTIONS (scoperta delle opzioni di comunicazione).                        |

### Annotazioni per la Negoziazione del Contenuto (Content Negotiation)

| Annotazione | Descrizione                                                                                             |
| :---------- | :------------------------------------------------------------------------------------------------------ |
| `@Produces` | Specifica il/i tipo/i MIME (es. `application/json`, `application/xml`) che il metodo della risorsa può produrre e restituire al client. |
| `@Consumes` | Specifica il/i tipo/i MIME che il metodo della risorsa può consumare, ovvero accettare in input dal client. |

### Esempio di Risorsa Base

```java
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/prodotti") // URI base per questa risorsa
public class ProdottoResource {

    @GET
    @Path("/{id}") // URI completo: /prodotti/{id}
    @Produces(MediaType.APPLICATION_JSON) // Restituisce JSON
    public Prodotto getProdotto(@PathParam("id") int id) {
        // Logica per recuperare il prodotto
        Prodotto p = findProdottoById(id);
        return p; // JAX-RS si occupa della conversione Prodotto -> JSON
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) // Accetta JSON in input
    public void creaProdotto(Prodotto prodotto) {
        // Logica per salvare il nuovo prodotto
        save(prodotto);
    }
}
```

## Configurazione e Bootstrap

Perché un'applicazione Java EE riconosca e pubblichi le risorse JAX-RS, è necessario registrarle. Con le specifiche di Servlet 3.0 e successive (incluse in Java EE 7), questo processo è stato semplificato e non richiede più configurazione XML.

### Approccio Moderno con `@ApplicationPath`

Il metodo standard e preferito consiste nel creare una classe che estende `javax.ws.rs.core.Application` e annotarla con `@ApplicationPath`.

```java
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// Definisce il path base per tutti i servizi REST dell'applicazione.
// Tutte le risorse annotate con @Path saranno relative a questo URI.
// Esempio: /api/prodotti/{id}
@ApplicationPath("/api")
public class JAXRSConfiguration extends Application {
    // Non è necessario aggiungere altro codice qui.
    // Il container Java EE scansionerà automaticamente il classpath 
    // alla ricerca di classi annotate con @Path e @Provider.
}
```

Una volta che questa classe è presente nel `WAR`, il container Java EE:

1. Rileva l'applicazione JAX-RS.
2. Mappa tutti i servizi REST sotto il prefisso `/api`.
3. Scansiona e registra automaticamente tutte le classi annotate con `@Path` (risorse) e `@Provider` (es. `ExceptionMapper`).

Questo approccio, basato sulla "configuration by convention", elimina la necessità di configurare manualmente la servlet JAX-RS nel file `web.xml`.

### Dipendenze e Implementazioni del Server

È importante ricordare che JAX-RS è una **specifica**. L'implementazione concreta è fornita dall'Application Server.

- **GlassFish** e **Payara** usano **Jersey** come implementazione di default.
- **WildFly** e **JBoss EAP** usano **RESTEasy**.
- **Apache TomEE** usa **CXF**.

Non è necessario aggiungere queste librerie al progetto se si utilizza un Application Server Java EE, poiché sono già incluse.

## Iniezione di Parametri (Parameter Injection)

JAX-RS può iniettare vari tipi di dati nei parametri dei metodi della risorsa, estratti direttamente dalla richiesta HTTP.

| Annotazione     | Descrizione                                                                                             | Esempio                               |
| :-------------- | :------------------------------------------------------------------------------------------------------ | :------------------------------------ |
| `@PathParam`    | Inietta un valore da un template nel `@Path`.                                                           | `@PathParam("id") int userId`         |
| `@QueryParam`   | Inietta un valore da un parametro della query string dell'URL (dopo il `?`).                            | `@QueryParam("sort") String sortBy`   |
| `@HeaderParam`  | Inietta il valore di un header HTTP.                                                                    | `@HeaderParam("User-Agent") String ua`|
| `@FormParam`    | Inietta un valore da un campo di un form inviato con `Content-Type` `application/x-www-form-urlencoded`. | `@FormParam("username") String user`  |
| `@CookieParam`  | Inietta il valore di un cookie HTTP.                                                                    | `@CookieParam("JSESSIONID") String sid`|
| `@MatrixParam`  | Inietta un parametro di matrice dall'URL (raro).                                                        | `@MatrixParam("author") String author`|
| `@Context`      | Inietta oggetti contestuali di JAX-RS (es. `UriInfo`, `HttpHeaders`, `SecurityContext`).                | `@Context UriInfo uriInfo`            |

## Marshalling e Unmarshalling Automatico

Una delle funzionalità più potenti di JAX-RS è la capacità di convertire automaticamente oggetti Java in rappresentazioni come JSON o XML (marshalling) e viceversa (unmarshalling).

- **Da Oggetto a JSON/XML (`@Produces`)**: Quando un metodo restituisce un POJO, JAX-RS usa un `MessageBodyWriter` per serializzarlo nel formato richiesto dal client (es. `application/json`).
- **Da JSON/XML a Oggetto (`@Consumes`)**: Quando un metodo accetta un POJO come parametro, JAX-RS usa un `MessageBodyReader` per deserializzare il corpo della richiesta nell'oggetto Java.

In Java EE 7, questo è spesso gestito da provider come **MOXy** o **Jackson** per JSON, e **JAXB** (Java Architecture for XML Binding) per XML. Per funzionare correttamente, i POJO devono seguire le convenzioni JavaBean (costruttore di default, getter/setter) o essere annotati con annotazioni JAXB.

## Controllo della Risposta con `Response`

Per un controllo più fine sulla risposta HTTP, invece di restituire direttamente un POJO, si può costruire e restituire un oggetto `Response`.

```java
import javax.ws.rs.core.Response;

@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response creaProdotto(Prodotto prodotto) {
    Prodotto nuovoProdotto = save(prodotto);
    
    // Costruisce una risposta con:
    // - Codice di stato 201 Created
    // - Header "Location" con l'URI della nuova risorsa
    // - Il nuovo prodotto nel corpo della risposta
    return Response.status(Response.Status.CREATED)
                   .entity(nuovoProdotto)
                   .header("Location", "/prodotti/" + nuovoProdotto.getId())
                   .build();
}
```

## Gestione delle Eccezioni

JAX-RS fornisce un meccanismo elegante per mappare le eccezioni Java a risposte HTTP.

1. **`WebApplicationException`**: Si può lanciare questa eccezione (o una delle sue sottoclassi come `NotFoundException`, `BadRequestException`) per restituire immediatamente un codice di stato HTTP specifico.

    ```java
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Prodotto getProdotto(@PathParam("id") int id) {
        Prodotto p = findProdottoById(id);
        if (p == null) {
            // Lanciare questa eccezione si traduce in una risposta 404 Not Found
            throw new NotFoundException("Prodotto non trovato");
        }
        return p;
    }
    ```

2. **`ExceptionMapper`**: Per una gestione centralizzata e personalizzata, si può creare un `ExceptionMapper`. Questa classe cattura un tipo specifico di eccezione e la converte in un oggetto `Response`.

    ```java
    @Provider // Rende il mapper visibile a JAX-RS
    public class MyCustomExceptionHandler implements ExceptionMapper<MyCustomException> {
        @Override
        public Response toResponse(MyCustomException exception) {
            return Response.status(418) // I'm a teapot
                           .entity(new ErrorMessage(exception.getMessage()))
                           .build();
        }
    }
    ```

## Altre Funzionalità Avanzate

- **Sub-Resource Locators**: Metodi in una classe risorsa annotati con `@Path` ma senza un'annotazione di metodo HTTP (`@GET`, `@POST`, etc.). Restituiscono un'altra classe risorsa, permettendo di creare API gerarchiche e ben organizzate.
- **Filtri e Interceptor (`Filters` / `Interceptors`)**: JAX-RS 2.0 ha introdotto un'API per intercettare le richieste e le risposte, sia lato client che server. I filtri sono usati per modificare header, URI, etc., mentre gli interceptor modificano il corpo (entity) del messaggio. Sono ideali per logging, autenticazione, compressione.
- **API Client**: JAX-RS 2.0 include un'API client fluente per consumare servizi REST in modo standardizzato.

### JAX-RS 2.0 Client API

Una delle novità più importanti di JAX-RS 2.0 è l'introduzione di un'API standard e fluente per consumare servizi REST. Questo permette di scrivere codice client portabile che non dipende da librerie di terze parti.

L'API si basa su un pattern builder che rende la costruzione delle richieste chiara e leggibile.

**Flusso di base:**

1. Creare un'istanza di `Client`.
2. Definire un `WebTarget` per l'URI del servizio.
3. Costruire una `request`.
4. Invocare il metodo HTTP desiderato (`get`, `post`, `put`, etc.).
5. Processare la `Response` o l'entità deserializzata.
6. Chiudere il `Client`.

**Esempio di codice:**

```java
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class CittaClient {

    public void run() {
        // È buona pratica gestire il client con try-with-resources per garantirne la chiusura
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("http://host/app/api/citta");

            // --- Esempio di GET per una lista di oggetti ---
            List<Citta> lista = target
                    .request(MediaType.APPLICATION_JSON) // Imposta l'header Accept: application/json
                    .get(new GenericType<List<Citta>>() {}); // Esegue GET e deserializza in una lista

            System.out.println("Città ricevute: " + lista.size());

            // --- Esempio di POST per creare un nuovo oggetto ---
            Citta nuova = new Citta("Milano");
            Response resp = target
                    .request()
                    .post(Entity.json(nuova)); // Serializza 'nuova' in JSON e invia con POST

            if (resp.getStatus() == 201) { // 201 Created
                System.out.println("Città creata con successo!");
                System.out.println("Location: " + resp.getHeaderString("Location"));
            }
            
            resp.close(); // Chiudere sempre la risposta
        }
    }
}
```

**Componenti chiave:**

- `ClientBuilder.newClient()`: Factory per creare un'istanza di `Client`.
- `client.target(...)`: Crea un `WebTarget` che punta a un URI specifico. È possibile aggiungere path e query parameter in modo fluente.
- `.request(MediaType.X)`: Prepara la chiamata specificando il tipo di media che ci si aspetta in risposta (imposta l'header `Accept`).
- `.get(Class<T>)` o `.get(GenericType<T>)`: Esegue la richiesta GET e tenta di deserializzare la risposta nel tipo specificato. `GenericType` è un wrapper necessario per preservare le informazioni sui tipi generici (come `List<Citta>`) a runtime.
- `Entity.json(obj)`: Helper per creare il corpo (payload) di una richiesta `POST` o `PUT`, serializzando l'oggetto `obj` in formato JSON e impostando l'header `Content-Type` a `application/json`.
- `client.close()`: Rilascia le risorse del client. È fondamentale per evitare resource leak. L'uso di `try-with-resources` è il modo migliore per garantirlo.

Come per il lato server, il client JAX-RS si affida a provider per il marshalling/unmarshalling. Se nel classpath è presente un provider JSON (es. Jackson, MOXy), la conversione da/verso JSON avverrà automaticamente, a patto che i POJO siano compatibili.

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Risorsa Root (Root Resource)** | Una classe Java annotata con `@Path` che rappresenta il punto di ingresso per un set di risorse REST.                                 |
| **Provider**                  | Una classe JAX-RS che estende le funzionalità del framework, come `MessageBodyReader/Writer` o `ExceptionMapper`. Annotata con `@Provider`. |
| **@ApplicationPath**          | Annotazione che definisce il percorso URI di base per tutti i servizi JAX-RS all'interno di un'applicazione.                             |
| **Marshalling**               | Il processo di conversione di un oggetto in memoria (es. un POJO) in un formato per la trasmissione o lo storage (es. JSON, XML).        |
| **Unmarshalling**             | Il processo inverso: convertire dati da un formato (JSON, XML) a un oggetto in memoria.                                                |
| **API Fluente (Fluent API)**  | Uno stile di programmazione in cui le chiamate ai metodi sono concatenate, rendendo il codice più leggibile (es. `Response.status(...).entity(...).build()`). |
| **Client API**                | L'API standard di JAX-RS 2.0 per consumare (chiamare) servizi REST in modo programmatico e portabile.                                    |
| **WebTarget**                 | Un oggetto dell'API Client che rappresenta un URI di una risorsa specifica, su cui si possono costruire e invocare richieste.             |
| **GenericType**               | Una classe usata nell'API Client per gestire la deserializzazione di tipi generici (es. `List<Citta>`) preservando l'informazione sul tipo. |
