# Introduzione ai Servizi REST e JAX-RS

**REST (REpresentational State Transfer)** non è un protocollo, ma uno **stile architetturale** per la creazione di servizi web distribuiti. Proposto da Roy Fielding nella sua tesi di dottorato, REST si basa sui principi e sui protocolli del Web, in particolare su HTTP. L'idea è di avere un'interfaccia uniforme e semplice per interagire con le risorse, rendendo i sistemi più scalabili, flessibili e facili da evolvere.

**JAX-RS (Java API for RESTful Web Services)** è la specifica di Java EE che fornisce un insieme di API, tramite annotazioni, per sviluppare servizi web secondo lo stile architetturale REST.

## Principi Fondamentali di REST

Un servizio è considerato "RESTful" se aderisce ai seguenti principi:

### 1. Architettura Client-Server

C'è una netta separazione tra il client (che si occupa dell'interfaccia utente) e il server (che si occupa della logica di business e del salvataggio dei dati). Comunicano attraverso una rete su un protocollo standard (HTTP).

### 2. Stateless (Senza Stato)

Ogni richiesta inviata dal client al server deve contenere tutte le informazioni necessarie al server per comprenderla ed eseguirla. Il server non memorizza alcun contesto del client tra una richiesta e l'altra. Se è necessario mantenere uno stato (es. una sessione utente), questo deve essere gestito dal client e inviato a ogni richiesta (es. tramite un token di autenticazione nell'header).

### 3. Cacheable (Memorizzabile nella Cache)

Le risposte del server dovrebbero, implicitamente o esplicitamente, definirsi come memorizzabili nella cache o meno. Questo migliora le performance e la scalabilità, riducendo il carico sul server.

### 4. Interfaccia Uniforme

Questo è il principio chiave di REST e si articola in quattro sotto-vincoli:

* **Identificazione delle Risorse tramite URI**: Ogni risorsa (es. un utente, un prodotto, un ordine) è identificata in modo univoco da un URI (Uniform Resource Identifier). Esempio: `/utenti/123` identifica l'utente con ID 123.
* **Manipolazione delle Risorse tramite Rappresentazioni**: Il client interagisce con una *rappresentazione* della risorsa, non con la risorsa stessa. Le rappresentazioni più comuni sono in formato **JSON** o **XML**. Il client può richiedere un formato specifico tramite l'header HTTP `Accept`.
* **Messaggi Auto-descrittivi**: Ogni messaggio (richiesta/risposta) contiene abbastanza informazioni per descrivere come processarlo. Ad esempio, l'header `Content-Type` specifica il formato del corpo del messaggio.
* **HATEOAS (Hypermedia as the Engine of Application State)**: Principio avanzato secondo cui la rappresentazione di una risorsa dovrebbe contenere link (hypermedia) ad altre azioni o risorse correlate, permettendo al client di "scoprire" dinamicamente come navigare l'API.

## HATEOAS (Hypermedia as the Engine of Application State)

**HATEOAS** è uno dei vincoli architetturali più sofisticati e spesso sottovalutati di REST. Il principio stabilisce che l'applicazione client deve essere guidata esclusivamente attraverso i link ipermediali forniti dinamicamente dal server nelle risposte, senza dover conoscere a priori la struttura dell'API.

### Concetti Chiave di HATEOAS

#### 1. **Hypermedia Controls**

I controlli ipermediali sono link e form che descrivono le azioni disponibili per il client in un determinato stato dell'applicazione. Questi includono:

* **Link**: Collegamenti ad altre risorse o azioni (`rel`, `href`, `method`)
* **Form**: Descrizioni di come inviare dati per modificare lo stato
* **Affordances**: Indicazioni su cosa il client può fare in un dato momento

#### 2. **Discoverable API**

Con HATEOAS, l'API diventa auto-descrittiva. Il client inizia da un singolo punto di ingresso (spesso chiamato "root" o "entry point") e da lì può scoprire tutte le funzionalità disponibili seguendo i link forniti.

#### 3. **Loose Coupling**

Il client non ha bisogno di conoscere gli URL specifici delle risorse. Questo riduce l'accoppiamento tra client e server, rendendo l'API più flessibile e evolvibile.

### Esempio Pratico di HATEOAS

Consideriamo un'API per la gestione di ordini:

**Richiesta iniziale:**

```http
GET /api/ordini/123
Accept: application/json
```

**Risposta con HATEOAS:**

```json
{
  "id": 123,
  "stato": "in_elaborazione",
  "totale": 99.99,
  "dataCreazione": "2024-10-13T10:30:00Z",
  "articoli": [
    {
      "prodottoId": 456,
      "quantita": 2,
      "prezzo": 49.99
    }
  ],
  "_links": {
    "self": {
      "href": "/api/ordini/123",
      "method": "GET"
    },
    "update": {
      "href": "/api/ordini/123",
      "method": "PUT",
      "title": "Aggiorna ordine"
    },
    "cancel": {
      "href": "/api/ordini/123/annulla",
      "method": "POST",
      "title": "Annulla ordine"
    },
    "customer": {
      "href": "/api/clienti/789",
      "method": "GET",
      "title": "Visualizza cliente"
    },
    "payment": {
      "href": "/api/ordini/123/pagamento",
      "method": "POST",
      "title": "Procedi al pagamento"
    }
  }
}
```

#### Navigazione Dinamica

Il client può ora:

1. **Seguire il link "customer"** per ottenere i dettagli del cliente
2. **Utilizzare il link "cancel"** per annullare l'ordine
3. **Procedere al pagamento** tramite il link "payment"

Se l'ordine fosse in uno stato diverso (es. "spedito"), i link disponibili sarebbero differenti:

```json
{
  "id": 123,
  "stato": "spedito",
  "totale": 99.99,
  "_links": {
    "self": {
      "href": "/api/ordini/123"
    },
    "tracking": {
      "href": "/api/ordini/123/tracking",
      "method": "GET",
      "title": "Traccia spedizione"
    },
    "return": {
      "href": "/api/ordini/123/reso",
      "method": "POST",
      "title": "Richiedi reso"
    }
  }
}
```

### Implementazione HATEOAS con JAX-RS

```java
@Path("/ordini")
public class OrdineResource {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrdine(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        Ordine ordine = ordineService.findById(id);
        
        // Creazione della rappresentazione con link
        OrdineDTO dto = new OrdineDTO(ordine);
        
        // Aggiunta link dinamici basati sullo stato
        addHateoasLinks(dto, ordine, uriInfo);
        
        return Response.ok(dto).build();
    }
    
    private void addHateoasLinks(OrdineDTO dto, Ordine ordine, UriInfo uriInfo) {
        // Link self sempre presente
        dto.addLink("self", 
            uriInfo.getAbsolutePathBuilder().build().toString(), 
            "GET");
        
        // Link condizionali basati sullo stato
        switch (ordine.getStato()) {
            case IN_ELABORAZIONE:
                dto.addLink("update",
                    uriInfo.getAbsolutePathBuilder().build().toString(),
                    "PUT");
                dto.addLink("cancel",
                    uriInfo.getAbsolutePathBuilder().path("annulla").build().toString(),
                    "POST");
                dto.addLink("payment",
                    uriInfo.getAbsolutePathBuilder().path("pagamento").build().toString(),
                    "POST");
                break;
                
            case SPEDITO:
                dto.addLink("tracking",
                    uriInfo.getAbsolutePathBuilder().path("tracking").build().toString(),
                    "GET");
                dto.addLink("return",
                    uriInfo.getAbsolutePathBuilder().path("reso").build().toString(),
                    "POST");
                break;
        }
        
        // Link a risorse correlate
        dto.addLink("customer",
            uriInfo.getBaseUriBuilder().path("clienti").path(ordine.getClienteId().toString()).build().toString(),
            "GET");
    }
}
```

### Vantaggi di HATEOAS

1. **Evolvibilità dell'API**: Gli URL possono cambiare senza rompere i client
2. **Auto-documentazione**: L'API descrive se stessa attraverso i link
3. **Controllo dello stato**: Il server guida il client attraverso i workflow
4. **Riduzione degli errori**: Il client non può eseguire azioni non permesse
5. **Loose coupling**: Minore dipendenza tra client e server

### Sfide e Considerazioni

1. **Complessità aggiuntiva**: Richiede più logica lato server e client
2. **Overhead**: Aumenta la dimensione delle risposte
3. **Caching**: I link dinamici possono complicare la cache
4. **Supporto client**: Non tutti i framework client supportano nativamente HATEOAS
5. **Standardizzazione**: Esistono diversi formati (HAL, JSON-LD, Siren)

### Standard e Formati Comuni

#### HAL (Hypertext Application Language)

```json
{
  "id": 123,
  "stato": "in_elaborazione",
  "_links": {
    "self": { "href": "/ordini/123" },
    "cancel": { "href": "/ordini/123/annulla" }
  },
  "_embedded": {
    "articoli": [...]
  }
}
```

#### JSON-LD (JSON for Linked Data)

```json
{
  "@context": "https://schema.org/",
  "@type": "Order",
  "@id": "/ordini/123",
  "orderStatus": "Processing",
  "potentialAction": {
    "@type": "CancelAction",
    "target": "/ordini/123/annulla"
  }
}
```

### 5. Sistema a Livelli (Layered System)

L'architettura può essere composta da più livelli (es. proxy, gateway, load balancer) che si interpongono tra il client e il server finale. Il client non ha bisogno di sapere con quale server sta comunicando direttamente.

## Utilizzo dei Metodi e dei Codici di Stato HTTP

REST sfrutta appieno le semantiche del protocollo HTTP.

### Metodi HTTP

| Metodo  | Scopo Principale                                       | Idempotente? | Sicuro? | Esempio di Utilizzo                |
| :------ | :----------------------------------------------------- | :----------- | :------ | :--------------------------------- |
| **GET**     | Recuperare una rappresentazione di una risorsa.        | Sì           | Sì      | `GET /utenti/123` (legge un utente) |
| **POST**    | Creare una nuova risorsa o eseguire un'azione.         | No           | No      | `POST /utenti` (crea un nuovo utente) |
| **PUT**     | Aggiornare/sostituire completamente una risorsa esistente o crearla se non esiste a un URI noto. | Sì           | No      | `PUT /utenti/123` (aggiorna l'utente 123) |
| **DELETE**  | Rimuovere una risorsa.                                 | Sì           | No      | `DELETE /utenti/123` (cancella l'utente 123) |
| **PATCH**   | Applicare un aggiornamento parziale a una risorsa.     | No           | No      | `PATCH /utenti/123` (aggiorna solo l'email) |
| **HEAD**    | Recuperare solo gli header di una risorsa (metadati).  | Sì           | Sì      | `HEAD /utenti/123`                 |
| **OPTIONS** | Scoprire le opzioni di comunicazione per una risorsa.  | Sì           | Sì      | `OPTIONS /utenti/123`              |

### Codici di Stato HTTP

I codici di stato sono usati per comunicare l'esito della richiesta.

* **2xx (Successo)**: `200 OK`, `201 Created`, `204 No Content`
* **3xx (Reindirizzamento)**: `301 Moved Permanently`, `304 Not Modified`
* **4xx (Errore del Client)**: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`
* **5xx (Errore del Server)**: `500 Internal Server Error`, `503 Service Unavailable`

## JAX-RS: REST in Java

JAX-RS semplifica lo sviluppo di servizi RESTful in Java tramite annotazioni.

**Esempio di base di una risorsa JAX-RS:**

```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/utenti") // Mappa questa classe all'URI /utenti
public class UtenteResource {

    @GET // Risponde a richieste HTTP GET
    @Path("/{id}") // Mappa questo metodo a /utenti/{id}
    @Produces(MediaType.APPLICATION_JSON) // Produce una risposta in formato JSON
    public Utente getUtenteById(@PathParam("id") int id) {
        // Logica per trovare l'utente con l'ID specificato
        Utente utente = ... ; 
        return utente;
    }
}
```

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Risorsa (Resource)**        | Qualsiasi "cosa" che può essere identificata da un URI (es. un documento, un utente, un'immagine). È l'astrazione chiave in REST.        |
| **Rappresentazione (Representation)** | Un formato specifico di una risorsa in un dato momento (es. un documento JSON o XML che descrive un utente).                       |
| **URI (Uniform Resource Identifier)** | Una stringa che identifica univocamente una risorsa.                                                                           |
| **Stateless**                 | Principio secondo cui il server non mantiene lo stato della sessione del client tra le richieste.                                        |
| **Idempotente**               | Un'operazione che produce lo stesso risultato se eseguita una o più volte (es. `PUT` o `DELETE`). `POST` non è idempotente.              |
| **Sicuro (Safe)**             | Un'operazione che non modifica lo stato della risorsa sul server (es. `GET`, `HEAD`).                                                  |
| **Content Negotiation**       | Il processo con cui client e server si accordano sul formato della rappresentazione da scambiare (es. tramite header `Accept` e `Content-Type`). |
| **HATEOAS**                   | Hypermedia as the Engine of Application State. Principio REST secondo cui le risposte del server devono contenere link ipermediali (hypermedia controls) che guidano dinamicamente il client attraverso le azioni disponibili e i possibili stati dell'applicazione, rendendo l'API auto-descrittiva e discoverable. |
| **JAX-RS**                    | La specifica Java EE per la creazione di servizi web RESTful.                                                                          |
