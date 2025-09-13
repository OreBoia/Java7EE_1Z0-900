# Gestione degli Errori, Integrazione e Casi d'Uso di WebSocket

Oltre alla gestione dei messaggi e del ciclo di vita, è importante capire come gestire le condizioni di errore, come integrare gli endpoint WebSocket con il resto dell'ecosistema Java EE e quando la tecnologia WebSocket è la scelta giusta rispetto ad alternative come HTTP.

## Gestione degli Errori

Gli errori in una comunicazione WebSocket possono verificarsi per molte ragioni: problemi di rete, errori di decodifica, eccezioni non gestite nella logica di business, o una chiusura anomala della connessione.

### Il Metodo `@OnError`

La Java API for WebSocket fornisce un meccanismo di callback centralizzato per la gestione degli errori tramite il metodo annotato con `@OnError`. Questo metodo viene invocato dal container quando si verifica un'eccezione durante il processamento dei messaggi o la gestione della sessione.

**Firma del metodo:**
Il metodo `@OnError` riceve tipicamente la `Session` in cui si è verificato l'errore e l'oggetto `Throwable` che rappresenta l'errore stesso.

```java
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/my-endpoint")
public class ErrorHandlingEndpoint {

    // ... altri metodi @OnOpen, @OnMessage, @OnClose ...

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Si è verificato un errore sulla sessione: " + session.getId());
        System.err.println("Causa: " + throwable.getMessage());
        
        // È buona pratica fare logging dell'errore per il monitoraggio
        // log.error("WebSocket error on session " + session.getId(), throwable);

        // A seconda della gravità, si potrebbe voler chiudere la connessione
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Errore interno."));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**Compiti comuni del metodo `@OnError`:**

- **Logging**: Registrare i dettagli dell'errore per il debug e il monitoraggio. Questa è la funzione più importante.
- **Pulizia**: Rimuovere la sessione da qualsiasi collezione condivisa per evitare di tentare di inviare messaggi a una connessione corrotta.
- **Notifica**: In alcuni casi, si potrebbe tentare di inviare un messaggio di errore al client prima di chiudere la connessione.
- **Chiusura della Connessione**: Chiudere la sessione in modo pulito se l'errore è considerato fatale.

## Integrazione con CDI ed EJB

Gli endpoint WebSocket non sono componenti isolati; possono e devono integrarsi con la logica di business dell'applicazione. La specifica JSR 356 permette l'uso della **Dependency Injection (DI)** per iniettare altri bean Java EE, come bean CDI o EJB, direttamente nell'istanza dell'endpoint.

Questo permette di separare le responsabilità: l'endpoint si occupa della comunicazione WebSocket, mentre la logica di business (es. salvare un messaggio nel database, processare un ordine) è delegata a servizi appositi.

### Esempio di Iniezione di un EJB

```java
import javax.ejb.EJB;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat-service")
public class IntegratedChatEndpoint {

    // Iniezione di un EJB che contiene la logica di business per la chat
    @EJB
    private ChatService chatService;

    @OnMessage
    public void onMessage(String message, Session session) {
        String username = (String) session.getUserProperties().get("username");
        
        // Delega la logica di business al servizio iniettato
        chatService.processAndBroadcastMessage(username, message);
    }
    
    // ... altri metodi ...
}
```

### Esempio di Iniezione di un Bean CDI

```java
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/notification")
public class NotificationEndpoint {

    // Iniezione di un bean CDI
    @Inject
    private NotificationService notificationService;

    @OnMessage
    public void onMessage(String message) {
        notificationService.handleNotification(message);
    }
    
    // ...
}
```

## WebSocket vs. HTTP: Quando Usare Cosa?

WebSocket è una tecnologia potente, ma non è la soluzione per ogni problema. Scegliere tra WebSocket e il tradizionale modello HTTP (o altre alternative) dipende dai requisiti specifici dell'applicazione.

| Caratteristica              | WebSocket                                                              | HTTP (Request/Response)                                                |
| --------------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| **Modello di Comunicazione**| **Full-duplex, persistente, stateful**. Ideale per interazioni continue. | **Half-duplex, stateless, a breve durata**. Ideale per interazioni discrete. |
| **Latenza**                 | **Bassa**. Dopo l'handshake, l'overhead per messaggio è minimo.          | **Più alta**. Ogni richiesta/risposta ha un overhead significativo (header). |
| **Push dal Server**         | **Nativo**. Il server può inviare dati in qualsiasi momento.             | **Non nativo**. Richiede tecniche come long-polling o Server-Sent Events. |
| **Scalabilità**             | Mantenere molte connessioni aperte può consumare risorse sul server.     | Altamente scalabile grazie alla sua natura stateless.                  |
| **Complessità**             | Maggiore complessità nella gestione dello stato della connessione.       | Modello più semplice e consolidato.                                    |

### Scenari Ideali per WebSocket

- **Applicazioni in tempo reale**: Chat, giochi online, editor collaborativi.
- **Aggiornamenti continui**: Dashboard finanziari, feed di social media, monitoraggio live.
- **Comunicazioni event-driven**: Notifiche push istantanee quando si verifica un evento sul server.

### Scenari Ideali per HTTP

- **API RESTful**: Operazioni CRUD (Create, Read, Update, Delete) su risorse.
- **Siti web tradizionali**: Caricamento di pagine, invio di form.
- **Eventi rari o poco frequenti**: Se il server deve inviare dati al client solo occasionalmente, alternative più leggere potrebbero essere migliori.

### Alternativa: Server-Sent Events (SSE)

SSE è uno standard HTML5 che permette al server di inviare aggiornamenti al client su una normale connessione HTTP.

- **Unidirezionale**: Solo il server può inviare dati al client.
- **Più semplice di WebSocket**: Si basa su HTTP e non richiede un protocollo separato.
- **Ideale per**: Notifiche, aggiornamenti di stato, feed di notizie dove il client deve solo ricevere.

## Glossario dei Termini Importanti

| Termine                 | Definizione                                                                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **`@OnError`**          | L'annotazione che identifica il metodo di callback per la gestione centralizzata degli errori in un endpoint WebSocket.                  |
| **Dependency Injection (DI)** | Un pattern di progettazione in cui le dipendenze di un oggetto (altri oggetti di cui ha bisogno) vengono fornite ("iniettate") da un framework esterno. |
| **Server-Sent Events (SSE)** | Una tecnologia che permette a un server di inviare eventi a un client in modo unidirezionale su una connessione HTTP.                |
| **Long Polling**        | Una tecnica di "emulazione" del push in cui il client fa una richiesta HTTP che il server tiene aperta finché non ha dati da inviare.     |
| **Full-Duplex**         | Comunicazione bidirezionale in cui i dati possono fluire in entrambe le direzioni simultaneamente.                                     |
