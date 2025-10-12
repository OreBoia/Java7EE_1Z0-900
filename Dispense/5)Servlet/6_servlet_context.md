# Il Contesto della Servlet: `ServletContext`

L'oggetto `ServletContext` è una delle componenti fondamentali dell'API Servlet. Rappresenta l'intera applicazione web e funge da "ponte" tra una Servlet e il Web Container in cui l'applicazione è in esecuzione. Ogni applicazione web ha un solo `ServletContext`, che viene creato dal container all'avvio dell'applicazione e distrutto quando l'applicazione viene fermata.

Il `ServletContext` fornisce un modo per:

- Condividere dati tra tutte le Servlet dell'applicazione (application scope).
- Accedere a parametri di inizializzazione globali.
- Leggere risorse statiche dall'applicazione.
- Loggare eventi a livello di applicazione.

## Come Ottenere il `ServletContext`

Esistono diversi modi per ottenere un riferimento all'oggetto `ServletContext`.

1. **Dalla `HttpServletRequest`**: `request.getServletContext()`
2. **Dalla `ServletConfig` nel metodo `init()`**: `getServletConfig().getServletContext()` (o più semplicemente `getServletContext()` se si estende `HttpServlet`).
3. **Tramite Dependency Injection**: Usando l'annotazione `@Inject` (se CDI è disponibile) o `@Context` (in contesti JAX-RS, meno comune per le Servlet pure).

### Esempio di Codice: Ottenere il Contesto

```java
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/context-example")
public class ContextExampleServlet extends HttpServlet {

    private ServletContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Ottiene il contesto una volta all'inizializzazione
        this.context = config.getServletContext();
        context.log("ContextExampleServlet: inizializzata e contesto ottenuto.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Si può ottenere anche dalla richiesta, ma è lo stesso oggetto
        ServletContext requestScopeContext = request.getServletContext();
        
        response.getWriter().println("ServletContext ottenuto con successo!");
    }
}
```

## Funzionalità Principali del `ServletContext`

### 1. Parametri di Inizializzazione Globali (`context-param`)

È possibile definire parametri di configurazione globali per l'intera applicazione nel file `web.xml`. Questi parametri sono accessibili da qualsiasi Servlet.

**`web.xml`:**

```xml
<web-app ...>
    <context-param>
        <param-name>appName</param-name>
        <param-value>La Mia Fantastica Applicazione</param-value>
    </context-param>
    <context-param>
        <param-name>supportEmail</param-name>
        <param-value>support@example.com</param-value>
    </context-param>
</web-app>
```

**Codice Servlet:**

```java
String appName = getServletContext().getInitParameter("appName");
String email = getServletContext().getInitParameter("supportEmail");
// Ora puoi usare appName e email nella tua logica
```

### 2. Attributi a Livello di Applicazione (Application Scope)

Il `ServletContext` può essere usato come una mappa per condividere oggetti tra tutte le Servlet e tutte le sessioni. Questo è utile per dati globali, cache o contatori.

```java
// In una Servlet o in un Listener all'avvio dell'app
DatabaseConnectionPool pool = new DatabaseConnectionPool();
getServletContext().setAttribute("dbPool", pool);

// In un'altra Servlet
DatabaseConnectionPool pool = (DatabaseConnectionPool) getServletContext().getAttribute("dbPool");
Connection conn = pool.getConnection();
```

### 3. Logging

Il metodo `log()` del `ServletContext` scrive messaggi nel file di log del server applicativo. È il modo standard e portabile per loggare eventi a livello di applicazione.

```java
getServletContext().log("Un evento importante si è verificato.");
getServletContext().log("Errore durante l'operazione X", exception);
```

### 4. Accesso alle Risorse

È possibile leggere file presenti nel classpath dell'applicazione (es. file di configurazione, template) in modo indipendente dal file system del server.

- `InputStream getResourceAsStream(String path)`: Restituisce un `InputStream` per leggere una risorsa. Il percorso deve iniziare con `/` ed è relativo alla radice dell'applicazione web.
- `URL getResource(String path)`: Restituisce l'URL della risorsa.

```java
// Legge un file di configurazione da /WEB-INF/config/settings.properties
InputStream input = getServletContext().getResourceAsStream("/WEB-INF/config/settings.properties");
Properties props = new Properties();
props.load(input);
```

## Tabella dei Termini e Metodi Chiave

| Termine/Metodo | Descrizione |
| :--- | :--- |
| **`ServletContext`** | Oggetto che rappresenta l'intera applicazione web e fornisce un'interfaccia con il container. |
| **Application Scope** | L'ambito di vita degli attributi memorizzati nel `ServletContext`. Sono globali e condivisi da tutte le componenti dell'app. |
| **`getServletContext()`** | Metodo (su `HttpServletRequest` o `ServletConfig`) per ottenere l'istanza del `ServletContext`. |
| **`<context-param>`** | Elemento in `web.xml` per definire un parametro di inizializzazione globale per l'applicazione. |
| **`getInitParameter(String)`** | Metodo del `ServletContext` per leggere il valore di un `<context-param>`. |
| **`setAttribute(String, Object)`** | Memorizza un oggetto nell'application scope. |
| **`getAttribute(String)`** | Recupera un oggetto dall'application scope. |
| **`log(String)` / `log(String, Throwable)`** | Scrive un messaggio o un'eccezione nel log del server. |
| **`getResourceAsStream(String)`** | Ottiene un `InputStream` per leggere una risorsa statica dal percorso dell'applicazione. |
