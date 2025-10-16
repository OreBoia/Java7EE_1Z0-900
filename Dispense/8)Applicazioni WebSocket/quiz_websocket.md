# Quiz Avanzato su Applicazioni WebSocket - Domande Miste con Codice

Questo quiz avanzato copre i concetti delle Applicazioni WebSocket (JSR 356) in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Introduzione e Protocollo WebSocket

### 💻 Domanda 1

Osserva questa sequenza di handshake WebSocket:

```http
GET /chat-app/chat HTTP/1.1
Host: server.example.com
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

```http
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
```

Cosa significa il codice di stato `101` nella risposta del server?

- a) Errore di protocollo
- b) Conferma il passaggio dal protocollo HTTP al protocollo WebSocket
- c) Richiesta accettata ma in attesa
- d) Redirect permanente al protocollo WebSocket

---

### 🔵 Domanda 2

Quale delle seguenti caratteristiche **NON** è propria del protocollo WebSocket?

- a) Comunicazione full-duplex
- b) Connessione persistente a lunga durata
- c) Stateless come HTTP
- d) Bassa latenza dopo l'handshake

---

### 🟢 Domanda 3

Quali degli seguenti sono **vantaggi** di WebSocket rispetto a HTTP tradizionale? (Seleziona tutti)

- a) Il server può iniziare la comunicazione senza una richiesta del client
- b) Minore overhead dopo l'handshake iniziale
- c) Supporto automatico per la cifratura
- d) Comunicazione bidirezionale simultanea
- e) Connessione persistente senza polling

---

## 2. Java API for WebSocket (JSR 356)

### 💻 Domanda 4

Analizza questo endpoint WebSocket:

```java
@ServerEndpoint("/chat/{username}")
public class ChatEndpoint {
    
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        session.getUserProperties().put("username", username);
        sessions.add(session);
        System.out.println("Connessione aperta: " + session.getId());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        broadcast("[" + username + "]: " + message);
    }
    
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Connessione chiusa: " + session.getId());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore: " + throwable.getMessage());
    }
    
    private void broadcast(String message) throws IOException {
        for (Session s : sessions) {
            s.getBasicRemote().sendText(message);
        }
    }
}
```

Perché la variabile `sessions` è dichiarata come `static`?

- a) Per migliorare le performance
- b) Per essere condivisa tra tutte le istanze dell'endpoint (tutte le connessioni)
- c) È obbligatorio per le collezioni in WebSocket
- d) Per evitare problemi di concorrenza

---

### 🔵 Domanda 5

Nel codice precedente, quale URI sarà accessibile per un client che vuole connettersi come "Mario"?

- a) `ws://host:port/context-path/chat`
- b) `ws://host:port/context-path/chat/Mario`
- c) `ws://host:port/chat/{username}`
- d) `ws://host:port/context-path/Mario`

---

### 🟢 Domanda 6

Quali delle seguenti sono **annotazioni valide** del ciclo di vita WebSocket? (Seleziona tutte)

- a) `@OnOpen`
- b) `@OnMessage`
- c) `@OnClose`
- d) `@OnError`
- e) `@OnConnect`
- f) `@OnDisconnect`

---

## 3. Gestione della Sessione WebSocket

### 💻 Domanda 7

Osserva questo codice per l'invio di messaggi:

```java
@ServerEndpoint("/notifications")
public class NotificationEndpoint {
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        // Approccio A: Invio sincrono
        session.getBasicRemote().sendText("Echo sincrono: " + message);
        
        // Approccio B: Invio asincrono
        session.getAsyncRemote().sendText("Echo asincrono: " + message);
    }
}
```

Quale è la **principale differenza** tra `getBasicRemote()` e `getAsyncRemote()`?

- a) `getBasicRemote()` è più veloce
- b) `getBasicRemote()` blocca il thread corrente fino al completamento, `getAsyncRemote()` no
- c) `getAsyncRemote()` garantisce la consegna del messaggio
- d) Non c'è differenza, sono sinonimi

---

### 💻 Domanda 8

Analizza questo uso delle proprietà della sessione:

```java
@ServerEndpoint("/secure-chat/{room}")
public class SecureChatEndpoint {
    
    @OnOpen
    public void onOpen(Session session, @PathParam("room") String room) {
        session.getUserProperties().put("room", room);
        session.getUserProperties().put("messageCount", 0);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String room = (String) session.getUserProperties().get("room");
        Integer count = (Integer) session.getUserProperties().get("messageCount");
        count++;
        session.getUserProperties().put("messageCount", count);
        
        session.getBasicRemote().sendText("Messaggio #" + count + " nella stanza " + room);
    }
}
```

A cosa serve `session.getUserProperties()`?

- a) Per accedere alle proprietà di sicurezza dell'utente
- b) Per memorizzare dati personalizzati associati alla sessione WebSocket
- c) Per recuperare informazioni dal Principal dell'utente
- d) Per gestire i permessi dell'utente

---

### 🔵 Domanda 9

Quale metodo permette di recuperare l'utente autenticato in una sessione WebSocket?

- a) `session.getUser()`
- b) `session.getUserPrincipal()`
- c) `session.getAuthentication()`
- d) `session.getPrincipal()`

---

## 4. Gestione dei Messaggi

### 💻 Domanda 10

Osserva questi diversi tipi di gestione messaggi:

```java
@ServerEndpoint("/multi-handler")
public class MultiHandlerEndpoint {
    
    // Handler 1: Messaggi di testo
    @OnMessage
    public void handleText(String message, Session session) {
        System.out.println("Testo: " + message);
    }
    
    // Handler 2: Messaggi binari (ByteBuffer)
    @OnMessage
    public void handleBinary(ByteBuffer message, Session session) {
        System.out.println("Binario: " + message.remaining() + " bytes");
    }
    
    // Handler 3: Messaggi di testo in streaming
    @OnMessage
    public void handleTextStream(Reader reader, Session session) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println("Stream: " + line);
        }
    }
}
```

Cosa succede se un client invia un messaggio di testo?

- a) Viene chiamato solo `handleText()`
- b) Vengono chiamati sia `handleText()` che `handleTextStream()`
- c) Si verifica un errore di compilazione
- d) Il container sceglie casualmente quale handler usare

---

### 💻 Domanda 11

Analizza questo codice per messaggi frammentati:

```java
@ServerEndpoint("/large-messages")
public class LargeMessageEndpoint {
    
    private StringBuilder textBuffer = new StringBuilder();
    
    @OnMessage
    public void handlePartialMessage(String partialMessage, boolean last) {
        textBuffer.append(partialMessage);
        
        if (last) {
            System.out.println("Messaggio completo: " + textBuffer.toString());
            textBuffer.setLength(0);
        } else {
            System.out.println("Frammento ricevuto...");
        }
    }
}
```

Quando il parametro `last` è `true`?

- a) All'inizio del messaggio
- b) Solo per l'ultimo frammento del messaggio
- c) Per ogni frammento
- d) Mai, è sempre false

---

### 💻 Domanda 12

Osserva questo uso di Encoder/Decoder:

```java
public class Message {
    private String sender;
    private String content;
    // costruttore, getter, setter...
}

public class MessageEncoder implements Encoder.Text<Message> {
    @Override
    public String encode(Message message) throws EncodeException {
        return message.getSender() + ":" + message.getContent();
    }
}

public class MessageDecoder implements Decoder.Text<Message> {
    @Override
    public Message decode(String s) throws DecodeException {
        String[] parts = s.split(":", 2);
        return new Message(parts[0], parts[1]);
    }
    
    @Override
    public boolean willDecode(String s) {
        return s != null && s.contains(":");
    }
}

@ServerEndpoint(value = "/encoded-chat", 
                encoders = {MessageEncoder.class}, 
                decoders = {MessageDecoder.class})
public class EncodedChatEndpoint {
    
    @OnMessage
    public Message onMessage(Message message, Session session) {
        System.out.println("Da " + message.getSender() + ": " + message.getContent());
        return new Message("Server", "Ricevuto: " + message.getContent());
    }
}
```

Cosa fa il metodo `willDecode()` nel Decoder?

- a) Decodifica il messaggio
- b) Verifica se il decoder può gestire il messaggio prima di tentare la decodifica
- c) Cripta il messaggio
- d) Valida il formato del messaggio dopo la decodifica

---

## 5. Ciclo di Vita e Concorrenza

### 💻 Domanda 13

Analizza questo endpoint con stato:

```java
@ServerEndpoint("/stateful-endpoint")
public class StatefulEndpoint {
    
    private String username;
    private int messageCount = 0;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.username = username;
        System.out.println("Nuovo endpoint per: " + this.username);
    }
    
    @OnMessage
    public void onMessage(String message) {
        messageCount++;
        System.out.println(username + " ha inviato " + messageCount + " messaggi");
    }
}
```

Cosa succede alla variabile `messageCount` quando due client diversi si connettono?

- a) Viene condivisa tra tutti i client
- b) Ogni client ha il suo counter isolato
- c) Si verifica un errore di concorrenza
- d) Il counter viene azzerato ad ogni messaggio

---

### 🔵 Domanda 14

Come vengono processati i messaggi per una **singola connessione** WebSocket?

- a) In parallelo su thread diversi
- b) In modo sequenziale (single-threaded)
- c) In modo casuale
- d) Dipende dalla configurazione del server

---

### 💻 Domanda 15

Osserva questa gestione di broadcast thread-safe:

```java
@ServerEndpoint("/broadcast")
public class BroadcastEndpoint {
    
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }
    
    @OnMessage
    public void onMessage(String message) throws IOException {
        synchronized (sessions) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(message);
                }
            }
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }
}
```

Perché si usa `synchronized (sessions)` nel metodo broadcast?

- a) Per migliorare le performance
- b) Per evitare `ConcurrentModificationException` durante l'iterazione
- c) È obbligatorio per tutte le operazioni su Set
- d) Per garantire l'ordine dei messaggi

---

## 6. Gestione degli Errori

### 💻 Domanda 16

Analizza questa gestione degli errori:

```java
@ServerEndpoint("/error-handling")
public class ErrorHandlingEndpoint {
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (message.equals("crash")) {
            throw new RuntimeException("Errore simulato!");
        }
        session.getBasicRemote().sendText("OK: " + message);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore su sessione " + session.getId());
        System.err.println("Causa: " + throwable.getMessage());
        
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                    "Errore interno"
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Quando viene invocato il metodo `@OnError`?

- a) Solo quando il client si disconnette
- b) Quando si verifica un'eccezione durante il processamento dei messaggi
- c) Prima della chiusura di ogni connessione
- d) Solo per errori di rete

---

### 🟢 Domanda 17

Quali delle seguenti sono **operazioni comuni** da eseguire in un metodo `@OnError`? (Seleziona tutte)

- a) Logging dell'errore
- b) Rimozione della sessione da collezioni condivise
- c) Riavvio automatico della connessione
- d) Chiusura della sessione se l'errore è fatale
- e) Notifica al client dell'errore

---

## 7. Integrazione con Java EE

### 💻 Domanda 18

Osserva questa integrazione con EJB:

```java
@Stateless
public class ChatService {
    
    @PersistenceContext
    private EntityManager em;
    
    public void saveMessage(String username, String message) {
        ChatMessage msg = new ChatMessage(username, message, new Date());
        em.persist(msg);
    }
    
    public List<ChatMessage> getRecentMessages(int limit) {
        return em.createQuery("SELECT m FROM ChatMessage m ORDER BY m.timestamp DESC", ChatMessage.class)
                 .setMaxResults(limit)
                 .getResultList();
    }
}

@ServerEndpoint("/persistent-chat")
public class PersistentChatEndpoint {
    
    @EJB
    private ChatService chatService;
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        List<ChatMessage> recent = chatService.getRecentMessages(10);
        for (ChatMessage msg : recent) {
            session.getBasicRemote().sendText(msg.getUsername() + ": " + msg.getContent());
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        String username = (String) session.getUserProperties().get("username");
        chatService.saveMessage(username, message);
    }
}
```

Quale annotazione permette di iniettare l'EJB nell'endpoint WebSocket?

- a) `@Inject`
- b) `@EJB`
- c) `@Resource`
- d) `@Autowired`

---

### 💻 Domanda 19

Analizza questa integrazione con CDI:

```java
@ApplicationScoped
public class NotificationManager {
    
    private Set<Session> subscribers = Collections.synchronizedSet(new HashSet<>());
    
    public void subscribe(Session session) {
        subscribers.add(session);
    }
    
    public void unsubscribe(Session session) {
        subscribers.remove(session);
    }
    
    public void notifyAll(String notification) {
        synchronized (subscribers) {
            for (Session s : subscribers) {
                if (s.isOpen()) {
                    s.getAsyncRemote().sendText(notification);
                }
            }
        }
    }
}

@ServerEndpoint("/notifications")
public class NotificationEndpoint {
    
    @Inject
    private NotificationManager manager;
    
    @OnOpen
    public void onOpen(Session session) {
        manager.subscribe(session);
    }
    
    @OnClose
    public void onClose(Session session) {
        manager.unsubscribe(session);
    }
}
```

Perché `NotificationManager` è annotato con `@ApplicationScoped`?

- a) Per migliorare le performance
- b) Per avere una singola istanza condivisa in tutta l'applicazione
- c) È obbligatorio per l'iniezione
- d) Per gestire le transazioni

---

## 8. Sicurezza WebSocket

### 💻 Domanda 20

Osserva questa implementazione della sicurezza:

```java
@ServerEndpoint("/secure-endpoint")
public class SecureEndpoint {
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        Principal principal = session.getUserPrincipal();
        
        if (principal == null || principal.getName() == null) {
            System.err.println("Connessione non autorizzata rifiutata");
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY, 
                "Autenticazione richiesta"
            ));
            return;
        }
        
        String username = principal.getName();
        session.getUserProperties().put("username", username);
        System.out.println("Utente autenticato: " + username);
    }
}
```

Quando viene eseguita l'autenticazione dell'utente?

- a) Nel metodo `@OnOpen`
- b) Durante l'handshake HTTP iniziale
- c) Ad ogni messaggio inviato
- d) Alla chiusura della connessione

---

### 🔵 Domanda 21

Quale schema URI deve essere usato per connessioni WebSocket sicure?

- a) `ws://`
- b) `wss://`
- c) `https://`
- d) `websocket://`

---

### 💻 Domanda 22

Analizza questa autenticazione basata su token:

```java
@ServerEndpoint("/token-auth")
public class TokenAuthEndpoint {
    
    @Inject
    private TokenValidator tokenValidator;
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        Map<String, List<String>> params = session.getRequestParameterMap();
        
        if (!params.containsKey("token")) {
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY, 
                "Token mancante"
            ));
            return;
        }
        
        String token = params.get("token").get(0);
        
        try {
            String username = tokenValidator.validateAndExtractUsername(token);
            session.getUserProperties().put("username", username);
            System.out.println("Token valido per: " + username);
        } catch (InvalidTokenException e) {
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY, 
                "Token non valido"
            ));
        }
    }
}
```

Come viene passato il token dal client JavaScript?

```javascript
// Quale delle seguenti è corretta?
// a)
const ws = new WebSocket("wss://example.com/token-auth");
ws.send("token=xyz123");

// b)
const ws = new WebSocket("wss://example.com/token-auth?token=xyz123");

// c)
const ws = new WebSocket("wss://example.com/token-auth", "xyz123");

// d)
const ws = new WebSocket("wss://example.com/token-auth");
ws.setHeader("Authorization", "Bearer xyz123");
```

Risposta corretta:

- a) Opzione a
- b) Opzione b
- c) Opzione c
- d) Opzione d

---

## 9. Client WebSocket

### 💻 Domanda 23

Osserva questo client JavaScript:

```javascript
const socket = new WebSocket("wss://example.com/chat/Mario");

socket.onopen = function(event) {
    console.log("Connessione aperta");
    socket.send("Ciao a tutti!");
};

socket.onmessage = function(event) {
    console.log("Messaggio ricevuto: " + event.data);
    document.getElementById("messages").innerHTML += "<p>" + event.data + "</p>";
};

socket.onerror = function(error) {
    console.error("Errore WebSocket: ", error);
};

socket.onclose = function(event) {
    if (event.wasClean) {
        console.log("Connessione chiusa pulitamente");
    } else {
        console.error("Connessione interrotta");
    }
    console.log("Codice: " + event.code + ", Motivo: " + event.reason);
};

// Invio di un messaggio quando l'utente preme un pulsante
function sendMessage() {
    const message = document.getElementById("messageInput").value;
    socket.send(message);
}

// Chiusura esplicita della connessione
function disconnect() {
    socket.close(1000, "Disconnessione volontaria");
}
```

Quale evento viene chiamato quando il server invia un messaggio al client?

- a) `onopen`
- b) `onmessage`
- c) `onsend`
- d) `onreceive`

---

### 🔵 Domanda 24

Nel codice precedente, cosa rappresenta il parametro `1000` in `socket.close(1000, ...)`?

- a) Il timeout in millisecondi
- b) Il codice di chiusura WebSocket (normal closure)
- c) Il numero di sessione
- d) La priorità della chiusura

---

### 💻 Domanda 25

Osserva questo client Java:

```java
@ClientEndpoint
public class ChatClient {
    
    private Session session;
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connesso al server");
    }
    
    @OnMessage
    public void onMessage(String message) {
        System.out.println("Messaggio dal server: " + message);
    }
    
    public void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }
    
    public static void main(String[] args) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ChatClient client = new ChatClient();
        
        URI uri = new URI("ws://localhost:8080/myapp/chat/JavaClient");
        Session session = container.connectToServer(client, uri);
        
        // Invia alcuni messaggi
        client.sendMessage("Ciao dal client Java!");
        Thread.sleep(1000);
        client.sendMessage("Come stai?");
        
        // Mantieni la connessione aperta
        Thread.sleep(5000);
        
        session.close();
    }
}
```

Quale annotazione si usa per definire un endpoint client?

- a) `@ServerEndpoint`
- b) `@ClientEndpoint`
- c) `@WebSocketClient`
- d) `@EndpointClient`

---

## 10. Casi d'Uso e Best Practices

### 💻 Domanda 26

Analizza questi due approcci per una dashboard in tempo reale:

```java
// Approccio A: Polling HTTP
@Path("/dashboard")
public class DashboardResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardData getData() {
        // Client chiama questo endpoint ogni 2 secondi
        return new DashboardData(/* dati aggiornati */);
    }
}

// Client JavaScript con polling
setInterval(() => {
    fetch('/api/dashboard')
        .then(response => response.json())
        .then(data => updateDashboard(data));
}, 2000);
```

```java
// Approccio B: WebSocket push
@ServerEndpoint("/dashboard-stream")
public class DashboardStreamEndpoint {
    
    private static Set<Session> dashboards = Collections.synchronizedSet(new HashSet<>());
    
    @OnOpen
    public void onOpen(Session session) {
        dashboards.add(session);
        // Invia immediatamente i dati correnti
        sendUpdate(session);
    }
    
    // Metodo chiamato quando i dati cambiano (es. da un EJB Timer)
    public static void notifyUpdate(DashboardData data) {
        String json = convertToJson(data);
        for (Session s : dashboards) {
            if (s.isOpen()) {
                s.getAsyncRemote().sendText(json);
            }
        }
    }
}

// Client JavaScript
const ws = new WebSocket("wss://example.com/dashboard-stream");
ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    updateDashboard(data);
};
```

Quale approccio è **preferibile** per aggiornamenti in tempo reale e perché?

- a) Approccio A: più semplice da implementare
- b) Approccio B: minore latenza, minore carico sul server, comunicazione push
- c) Sono equivalenti in termini di performance
- d) Approccio A: più affidabile

---

### 🟢 Domanda 27

Quali delle seguenti sono **best practices** per applicazioni WebSocket? (Seleziona tutte)

- a) Usare sempre `wss://` in produzione
- b) Implementare gestione degli errori con `@OnError`
- c) Validare l'autenticazione durante l'handshake
- d) Usare `getAsyncRemote()` per broadcast a molti client
- e) Memorizzare la logica di business nell'endpoint

---

### 💻 Domanda 28

Osserva questa implementazione di heartbeat:

```java
@ServerEndpoint("/heartbeat")
public class HeartbeatEndpoint {
    
    private static final long TIMEOUT = 60000; // 60 secondi
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @OnOpen
    public void onOpen(Session session) {
        // Programma un ping periodico
        ScheduledFuture<?> pingTask = scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
                } catch (IOException e) {
                    System.err.println("Errore invio ping: " + e.getMessage());
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        session.getUserProperties().put("pingTask", pingTask);
    }
    
    @OnMessage
    public void onPong(PongMessage pong, Session session) {
        System.out.println("Pong ricevuto dalla sessione: " + session.getId());
    }
    
    @OnClose
    public void onClose(Session session) {
        ScheduledFuture<?> task = (ScheduledFuture<?>) session.getUserProperties().get("pingTask");
        if (task != null) {
            task.cancel(true);
        }
    }
}
```

A cosa serve il meccanismo di Ping/Pong in WebSocket?

- a) Per misurare la latenza della connessione
- b) Per mantenere la connessione attiva e rilevare connessioni morte
- c) Per sincronizzare i messaggi
- d) Per autenticare periodicamente il client

---

### 🔵 Domanda 29

Quando **NON** è appropriato usare WebSocket?

- a) Per una chat in tempo reale
- b) Per recuperare una lista di prodotti da visualizzare una volta
- c) Per notifiche push
- d) Per un gioco multiplayer online

---

### 💻 Domanda 30

Analizza questo pattern di disconnessione gestita:

```java
@ServerEndpoint("/managed-connection")
public class ManagedConnectionEndpoint {
    
    private static Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        Principal principal = session.getUserPrincipal();
        if (principal == null) {
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY, 
                "Autenticazione richiesta"
            ));
            return;
        }
        
        String username = principal.getName();
        
        // Chiude la connessione precedente se l'utente è già connesso
        Session oldSession = userSessions.get(username);
        if (oldSession != null && oldSession.isOpen()) {
            oldSession.close(new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE, 
                "Nuova connessione dallo stesso utente"
            ));
        }
        
        userSessions.put(username, session);
        session.getUserProperties().put("username", username);
    }
    
    @OnClose
    public void onClose(Session session) {
        String username = (String) session.getUserProperties().get("username");
        if (username != null) {
            userSessions.remove(username);
        }
    }
    
    public static void sendToUser(String username, String message) throws IOException {
        Session session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }
}
```

Quale pattern implementa questo codice?

- a) Single Session Per User (una sola connessione attiva per utente)
- b) Multiple Sessions (connessioni multiple per lo stesso utente)
- c) Round Robin Load Balancing
- d) Session Replication

---

---

## Risposte Corrette

### 1. **b)** Conferma il passaggio dal protocollo HTTP al protocollo WebSocket

Il codice `101 Switching Protocols` indica il cambio di protocollo da HTTP a WebSocket.

### 2. **c)** Stateless come HTTP

WebSocket è stateful, mantiene una connessione persistente con stato.

### 3. **a, b, d, e)** Server può iniziare comunicazione, minore overhead, bidirezionale, connessione persistente

La cifratura richiede `wss://` esplicitamente, non è automatica.

### 4. **b)** Per essere condivisa tra tutte le istanze dell'endpoint (tutte le connessioni)

Le variabili `static` sono condivise a livello di classe tra tutte le istanze.

### 5. **b)** `ws://host:port/context-path/chat/Mario`

Il `@PathParam` viene sostituito nell'URI con il valore effettivo.

### 6. **a, b, c, d)** `@OnOpen`, `@OnMessage`, `@OnClose`, `@OnError`

`@OnConnect` e `@OnDisconnect` non esistono in JSR 356.

### 7. **b)** `getBasicRemote()` blocca il thread corrente, `getAsyncRemote()` no

L'invio sincrono blocca, l'asincrono delega a un thread in background.

### 8. **b)** Per memorizzare dati personalizzati associati alla sessione WebSocket

`getUserProperties()` restituisce una mappa per dati arbitrari della sessione.

### 9. **b)** `session.getUserPrincipal()`

Questo metodo restituisce il `Principal` dell'utente autenticato.

### 10. **a)** Viene chiamato solo `handleText()`

Il container sceglie l'handler in base al tipo effettivo del messaggio ricevuto.

### 11. **b)** Solo per l'ultimo frammento del messaggio

Il parametro `last` è `true` solo quando il messaggio è completo.

### 12. **b)** Verifica se il decoder può gestire il messaggio prima di tentare la decodifica

`willDecode()` permette di controllare se il decoder è applicabile al messaggio.

### 13. **b)** Ogni client ha il suo counter isolato

Ogni connessione crea una nuova istanza dell'endpoint con le sue variabili.

### 14. **b)** In modo sequenziale (single-threaded)

I messaggi per una singola connessione sono processati sequenzialmente.

### 15. **b)** Per evitare `ConcurrentModificationException` durante l'iterazione

Il blocco sincronizzato protegge l'iterazione da modifiche concorrenti.

### 16. **b)** Quando si verifica un'eccezione durante il processamento dei messaggi

`@OnError` è il callback per la gestione degli errori.

### 17. **a, b, d, e)** Logging, rimozione da collezioni, chiusura sessione, notifica client

Il riavvio automatico non è una funzionalità standard dell'endpoint.

### 18. **b)** `@EJB`

`@EJB` è l'annotazione standard per l'iniezione di EJB.

### 19. **b)** Per avere una singola istanza condivisa in tutta l'applicazione

`@ApplicationScoped` crea un singleton CDI a livello di applicazione.

### 20. **b)** Durante l'handshake HTTP iniziale

L'autenticazione avviene durante l'upgrade del protocollo HTTP.

### 21. **b)** `wss://`

`wss://` è lo schema per WebSocket sicuro (TLS).

### 22. **b)** Opzione b

Il token viene passato come parametro della query string nell'URI.

### 23. **b)** `onmessage`

L'evento `onmessage` viene chiamato alla ricezione di messaggi.

### 24. **b)** Il codice di chiusura WebSocket (normal closure)

`1000` è il codice standard per chiusura normale.

### 25. **b)** `@ClientEndpoint`

`@ClientEndpoint` definisce un endpoint WebSocket client.

### 26. **b)** Approccio B: minore latenza, minore carico sul server, comunicazione push

WebSocket elimina l'overhead del polling e permette push in tempo reale.

### 27. **a, b, c, d)** Usare `wss://`, gestire errori, validare auth, usare async per broadcast

La logica di business dovrebbe stare in servizi separati, non nell'endpoint.

### 28. **b)** Per mantenere la connessione attiva e rilevare connessioni morte

Ping/Pong serve come heartbeat per verificare che la connessione sia ancora valida.

### 29. **b)** Per recuperare una lista di prodotti da visualizzare una volta

Per operazioni one-shot come recuperare dati, HTTP REST è più appropriato.

### 30. **a)** Single Session Per User (una sola connessione attiva per utente)

Il pattern chiude connessioni precedenti dello stesso utente, mantenendone solo una attiva.
