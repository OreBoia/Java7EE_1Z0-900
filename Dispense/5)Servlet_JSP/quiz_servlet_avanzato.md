# Quiz Avanzato su Servlet - Parte 2 - Domande Miste con Codice

Questo quiz avanzato copre ulteriori concetti delle Servlet in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Configurazione Avanzata e Deployment

### 💻 Domanda 1

Osserva questa configurazione avanzata in `web.xml`:

```xml
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee">
    
    <servlet>
        <servlet-name>ConfigurableServlet</servlet-name>
        <servlet-class>com.example.ConfigurableServlet</servlet-class>
        <init-param>
            <param-name>database.url</param-name>
            <param-value>jdbc:mysql://localhost:3306/mydb</param-value>
        </init-param>
        <init-param>
            <param-name>cache.size</param-name>
            <param-value>1000</param-value>
        </init-param>
        <load-on-startup>2</load-on-startup>
        <async-supported>true</async-supported>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>ConfigurableServlet</servlet-name>
        <url-pattern>/api/v1/*</url-pattern>
    </servlet-mapping>
    
</web-app>
```

Se nell'applicazione sono presenti altre servlet con `load-on-startup` valori 1, 3 e 5, in che ordine verranno inizializzate?

- a) 1, 2, 3, 5
- b) 5, 3, 2, 1
- c) In ordine casuale
- d) 2, 1, 3, 5

---

### 🟢 Domanda 2

Quali delle seguenti affermazioni sui **pattern URL** sono corrette? (Seleziona tutte)

- a) `/api/*` cattura tutte le richieste che iniziano con `/api/`
- b) `*.json` cattura tutte le richieste che finiscono con `.json`
- c) `/` è il pattern di default che cattura tutte le richieste non gestite
- d) `/exact` cattura solo richieste per l'URL esatto `/exact`
- e) `/*` ha priorità più alta di `/api/*`

---

### 💻 Domanda 3

Analizza questa Servlet con configurazione dinamica:

```java
@WebServlet
public class DynamicConfigServlet extends HttpServlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        ServletContext context = config.getServletContext();
        
        // Registrazione dinamica di un'altra Servlet
        ServletRegistration.Dynamic registration = 
            context.addServlet("DynamicServlet", new SimpleServlet());
        
        registration.addMapping("/dynamic/*");
        registration.setInitParameter("mode", "dynamic");
        registration.setAsyncSupported(true);
        
        // Registrazione filtro dinamico
        FilterRegistration.Dynamic filterReg = 
            context.addFilter("LoggingFilter", new RequestLoggingFilter());
        filterReg.addMappingForUrlPatterns(null, true, "/dynamic/*");
    }
}
```

Quando è possibile registrare Servlet e Filtri dinamicamente?

- a) In qualsiasi momento durante l'esecuzione
- b) Solo durante la fase di inizializzazione dell'applicazione
- c) Solo nei listener `ServletContextListener`
- d) Mai, devono essere sempre configurati staticamente

---

## 2. Thread Safety e Concorrenza

### 💻 Domanda 4

Osserva questa Servlet con problemi di thread safety:

```java
@WebServlet("/counter")
public class CounterServlet extends HttpServlet {
    
    private int globalCounter = 0;
    private List<String> visitors = new ArrayList<>();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String visitorId = request.getParameter("userId");
        
        // Problema di thread safety
        globalCounter++;
        visitors.add(visitorId);
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>Visitor #" + globalCounter + "</h1>");
        out.println("<p>Total visitors: " + visitors.size() + "</p>");
    }
}
```

Quali problemi di concorrenza presenta questo codice?

- a) Solo il counter può essere inconsistente
- b) Solo la lista può essere corrotta
- c) Entrambi counter e lista possono avere problemi di concorrenza
- d) Non ci sono problemi perché ogni richiesta ha il suo thread

---

### 🟢 Domanda 5

Quali delle seguenti tecniche risolvono i problemi di thread safety nelle Servlet? (Seleziona tutte)

- a) Usare `synchronized` sui metodi critici
- b) Usare `AtomicInteger` per i contatori
- c) Usare `ConcurrentHashMap` invece di `HashMap`
- d) Memorizzare tutto in `HttpSession`
- e) Usare `ThreadLocal` per dati specifici del thread

---

### 💻 Domanda 6

Analizza questa implementazione thread-safe:

```java
@WebServlet("/safe-counter")
public class ThreadSafeCounterServlet extends HttpServlet {
    
    private final AtomicLong counter = new AtomicLong(0);
    private final ConcurrentMap<String, Long> userCounters = new ConcurrentHashMap<>();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String userId = request.getParameter("userId");
        
        long globalCount = counter.incrementAndGet();
        long userCount = userCounters.merge(userId, 1L, Long::sum);
        
        response.setContentType("application/json");
        response.getWriter().printf(
            "{\"globalCount\":%d,\"userCount\":%d}", 
            globalCount, userCount
        );
    }
}
```

Cosa fa il metodo `merge()` di `ConcurrentMap`?

- a) Sostituisce sempre il valore esistente
- b) Se la chiave non esiste la crea con valore 1, altrimenti somma 1 al valore esistente
- c) Elimina la chiave se esiste
- d) Restituisce solo il valore senza modificarlo

---

## 3. Gestione Avanzata delle Richieste

### 💻 Domanda 7

Osserva questa Servlet che gestisce diversi Content-Type:

```java
@WebServlet("/api/users")
public class ContentNegotiationServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accept = request.getHeader("Accept");
        List<User> users = getUsersFromDatabase();
        
        if (accept != null && accept.contains("application/xml")) {
            response.setContentType("application/xml");
            generateXMLResponse(users, response);
        } else if (accept != null && accept.contains("application/json")) {
            response.setContentType("application/json");
            generateJSONResponse(users, response);
        } else {
            response.setContentType("text/html");
            generateHTMLResponse(users, response);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String contentType = request.getContentType();
        
        User user;
        if ("application/json".equals(contentType)) {
            user = parseJSONUser(request.getInputStream());
        } else if ("application/xml".equals(contentType)) {
            user = parseXMLUser(request.getInputStream());
        } else {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        
        updateUser(user);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
```

Quale header HTTP viene utilizzato per la **content negotiation** nella richiesta?

- a) `Content-Type`
- b) `Accept`
- c) `Accept-Encoding`
- d) `Content-Negotiation`

---

### 🔵 Domanda 8

Nel codice precedente, quale codice di stato HTTP viene restituito per un aggiornamento utente riuscito?

- a) 200 OK
- b) 201 Created
- c) 204 No Content
- d) 202 Accepted

---

### 💻 Domanda 9

Analizza questa gestione di file streaming:

```java
@WebServlet("/download/*")
public class FileDownloadServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String fileName = pathInfo.substring(1);
        File file = new File("/secure/files", fileName);
        
        // Security check
        if (!file.exists() || !file.canRead() || fileName.contains("..")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Set headers for download
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", 
            "attachment; filename=\"" + fileName + "\"");
        
        // Stream file content
        try (FileInputStream fis = new FileInputStream(file);
             ServletOutputStream sos = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sos.write(buffer, 0, bytesRead);
            }
        }
    }
}
```

Perché si controlla `fileName.contains("..")`?

- a) Per evitare nomi di file troppo lunghi
- b) Per prevenire attacchi di path traversal
- c) Per validare l'estensione del file
- d) Per controllare caratteri speciali

---

## 4. Integrazione con WebSocket

### 💻 Domanda 10

Osserva questa Servlet che inizializza WebSocket:

```java
@WebServlet("/chat")
public class ChatInitServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String upgrade = request.getHeader("Upgrade");
        String connection = request.getHeader("Connection");
        
        if ("websocket".equalsIgnoreCase(upgrade) && 
            "Upgrade".equalsIgnoreCase(connection)) {
            
            // WebSocket handshake detected
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            
            if (userId == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Store user info for WebSocket endpoint
            request.getServletContext().setAttribute("ws.user." + userId, session);
            
            // Let WebSocket endpoint handle the upgrade
            response.setStatus(HttpServletResponse.SC_SWITCHING_PROTOCOLS);
        } else {
            // Serve chat page
            request.getRequestDispatcher("/WEB-INF/chat.jsp").forward(request, response);
        }
    }
}

@ServerEndpoint("/websocket/chat")
public class ChatWebSocketEndpoint {
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // WebSocket session opened
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle incoming message
    }
}
```

Quale codice di stato HTTP indica l'inizio dell'upgrade a WebSocket?

- a) 200 OK
- b) 101 Switching Protocols
- c) 102 Processing
- d) 201 Created

---

### 🔵 Domanda 11

Nell'esempio precedente, dove vengono memorizzate le informazioni della sessione HTTP per l'uso in WebSocket?

- a) Nella sessione WebSocket
- b) Nel ServletContext
- c) In una variabile statica
- d) Nel database

---

## 5. Listener Avanzati

### 💻 Domanda 12

Analizza questi listener per il monitoraggio dell'applicazione:

```java
@WebListener
public class SessionTrackingListener implements HttpSessionListener, 
                                               HttpSessionAttributeListener {
    
    private static final AtomicInteger activeSessions = new AtomicInteger(0);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int count = activeSessions.incrementAndGet();
        se.getSession().getServletContext().log(
            "Nuova sessione creata. Sessioni attive: " + count);
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        int count = activeSessions.decrementAndGet();
        se.getSession().getServletContext().log(
            "Sessione distrutta. Sessioni attive: " + count);
    }
    
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            event.getSession().getServletContext().log(
                "Utente loggato: " + event.getValue());
        }
    }
    
    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            event.getSession().getServletContext().log(
                "Utente disconnesso: " + event.getValue());
        }
    }
    
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        // User info updated
    }
}
```

Quando viene chiamato il metodo `attributeReplaced()`?

- a) Quando un attributo viene aggiunto per la prima volta
- b) Quando un attributo esistente viene sovrascritto con un nuovo valore
- c) Quando un attributo viene rimosso
- d) Quando la sessione viene invalidata

---

### 🟢 Domanda 13

Quali interfacce listener sono disponibili per le Servlet? (Seleziona tutte)

- a) `ServletContextListener`
- b) `HttpSessionListener`
- c) `ServletRequestListener`
- d) `HttpSessionAttributeListener`
- e) `ServletContextAttributeListener`

---

### 💻 Domanda 14

Osserva questo listener per il caricamento delle configurazioni:

```java
@WebListener
public class ConfigurationLoader implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        try {
            // Load external configuration
            Properties config = new Properties();
            String configPath = context.getInitParameter("config.file.path");
            
            if (configPath != null) {
                config.load(new FileInputStream(configPath));
            } else {
                // Load from classpath
                config.load(getClass().getResourceAsStream("/app.properties"));
            }
            
            // Store configuration in application scope
            context.setAttribute("app.config", config);
            
            // Initialize database pool
            String dbUrl = config.getProperty("database.url");
            String dbUser = config.getProperty("database.user");
            String dbPassword = config.getProperty("database.password");
            
            DataSource dataSource = createDataSource(dbUrl, dbUser, dbPassword);
            context.setAttribute("dataSource", dataSource);
            
            context.log("Configurazione caricata e database inizializzato");
            
        } catch (IOException e) {
            context.log("Errore nel caricamento della configurazione", e);
            throw new RuntimeException("Impossibile avviare l'applicazione", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        DataSource dataSource = (DataSource) context.getAttribute("dataSource");
        
        if (dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                context.log("Errore nella chiusura del DataSource", e);
            }
        }
    }
}
```

Se il caricamento della configurazione fallisce, cosa succede all'applicazione?

- a) L'applicazione continua con configurazioni di default
- b) L'applicazione non si avvia a causa della RuntimeException
- c) Solo il listener fallisce, il resto funziona
- d) Viene mostrato un errore HTTP 500

---

## 6. Filtri Avanzati

### 💻 Domanda 15

Analizza questa catena di filtri per la sicurezza:

```java
@WebFilter(urlPatterns = "/*", filterName = "AuthenticationFilter")
public class AuthenticationFilter implements Filter {
    
    private Set<String> publicPaths;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        publicPaths = Set.of("/login", "/register", "/public", "/css", "/js");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        boolean isPublicPath = publicPaths.stream().anyMatch(path::startsWith);
        
        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }
        
        // User is authenticated, proceed
        chain.doFilter(request, response);
    }
}

@WebFilter(urlPatterns = "/admin/*", filterName = "AuthorizationFilter")
public class AuthorizationFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.hasRole("ADMIN")) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

Per una richiesta a `/admin/users`, in che ordine vengono eseguiti i filtri?

- a) Solo AuthorizationFilter
- b) AuthenticationFilter poi AuthorizationFilter
- c) AuthorizationFilter poi AuthenticationFilter
- d) Dipende dall'ordine di caricamento delle classi

---

### 🟢 Domanda 16

Quali delle seguenti sono **best practices** per i filtri? (Seleziona tutte)

- a) Sempre chiamare `chain.doFilter()` alla fine
- b) Gestire le eccezioni appropriatamente
- c) Usare `@WebFilter` invece di configurazione XML quando possibile
- d) Evitare di modificare la risposta dopo `chain.doFilter()`
- e) Implementare logica di business complessa nei filtri

---

### 💻 Domanda 17

Osserva questo filtro per il caching delle risposte:

```java
@WebFilter("/api/cache/*")
public class CacheFilter implements Filter {
    
    private static final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minuti
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        if (!"GET".equals(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        
        String cacheKey = httpRequest.getRequestURI() + "?" + 
                         httpRequest.getQueryString();
        
        CachedResponse cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            // Serve from cache
            httpResponse.setContentType(cached.getContentType());
            httpResponse.getWriter().write(cached.getContent());
            return;
        }
        
        // Capture response
        CachingResponseWrapper wrapper = new CachingResponseWrapper(httpResponse);
        chain.doFilter(request, wrapper);
        
        // Cache the response
        String content = wrapper.getCapturedContent();
        String contentType = wrapper.getContentType();
        cache.put(cacheKey, new CachedResponse(content, contentType));
        
        // Send response to client
        httpResponse.getWriter().write(content);
    }
}
```

Per quali metodi HTTP viene applicato il caching?

- a) Tutti i metodi HTTP
- b) Solo GET
- c) GET e POST
- d) Solo POST

---

## 7. Gestione delle Eccezioni e Debugging

### 💻 Domanda 18

Analizza questa gestione centralizzata degli errori:

```java
@WebServlet("/error-handler")
public class ErrorHandlerServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Retrieve error information
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        // Log the error
        String logMessage = String.format(
            "Errore %d per URI %s: %s", 
            statusCode, requestUri, errorMessage
        );
        
        if (exception != null) {
            log(logMessage, exception);
        } else {
            log(logMessage);
        }
        
        // Generate error response
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Errore</title></head><body>");
        out.println("<h1>Si è verificato un errore</h1>");
        out.println("<p>Codice errore: " + statusCode + "</p>");
        
        if (request.isUserInRole("admin")) {
            out.println("<p>URI: " + requestUri + "</p>");
            out.println("<p>Messaggio: " + errorMessage + "</p>");
            if (exception != null) {
                out.println("<pre>");
                exception.printStackTrace(out);
                out.println("</pre>");
            }
        }
        
        out.println("</body></html>");
    }
}
```

Quale attributo contiene l'eccezione originale che ha causato l'errore?

- a) `RequestDispatcher.ERROR_MESSAGE`
- b) `RequestDispatcher.ERROR_EXCEPTION`
- c) `RequestDispatcher.ERROR_EXCEPTION_TYPE`
- d) `RequestDispatcher.EXCEPTION`

---

### 🔵 Domanda 19

Nel codice precedente, quando vengono mostrati i dettagli tecnici dell'errore?

- a) Sempre a tutti gli utenti
- b) Solo agli utenti con ruolo "admin"
- c) Mai, per motivi di sicurezza
- d) Solo in ambiente di sviluppo

---

### 💻 Domanda 20

Osserva questo filter per il logging delle richieste:

```java
@WebFilter("/*")
public class RequestLoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Add request ID to thread context
        MDC.put("requestId", requestId);
        
        try {
            log(String.format("[%s] Started %s %s from %s", 
                requestId,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr()
            ));
            
            ResponseWrapper wrapper = new ResponseWrapper(httpResponse);
            chain.doFilter(request, wrapper);
            
            long duration = System.currentTimeMillis() - startTime;
            log(String.format("[%s] Completed with status %d in %dms", 
                requestId,
                wrapper.getStatus(),
                duration
            ));
            
        } finally {
            MDC.clear();
        }
    }
    
    private void log(String message) {
        // Use proper logging framework
        Logger.getLogger(RequestLoggingFilter.class.getName()).info(message);
    }
}
```

A cosa serve `MDC.put("requestId", requestId)`?

- a) Memorizzare l'ID nella sessione HTTP
- b) Aggiungere l'ID al contesto di logging per il thread corrente
- c) Creare un cookie con l'ID della richiesta
- d) Inviare l'ID nel header della risposta

---

## 8. Performance e Ottimizzazione Avanzata

### 💻 Domanda 21

Analizza questa implementazione di connection pooling personalizzato:

```java
@WebListener
public class DatabasePoolListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        // Create custom connection pool
        ConnectionPool pool = new ConnectionPool();
        pool.setMaxConnections(20);
        pool.setMinConnections(5);
        pool.setConnectionTimeout(30000);
        pool.initialize();
        
        context.setAttribute("connectionPool", pool);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ConnectionPool pool = (ConnectionPool) context.getAttribute("connectionPool");
        
        if (pool != null) {
            pool.shutdown();
        }
    }
}

@WebServlet("/data/*")
public class DataAccessServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ConnectionPool pool = (ConnectionPool) getServletContext()
                                .getAttribute("connectionPool");
        
        Connection conn = null;
        try {
            conn = pool.getConnection();
            
            // Database operations
            processDataRequest(request, response, conn);
            
        } catch (SQLException e) {
            log("Database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (conn != null) {
                pool.releaseConnection(conn);
            }
        }
    }
}
```

Qual è il vantaggio principale del connection pooling?

- a) Sicurezza migliorata delle connessioni
- b) Riduzione dell'overhead di creazione/distruzione delle connessioni
- c) Migliore gestione delle transazioni
- d) Supporto per database distribuiti

---

### 🟢 Domanda 22

Quali delle seguenti sono tecniche di ottimizzazione per le Servlet? (Seleziona tutte)

- a) Usare connection pooling per il database
- b) Implementare caching a livello applicazione
- c) Minimizzare le operazioni sincronizzate
- d) Usare servlet singleton con stato condiviso
- e) Ottimizzare le query SQL

---

### 💻 Domanda 23

Osserva questa implementazione di rate limiting:

```java
@WebFilter("/api/*")
public class RateLimitingFilter implements Filter {
    
    private static final Map<String, RateLimiter> clientLimiters = new ConcurrentHashMap<>();
    private static final int REQUESTS_PER_MINUTE = 60;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientIdentifier(httpRequest);
        RateLimiter limiter = clientLimiters.computeIfAbsent(clientId, 
            k -> new RateLimiter(REQUESTS_PER_MINUTE, TimeUnit.MINUTES));
        
        if (!limiter.tryAcquire()) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.getWriter().println("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return "api-" + apiKey;
        }
        return "ip-" + request.getRemoteAddr();
    }
}
```

Quale codice di stato HTTP viene utilizzato per indicare il superamento del rate limit?

- a) 400 Bad Request
- b) 403 Forbidden
- c) 429 Too Many Requests
- d) 503 Service Unavailable

---

## 9. Sicurezza Avanzata

### 💻 Domanda 24

Analizza questa implementazione di CSRF protection:

```java
@WebFilter(urlPatterns = {"/*"})
public class CSRFProtectionFilter implements Filter {
    
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        
        if ("GET".equals(method) || "HEAD".equals(method)) {
            // Generate token for safe methods
            HttpSession session = httpRequest.getSession();
            if (session.getAttribute(CSRF_TOKEN_ATTR) == null) {
                String token = generateCSRFToken();
                session.setAttribute(CSRF_TOKEN_ATTR, token);
            }
            chain.doFilter(request, response);
            return;
        }
        
        // Validate token for state-changing methods
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        String requestToken = httpRequest.getHeader(CSRF_HEADER);
        
        if (requestToken == null) {
            requestToken = httpRequest.getParameter("_csrf");
        }
        
        if (!isValidToken(sessionToken, requestToken)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                "CSRF token validation failed");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String generateCSRFToken() {
        return UUID.randomUUID().toString();
    }
    
    private boolean isValidToken(String sessionToken, String requestToken) {
        return sessionToken != null && sessionToken.equals(requestToken);
    }
}
```

Per quali metodi HTTP viene validato il token CSRF?

- a) Solo POST
- b) Tutti i metodi tranne GET e HEAD
- c) Solo PUT e DELETE
- d) Tutti i metodi HTTP

---

### 🔵 Domanda 25

Nel codice precedente, dove può essere inviato il token CSRF?

- a) Solo nell'header `X-CSRF-Token`
- b) Solo nel parametro `_csrf`
- c) In entrambi header e parametro
- d) Solo nei cookie

---

### 💻 Domanda 26

Osserva questa implementazione di Content Security Policy:

```java
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://cdn.example.com; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self' https://api.example.com; " +
            "frame-ancestors 'none';");
        
        // Other security headers
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        chain.doFilter(request, response);
    }
}
```

Cosa previene l'header `X-Frame-Options: DENY`?

- a) Attacchi XSS
- b) Clickjacking
- c) CSRF
- d) SQL Injection

---

## 10. Testing e Debugging

### 💻 Domanda 27

Analizza questo servlet per il testing:

```java
@WebServlet("/test/mock-service")
public class MockServiceServlet extends HttpServlet {
    
    private static final Map<String, String> mockResponses = Map.of(
        "/users", "[{\"id\":1,\"name\":\"John\"},{\"id\":2,\"name\":\"Jane\"}]",
        "/orders", "[{\"id\":100,\"userId\":1,\"amount\":99.99}]",
        "/products", "[{\"id\":1,\"name\":\"Product A\",\"price\":29.99}]"
    );
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String mockPath = request.getParameter("path");
        String delay = request.getParameter("delay");
        String error = request.getParameter("error");
        
        // Simulate delay
        if (delay != null) {
            try {
                Thread.sleep(Integer.parseInt(delay));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Simulate error
        if (error != null) {
            int errorCode = Integer.parseInt(error);
            response.sendError(errorCode, "Simulated error");
            return;
        }
        
        // Return mock data
        String mockData = mockResponses.get(mockPath);
        if (mockData != null) {
            response.setContentType("application/json");
            response.getWriter().println(mockData);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
```

A cosa serve questo servlet mock?

- a) Produzione di API reali
- b) Testing di client senza server reale
- c) Caching delle risposte
- d) Logging delle richieste

---

### 🟢 Domanda 28

Quali delle seguenti sono **best practices** per il testing delle Servlet? (Seleziona tutte)

- a) Usare servlet mock per testare i client
- b) Testare la logica di business separatamente dalle servlet
- c) Utilizzare framework come JUnit e Mockito
- d) Testare solo in ambiente di produzione
- e) Creare servlet dedicate per simulare errori

---

### 💻 Domanda 29

Osserva questa servlet per il profiling delle performance:

```java
@WebServlet("/profile/*")
public class ProfilingServlet extends HttpServlet {
    
    private static final Map<String, PerformanceMetrics> metrics = 
        new ConcurrentHashMap<>();
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String operation = request.getPathInfo();
        long startTime = System.nanoTime();
        
        try {
            super.service(request, response);
        } finally {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            
            metrics.compute(operation, (key, existing) -> {
                if (existing == null) {
                    return new PerformanceMetrics(1, duration, duration, duration);
                } else {
                    return existing.addMeasurement(duration);
                }
            });
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if ("/stats".equals(request.getPathInfo())) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            
            out.println("{");
            boolean first = true;
            for (Map.Entry<String, PerformanceMetrics> entry : metrics.entrySet()) {
                if (!first) out.println(",");
                out.printf("\"%s\": %s", entry.getKey(), entry.getValue().toJson());
                first = false;
            }
            out.println("}");
        } else {
            // Simulate business logic
            simulateWork();
            response.getWriter().println("Operation completed");
        }
    }
}
```

Perché si usa `System.nanoTime()` invece di `System.currentTimeMillis()`?

- a) Per avere timestamp assoluti
- b) Per misurazioni di performance ad alta precisione
- c) Per compatibilità con diversi fusi orari
- d) Per ridurre l'uso di memoria

---

### 🔵 Domanda 30

Nel codice precedente, quando vengono registrate le metriche di performance?

- a) Solo per richieste GET
- b) Solo per richieste che generano errori
- c) Per tutte le richieste HTTP
- d) Solo per richieste che durano più di 1 secondo

---

---

## Risposte Corrette

### 1. **a)** 1, 2, 3, 5

Le servlet con `load-on-startup` vengono caricate in ordine crescente del valore numerico.

### 2. **a, b, c, d)** Tutti tranne `/*` ha priorità più alta

`/*` ha priorità più bassa dei pattern specifici come `/api/*`.

### 3. **b)** Solo durante la fase di inizializzazione dell'applicazione

La registrazione dinamica è possibile solo durante `contextInitialized()` o `init()`.

### 4. **c)** Entrambi counter e lista possono avere problemi di concorrenza

Sia `int++` che `ArrayList.add()` non sono thread-safe.

### 5. **a, b, c, e)** Sincronizzazione, Atomic, ConcurrentHashMap, ThreadLocal

Memorizzare tutto in sessione non risolve i problemi di concorrenza globali.

### 6. **b)** Se la chiave non esiste la crea con valore 1, altrimenti somma 1 al valore esistente

`merge()` applica la funzione se la chiave esiste, altrimenti usa il valore di default.

### 7. **b)** `Accept`

L'header `Accept` indica i tipi di contenuto che il client può gestire.

### 8. **c)** 204 No Content

204 indica successo senza contenuto nel corpo della risposta.

### 9. **b)** Per prevenire attacchi di path traversal

`..` può essere usato per accedere a file fuori dalla directory autorizzata.

### 10. **b)** 101 Switching Protocols

101 è il codice standard per l'upgrade di protocollo, incluso WebSocket.

### 11. **b)** Nel ServletContext

Le informazioni vengono memorizzate nel `ServletContext` per accesso globale.

### 12. **b)** Quando un attributo esistente viene sovrascritto con un nuovo valore

`attributeReplaced()` viene chiamato quando si imposta un nuovo valore per un attributo esistente.

### 13. **a, b, c, d, e)** Tutti i listener elencati sono disponibili

Java EE fornisce listener per contesto, sessioni, richieste e attributi.

### 14. **b)** L'applicazione non si avvia a causa della RuntimeException

La `RuntimeException` in `contextInitialized()` impedisce l'avvio dell'applicazione.

### 15. **b)** AuthenticationFilter poi AuthorizationFilter

I filtri vengono eseguiti nell'ordine in cui matchano i pattern URL.

### 16. **b, c, d)** Gestire eccezioni, usare annotazioni, non modificare dopo chain

Non sempre si deve chiamare `chain.doFilter()` - i filtri possono bloccare richieste.

### 17. **b)** Solo GET

Il filtro controlla esplicitamente il metodo HTTP ed esegue il caching solo per GET.

### 18. **b)** `RequestDispatcher.ERROR_EXCEPTION`

`ERROR_EXCEPTION` contiene l'eccezione originale che ha causato l'errore.

### 19. **b)** Solo agli utenti con ruolo "admin"

I dettagli tecnici vengono mostrati solo se `request.isUserInRole("admin")` è true.

### 20. **b)** Aggiungere l'ID al contesto di logging per il thread corrente

MDC (Mapped Diagnostic Context) permette di aggiungere informazioni al contesto di logging.

### 21. **b)** Riduzione dell'overhead di creazione/distruzione delle connessioni

Il connection pooling riutilizza connessioni esistenti evitando il costo di creazione/chiusura.

### 22. **a, b, c, e)** Connection pooling, caching, minimizzare sincronizzazione, ottimizzare SQL

Usare servlet singleton con stato condiviso causa problemi di thread safety.

### 23. **c)** 429 Too Many Requests

429 è il codice HTTP standard per indicare il superamento dei limiti di rate.

### 24. **b)** Tutti i metodi tranne GET e HEAD

Il filtro controlla il token CSRF per tutti i metodi che possono modificare lo stato.

### 25. **c)** In entrambi header e parametro

Il codice controlla prima l'header `X-CSRF-Token`, poi il parametro `_csrf`.

### 26. **b)** Clickjacking

`X-Frame-Options: DENY` previene l'incorporamento della pagina in frame/iframe.

### 27. **b)** Testing di client senza server reale

Il servlet mock simula risposte di servizi esterni per facilitare il testing.

### 28. **a, b, c, e)** Mock per testing, separare logica, JUnit/Mockito, servlet per errori

Non si dovrebbe testare solo in produzione - i test devono essere fatti in tutti gli ambienti.

### 29. **b)** Per misurazioni di performance ad alta precisione

`nanoTime()` fornisce precisione maggiore ed è progettato per misurare intervalli di tempo.

### 30. **c)** Per tutte le richieste HTTP

Il metodo `service()` viene chiamato per tutte le richieste HTTP, quindi le metriche vengono registrate sempre.
