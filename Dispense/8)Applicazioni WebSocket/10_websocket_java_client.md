# Client WebSocket Java con @ClientEndpoint

Oltre agli endpoint server, la specifica JSR 356 fornisce un'API completa per creare **client WebSocket** in Java. Questo permette alle applicazioni Java di connettersi a server WebSocket come client, abilitando scenari come:

- Test automatizzati di endpoint WebSocket
- Microservizi che comunicano via WebSocket
- Applicazioni desktop o console che si connettono a servizi in tempo reale
- Bot e client automatizzati
- Integrazione con sistemi di terze parti

## Annotazione @ClientEndpoint

L'annotazione `@ClientEndpoint` designa una classe POJO come un endpoint WebSocket lato client. Funziona in modo simile a `@ServerEndpoint`, ma per le connessioni in uscita.

### Caratteristiche Principali

- Usa le stesse annotazioni di ciclo di vita: `@OnOpen`, `@OnMessage`, `@OnClose`, `@OnError`
- Supporta Encoder e Decoder personalizzati
- Gestisce automaticamente l'handshake e la connessione
- Permette l'uso di configuratori personalizzati

## Client WebSocket Base

### Esempio Semplice

```java
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class SimpleWebSocketClient {
    
    private Session session;
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connesso al server WebSocket");
        System.out.println("Session ID: " + session.getId());
    }
    
    @OnMessage
    public void onMessage(String message) {
        System.out.println("Messaggio ricevuto dal server: " + message);
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Disconnesso dal server");
        System.out.println("Codice chiusura: " + closeReason.getCloseCode());
        System.out.println("Motivo: " + closeReason.getReasonPhrase());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore WebSocket: " + throwable.getMessage());
        throwable.printStackTrace();
    }
    
    /**
     * Invia un messaggio al server
     */
    public void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
            System.out.println("Messaggio inviato: " + message);
        } else {
            throw new IllegalStateException("Sessione non aperta");
        }
    }
    
    /**
     * Chiude la connessione
     */
    public void disconnect() throws IOException {
        if (session != null && session.isOpen()) {
            session.close(new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE,
                "Client disconnecting"
            ));
        }
    }
    
    /**
     * Main per testare il client
     */
    public static void main(String[] args) {
        try {
            // 1. Ottieni il container WebSocket
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            
            // 2. Crea l'istanza del client
            SimpleWebSocketClient client = new SimpleWebSocketClient();
            
            // 3. Connettiti al server
            String uri = "ws://localhost:8080/myapp/chat/JavaClient";
            Session session = container.connectToServer(client, new URI(uri));
            
            // 4. Invia alcuni messaggi
            client.sendMessage("Ciao dal client Java!");
            Thread.sleep(1000);
            client.sendMessage("Come stai?");
            Thread.sleep(1000);
            
            // 5. Mantieni la connessione aperta per ricevere risposte
            Thread.sleep(5000);
            
            // 6. Disconnetti
            client.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## WebSocketContainer

Il `WebSocketContainer` è il punto di ingresso per creare connessioni client. Fornisce metodi per:

- Connettersi a endpoint remoti
- Configurare parametri della connessione
- Gestire encoder e decoder
- Impostare timeout e dimensioni dei buffer

### Ottenere il Container

```java
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

// Ottieni l'istanza singleton del container
WebSocketContainer container = ContainerProvider.getWebSocketContainer();
```

### Metodi Principali di WebSocketContainer

| Metodo | Descrizione |
|--------|-------------|
| `connectToServer(Class<?> annotatedEndpointClass, URI path)` | Connette usando una classe annotata |
| `connectToServer(Object annotatedEndpointInstance, URI path)` | Connette usando un'istanza già creata |
| `connectToServer(Endpoint endpoint, ClientEndpointConfig config, URI path)` | Connette usando l'approccio programmatico |
| `setDefaultMaxSessionIdleTimeout(long)` | Imposta il timeout di inattività |
| `setDefaultMaxTextMessageBufferSize(int)` | Imposta la dimensione del buffer per messaggi di testo |
| `setDefaultMaxBinaryMessageBufferSize(int)` | Imposta la dimensione del buffer per messaggi binari |

## Client con Encoder e Decoder

### Definizione delle Classi

```java
// POJO per i messaggi
public class ChatMessage {
    private String username;
    private String content;
    private long timestamp;
    
    public ChatMessage() {}
    
    public ChatMessage(String username, String content) {
        this.username = username;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getter e Setter...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
```

```java
import com.google.gson.Gson;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

// Encoder: ChatMessage -> JSON String
public class ChatMessageEncoder implements Encoder.Text<ChatMessage> {
    
    private static Gson gson = new Gson();
    
    @Override
    public String encode(ChatMessage message) throws EncodeException {
        return gson.toJson(message);
    }
    
    @Override
    public void init(EndpointConfig config) {}
    
    @Override
    public void destroy() {}
}
```

```java
import com.google.gson.Gson;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

// Decoder: JSON String -> ChatMessage
public class ChatMessageDecoder implements Decoder.Text<ChatMessage> {
    
    private static Gson gson = new Gson();
    
    @Override
    public ChatMessage decode(String s) throws DecodeException {
        return gson.fromJson(s, ChatMessage.class);
    }
    
    @Override
    public boolean willDecode(String s) {
        return s != null && s.contains("username") && s.contains("content");
    }
    
    @Override
    public void init(EndpointConfig config) {}
    
    @Override
    public void destroy() {}
}
```

### Client con Encoder/Decoder

```java
@ClientEndpoint(
    encoders = {ChatMessageEncoder.class},
    decoders = {ChatMessageDecoder.class}
)
public class ChatClient {
    
    private Session session;
    private String username;
    
    public ChatClient(String username) {
        this.username = username;
    }
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println(username + " connesso alla chat");
        
        // Invia messaggio di ingresso
        try {
            ChatMessage joinMsg = new ChatMessage(username, "è entrato nella chat");
            session.getBasicRemote().sendObject(joinMsg); // sendObject usa l'Encoder
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @OnMessage
    public void onMessage(ChatMessage message) {
        // Il Decoder ha già convertito il JSON in ChatMessage
        System.out.println("[" + message.getUsername() + "]: " + message.getContent());
    }
    
    @OnClose
    public void onClose() {
        System.out.println(username + " disconnesso dalla chat");
    }
    
    @OnError
    public void onError(Throwable throwable) {
        System.err.println("Errore per " + username + ": " + throwable.getMessage());
    }
    
    public void sendChatMessage(String content) throws Exception {
        if (session != null && session.isOpen()) {
            ChatMessage msg = new ChatMessage(username, content);
            session.getBasicRemote().sendObject(msg);
        }
    }
    
    public void disconnect() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
    
    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        
        // Crea il client
        ChatClient client = new ChatClient("Alice");
        
        // Connetti
        String uri = "ws://localhost:8080/chat-app/chat";
        Session session = container.connectToServer(client, new URI(uri));
        
        // Invia messaggi
        client.sendChatMessage("Ciao a tutti!");
        Thread.sleep(1000);
        client.sendChatMessage("Come state?");
        Thread.sleep(5000);
        
        // Disconnetti
        client.disconnect();
    }
}
```

## Configurazione Avanzata del Client

### ClientEndpointConfig

Per una configurazione più avanzata, puoi usare `ClientEndpointConfig`:

```java
import javax.websocket.*;
import java.net.URI;
import java.util.*;

public class AdvancedClient {
    
    @ClientEndpoint
    public static class MyClient {
        @OnOpen
        public void onOpen(Session session) {
            System.out.println("Connesso con configurazione personalizzata");
        }
        
        @OnMessage
        public void onMessage(String message) {
            System.out.println("Ricevuto: " + message);
        }
    }
    
    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        
        // Configura il container
        container.setDefaultMaxTextMessageBufferSize(64 * 1024); // 64KB
        container.setDefaultMaxBinaryMessageBufferSize(64 * 1024);
        container.setDefaultMaxSessionIdleTimeout(300000); // 5 minuti
        
        // Crea configurazione personalizzata
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    // Aggiungi header personalizzati
                    headers.put("Authorization", 
                               Arrays.asList("Bearer " + getAuthToken()));
                    headers.put("X-Client-Version", 
                               Arrays.asList("1.0.0"));
                }
            })
            .build();
        
        // Connetti con la configurazione
        MyClient client = new MyClient();
        Session session = container.connectToServer(client, config, 
                                                    new URI("ws://localhost:8080/app"));
        
        Thread.sleep(10000);
        session.close();
    }
    
    private static String getAuthToken() {
        return "your-jwt-token-here";
    }
}
```

## Client Sincrono per Testing

Utile per test unitari e integrazione:

```java
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.*;

@ClientEndpoint
public class SyncWebSocketClient {
    
    private Session session;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connectLatch.countDown();
    }
    
    @OnMessage
    public void onMessage(String message) {
        messageQueue.offer(message);
    }
    
    @OnClose
    public void onClose() {
        closeLatch.countDown();
    }
    
    @OnError
    public void onError(Throwable throwable) {
        System.err.println("Errore: " + throwable.getMessage());
    }
    
    /**
     * Attende che la connessione sia stabilita
     */
    public boolean awaitConnection(long timeout, TimeUnit unit) 
            throws InterruptedException {
        return connectLatch.await(timeout, unit);
    }
    
    /**
     * Invia un messaggio e attende una risposta
     */
    public String sendAndReceive(String message, long timeout, TimeUnit unit) 
            throws IOException, InterruptedException {
        session.getBasicRemote().sendText(message);
        return messageQueue.poll(timeout, unit);
    }
    
    /**
     * Riceve il prossimo messaggio (bloccante)
     */
    public String receiveMessage(long timeout, TimeUnit unit) 
            throws InterruptedException {
        return messageQueue.poll(timeout, unit);
    }
    
    /**
     * Chiude la connessione e attende la chiusura
     */
    public void closeAndAwait(long timeout, TimeUnit unit) 
            throws IOException, InterruptedException {
        session.close();
        closeLatch.await(timeout, unit);
    }
    
    // Esempio di uso in un test
    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        SyncWebSocketClient client = new SyncWebSocketClient();
        
        // Connetti
        Session session = container.connectToServer(client, 
                                                    new URI("ws://localhost:8080/echo"));
        
        // Attendi connessione
        if (!client.awaitConnection(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout connessione");
        }
        
        // Invia e ricevi
        String response = client.sendAndReceive("Hello", 5, TimeUnit.SECONDS);
        System.out.println("Risposta: " + response);
        
        // Chiudi
        client.closeAndAwait(5, TimeUnit.SECONDS);
    }
}
```

## Client Multi-Connessione

Gestione di più connessioni simultanee:

```java
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiConnectionClient {
    
    private final Map<String, Session> connections = new ConcurrentHashMap<>();
    
    @ClientEndpoint
    public class ConnectionHandler {
        private final String connectionId;
        
        public ConnectionHandler(String connectionId) {
            this.connectionId = connectionId;
        }
        
        @OnOpen
        public void onOpen(Session session) {
            connections.put(connectionId, session);
            System.out.println("Connessione " + connectionId + " aperta");
        }
        
        @OnMessage
        public void onMessage(String message) {
            System.out.println("[" + connectionId + "] Ricevuto: " + message);
        }
        
        @OnClose
        public void onClose() {
            connections.remove(connectionId);
            System.out.println("Connessione " + connectionId + " chiusa");
        }
        
        @OnError
        public void onError(Throwable throwable) {
            System.err.println("[" + connectionId + "] Errore: " + 
                             throwable.getMessage());
        }
    }
    
    public void connect(String connectionId, String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ConnectionHandler handler = new ConnectionHandler(connectionId);
        container.connectToServer(handler, new URI(uri));
    }
    
    public void sendTo(String connectionId, String message) throws IOException {
        Session session = connections.get(connectionId);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }
    
    public void broadcast(String message) throws IOException {
        for (Session session : connections.values()) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        }
    }
    
    public void disconnect(String connectionId) throws IOException {
        Session session = connections.get(connectionId);
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
    
    public void disconnectAll() throws IOException {
        for (Session session : connections.values()) {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        MultiConnectionClient client = new MultiConnectionClient();
        
        // Apri multiple connessioni
        client.connect("conn1", "ws://localhost:8080/app/stream1");
        client.connect("conn2", "ws://localhost:8080/app/stream2");
        client.connect("conn3", "ws://localhost:8080/app/stream3");
        
        Thread.sleep(1000);
        
        // Invia a connessioni specifiche
        client.sendTo("conn1", "Messaggio per stream 1");
        client.sendTo("conn2", "Messaggio per stream 2");
        
        Thread.sleep(1000);
        
        // Broadcast
        client.broadcast("Messaggio a tutti gli stream");
        
        Thread.sleep(5000);
        
        // Disconnetti tutto
        client.disconnectAll();
    }
}
```

## Best Practices per Client WebSocket

| Pratica | Descrizione | Raccomandazione |
|---------|-------------|----------------|
| **Gestione Riconnessione** | Implementare logica di retry in caso di disconnessione | Backoff esponenziale |
| **Timeout** | Configurare timeout appropriati | 30-60 secondi per connessione |
| **Thread Safety** | Sincronizzare accesso alla sessione | Usare metodi thread-safe |
| **Pulizia Risorse** | Chiudere sempre le connessioni | Try-with-resources quando possibile |
| **Gestione Errori** | Implementare `@OnError` completo | Logging e recovery |
| **Buffer Size** | Configurare dimensioni buffer adeguate | In base ai messaggi attesi |
| **Heartbeat** | Implementare keep-alive | Ping ogni 30-60 secondi |
| **Header Personalizzati** | Usare Configurator per header | Per autenticazione e metadata |

## Pattern: Client con Riconnessione Automatica

```java
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.*;

public class ResilientWebSocketClient {
    
    private final String uri;
    private final long reconnectDelay;
    private final int maxReconnectAttempts;
    
    private Session session;
    private int reconnectAttempts = 0;
    private final ScheduledExecutorService scheduler = 
        Executors.newSingleThreadScheduledExecutor();
    
    @ClientEndpoint
    public class ClientHandler {
        @OnOpen
        public void onOpen(Session s) {
            session = s;
            reconnectAttempts = 0;
            System.out.println("Connesso al server");
        }
        
        @OnMessage
        public void onMessage(String message) {
            System.out.println("Ricevuto: " + message);
        }
        
        @OnClose
        public void onClose(CloseReason reason) {
            System.out.println("Disconnesso: " + reason.getReasonPhrase());
            scheduleReconnect();
        }
        
        @OnError
        public void onError(Throwable throwable) {
            System.err.println("Errore: " + throwable.getMessage());
        }
    }
    
    public ResilientWebSocketClient(String uri, long reconnectDelay, 
                                   int maxReconnectAttempts) {
        this.uri = uri;
        this.reconnectDelay = reconnectDelay;
        this.maxReconnectAttempts = maxReconnectAttempts;
    }
    
    public void connect() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientHandler handler = new ClientHandler();
        container.connectToServer(handler, new URI(uri));
    }
    
    private void scheduleReconnect() {
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            long delay = reconnectDelay * reconnectAttempts; // Backoff lineare
            
            System.out.println("Tentativo di riconnessione " + reconnectAttempts + 
                             " tra " + delay + "ms");
            
            scheduler.schedule(() -> {
                try {
                    connect();
                } catch (Exception e) {
                    System.err.println("Riconnessione fallita: " + e.getMessage());
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            System.err.println("Raggiunto numero massimo di tentativi di riconnessione");
        }
    }
    
    public void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        } else {
            throw new IllegalStateException("Non connesso");
        }
    }
    
    public void shutdown() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdown();
        }
    }
    
    public static void main(String[] args) throws Exception {
        ResilientWebSocketClient client = new ResilientWebSocketClient(
            "ws://localhost:8080/app",
            2000,  // 2 secondi di delay iniziale
            5      // Massimo 5 tentativi
        );
        
        client.connect();
        
        // Simula utilizzo
        Thread.sleep(60000); // 1 minuto
        
        client.shutdown();
    }
}
```

## Glossario dei Termini

| Termine | Definizione |
|---------|-------------|
| **@ClientEndpoint** | Annotazione che designa una classe come client WebSocket |
| **WebSocketContainer** | Factory per creare connessioni client WebSocket |
| **ContainerProvider** | Classe che fornisce l'istanza del WebSocketContainer |
| **ClientEndpointConfig** | Configurazione per connessioni client (header, timeout, etc.) |
| **connectToServer()** | Metodo per stabilire una connessione WebSocket a un server remoto |
| **Configurator** | Classe per personalizzare l'handshake del client (es. aggiungere header) |
| **Backoff** | Strategia di attesa crescente tra tentativi di riconnessione |
| **Resilient Client** | Client che gestisce automaticamente disconnessioni e riconnessioni |
