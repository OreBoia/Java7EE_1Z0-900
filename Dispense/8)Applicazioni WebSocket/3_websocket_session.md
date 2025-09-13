# La Sessione WebSocket (`javax.websocket.Session`)

Nel contesto della Java API for WebSocket (JSR 356), l'oggetto `javax.websocket.Session` è il cuore della comunicazione. Rappresenta la **connessione logica e persistente** tra un endpoint server e un singolo client. Viene creato all'apertura della connessione (handshake) e distrutto alla sua chiusura.

Ogni interazione che il server ha con un client specifico passa attraverso l'oggetto `Session` associato a quel client.

## Funzionalità Chiave della Sessione

### 1. Inviare Messaggi al Client

La `Session` fornisce l'oggetto `RemoteEndpoint` per inviare messaggi dal server al client. Esistono due modalità di invio:

- **Sincrona (bloccante)**: Il thread che invia il messaggio attende che l'invio sia completato. È più semplice da usare ma può bloccare l'esecuzione se la rete è lenta o il client non risponde.

    ```java
    session.getBasicRemote().sendText("Questo è un messaggio sincrono.");
    ```

- **Asincrona (non bloccante)**: L'invio del messaggio viene delegato a un thread in background, e il thread corrente prosegue immediatamente l'esecuzione. È l'approccio consigliato per applicazioni ad alte prestazioni per evitare di bloccare il thread principale.

    ```java
    session.getAsyncRemote().sendText("Questo è un messaggio asincrono.");
    ```

| Metodo Remoto         | Descrizione                                                                                             |
| --------------------- | ------------------------------------------------------------------------------------------------------- |
| `getBasicRemote()`    | Ottiene un `RemoteEndpoint.Basic`, che fornisce metodi di invio **sincroni** (`sendText`, `sendBinary`, `sendObject`). |
| `getAsyncRemote()`    | Ottiene un `RemoteEndpoint.Async`, che fornisce metodi di invio **asincroni** (`sendText`, `sendBinary`, `sendObject`). |

### 2. Gestire lo Stato della Sessione

La sessione WebSocket può memorizzare dati personalizzati, utili per associare informazioni specifiche a una connessione, come l'identità dell'utente. Questo si ottiene tramite una mappa di proprietà.

```java
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat/{username}")
public class ChatEndpoint {

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        // Memorizza l'username nella mappa delle proprietà della sessione
        session.getUserProperties().put("username", username);
        
        // Recupera l'username in un altro metodo
        String user = (String) session.getUserProperties().get("username");
        System.out.println(user + " si è connesso.");
    }
    // ...
}
```

- `session.getUserProperties()`: Restituisce una `Map<String, Object>` dove è possibile leggere e scrivere attributi legati alla sessione.

### 3. Accedere alle Informazioni della Connessione

L'oggetto `Session` espone diverse informazioni utili sulla connessione e sull'handshake iniziale.

- **ID Univoco**: Ogni sessione ha un ID univoco generato dal container.

    ```java
    String sessionId = session.getId();
    ```

- **Parametri della Query String**: È possibile accedere ai parametri passati nell'URL durante l'handshake.

    ```java
    // Per un URL tipo: ws://.../app?token=xyz123
    String token = session.getRequestParameterMap().get("token").get(0);
    ```

- **URI della Richiesta**: L'URI completo richiesto dal client.

    ```java
    java.net.URI uri = session.getRequestURI();
    ```

- **User Principal**: Se l'handshake WebSocket è avvenuto in un contesto di sicurezza Java EE e l'utente è stato autenticato, è possibile recuperare il `Principal`.

    ```java
    java.security.Principal principal = session.getUserPrincipal();
    if (principal != null) {
        String username = principal.getName();
    }
    ```

## Considerazioni sulla Sicurezza

A differenza delle sessioni HTTP, le sessioni WebSocket **non hanno un meccanismo di sicurezza integrato e standardizzato** per l'autorizzazione dopo l'handshake. L'autenticazione avviene tipicamente solo durante la richiesta di handshake iniziale.

**Pattern comuni per la gestione della sicurezza:**

1. **Autenticazione tramite Contesto di Sicurezza Java EE**: Se l'handshake HTTP passa attraverso il normale meccanismo di sicurezza dell'applicazione (es. form-based login), il `Principal` dell'utente sarà disponibile nella sessione WebSocket.
2. **Passaggio di un Token**: Il client invia un token (es. un JWT) come parametro nella query string dell'URL WebSocket (`ws://...?token=...`). Durante l'evento `@OnOpen`, il server valida il token e, se valido, associa l'identità dell'utente alla `Session`.
3. **Uso del Cookie di Sessione HTTP**: Durante l'handshake, il container può rendere disponibili i cookie della richiesta HTTP. Il server può leggere il cookie di sessione (`JSESSIONID`), recuperare la `HttpSession` associata e da lì ottenere le informazioni sull'utente autenticato. Questo approccio richiede una configurazione specifica (`ServerEndpointConfig.Configurator`).

## Tabella Riassuntiva dei Metodi `Session`

| Metodo                                | Descrizione                                                                                             |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `getId()`                             | Restituisce l'ID univoco della sessione.                                                                |
| `getBasicRemote()`                    | Ottiene l'endpoint remoto per l'invio di messaggi in modalità **sincrona**.                             |
| `getAsyncRemote()`                    | Ottiene l'endpoint remoto per l'invio di messaggi in modalità **asincrona**.                            |
| `getUserProperties()`                 | Restituisce una `Map` per memorizzare dati personalizzati associati alla sessione.                      |
| `getRequestParameterMap()`            | Restituisce una mappa dei parametri della query string presenti nell'URI della richiesta di handshake.  |
| `getRequestURI()`                     | Restituisce l'URI usato per stabilire la connessione.                                                   |
| `getUserPrincipal()`                  | Restituisce il `Principal` dell'utente se autenticato durante l'handshake.                              |
| `isOpen()`                            | Verifica se la connessione è ancora aperta.                                                             |
| `close()` / `close(CloseReason)`      | Chiude la connessione WebSocket dal lato server.                                                        |

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **`Session`**                 | L'oggetto che rappresenta una connessione WebSocket attiva tra un client e un server.                                                 |
| **Invio Sincrono**            | Un'operazione di invio che blocca il thread chiamante finché il messaggio non è stato completamente trasmesso.                         |
| **Invio Asincrono**           | Un'operazione di invio che non blocca il thread chiamante, restituendo immediatamente il controllo e completando l'invio in background.   |
| **`RemoteEndpoint`**          | L'oggetto, ottenuto dalla `Session`, che fornisce i metodi per inviare messaggi all'altro capo della connessione.                       |
| **User Properties**           | Una mappa di dati personalizzati associata a una `Session`, utile per mantenere lo stato (es. nome utente, preferenze).                 |
