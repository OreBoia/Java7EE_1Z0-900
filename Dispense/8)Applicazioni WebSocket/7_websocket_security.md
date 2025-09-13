# Sicurezza nelle Applicazioni WebSocket

La sicurezza è un aspetto critico per qualsiasi applicazione, e le applicazioni WebSocket non fanno eccezione. Poiché stabiliscono connessioni persistenti, è fondamentale proteggere il canale di comunicazione e garantire che solo gli utenti autorizzati possano connettersi e scambiare dati.

La sicurezza in WebSocket si articola su due livelli principali:

1. **Sicurezza a livello di trasporto**: Cifratura dei dati in transito.
2. **Autenticazione e Autorizzazione**: Verifica dell'identità dell'utente e controllo dei suoi permessi.

## 1. Sicurezza a Livello di Trasporto: `ws://` vs `wss://`

Similmente a HTTP e HTTPS, il protocollo WebSocket ha due schemi URI:

- `ws://` (WebSocket): Una connessione non cifrata. I dati viaggiano in chiaro e sono suscettibili di intercettazione (eavesdropping). **Da non usare mai in produzione.**
- `wss://` (WebSocket Secure): Una connessione sicura che utilizza **TLS (Transport Layer Security)**, lo stesso protocollo di cifratura usato da HTTPS. Tutti i dati scambiati tra client e server sono cifrati. **È l'unica opzione sicura per le applicazioni reali.**

Per abilitare `wss://` su un server, è necessario configurare un certificato TLS/SSL per il server web o l'application server, esattamente come si farebbe per abilitare HTTPS.

## 2. Autenticazione e Autorizzazione

Il protocollo WebSocket di per sé non definisce un meccanismo di autenticazione. L'identità dell'utente viene invece verificata durante la **fase di handshake iniziale**, che è una richiesta HTTP. Una volta che la connessione è stabilita, non ci sono ulteriori controlli di autenticazione a livello di protocollo.

Di seguito sono riportati i pattern più comuni per autenticare un utente in un'applicazione WebSocket Java EE.

### Pattern 1: Sfruttare il Contesto di Sicurezza di Java EE

Questo è l'approccio più integrato e standard in un'applicazione Java EE.

1. L'utente si autentica all'applicazione web tramite un meccanismo standard (es. form-based login, Basic Auth).
2. Il container crea una `HttpSession` e un `SecurityContext` per l'utente.
3. Quando il client (JavaScript) avvia la connessione WebSocket, il browser invia automaticamente i cookie associati al dominio, incluso il `JSESSIONID`.
4. Il container intercetta la richiesta di handshake, riconosce la sessione HTTP esistente e associa il `Principal` dell'utente alla connessione WebSocket.
5. Nell'endpoint server, si può accedere all'utente autenticato tramite `session.getUserPrincipal()`.

**Esempio:**

```java
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.Principal;

@ServerEndpoint("/notifications")
public class SecureNotificationEndpoint {

    @OnOpen
    public void onOpen(Session session) throws IOException {
        Principal principal = session.getUserPrincipal();

        if (principal == null || principal.getName() == null) {
            // Utente non autenticato, rifiuta la connessione
            System.err.println("Tentativo di connessione non autorizzato.");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Autenticazione richiesta."));
            return;
        }

        String username = principal.getName();
        session.getUserProperties().put("username", username);
        System.out.println("Utente " + username + " connesso al servizio di notifiche.");
    }
    // ...
}
```

### Pattern 2: Autenticazione basata su Token (es. JWT)

Questo pattern è comune in architetture basate su API REST e microservizi, dove lo stato non è mantenuto in una `HttpSession`.

1. Il client si autentica (es. tramite una chiamata a un endpoint REST `/login`) e riceve un token (es. un JSON Web Token - JWT).
2. Il client avvia la connessione WebSocket passando il token come parametro nella query string dell'URI.

    ```javascript
    const token = "ey..."; // Token ricevuto dal server
    const ws = new WebSocket(`wss://example.com/api/chat?token=${token}`);
    ```

3. Nell'endpoint server, il metodo `@OnOpen` estrae il token, lo valida (controlla la firma, la scadenza, etc.) e, se valido, permette la connessione.

**Esempio:**

```java
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/chat")
public class TokenAuthEndpoint {

    @OnOpen
    public void onOpen(Session session) throws IOException {
        Map<String, List<String>> params = session.getRequestParameterMap();
        if (!params.containsKey("token")) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token mancante."));
            return;
        }

        String token = params.get("token").get(0);
        
        // La logica di validazione del token va implementata qui
        if (isValidToken(token)) {
            String username = getUsernameFromToken(token);
            session.getUserProperties().put("username", username);
            System.out.println("Utente " + username + " autenticato con token.");
        } else {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token non valido."));
        }
    }
    
    private boolean isValidToken(String token) { /* ... logica di validazione ... */ return true; }
    private String getUsernameFromToken(String token) { /* ... logica di estrazione ... */ return "user"; }
    // ...
}
```

### Pattern 3: Configurazione Avanzata con `ServerEndpointConfig.Configurator`

Per un controllo ancora più fine sulla fase di handshake, si può implementare un `ServerEndpointConfig.Configurator`. Questo permette di intercettare la richiesta HTTP prima che la connessione WebSocket venga stabilita.

È utile per leggere header HTTP personalizzati (es. `Authorization: Bearer ...`) o per eseguire logica complessa.

**Esempio:**

```java
// 1. Creare il Configuratore
public class MyEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // Esempio: recupera un header HTTP personalizzato
        String apiKey = request.getHeaders().get("X-API-Key").get(0);
        if (isValidApiKey(apiKey)) {
            // Salva informazioni nel contesto utente per l'endpoint
            sec.getUserProperties().put("apiKey", apiKey);
        } else {
            // Qui non si può chiudere la connessione, ma si può impostare uno stato
            // che verrà controllato in @OnOpen per chiuderla immediatamente.
            sec.getUserProperties().put("auth_failed", true);
        }
    }
    private boolean isValidApiKey(String key) { /* ... */ return true; }
}

// 2. Applicare il Configuratore all'Endpoint
@ServerEndpoint(value = "/data-feed", configurator = MyEndpointConfigurator.class)
public class ConfiguredEndpoint {
    @OnOpen
    public void onOpen(Session session) throws IOException {
        if (session.getUserProperties().containsKey("auth_failed")) {
            session.close();
            return;
        }
        String apiKey = (String) session.getUserProperties().get("apiKey");
        System.out.println("Connessione stabilita con API Key: " + apiKey);
    }
}
```

## Tabella Riassuntiva dei Meccanismi di Sicurezza

| Meccanismo                  | Descrizione                                                                                             | Pro                                                              | Contro                                                              |
| --------------------------- | ------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------- |
| **`wss://` (TLS)**          | Cifra tutto il traffico WebSocket.                                                                      | **Essenziale**. Protegge da intercettazione e man-in-the-middle. | Nessuno. Va sempre usato in produzione.                             |
| **Contesto di Sicurezza EE**| Sfrutta l'autenticazione esistente basata su `HttpSession`.                                             | Integrato, standard, robusto.                                    | Lega l'autenticazione alla sessione HTTP, meno adatto per API stateless. |
| **Token nell'URI**          | Passa un token di autenticazione come parametro della query string.                                     | Stateless, ottimo per API REST e microservizi.                   | Il token può finire nei log del server, potenziale rischio di sicurezza. |
| **`Configurator`**          | Intercetta l'handshake HTTP per logica di autenticazione personalizzata (es. header `Authorization`). | Molto flessibile, permette di usare header HTTP standard.        | Più complesso da implementare.                                      |

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **`wss://`**                  | Lo schema URI per le connessioni WebSocket sicure, basate su TLS.                                                                      |
| **TLS (Transport Layer Security)** | Il protocollo crittografico standard che fornisce comunicazioni sicure su una rete di computer. È il successore di SSL.             |
| **Handshake**                 | La negoziazione iniziale basata su HTTP durante la quale viene verificata l'identità e stabilita la connessione WebSocket.            |
| **`Principal`**               | Un oggetto `java.security.Principal` che rappresenta l'identità di un utente autenticato nel contesto di sicurezza di Java EE.         |
| **Token**                     | Una stringa di dati (es. JWT) che rappresenta l'autorizzazione di un client ad accedere a determinate risorse.                         |
| **`ServerEndpointConfig.Configurator`** | Una classe che permette di intercettare e personalizzare il processo di handshake di un endpoint WebSocket sul server.         |
