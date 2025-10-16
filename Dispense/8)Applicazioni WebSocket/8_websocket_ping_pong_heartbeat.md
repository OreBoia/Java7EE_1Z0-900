# Ping/Pong e Heartbeat in WebSocket

Il protocollo WebSocket include meccanismi integrati per verificare che una connessione sia ancora attiva e funzionante. Questi meccanismi, noti come **Ping/Pong** e **Heartbeat**, sono essenziali per rilevare connessioni "morte" e mantenere le connessioni attive anche in presenza di proxy o firewall che potrebbero chiudere connessioni inattive.

## Il Meccanismo Ping/Pong

Il protocollo WebSocket definisce due tipi di **frame di controllo** per verificare lo stato della connessione:

- **Ping**: Un frame di controllo inviato da una parte (client o server) per verificare che l'altra parte sia ancora attiva.
- **Pong**: La risposta automatica al frame Ping. Quando un endpoint riceve un Ping, deve rispondere con un Pong contenente gli stessi dati del Ping.

### Caratteristiche del Ping/Pong

| Caratteristica | Descrizione |
|----------------|-------------|
| **Direzione** | Può essere inviato sia dal client che dal server |
| **Automatico** | Il Pong è automaticamente generato dal container WebSocket in risposta a un Ping |
| **Payload** | Può contenere dati opzionali (massimo 125 byte) |
| **Non bloccante** | Non interferisce con il flusso normale dei messaggi |

## Implementazione del Ping in Java

In Java, il metodo `sendPing()` è disponibile tramite l'oggetto `RemoteEndpoint` della sessione.

### Esempio Base: Invio di un Ping

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;

@ServerEndpoint("/ping-example")
public class PingExample {
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Invia un ping con un payload opzionale
        ByteBuffer payload = ByteBuffer.wrap("ping-test".getBytes());
        session.getBasicRemote().sendPing(payload);
        
        System.out.println("Ping inviato alla sessione: " + session.getId());
    }
    
    @OnMessage
    public void onPong(PongMessage pong, Session session) {
        // Questo metodo viene chiamato quando si riceve un Pong
        ByteBuffer data = pong.getApplicationData();
        String payload = new String(data.array());
        
        System.out.println("Pong ricevuto con payload: " + payload);
    }
}
```

### Ricevere e Gestire i Pong

Per gestire i messaggi Pong in arrivo, si definisce un metodo `@OnMessage` che accetta come parametro un oggetto di tipo `PongMessage`.

```java
@OnMessage
public void handlePong(PongMessage pongMessage, Session session) {
    ByteBuffer applicationData = pongMessage.getApplicationData();
    
    if (applicationData != null && applicationData.hasRemaining()) {
        byte[] data = new byte[applicationData.remaining()];
        applicationData.get(data);
        String payload = new String(data);
        System.out.println("Pong ricevuto da " + session.getId() + ": " + payload);
    }
    
    // Aggiorna il timestamp dell'ultimo pong ricevuto
    session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
}
```

## Pattern Heartbeat: Mantenere le Connessioni Attive

Il pattern **Heartbeat** consiste nell'inviare periodicamente messaggi Ping per:

1. **Verificare che la connessione sia ancora valida**
2. **Mantenere la connessione attiva** (alcuni proxy/firewall chiudono connessioni idle)
3. **Rilevare connessioni "morte"** (quando non si riceve risposta)

### Implementazione Completa del Heartbeat

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

@ServerEndpoint("/heartbeat")
public class HeartbeatEndpoint {
    
    // Executor per schedulare i ping periodici
    private static final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    
    // Intervallo tra i ping (30 secondi)
    private static final long PING_INTERVAL = 30;
    
    // Timeout massimo per considerare una connessione morta (90 secondi)
    private static final long PING_TIMEOUT = 90;
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Nuova connessione: " + session.getId());
        
        // Inizializza il timestamp dell'ultimo pong
        session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
        
        // Schedula l'invio periodico di ping
        ScheduledFuture<?> pingTask = scheduler.scheduleAtFixedRate(() -> {
            sendHeartbeat(session);
        }, PING_INTERVAL, PING_INTERVAL, TimeUnit.SECONDS);
        
        // Salva il task per poterlo cancellare alla chiusura
        session.getUserProperties().put("pingTask", pingTask);
        
        // Schedula il controllo del timeout
        ScheduledFuture<?> timeoutCheckTask = scheduler.scheduleAtFixedRate(() -> {
            checkTimeout(session);
        }, PING_TIMEOUT, PING_TIMEOUT, TimeUnit.SECONDS);
        
        session.getUserProperties().put("timeoutCheckTask", timeoutCheckTask);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        // Gestisce i messaggi normali
        session.getBasicRemote().sendText("Echo: " + message);
    }
    
    @OnMessage
    public void onPong(PongMessage pong, Session session) {
        // Aggiorna il timestamp dell'ultimo pong ricevuto
        session.getUserProperties().put("lastPongTime", System.currentTimeMillis());
        System.out.println("Heartbeat ricevuto da: " + session.getId());
    }
    
    @OnClose
    public void onClose(Session session) {
        // Cancella i task schedulati
        cancelScheduledTasks(session);
        System.out.println("Connessione chiusa: " + session.getId());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore sulla sessione " + session.getId() + ": " + throwable.getMessage());
        cancelScheduledTasks(session);
    }
    
    // Metodi helper
    
    private void sendHeartbeat(Session session) {
        if (session.isOpen()) {
            try {
                ByteBuffer payload = ByteBuffer.wrap(
                    String.valueOf(System.currentTimeMillis()).getBytes()
                );
                session.getBasicRemote().sendPing(payload);
                System.out.println("Ping inviato a: " + session.getId());
            } catch (IOException e) {
                System.err.println("Errore invio ping a " + session.getId() + ": " + e.getMessage());
                closeSession(session, "Errore invio ping");
            }
        }
    }
    
    private void checkTimeout(Session session) {
        if (!session.isOpen()) {
            return;
        }
        
        Long lastPongTime = (Long) session.getUserProperties().get("lastPongTime");
        long currentTime = System.currentTimeMillis();
        long timeSinceLastPong = currentTime - lastPongTime;
        
        if (timeSinceLastPong > PING_TIMEOUT * 1000) {
            System.err.println("Timeout per la sessione " + session.getId() + 
                             ". Ultimo pong: " + timeSinceLastPong + "ms fa");
            closeSession(session, "Ping timeout");
        }
    }
    
    private void closeSession(Session session, String reason) {
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(
                    CloseReason.CloseCodes.GOING_AWAY, 
                    reason
                ));
            }
        } catch (IOException e) {
            System.err.println("Errore chiusura sessione: " + e.getMessage());
        } finally {
            cancelScheduledTasks(session);
        }
    }
    
    private void cancelScheduledTasks(Session session) {
        ScheduledFuture<?> pingTask = 
            (ScheduledFuture<?>) session.getUserProperties().get("pingTask");
        if (pingTask != null) {
            pingTask.cancel(true);
        }
        
        ScheduledFuture<?> timeoutCheckTask = 
            (ScheduledFuture<?>) session.getUserProperties().get("timeoutCheckTask");
        if (timeoutCheckTask != null) {
            timeoutCheckTask.cancel(true);
        }
    }
}
```

## Considerazioni sul Lato Client

### Client JavaScript

I browser gestiscono automaticamente i frame Pong in risposta ai Ping del server. **Non è necessario implementare nulla lato client** per il meccanismo Ping/Pong standard.

Tuttavia, se si vuole implementare un heartbeat applicativo (messaggi normali invece di frame di controllo):

```javascript
const ws = new WebSocket("wss://example.com/heartbeat");
let heartbeatInterval;

ws.onopen = function() {
    console.log("Connesso");
    
    // Invia un heartbeat ogni 30 secondi
    heartbeatInterval = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send("HEARTBEAT");
        }
    }, 30000);
};

ws.onmessage = function(event) {
    if (event.data === "HEARTBEAT_ACK") {
        console.log("Heartbeat confermato dal server");
    } else {
        console.log("Messaggio: " + event.data);
    }
};

ws.onclose = function() {
    clearInterval(heartbeatInterval);
    console.log("Disconnesso");
};
```

### Client Java

Con `@ClientEndpoint`, il meccanismo è simile al server:

```java
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

@ClientEndpoint
public class HeartbeatClient {
    
    private Session session;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> pingTask;
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connesso al server");
        
        // Schedula l'invio periodico di ping
        pingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendPing(
                        ByteBuffer.wrap("client-ping".getBytes())
                    );
                    System.out.println("Ping inviato al server");
                }
            } catch (IOException e) {
                System.err.println("Errore invio ping: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    @OnMessage
    public void onPong(PongMessage pong) {
        System.out.println("Pong ricevuto dal server");
    }
    
    @OnClose
    public void onClose() {
        if (pingTask != null) {
            pingTask.cancel(true);
        }
        scheduler.shutdown();
        System.out.println("Disconnesso dal server");
    }
    
    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        HeartbeatClient client = new HeartbeatClient();
        Session session = container.connectToServer(client, 
            new URI("ws://localhost:8080/app/heartbeat"));
        
        // Mantieni la connessione aperta
        Thread.sleep(120000); // 2 minuti
        
        session.close();
    }
}
```

## Best Practices per il Heartbeat

| Pratica | Descrizione | Raccomandazione |
|---------|-------------|----------------|
| **Intervallo Ping** | Quanto spesso inviare i ping | 30-60 secondi in produzione |
| **Timeout** | Dopo quanto considerare morta una connessione | 2-3 volte l'intervallo dei ping |
| **Pulizia Risorse** | Cancellare i task schedulati | Sempre in `@OnClose` e `@OnError` |
| **Gestione Errori** | Gestire IOException durante l'invio | Chiudere la sessione se l'invio fallisce |
| **Thread Pool** | Dimensionare correttamente l'executor | Basato sul numero di connessioni attese |
| **Logging** | Registrare eventi heartbeat | Solo in debug, non in produzione |

## Vantaggi del Heartbeat

1. **Rilevamento Connessioni Morte**: Identifica rapidamente client disconnessi senza notifica
2. **Keep-Alive**: Previene la chiusura di connessioni idle da parte di proxy/firewall
3. **Monitoraggio**: Permette di tracciare lo stato di salute delle connessioni
4. **Pulizia Risorse**: Facilita la rimozione di sessioni "zombie" dalle collezioni

## Glossario dei Termini

| Termine | Definizione |
|---------|-------------|
| **Ping** | Frame di controllo WebSocket per verificare se la connessione è attiva |
| **Pong** | Frame di risposta automatica a un Ping |
| **Heartbeat** | Pattern che usa Ping/Pong per mantenere e monitorare connessioni |
| **Idle Connection** | Connessione senza attività che potrebbe essere chiusa da proxy/firewall |
| **Dead Connection** | Connessione tecnicamente aperta ma dove l'altra parte non risponde |
| **PongMessage** | Oggetto Java che rappresenta un messaggio Pong ricevuto |
| **Frame di Controllo** | Frame WebSocket speciali per gestire la connessione (Ping, Pong, Close) |
| **Keep-Alive** | Meccanismo per mantenere una connessione aperta e attiva |
