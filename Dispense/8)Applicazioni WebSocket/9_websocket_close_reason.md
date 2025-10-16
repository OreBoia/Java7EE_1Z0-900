# Chiusura delle Connessioni WebSocket e CloseReason

La chiusura delle connessioni WebSocket può avvenire in diversi modi e per diverse ragioni. Comprendere i meccanismi di chiusura, i codici standard e come gestirli correttamente è essenziale per creare applicazioni WebSocket robuste e affidabili.

## Modalità di Chiusura

Una connessione WebSocket può essere chiusa in tre modi principali:

### 1. Chiusura Normale (Clean Closure)

Una chiusura pulita avviene quando una delle parti (client o server) decide volontariamente di terminare la connessione e invia un frame di controllo `Close`.

**Caratteristiche:**

- Il frame `Close` contiene un codice di chiusura e un motivo opzionale
- L'altra parte risponde con il proprio frame `Close`
- Entrambe le parti eseguono la pulizia delle risorse
- Il metodo `@OnClose` viene invocato

### 2. Chiusura Anomala (Abnormal Closure)

Si verifica quando la connessione viene interrotta senza un handshake di chiusura appropriato.

**Cause comuni:**

- Perdita di connettività di rete
- Crash dell'applicazione
- Timeout di rete
- Chiusura forzata del processo

**Comportamento:**

- Il metodo `@OnError` potrebbe essere invocato (dipende dal tipo di errore)
- Il metodo `@OnClose` viene comunque invocato
- Non c'è scambio di frame `Close`

### 3. Chiusura per Errore di Protocollo

Il container WebSocket chiude automaticamente la connessione quando rileva violazioni del protocollo.

**Esempi:**

- Frame malformati
- Messaggi troppo grandi (oltre i limiti configurati)
- Errori di decodifica
- Violazioni di sicurezza

## L'Oggetto CloseReason

La classe `javax.websocket.CloseReason` rappresenta il motivo della chiusura di una connessione WebSocket. È composta da due parti:

1. **Codice di chiusura** (`CloseCode`): Un numero che identifica la categoria del motivo
2. **Frase descrittiva** (`ReasonPhrase`): Una stringa opzionale che fornisce dettagli aggiuntivi

### Struttura di CloseReason

```java
import javax.websocket.CloseReason;

// Creare un CloseReason con un codice standard
CloseReason normalClosure = new CloseReason(
    CloseReason.CloseCodes.NORMAL_CLOSURE,  // Codice: 1000
    "Disconnessione volontaria"              // Motivo testuale
);

// Creare un CloseReason personalizzato (codici 4000-4999)
CloseReason customReason = new CloseReason(
    new CloseReason.CloseCode() {
        @Override
        public int getCode() {
            return 4001; // Codice applicativo personalizzato
        }
    },
    "Token scaduto"
);
```

## Codici di Chiusura Standard (CloseCodes)

La specifica WebSocket definisce una serie di codici standard nella classe `CloseReason.CloseCodes`:

| Codice | Nome | Descrizione | Quando Usarlo |
|--------|------|-------------|---------------|
| **1000** | `NORMAL_CLOSURE` | Chiusura normale e prevista | Quando client o server decidono volontariamente di chiudere |
| **1001** | `GOING_AWAY` | Un endpoint sta per sparire | Shutdown del server, navigazione via dal client |
| **1002** | `PROTOCOL_ERROR` | Errore nel protocollo WebSocket | Violazione del protocollo (gestito automaticamente) |
| **1003** | `CANNOT_ACCEPT` | Tipo di dato non supportato | Ricevuto un tipo di messaggio non gestito |
| **1007** | `NOT_CONSISTENT` | Dati non validi o inconsistenti | Errori di validazione del messaggio |
| **1008** | `VIOLATED_POLICY` | Violazione di policy | Autenticazione fallita, autorizzazione negata |
| **1009** | `TOO_BIG` | Messaggio troppo grande | Superato il limite di dimensione (configurabile) |
| **1010** | `UNEXPECTED_CONDITION` | Condizione inaspettata sul server | Errori interni del server |
| **1011** | `SERVICE_RESTART` | Il server sta riavviando | Manutenzione programmata |
| **1012** | `TRY_AGAIN_LATER` | Il server è temporaneamente non disponibile | Sovraccarico temporaneo |
| **1013** | - | Riservato | - |
| **1015** | `TLS_HANDSHAKE_FAILURE` | Errore nel handshake TLS | (Solo per report, non inviabile) |
| **4000-4999** | - | Codici personalizzati per applicazioni | Errori specifici dell'applicazione |

### Intervalli di Codici

- **0-999**: Riservati (non usare)
- **1000-2999**: Definiti dallo standard WebSocket
- **3000-3999**: Riservati per librerie e framework
- **4000-4999**: Disponibili per applicazioni personalizzate
- **5000+**: Non validi

## Chiudere una Connessione dal Server

### Chiusura Semplice

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/example")
public class CloseExampleEndpoint {
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if ("quit".equalsIgnoreCase(message)) {
            // Chiusura semplice senza motivo esplicito
            session.close();
        }
    }
}
```

### Chiusura con Motivo

```java
@OnMessage
public void onMessage(String message, Session session) throws IOException {
    if ("logout".equalsIgnoreCase(message)) {
        // Chiusura con codice e motivo specifici
        CloseReason reason = new CloseReason(
            CloseReason.CloseCodes.NORMAL_CLOSURE,
            "Logout richiesto dall'utente"
        );
        session.close(reason);
    }
}
```

### Chiusura per Errore di Autenticazione

```java
@OnOpen
public void onOpen(Session session) throws IOException {
    String token = getTokenFromSession(session);
    
    if (!isValidToken(token)) {
        CloseReason reason = new CloseReason(
            CloseReason.CloseCodes.VIOLATED_POLICY,
            "Token di autenticazione non valido o scaduto"
        );
        session.close(reason);
    }
}

private String getTokenFromSession(Session session) {
    var params = session.getRequestParameterMap();
    return params.containsKey("token") ? params.get("token").get(0) : null;
}

private boolean isValidToken(String token) {
    // Logica di validazione
    return token != null && !token.isEmpty();
}
```

### Chiusura per Timeout o Inattività

```java
import java.util.concurrent.*;

@ServerEndpoint("/activity-monitor")
public class ActivityMonitorEndpoint {
    
    private static final long INACTIVITY_TIMEOUT = 300000; // 5 minuti
    private static final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(2);
    
    @OnOpen
    public void onOpen(Session session) {
        // Inizializza il timestamp dell'ultima attività
        updateLastActivity(session);
        
        // Schedula controllo periodico dell'inattività
        ScheduledFuture<?> inactivityCheck = scheduler.scheduleAtFixedRate(() -> {
            checkInactivity(session);
        }, 60, 60, TimeUnit.SECONDS);
        
        session.getUserProperties().put("inactivityCheck", inactivityCheck);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        // Aggiorna il timestamp ad ogni messaggio
        updateLastActivity(session);
        
        // Processa il messaggio...
        session.getBasicRemote().sendText("Ricevuto: " + message);
    }
    
    @OnClose
    public void onClose(Session session) {
        ScheduledFuture<?> task = 
            (ScheduledFuture<?>) session.getUserProperties().get("inactivityCheck");
        if (task != null) {
            task.cancel(true);
        }
    }
    
    private void updateLastActivity(Session session) {
        session.getUserProperties().put("lastActivity", System.currentTimeMillis());
    }
    
    private void checkInactivity(Session session) {
        if (!session.isOpen()) {
            return;
        }
        
        Long lastActivity = (Long) session.getUserProperties().get("lastActivity");
        long inactiveTime = System.currentTimeMillis() - lastActivity;
        
        if (inactiveTime > INACTIVITY_TIMEOUT) {
            try {
                CloseReason reason = new CloseReason(
                    CloseReason.CloseCodes.NORMAL_CLOSURE,
                    "Connessione chiusa per inattività"
                );
                session.close(reason);
            } catch (IOException e) {
                System.err.println("Errore chiusura sessione: " + e.getMessage());
            }
        }
    }
}
```

## Gestire la Chiusura con @OnClose

Il metodo annotato con `@OnClose` viene invocato quando una connessione viene chiusa, indipendentemente dal motivo.

### Parametri Disponibili

```java
@OnClose
public void onClose(Session session, CloseReason closeReason) {
    // Entrambi i parametri sono opzionali
    
    String sessionId = session.getId();
    int closeCode = closeReason.getCloseCode().getCode();
    String reasonPhrase = closeReason.getReasonPhrase();
    
    System.out.println("Sessione " + sessionId + " chiusa.");
    System.out.println("Codice: " + closeCode + ", Motivo: " + reasonPhrase);
    
    // Pulizia delle risorse...
}
```

### Esempio Completo di Gestione Chiusura

```java
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/managed")
public class ManagedCloseEndpoint {
    
    private static Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session) {
        String userId = getUserId(session);
        activeSessions.put(userId, session);
        System.out.println("Utente " + userId + " connesso. Sessioni attive: " + 
                         activeSessions.size());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if ("shutdown".equals(message)) {
            // Chiusura gestita dall'applicazione
            CloseReason reason = new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE,
                "Shutdown richiesto"
            );
            session.close(reason);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        String userId = getUserId(session);
        activeSessions.remove(userId);
        
        // Analizza il tipo di chiusura
        int code = closeReason.getCloseCode().getCode();
        String phrase = closeReason.getReasonPhrase();
        
        if (code == CloseReason.CloseCodes.NORMAL_CLOSURE.getCode()) {
            System.out.println("Chiusura normale per " + userId + ": " + phrase);
        } else if (code == CloseReason.CloseCodes.GOING_AWAY.getCode()) {
            System.out.println("Client " + userId + " sta navigando via");
        } else if (code == CloseReason.CloseCodes.VIOLATED_POLICY.getCode()) {
            System.err.println("Violazione policy per " + userId + ": " + phrase);
        } else {
            System.err.println("Chiusura anomala per " + userId + 
                             " - Codice: " + code + ", Motivo: " + phrase);
        }
        
        System.out.println("Sessioni attive rimanenti: " + activeSessions.size());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        String userId = getUserId(session);
        System.err.println("Errore per " + userId + ": " + throwable.getMessage());
        
        // In caso di errore, chiudi esplicitamente la sessione
        try {
            if (session.isOpen()) {
                CloseReason reason = new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                    "Errore interno: " + throwable.getMessage()
                );
                session.close(reason);
            }
        } catch (IOException e) {
            System.err.println("Errore durante chiusura sessione: " + e.getMessage());
        }
    }
    
    private String getUserId(Session session) {
        return (String) session.getUserProperties().getOrDefault("userId", 
                                                                 session.getId());
    }
}
```

## Chiusura dal Client JavaScript

```javascript
const ws = new WebSocket("wss://example.com/app");

ws.onopen = function() {
    console.log("Connesso");
};

// Chiusura normale
function disconnect() {
    // Codice 1000 = NORMAL_CLOSURE
    ws.close(1000, "Disconnessione volontaria dell'utente");
}

// Gestire la chiusura
ws.onclose = function(event) {
    console.log("Connessione chiusa");
    console.log("Codice:", event.code);
    console.log("Motivo:", event.reason);
    console.log("Chiusura pulita:", event.wasClean);
    
    // Analizza il codice di chiusura
    if (event.code === 1000) {
        console.log("Chiusura normale");
    } else if (event.code === 1001) {
        console.log("Server in shutdown");
    } else if (event.code === 1008) {
        console.log("Violazione di policy - probabilmente non autenticato");
    } else if (event.code === 1006) {
        console.log("Connessione persa senza chiusura pulita");
    } else {
        console.log("Chiusura inaspettata");
    }
};
```

## Best Practices per la Chiusura

| Pratica | Descrizione | Raccomandazione |
|---------|-------------|----------------|
| **Sempre fornire un motivo** | Specificare il codice e la descrizione | Facilita il debugging e il logging |
| **Usare codici appropriati** | Scegliere il codice che meglio descrive la situazione | `NORMAL_CLOSURE` per chiusure volontarie, `VIOLATED_POLICY` per errori di autenticazione |
| **Verificare isOpen()** | Prima di chiamare `close()` | Evita `IOException` su sessioni già chiuse |
| **Gestire IOException** | Quando si chiama `close()` | La chiusura può fallire, gestire l'eccezione |
| **Pulizia in @OnClose** | Liberare risorse, cancellare task | Sempre eseguire cleanup, indipendentemente dal motivo |
| **Non bloccare in @OnClose** | Operazioni veloci | Il container potrebbe avere timeout |
| **Codici personalizzati** | Per errori specifici dell'applicazione | Usare range 4000-4999 |
| **Logging appropriato** | Registrare chiusure anomale | Facilita il monitoraggio e debug |

## Pattern: Gestione Centralizzata della Chiusura

```java
import javax.websocket.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionCloser {
    
    private static final Logger logger = Logger.getLogger(SessionCloser.class.getName());
    
    /**
     * Chiude una sessione in modo sicuro con gestione completa degli errori
     */
    public static void closeGracefully(Session session, 
                                      CloseReason.CloseCode code, 
                                      String reason) {
        if (session == null) {
            return;
        }
        
        if (!session.isOpen()) {
            logger.log(Level.WARNING, 
                      "Tentativo di chiudere una sessione già chiusa: " + session.getId());
            return;
        }
        
        try {
            CloseReason closeReason = new CloseReason(code, reason);
            session.close(closeReason);
            logger.log(Level.INFO, 
                      "Sessione {0} chiusa con successo. Codice: {1}, Motivo: {2}",
                      new Object[]{session.getId(), code.getCode(), reason});
        } catch (IOException e) {
            logger.log(Level.SEVERE, 
                      "Errore durante la chiusura della sessione " + session.getId(), 
                      e);
        }
    }
    
    /**
     * Chiusura normale
     */
    public static void closeNormal(Session session, String reason) {
        closeGracefully(session, CloseReason.CloseCodes.NORMAL_CLOSURE, reason);
    }
    
    /**
     * Chiusura per violazione di policy (es. autenticazione fallita)
     */
    public static void closeUnauthorized(Session session, String reason) {
        closeGracefully(session, CloseReason.CloseCodes.VIOLATED_POLICY, reason);
    }
    
    /**
     * Chiusura per errore interno
     */
    public static void closeOnError(Session session, Throwable error) {
        String reason = "Errore interno: " + error.getMessage();
        closeGracefully(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, reason);
    }
    
    /**
     * Chiusura per shutdown del server
     */
    public static void closeGoingAway(Session session) {
        closeGracefully(session, CloseReason.CloseCodes.GOING_AWAY, 
                       "Server in shutdown");
    }
}
```

## Glossario dei Termini

| Termine | Definizione |
|---------|-------------|
| **CloseReason** | Oggetto che contiene il codice e il motivo della chiusura di una connessione |
| **CloseCode** | Numero intero che identifica la categoria del motivo di chiusura |
| **Clean Closure** | Chiusura con handshake corretto tramite frame Close |
| **Abnormal Closure** | Chiusura senza handshake dovuta a errore o interruzione |
| **NORMAL_CLOSURE** | Codice 1000 - chiusura volontaria e prevista |
| **VIOLATED_POLICY** | Codice 1008 - violazione di regole di sicurezza o autorizzazione |
| **GOING_AWAY** | Codice 1001 - un endpoint sta per non essere più disponibile |
| **ReasonPhrase** | Stringa descrittiva opzionale che spiega il motivo della chiusura |
| **Frame Close** | Frame di controllo WebSocket che inizia la procedura di chiusura |
