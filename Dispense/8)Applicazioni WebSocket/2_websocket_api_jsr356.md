# Java API for WebSocket (JSR 356)

La specifica **JSR 356** definisce l'API standard di Java EE 7 per la creazione di applicazioni WebSocket. Fornisce un modello di programmazione basato su annotazioni e un'interfaccia programmatica per la gestione degli **endpoint** WebSocket, sia lato server che lato client.

## Endpoint Lato Server

Un endpoint WebSocket sul server è il componente che gestisce le connessioni e i messaggi in arrivo dai client. JSR 356 offre due modi per creare un endpoint:

1. **Approccio basato su Annotazioni (POJO)**: Il più comune e semplice. Si crea una classe POJO (Plain Old Java Object) e la si annota con `@ServerEndpoint`.
2. **Approccio Programmatico**: Si estende la classe `javax.websocket.Endpoint`. Questo approccio è più flessibile ma anche più verboso.

### Approccio con Annotazioni (`@ServerEndpoint`)

Questo metodo permette di definire un endpoint WebSocket senza dover implementare interfacce specifiche. Il ciclo di vita della connessione e la gestione dei messaggi sono gestiti da metodi annotati.

#### Annotazioni del Ciclo di Vita

| Annotazione | Descrizione                                                                                                                            |
| :---------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `@OnOpen`   | Metodo invocato quando un client stabilisce una nuova connessione con l'endpoint. Ideale per inizializzare la sessione.                 |
| `@OnMessage`| Metodo invocato quando arriva un messaggio dal client. Si possono avere metodi diversi per messaggi di testo, binari o oggetti decodificati. |
| `@OnClose`  | Metodo invocato quando la connessione viene chiusa (dal client o dal server). Utile per operazioni di pulizia.                           |
| `@OnError`  | Metodo invocato quando si verifica un errore di comunicazione durante la sessione.                                                      |

#### Esempio di Endpoint Server (Chat Semplice)

```java
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// Definisce l'URI dell'endpoint. Sarà accessibile a ws://host:port/context-path/chat/{username}
@ServerEndpoint("/chat/{username}")
public class ChatEndpoint {

    // Set sincronizzato per memorizzare tutte le sessioni attive
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        session.getUserProperties().put("username", username);
        sessions.add(session);
        broadcast("Utente [" + username + "] si è unito alla chat.");
        System.out.println("Connessione aperta: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        broadcast("[" + username + "]: " + message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        sessions.remove(session);
        broadcast("Utente [" + username + "] ha lasciato la chat.");
        System.out.println("Connessione chiusa: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore sulla sessione " + session.getId() + ": " + throwable.getMessage());
    }

    // Metodo helper per inviare un messaggio a tutti i client connessi
    private void broadcast(String message) throws IOException {
        for (Session s : sessions) {
            s.getBasicRemote().sendText(message);
        }
    }
}
```

#### Iniezione di Parametri

I metodi annotati possono ricevere parametri utili per gestire la comunicazione:

- `javax.websocket.Session`: Rappresenta la connessione corrente.
- **Payload del messaggio**: `String` per messaggi di testo, `byte[]` o `ByteBuffer` per messaggi binari, o un POJO personalizzato se si usa un `Decoder`.
- `@PathParam`: Per estrarre parti dall'URI, simile a JAX-RS.
- `java.lang.Throwable`: Nel metodo `@OnError`, per ricevere l'eccezione che ha causato l'errore.

## Endpoint Lato Client

Anche se JSR 356 definisce un'API client Java (`@ClientEndpoint`), il caso d'uso più comune per WebSocket è un client basato su JavaScript che gira in un browser web.

### Client JavaScript (Browser)

L'API WebSocket di HTML5 è semplice e intuitiva.

```javascript
// Stabilisce la connessione con l'endpoint server
// L'URL usa ws:// (o wss:// per connessioni sicure)
const username = prompt("Inserisci il tuo username:");
const ws = new WebSocket(`ws://localhost:8080/my-app/chat/${username}`);

// Funzione chiamata all'apertura della connessione
ws.onopen = function(event) {
    console.log("Connessione WebSocket stabilita.");
    document.getElementById("status").innerHTML = "Connesso";
};

// Funzione chiamata alla ricezione di un messaggio dal server
ws.onmessage = function(event) {
    // event.data contiene il messaggio ricevuto
    const messagesDiv = document.getElementById("messages");
    messagesDiv.innerHTML += `<p>${event.data}</p>`;
};

// Funzione chiamata alla chiusura della connessione
ws.onclose = function(event) {
    console.log("Connessione WebSocket chiusa.");
    document.getElementById("status").innerHTML = "Disconnesso";
};

// Funzione chiamata in caso di errore
ws.onerror = function(event) {
    console.error("Errore WebSocket:", event);
};

// Funzione per inviare un messaggio al server
function sendMessage() {
    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value;
    ws.send(message); // Invia il messaggio come testo
    messageInput.value = "";
}
```

## Glossario dei Termini e delle Annotazioni

| Termine/Annotazione | Definizione                                                                                                                            |
| :------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `@ServerEndpoint`   | Annotazione che designa una classe POJO come un endpoint WebSocket lato server, mappandolo a un URI.                                     |
| `@ClientEndpoint`   | Annotazione che designa una classe POJO come un endpoint WebSocket lato client (per client Java).                                        |
| `Session`           | Rappresenta una singola connessione WebSocket attiva e permette di inviare messaggi, chiudere la connessione e memorizzare dati utente. |
| `@OnOpen`           | Identifica il metodo da eseguire all'apertura di una nuova connessione.                                                                |
| `@OnMessage`        | Identifica il metodo da eseguire alla ricezione di un messaggio.                                                                       |
| `@OnClose`          | Identifica il metodo da eseguire alla chiusura della connessione.                                                                      |
| `@OnError`          | Identifica il metodo da eseguire in caso di errore di protocollo o di comunicazione.                                                   |
| `@PathParam`        | Inietta nei parametri di un metodo una parte dell'URI dell'endpoint.                                                                  |
| `Decoder` / `Encoder` | Interfacce che permettono di convertire messaggi WebSocket da/verso oggetti Java personalizzati (POJO).                                |
