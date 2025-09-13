# Ciclo di Vita e Concorrenza negli Endpoint WebSocket

Comprendere il modello di ciclo di vita e di concorrenza degli endpoint WebSocket è fondamentale per scrivere applicazioni robuste e scalabili. La specifica JSR 356 adotta un approccio che semplifica notevolmente la gestione dello stato e la programmazione concorrente.

## Ciclo di Vita dell'Endpoint (Lifecycle)

Per impostazione predefinita, quando si utilizza un endpoint basato su POJO (annotato con `@ServerEndpoint`), il container WebSocket crea una **nuova istanza della classe dell'endpoint per ogni singola connessione client**.

Questo significa che:

1. Quando un client si connette, viene creato un nuovo oggetto `MyEndpoint`.
2. Il metodo `@OnOpen` viene chiamato su questa nuova istanza.
3. Tutti i metodi `@OnMessage`, `@OnError` e `@OnClose` per quella specifica connessione verranno invocati su **quella stessa istanza**.
4. Quando la connessione viene chiusa, l'istanza dell'endpoint diventa idonea per il garbage collection.

Questo modello è **stateful per sessione**. Ogni connessione ha il proprio stato isolato, incapsulato all'interno delle variabili d'istanza del proprio oggetto endpoint.

### Esempio: Gestione dello Stato a Livello di Istanza

Poiché ogni connessione ha la sua istanza, possiamo usare le variabili d'istanza per memorizzare dati specifici di quella sessione, come il nome utente, senza preoccuparci di conflitti con altre connessioni.

```java
@ServerEndpoint("/user-chat/{username}")
public class UserSpecificEndpoint {

    // Variabile d'istanza: ogni connessione avrà il suo 'username'
    private String username;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        this.username = username; // Memorizza lo stato specifico della connessione
        System.out.println("Nuova connessione per l'utente: " + this.username);
    }

    @OnMessage
    public void onMessage(String message) {
        // Possiamo usare 'this.username' e 'this.session' in modo sicuro
        System.out.println("Messaggio da " + this.username + ": " + message);
        try {
            this.session.getBasicRemote().sendText("Messaggio ricevuto!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("Connessione chiusa per l'utente: " + this.username);
    }
}
```

## Modello di Concorrenza (Concurrency)

Il container WebSocket garantisce che, **per una singola istanza di endpoint (e quindi per una singola connessione), i messaggi vengano processati in modo sequenziale**.

- **Single-Threaded per Connessione**: Il container non invocherà mai contemporaneamente due metodi `@OnMessage` sulla stessa istanza di endpoint. Se un client invia più messaggi rapidamente, questi verranno accodati e processati uno alla volta. Questo elimina la necessità di sincronizzare l'accesso alle variabili d'istanza all'interno di un endpoint.

- **Multi-Threaded tra Connessioni Diverse**: Mentre ogni connessione è gestita in modo single-threaded, il container può (e lo farà) processare più connessioni diverse **in parallelo**, ciascuna sul proprio thread e con la propria istanza di endpoint.

### Gestione dello Stato Condiviso per il Broadcast

Il modello per-istanza è ottimo per lo stato individuale, ma cosa succede se dobbiamo inviare un messaggio a *tutti* i client connessi (broadcast)?

In questo caso, è necessario mantenere una collezione di sessioni condivisa tra tutte le istanze dell'endpoint. La soluzione più comune è usare una **variabile statica e thread-safe**.

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/broadcast-chat")
public class BroadcastEndpoint {

    // Collezione statica e sincronizzata per memorizzare tutte le sessioni attive.
    // Condivisa tra tutte le istanze di BroadcastEndpoint.
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session); // Aggiunge la nuova sessione alla collezione condivisa
        System.out.println("Nuova sessione aggiunta. Totale: " + sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Invia il messaggio a tutte le sessioni connesse
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session); // Rimuove la sessione alla chiusura
        System.out.println("Sessione rimossa. Totale: " + sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // È buona norma rimuovere la sessione anche in caso di errore
        sessions.remove(session);
        throwable.printStackTrace();
    }

    // Metodo per il broadcast
    private void broadcast(String message) {
        // È necessario iterare in un blocco sincronizzato per evitare ConcurrentModificationException
        // se una sessione viene aggiunta/rimossa mentre si itera.
        synchronized (sessions) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
```

## Tabella Riassuntiva del Modello

| Aspetto                   | Descrizione                                                                                                                            | Implicazioni                                                                                             |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| **Istanza dell'Endpoint** | Una nuova istanza per ogni connessione client.                                                                                         | Lo stato nelle variabili d'istanza è isolato e sicuro per ogni connessione. Modello "stateful-per-session". |
| **Concorrenza Interna**   | Single-threaded per ogni istanza. I metodi `@OnMessage` sono chiamati sequenzialmente.                                                 | Non è necessaria la sincronizzazione per le variabili d'istanza.                                         |
| **Concorrenza Esterna**   | Multi-threaded tra istanze diverse. Più connessioni vengono gestite in parallelo.                                                      | Le strutture dati condivise (es. campi `static`) devono essere thread-safe (es. `synchronizedSet`, `ConcurrentHashMap`). |
| **Stato Condiviso**       | Gestito tramite campi `static` o bean CDI con scope `@ApplicationScoped`.                                                              | Richiede un'attenta gestione della concorrenza (sincronizzazione) durante l'accesso e la modifica.       |

## Glossario dei Termini Importanti

| Termine                 | Definizione                                                                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Stateful-per-Session**| Un modello in cui ogni sessione/connessione ha la propria istanza di un componente (l'endpoint), che può mantenere uno stato specifico per quella sessione. |
| **Thread-Safe**         | Si dice di una porzione di codice o di una struttura dati che può essere acceduta da più thread contemporaneamente senza causare errori o stati inconsistenti. |
| **Broadcast**           | L'atto di inviare un singolo messaggio a tutti i client connessi a un servizio.                                                        |
| **Stato Condiviso**     | Dati (es. una lista di utenti) che sono accessibili e modificabili da più istanze di endpoint (e quindi da più thread) contemporaneamente. |
