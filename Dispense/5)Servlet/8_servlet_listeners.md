# Servlet Listeners in Java EE 7

## Introduzione ai Listeners

I **Listeners** in Java EE 7 sono componenti che implementano il pattern **Observer/Callback** e permettono di intercettare e reagire a specifici eventi che accadono durante il ciclo di vita di un'applicazione web. Il container (server applicativo) notifica automaticamente i listener quando si verificano eventi di interesse.

### Vantaggi dei Listeners
- **Separazione delle responsabilità**: Separano la logica di business dalla gestione degli eventi
- **Centralizzazione della logica**: Permettono di centralizzare operazioni comuni come inizializzazione risorse, logging, monitoraggio
- **Non invasività**: Non richiedono modifiche al codice esistente delle servlet o JSP

## Tipi di Eventi Intercettabili

I listener possono intercettare eventi relativi a:

1. **Contesto Web (ServletContext)**
   - Avvio/distruzione dell'applicazione
   - Modifica degli attributi del contesto

2. **Richieste HTTP (ServletRequest)**
   - Inizio/fine di una richiesta
   - Modifica degli attributi della richiesta

3. **Sessioni HTTP (HttpSession)**
   - Creazione/distruzione di sessioni
   - Modifica degli attributi della sessione
   - Passivazione/attivazione di sessioni (clustering)

## Tipi di Listeners

### 1. ServletContextListener
Intercetta eventi di avvio e distruzione dell'applicazione web.

```java
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class ApplicationStartupListener implements ServletContextListener {
    
    private static final Logger logger = Logger.getLogger(ApplicationStartupListener.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Applicazione web avviata");
        
        // Inizializzazione risorse globali
        DatabaseConnectionPool pool = new DatabaseConnectionPool();
        sce.getServletContext().setAttribute("dbPool", pool);
        
        // Caricamento configurazioni
        loadApplicationProperties(sce.getServletContext());
        
        logger.info("Risorse dell'applicazione inizializzate correttamente");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Applicazione web in fase di spegnimento");
        
        // Cleanup delle risorse
        DatabaseConnectionPool pool = (DatabaseConnectionPool) 
            sce.getServletContext().getAttribute("dbPool");
        if (pool != null) {
            pool.close();
        }
        
        logger.info("Risorse dell'applicazione rilasciate");
    }
    
    private void loadApplicationProperties(ServletContext context) {
        // Caricamento configurazioni dal file properties
        Properties props = new Properties();
        // ... caricamento configurazioni
        context.setAttribute("appConfig", props);
    }
}
```

### 2. ServletContextAttributeListener
Intercetta modifiche agli attributi del ServletContext.

```java
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class ContextAttributeListener implements ServletContextAttributeListener {
    
    private static final Logger logger = Logger.getLogger(ContextAttributeListener.class.getName());
    
    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        logger.info(String.format("Attributo aggiunto al contesto: %s = %s", 
            event.getName(), event.getValue()));
    }
    
    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        logger.info(String.format("Attributo rimosso dal contesto: %s", 
            event.getName()));
    }
    
    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        logger.info(String.format("Attributo sostituito nel contesto: %s, vecchio valore: %s, nuovo valore: %s", 
            event.getName(), event.getValue(), 
            event.getServletContext().getAttribute(event.getName())));
    }
}
```

### 3. ServletRequestListener
Intercetta inizio e fine delle richieste HTTP.

```java
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

@WebListener
public class RequestTrackingListener implements ServletRequestListener {
    
    private static final Logger logger = Logger.getLogger(RequestTrackingListener.class.getName());
    
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        
        // Logging della richiesta
        String requestInfo = String.format("Nuova richiesta: %s %s da %s", 
            request.getMethod(), 
            request.getRequestURI(), 
            request.getRemoteAddr());
        
        logger.info(requestInfo);
        
        // Timestamp di inizio richiesta
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        
        // Contatore richieste (esempio di monitoraggio)
        incrementRequestCounter(sre.getServletContext());
    }
    
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        
        // Calcolo tempo di elaborazione
        Long startTime = (Long) request.getAttribute("requestStartTime");
        if (startTime != null) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.info(String.format("Richiesta completata in %d ms: %s", 
                processingTime, request.getRequestURI()));
        }
    }
    
    private void incrementRequestCounter(ServletContext context) {
        Integer counter = (Integer) context.getAttribute("requestCount");
        if (counter == null) {
            counter = 0;
        }
        context.setAttribute("requestCount", counter + 1);
    }
}
```

### 4. ServletRequestAttributeListener
Intercetta modifiche agli attributi delle richieste.

```java
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class RequestAttributeListener implements ServletRequestAttributeListener {
    
    private static final Logger logger = Logger.getLogger(RequestAttributeListener.class.getName());
    
    @Override
    public void attributeAdded(ServletRequestAttributeEvent srae) {
        if (isSecurityAttribute(srae.getName())) {
            logger.warning(String.format("Attributo di sicurezza aggiunto: %s", srae.getName()));
        }
    }
    
    @Override
    public void attributeRemoved(ServletRequestAttributeEvent srae) {
        logger.fine(String.format("Attributo rimosso dalla richiesta: %s", srae.getName()));
    }
    
    @Override
    public void attributeReplaced(ServletRequestAttributeEvent srae) {
        if (isSecurityAttribute(srae.getName())) {
            logger.warning(String.format("Attributo di sicurezza modificato: %s", srae.getName()));
        }
    }
    
    private boolean isSecurityAttribute(String attributeName) {
        return attributeName.startsWith("security.") || 
               attributeName.equals("user") || 
               attributeName.equals("role");
    }
}
```

### 5. HttpSessionListener
Intercetta creazione e distruzione delle sessioni HTTP.

```java
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@WebListener
public class SessionTrackingListener implements HttpSessionListener {
    
    private static final Logger logger = Logger.getLogger(SessionTrackingListener.class.getName());
    private static final AtomicInteger activeSessions = new AtomicInteger(0);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int sessionCount = activeSessions.incrementAndGet();
        
        logger.info(String.format("Nuova sessione creata: %s. Sessioni attive: %d", 
            se.getSession().getId(), sessionCount));
        
        // Impostazione timeout sessione
        se.getSession().setMaxInactiveInterval(30 * 60); // 30 minuti
        
        // Salvataggio timestamp creazione
        se.getSession().setAttribute("createdAt", System.currentTimeMillis());
        
        // Aggiornamento statistiche nel contesto
        updateSessionStatistics(se, sessionCount);
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        int sessionCount = activeSessions.decrementAndGet();
        
        // Calcolo durata sessione
        Long createdAt = (Long) se.getSession().getAttribute("createdAt");
        if (createdAt != null) {
            long duration = (System.currentTimeMillis() - createdAt) / 1000; // in secondi
            logger.info(String.format("Sessione distrutta: %s (durata: %d secondi). Sessioni attive: %d", 
                se.getSession().getId(), duration, sessionCount));
        }
        
        // Cleanup risorse associate alla sessione
        cleanupSessionResources(se);
        
        updateSessionStatistics(se, sessionCount);
    }
    
    private void cleanupSessionResources(HttpSessionEvent se) {
        // Cleanup di eventuali risorse associate alla sessione
        // Esempio: chiusura connessioni, rilascio lock, etc.
    }
    
    private void updateSessionStatistics(HttpSessionEvent se, int activeCount) {
        se.getSession().getServletContext().setAttribute("activeSessionCount", activeCount);
    }
}
```

### 6. HttpSessionAttributeListener
Intercetta modifiche agli attributi delle sessioni.

```java
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.util.logging.Logger;

@WebListener
public class SessionAttributeListener implements HttpSessionAttributeListener {
    
    private static final Logger logger = Logger.getLogger(SessionAttributeListener.class.getName());
    
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            logger.info(String.format("Utente loggato: %s nella sessione: %s", 
                event.getValue(), event.getSession().getId()));
            
            // Tracking login
            trackUserLogin(event);
        }
    }
    
    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            logger.info(String.format("Utente disconnesso: %s dalla sessione: %s", 
                event.getValue(), event.getSession().getId()));
            
            // Tracking logout
            trackUserLogout(event);
        }
    }
    
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        logger.fine(String.format("Attributo sessione sostituito: %s in sessione: %s", 
            event.getName(), event.getSession().getId()));
    }
    
    private void trackUserLogin(HttpSessionBindingEvent event) {
        // Implementazione tracking login
        // Esempio: salvataggio in database, aggiornamento contatori, etc.
    }
    
    private void trackUserLogout(HttpSessionBindingEvent event) {
        // Implementazione tracking logout
        // Esempio: aggiornamento ultimo accesso, pulizia dati temporanei, etc.
    }
}
```

### 7. HttpSessionActivationListener
Intercetta passivazione/attivazione delle sessioni (utile per clustering).

```java
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import java.io.Serializable;
import java.util.logging.Logger;

public class ClusterSessionListener implements HttpSessionActivationListener, Serializable {
    
    private static final Logger logger = Logger.getLogger(ClusterSessionListener.class.getName());
    private transient String resourceId; // risorsa non serializzabile
    
    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        logger.info(String.format("Sessione in fase di passivazione: %s", se.getSession().getId()));
        
        // Rilascio risorse non serializzabili prima della passivazione
        if (resourceId != null) {
            releaseResource(resourceId);
            resourceId = null;
        }
    }
    
    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        logger.info(String.format("Sessione riattivata: %s", se.getSession().getId()));
        
        // Ricostruzione risorse non serializzabili dopo attivazione
        resourceId = acquireResource();
    }
    
    private void releaseResource(String resourceId) {
        // Implementazione rilascio risorsa
    }
    
    private String acquireResource() {
        // Implementazione acquisizione risorsa
        return "resource_" + System.currentTimeMillis();
    }
}
```

## Configurazione dei Listeners

### 1. Configurazione tramite Annotazioni (Java EE 6+)

```java
@WebListener
public class MyListener implements ServletContextListener {
    // implementazione...
}
```

### 2. Configurazione tramite web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
         http://java.sun.com/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

    <!-- ServletContextListener -->
    <listener>
        <listener-class>com.example.ApplicationStartupListener</listener-class>
    </listener>
    
    <!-- HttpSessionListener -->
    <listener>
        <listener-class>com.example.SessionTrackingListener</listener-class>
    </listener>
    
    <!-- ServletRequestListener -->
    <listener>
        <listener-class>com.example.RequestTrackingListener</listener-class>
    </listener>

</web-app>
```

## Esempio Pratico Completo: Sistema di Monitoraggio

```java
@WebListener
public class ApplicationMonitoringListener implements 
    ServletContextListener, HttpSessionListener, ServletRequestListener {
    
    private static final Logger logger = Logger.getLogger(ApplicationMonitoringListener.class.getName());
    
    // Statistiche applicazione
    private static class AppStats {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicInteger activeSessions = new AtomicInteger(0);
        private final AtomicLong totalSessions = new AtomicLong(0);
        private final long startTime = System.currentTimeMillis();
        
        // Getters
        public long getTotalRequests() { return totalRequests.get(); }
        public int getActiveSessions() { return activeSessions.get(); }
        public long getTotalSessions() { return totalSessions.get(); }
        public long getUptime() { return System.currentTimeMillis() - startTime; }
    }
    
    private AppStats stats;
    
    // === ServletContextListener ===
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        stats = new AppStats();
        sce.getServletContext().setAttribute("appStats", stats);
        
        logger.info("Sistema di monitoraggio inizializzato");
        
        // Avvio thread di reporting periodico
        startPeriodicReporting(sce.getServletContext());
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info(String.format("Applicazione terminata - Statistiche finali: " +
            "Richieste totali: %d, Sessioni totali: %d, Uptime: %d ms",
            stats.getTotalRequests(), stats.getTotalSessions(), stats.getUptime()));
    }
    
    // === HttpSessionListener ===
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        stats.activeSessions.incrementAndGet();
        stats.totalSessions.incrementAndGet();
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        stats.activeSessions.decrementAndGet();
    }
    
    // === ServletRequestListener ===
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        long requestId = stats.totalRequests.incrementAndGet();
        sre.getServletRequest().setAttribute("requestId", requestId);
        sre.getServletRequest().setAttribute("requestStartTime", System.currentTimeMillis());
    }
    
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        Long startTime = (Long) sre.getServletRequest().getAttribute("requestStartTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 5000) { // Log richieste lente (>5s)
                Long requestId = (Long) sre.getServletRequest().getAttribute("requestId");
                logger.warning(String.format("Richiesta lenta #%d completata in %d ms", 
                    requestId, duration));
            }
        }
    }
    
    private void startPeriodicReporting(ServletContext context) {
        // Timer per reporting periodico delle statistiche
        java.util.Timer timer = new java.util.Timer("MonitoringReporter", true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                logger.info(String.format("Stato applicazione - " +
                    "Richieste: %d, Sessioni attive: %d, Uptime: %d minuti",
                    stats.getTotalRequests(), 
                    stats.getActiveSessions(),
                    stats.getUptime() / (1000 * 60)));
            }
        }, 60000, 300000); // Ogni 5 minuti dopo 1 minuto
        
        context.setAttribute("monitoringTimer", timer);
    }
}
```

## Servlet per Visualizzare le Statistiche

```java
@WebServlet("/admin/stats")
public class StatsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Recupero statistiche dal contesto
        Object statsObj = getServletContext().getAttribute("appStats");
        Integer requestCount = (Integer) getServletContext().getAttribute("requestCount");
        Integer activeSessionCount = (Integer) getServletContext().getAttribute("activeSessionCount");
        
        // Creazione JSON response
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"timestamp\": ").append(System.currentTimeMillis()).append(",");
        json.append("\"requestCount\": ").append(requestCount != null ? requestCount : 0).append(",");
        json.append("\"activeSessionCount\": ").append(activeSessionCount != null ? activeSessionCount : 0);
        
        if (statsObj instanceof ApplicationMonitoringListener.AppStats) {
            // Se disponibili, aggiungi statistiche dettagliate
        }
        
        json.append("}");
        
        response.getWriter().write(json.toString());
    }
}
```

## Best Practices

### 1. Gestione delle Eccezioni
```java
@Override
public void contextInitialized(ServletContextEvent sce) {
    try {
        // Inizializzazione risorse
        initializeResources(sce.getServletContext());
    } catch (Exception e) {
        logger.severe("Errore durante l'inizializzazione: " + e.getMessage());
        // Non rilanciare l'eccezione per evitare di bloccare l'avvio
    }
}
```

### 2. Thread Safety
```java
public class ThreadSafeListener implements ServletContextListener {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Object> resources = new ConcurrentHashMap<>();
    
    // Utilizzo di strutture dati thread-safe
}
```

### 3. Cleanup delle Risorse
```java
@Override
public void contextDestroyed(ServletContextEvent sce) {
    // Sempre effettuare cleanup delle risorse allocate
    ExecutorService executor = (ExecutorService) sce.getServletContext().getAttribute("executor");
    if (executor != null) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

## Considerazioni Avanzate

### Ordinamento dei Listeners
I listener vengono eseguiti nell'ordine di registrazione. Per controllare l'ordine:

```xml
<!-- In web.xml, l'ordine di dichiarazione determina l'ordine di esecuzione -->
<listener>
    <listener-class>com.example.FirstListener</listener-class>
</listener>
<listener>
    <listener-class>com.example.SecondListener</listener-class>
</listener>
```

### Utilizzo con CDI
```java
@WebListener
public class CDIIntegratedListener implements ServletContextListener {
    
    @Inject
    private ConfigurationService configService;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // I CDI beans sono disponibili nei listener
        String config = configService.getProperty("app.name");
        sce.getServletContext().setAttribute("appName", config);
    }
}
```
## Filters vs Listeners

Sebbene sia i **Filters** che i **Listeners** permettano di intercettare eventi in un'applicazione web, i loro scopi e meccanismi sono fondamentalmente diversi.

| Caratteristica | Servlet Filter | Servlet Listener |
| :--- | :--- | :--- |
| **Scopo Principale** | Intercettare e processare richieste e risposte. | Reagire a eventi del ciclo di vita del container. |
| **Interazione** | Può modificare la richiesta e la risposta. Può bloccare la catena di elaborazione (non inoltrando la richiesta). | Non può modificare il flusso della richiesta/risposta. È un osservatore passivo. |
| **Oggetto di Intervento** | `ServletRequest`, `ServletResponse` | `ServletContext`, `HttpSession`, `ServletRequest` (come oggetti evento) |
| **Catena di Esecuzione** | Parte di una catena (`FilterChain`). L'ordine è cruciale. | Notificato direttamente dal container. L'ordine è definito dalla configurazione. |
| **Casi d'Uso Tipici** | Autenticazione, logging delle richieste, compressione, crittografia, trasformazione dei dati (XSLT). | Inizializzazione di pool di connessioni, caricamento configurazioni, monitoraggio delle sessioni attive, cleanup delle risorse. |

### Quando Usare Cosa?

-   **Usa un Filter** quando hai bisogno di **pre-processare una richiesta** o **post-processare una risposta**. I filtri sono ideali per compiti che si applicano a più servlet/JSP, come la sicurezza (`AuthorizationFilter`), la compressione dei dati (`GzipFilter`) o la manipolazione degli header. Sono parte attiva del flusso richiesta-risposta.

-   **Usa un Listener** quando hai bisogno di **reagire a un cambiamento di stato** nell'applicazione, come l'avvio del server, la creazione di una sessione o l'arrivo di una nuova richiesta. I listener sono perfetti per la gestione di risorse globali, il monitoraggio e le statistiche. Sono osservatori passivi degli eventi del container.
## Conclusioni

I Servlet Listener in Java EE 7 offrono un meccanismo potente per:

- **Monitoraggio**: Tracking di performance e utilizzo
- **Inizializzazione**: Setup di risorse globali e configurazioni
- **Sicurezza**: Auditing e controllo accessi
- **Cleanup**: Rilascio pulito delle risorse
- **Integrazione**: Collegamento con sistemi esterni

Utilizzando i listener appropriati, è possibile creare applicazioni web più robuste, monitorate e facilmente manutenibili, separando la logica di gestione degli eventi dalla logica di business principale.