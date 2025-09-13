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
| **HATEOAS**                   | Hypermedia as the Engine of Application State. L'idea che una risposta REST debba contenere link per guidare il client nelle interazioni successive. |
| **JAX-RS**                    | La specifica Java EE per la creazione di servizi web RESTful.                                                                          |
