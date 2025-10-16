# Pattern Avanzati per Applicazioni WebSocket

Questo documento presenta pattern architetturali e best practices per implementare applicazioni WebSocket enterprise-ready, inclusi pattern per gestione delle sessioni, scaling, e integrazione con altri componenti Java EE.

## Pattern: Single Session Per User

Questo pattern garantisce che un utente possa avere **una sola connessione WebSocket attiva** alla volta. Se l'utente apre una nuova connessione (es. da un altro tab o dispositivo), la connessione precedente viene automaticamente chiusa.

### Vantaggi

- **Consistenza**: Evita problemi di stato duplicato
- **Risorse**: Riduce l'uso di memoria e connessioni
- **Sicurezza**: Previene sessioni multiple non autorizzate
- **Notifiche**: Garantisce che i messaggi siano consegnati a una sola sessione

### Implementazione Base

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/single-session")
public class SingleSessionEndpoint {
    
    // Mappa username -> session (una sola session per username)
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        Principal principal = session.getUserPrincipal();
        
        // Verifica autenticazione
        if (principal == null) {
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY,
                "Autenticazione richiesta"
            ));
            return;
        }
        
        String username = principal.getName();
        
        // Chiudi la sessione precedente se esiste
        Session oldSession = userSessions.get(username);
        if (oldSession != null && oldSession.isOpen()) {
            System.out.println("Chiusura sessione precedente per: " + username);
            oldSession.close(new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE,
                "Nuova connessione dallo stesso utente"
            ));
        }
        
        // Registra la nuova sessione
        userSessions.put(username, session);
        session.getUserProperties().put("username", username);
        
        System.out.println("Utente " + username + " connesso. Sessioni attive: " + 
                         userSessions.size());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        System.out.println("Messaggio da " + username + ": " + message);
        
        // Processa il messaggio...
        session.getBasicRemote().sendText("Echo: " + message);
    }
    
    @OnClose
    public void onClose(Session session) {
        String username = (String) session.getUserProperties().get("username");
        if (username != null) {
            userSessions.remove(username);
            System.out.println("Utente " + username + " disconnesso. Sessioni attive: " + 
                             userSessions.size());
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore: " + throwable.getMessage());
    }
    
    /**
     * Invia un messaggio a un utente specifico
     */
    public static boolean sendToUser(String username, String message) {
        Session session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                return true;
            } catch (IOException e) {
                System.err.println("Errore invio messaggio a " + username + ": " + 
                                 e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * Verifica se un utente è online
     */
    public static boolean isUserOnline(String username) {
        Session session = userSessions.get(username);
        return session != null && session.isOpen();
    }
    
    /**
     * Ottieni la lista degli utenti online
     */
    public static Set<String> getOnlineUsers() {
        return new HashSet<>(userSessions.keySet());
    }
}
```

### Variante: Notifica alla Sessione Precedente

```java
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
    Session oldSession = userSessions.get(username);
    
    if (oldSession != null && oldSession.isOpen()) {
        // Notifica la sessione precedente prima di chiuderla
        try {
            oldSession.getBasicRemote().sendText(
                "{\"type\":\"KICKED\",\"reason\":\"Nuova connessione da altro dispositivo\"}"
            );
            Thread.sleep(500); // Breve pausa per permettere l'invio
        } catch (Exception e) {
            System.err.println("Errore notifica sessione precedente: " + e.getMessage());
        }
        
        oldSession.close(new CloseReason(
            CloseReason.CloseCodes.NORMAL_CLOSURE,
            "Nuova connessione dallo stesso utente"
        ));
    }
    
    userSessions.put(username, session);
    session.getUserProperties().put("username", username);
}
```

## Pattern: Room-Based Communication

Organizza le connessioni in "stanze" dove i messaggi vengono broadcast solo agli utenti nella stessa stanza.

```java
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/room/{roomId}/{username}")
public class RoomBasedEndpoint {
    
    // Struttura: roomId -> Set di sessioni
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, 
                       @PathParam("roomId") String roomId,
                       @PathParam("username") String username) throws IOException {
        
        // Salva le informazioni nella sessione
        session.getUserProperties().put("roomId", roomId);
        session.getUserProperties().put("username", username);
        
        // Aggiungi la sessione alla stanza
        rooms.computeIfAbsent(roomId, k -> Collections.synchronizedSet(new HashSet<>()))
             .add(session);
        
        System.out.println(username + " è entrato nella stanza " + roomId);
        
        // Notifica gli altri nella stanza
        broadcastToRoom(roomId, 
                       "{\"type\":\"USER_JOINED\",\"username\":\"" + username + "\"}", 
                       session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String roomId = (String) session.getUserProperties().get("roomId");
        String username = (String) session.getUserProperties().get("username");
        
        // Broadcast a tutti nella stessa stanza
        String formattedMessage = "{\"type\":\"MESSAGE\",\"username\":\"" + username + 
                                 "\",\"content\":\"" + message + "\"}";
        broadcastToRoom(roomId, formattedMessage, null);
    }
    
    @OnClose
    public void onClose(Session session) throws IOException {
        String roomId = (String) session.getUserProperties().get("roomId");
        String username = (String) session.getUserProperties().get("username");
        
        // Rimuovi la sessione dalla stanza
        Set<Session> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            
            // Rimuovi la stanza se vuota
            if (roomSessions.isEmpty()) {
                rooms.remove(roomId);
                System.out.println("Stanza " + roomId + " eliminata (vuota)");
            } else {
                // Notifica gli altri
                broadcastToRoom(roomId,
                               "{\"type\":\"USER_LEFT\",\"username\":\"" + username + "\"}",
                               null);
            }
        }
        
        System.out.println(username + " ha lasciato la stanza " + roomId);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore: " + throwable.getMessage());
    }
    
    /**
     * Invia un messaggio a tutti in una stanza, escludendo opzionalmente una sessione
     */
    private void broadcastToRoom(String roomId, String message, Session excludeSession) 
            throws IOException {
        Set<Session> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            synchronized (roomSessions) {
                for (Session s : roomSessions) {
                    if (s.isOpen() && !s.equals(excludeSession)) {
                        s.getBasicRemote().sendText(message);
                    }
                }
            }
        }
    }
    
    /**
     * Ottieni informazioni sulle stanze attive
     */
    public static Map<String, Integer> getRoomInfo() {
        Map<String, Integer> info = new HashMap<>();
        rooms.forEach((roomId, sessions) -> info.put(roomId, sessions.size()));
        return info;
    }
}
```

## Pattern: Command Pattern per Messaggi

Struttura i messaggi come comandi per separare la logica di routing dalla business logic.

```java
// Interfaccia base per i comandi
public interface WebSocketCommand {
    void execute(Session session) throws IOException;
}

// Comandi concreti
public class SendMessageCommand implements WebSocketCommand {
    private final String recipient;
    private final String content;
    
    public SendMessageCommand(String recipient, String content) {
        this.recipient = recipient;
        this.content = content;
    }
    
    @Override
    public void execute(Session session) throws IOException {
        // Logica per inviare messaggio privato
        String sender = (String) session.getUserProperties().get("username");
        String message = "{\"from\":\"" + sender + "\",\"to\":\"" + recipient + 
                        "\",\"content\":\"" + content + "\"}";
        SingleSessionEndpoint.sendToUser(recipient, message);
    }
}

public class JoinRoomCommand implements WebSocketCommand {
    private final String roomId;
    
    public JoinRoomCommand(String roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public void execute(Session session) throws IOException {
        // Logica per entrare in una stanza
        session.getUserProperties().put("currentRoom", roomId);
        session.getBasicRemote().sendText(
            "{\"type\":\"JOINED_ROOM\",\"roomId\":\"" + roomId + "\"}"
        );
    }
}

// Parser dei comandi
public class CommandParser {
    
    public static WebSocketCommand parse(String jsonMessage) {
        // Usa una libreria JSON (es. Gson, Jackson)
        JsonObject json = JsonParser.parseString(jsonMessage).getAsJsonObject();
        String type = json.get("type").getAsString();
        
        switch (type) {
            case "SEND_MESSAGE":
                return new SendMessageCommand(
                    json.get("recipient").getAsString(),
                    json.get("content").getAsString()
                );
            case "JOIN_ROOM":
                return new JoinRoomCommand(
                    json.get("roomId").getAsString()
                );
            // Altri comandi...
            default:
                throw new IllegalArgumentException("Comando sconosciuto: " + type);
        }
    }
}

// Endpoint che usa il pattern Command
@ServerEndpoint("/command-based")
public class CommandBasedEndpoint {
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            WebSocketCommand command = CommandParser.parse(message);
            command.execute(session);
        } catch (Exception e) {
            System.err.println("Errore esecuzione comando: " + e.getMessage());
            try {
                session.getBasicRemote().sendText(
                    "{\"type\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}"
                );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
```

## Pattern: Session Manager Centralizzato

Gestisce tutte le sessioni attraverso un componente CDI centralizzato.

```java
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class SessionManager {
    
    // username -> Session
    private final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    // sessionId -> username
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    
    // username -> Set<tags>
    private final Map<String, Set<String>> userTags = new ConcurrentHashMap<>();
    
    /**
     * Registra una nuova sessione
     */
    public synchronized void registerSession(String username, Session session) {
        // Chiudi sessione precedente
        Session oldSession = userSessions.get(username);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close(new CloseReason(
                    CloseReason.CloseCodes.NORMAL_CLOSURE,
                    "Nuova connessione"
                ));
            } catch (IOException e) {
                System.err.println("Errore chiusura sessione precedente: " + e.getMessage());
            }
        }
        
        userSessions.put(username, session);
        sessionToUser.put(session.getId(), username);
        userTags.putIfAbsent(username, new HashSet<>());
    }
    
    /**
     * Rimuove una sessione
     */
    public void unregisterSession(Session session) {
        String username = sessionToUser.remove(session.getId());
        if (username != null) {
            userSessions.remove(username);
            userTags.remove(username);
        }
    }
    
    /**
     * Invia un messaggio a un utente specifico
     */
    public boolean sendToUser(String username, String message) {
        Session session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
                return true;
            } catch (Exception e) {
                System.err.println("Errore invio a " + username + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * Broadcast a tutti gli utenti
     */
    public void broadcastToAll(String message) {
        userSessions.values().parallelStream()
            .filter(Session::isOpen)
            .forEach(session -> {
                try {
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    System.err.println("Errore broadcast: " + e.getMessage());
                }
            });
    }
    
    /**
     * Invia a utenti con un determinato tag
     */
    public void sendToUsersWithTag(String tag, String message) {
        userTags.entrySet().stream()
            .filter(entry -> entry.getValue().contains(tag))
            .map(Map.Entry::getKey)
            .forEach(username -> sendToUser(username, message));
    }
    
    /**
     * Aggiungi tag a un utente
     */
    public void addTagToUser(String username, String tag) {
        userTags.computeIfAbsent(username, k -> new HashSet<>()).add(tag);
    }
    
    /**
     * Rimuovi tag da un utente
     */
    public void removeTagFromUser(String username, String tag) {
        Set<String> tags = userTags.get(username);
        if (tags != null) {
            tags.remove(tag);
        }
    }
    
    /**
     * Ottieni statistiche
     */
    public SessionStatistics getStatistics() {
        return new SessionStatistics(
            userSessions.size(),
            userSessions.values().stream().filter(Session::isOpen).count(),
            userTags.values().stream().mapToInt(Set::size).sum()
        );
    }
    
    /**
     * Verifica se un utente è online
     */
    public boolean isUserOnline(String username) {
        Session session = userSessions.get(username);
        return session != null && session.isOpen();
    }
    
    /**
     * Ottieni lista utenti online
     */
    public List<String> getOnlineUsers() {
        return userSessions.entrySet().stream()
            .filter(entry -> entry.getValue().isOpen())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}

// Classe per statistiche
public class SessionStatistics {
    private final int totalSessions;
    private final long activeSessions;
    private final int totalTags;
    
    public SessionStatistics(int totalSessions, long activeSessions, int totalTags) {
        this.totalSessions = totalSessions;
        this.activeSessions = activeSessions;
        this.totalTags = totalTags;
    }
    
    // Getter...
}

// Endpoint che usa il SessionManager
@ServerEndpoint("/managed")
public class ManagedEndpoint {
    
    @Inject
    private SessionManager sessionManager;
    
    @OnOpen
    public void onOpen(Session session) {
        Principal principal = session.getUserPrincipal();
        if (principal != null) {
            String username = principal.getName();
            sessionManager.registerSession(username, session);
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // Usa il session manager per routing
    }
    
    @OnClose
    public void onClose(Session session) {
        sessionManager.unregisterSession(session);
    }
}
```

## Pattern: Presenza e Stato Utente

Gestisce lo stato di presenza degli utenti (online, away, busy, etc.).

```java
public enum UserStatus {
    ONLINE, AWAY, BUSY, OFFLINE
}

@ApplicationScoped
public class PresenceManager {
    
    private final Map<String, UserStatus> userStatuses = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivity = new ConcurrentHashMap<>();
    
    @Inject
    private SessionManager sessionManager;
    
    public void setUserStatus(String username, UserStatus status) {
        UserStatus oldStatus = userStatuses.put(username, status);
        lastActivity.put(username, System.currentTimeMillis());
        
        if (oldStatus != status) {
            // Notifica il cambio di stato
            String notification = "{\"type\":\"STATUS_CHANGE\",\"username\":\"" + 
                                username + "\",\"status\":\"" + status + "\"}";
            sessionManager.broadcastToAll(notification);
        }
    }
    
    public UserStatus getUserStatus(String username) {
        return userStatuses.getOrDefault(username, UserStatus.OFFLINE);
    }
    
    public void updateActivity(String username) {
        lastActivity.put(username, System.currentTimeMillis());
        
        // Se era AWAY, torna ONLINE
        if (userStatuses.get(username) == UserStatus.AWAY) {
            setUserStatus(username, UserStatus.ONLINE);
        }
    }
    
    // Task schedulato per marcare utenti inattivi come AWAY
    @Schedule(hour = "*", minute = "*", second = "*/30") // Ogni 30 secondi
    public void checkInactiveUsers() {
        long now = System.currentTimeMillis();
        long awayThreshold = 5 * 60 * 1000; // 5 minuti
        
        lastActivity.entrySet().stream()
            .filter(entry -> (now - entry.getValue()) > awayThreshold)
            .filter(entry -> userStatuses.get(entry.getKey()) == UserStatus.ONLINE)
            .forEach(entry -> setUserStatus(entry.getKey(), UserStatus.AWAY));
    }
    
    public Map<String, UserStatus> getAllStatuses() {
        return new HashMap<>(userStatuses);
    }
}
```

## Pattern: Message Queue con Persistenza

Per garantire la consegna dei messaggi anche se l'utente non è online.

```java
@ApplicationScoped
public class MessageQueue {
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private SessionManager sessionManager;
    
    /**
     * Invia un messaggio, con fallback su queue se l'utente è offline
     */
    @Transactional
    public void sendOrQueue(String recipient, String message) {
        // Prova a inviare immediatamente
        if (sessionManager.sendToUser(recipient, message)) {
            return;
        }
        
        // Utente offline, salva nel database
        QueuedMessage qm = new QueuedMessage();
        qm.setRecipient(recipient);
        qm.setContent(message);
        qm.setTimestamp(new Date());
        em.persist(qm);
    }
    
    /**
     * Consegna i messaggi in coda quando l'utente si connette
     */
    @Transactional
    public void deliverQueuedMessages(String username) {
        List<QueuedMessage> messages = em.createQuery(
            "SELECT m FROM QueuedMessage m WHERE m.recipient = :username ORDER BY m.timestamp",
            QueuedMessage.class
        )
        .setParameter("username", username)
        .getResultList();
        
        for (QueuedMessage msg : messages) {
            if (sessionManager.sendToUser(username, msg.getContent())) {
                em.remove(msg);
            }
        }
    }
}

@Entity
public class QueuedMessage {
    @Id @GeneratedValue
    private Long id;
    private String recipient;
    @Lob
    private String content;
    private Date timestamp;
    
    // Getter e setter...
}
```

## Glossario dei Termini

| Termine | Definizione |
|---------|-------------|
| **Single Session Per User** | Pattern che limita un utente a una sola connessione attiva |
| **Room** | Gruppo logico di connessioni che condividono messaggi |
| **Command Pattern** | Pattern che incapsula richieste come oggetti |
| **Session Manager** | Componente centralizzato per gestire tutte le sessioni WebSocket |
| **Presence** | Stato di disponibilità di un utente (online, away, busy) |
| **Message Queue** | Coda per messaggi non consegnabili immediatamente |
| **Tag** | Etichetta associata a un utente per routing selettivo |
| **Broadcast** | Invio di un messaggio a tutti i client connessi |
| **Multicast** | Invio di un messaggio a un sottoinsieme di client |
