# Quiz Avanzato su Servlet - Domande Miste con Codice

Questo quiz avanzato copre i concetti delle Servlet in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Ciclo di Vita e Configurazione delle Servlet

### 🔵 Domanda 1

Osserva il seguente codice di una Servlet:

```java
@WebServlet("/user-manager")
public class UserManagerServlet extends HttpServlet {
    
    private DatabaseConnection dbConnection;
    
    @Override
    public void init() throws ServletException {
        super.init();
        dbConnection = new DatabaseConnection();
        System.out.println("UserManagerServlet inizializzata");
    }
    
    @Override
    public void destroy() {
        if (dbConnection != null) {
            dbConnection.close();
        }
        System.out.println("UserManagerServlet distrutta");
    }
}
```

Quante volte viene chiamato il metodo `init()` durante il ciclo di vita dell'applicazione?

- a) Ad ogni richiesta HTTP
- b) Una sola volta, all'avvio dell'applicazione
- c) Una volta per ogni sessione utente
- d) Dipende dal numero di thread concorrenti

---

### 🟢 Domanda 2

Quali delle seguenti affermazioni sul ciclo di vita delle Servlet sono corrette? (Seleziona tutte quelle corrette)

- a) Il container crea una sola istanza per ogni classe Servlet
- b) Il metodo `service()` viene eseguito in thread separati per ogni richiesta
- c) Il metodo `destroy()` viene chiamato prima dello shutdown dell'applicazione
- d) Il costruttore della Servlet viene chiamato ad ogni richiesta
- e) Gli oggetti `HttpServletRequest` e `HttpServletResponse` sono thread-safe

---

### 💻 Domanda 3

Analizza questa configurazione di mapping:

```java
@WebServlet(
    urlPatterns = {"/api/users/*", "/users"},
    loadOnStartup = 1,
    initParams = {
        @WebInitParam(name = "maxUsers", value = "100"),
        @WebInitParam(name = "cacheTimeout", value = "300")
    }
)
public class UserAPIServlet extends HttpServlet {
    
    private int maxUsers;
    
    @Override
    public void init() throws ServletException {
        String maxUsersParam = getInitParameter("maxUsers");
        maxUsers = Integer.parseInt(maxUsersParam);
    }
}
```

Cosa significa `loadOnStartup = 1`?

- a) La Servlet può gestire massimo 1 richiesta concorrente
- b) La Servlet viene inizializzata all'avvio dell'applicazione con priorità 1
- c) La Servlet ha un timeout di 1 secondo
- d) La Servlet viene ricaricata ogni 1 minuto

---

### 🔵 Domanda 4

In una configurazione `web.xml`, quale elemento **NON** è obbligatorio per definire una Servlet?

- a) `<servlet-name>`
- b) `<servlet-class>`
- c) `<load-on-startup>`
- d) `<servlet-mapping>`

---

## 2. Gestione delle Richieste HTTP

### 💻 Domanda 5

Osserva questo metodo di gestione di una richiesta POST:

```java
@WebServlet("/user/create")
public class CreateUserServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String[] hobbies = request.getParameterValues("hobbies");
        
        if (username == null || username.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                "Username è obbligatorio");
            return;
        }
        
        // Logica di creazione utente...
        response.setContentType("application/json");
        response.getWriter().println("{\"status\":\"created\",\"id\":123}");
    }
}
```

Se il form HTML invia `hobbies=calcio&hobbies=tennis&hobbies=lettura`, cosa conterrà l'array `hobbies`?

- a) `null`
- b) `["calcio"]`
- c) `["calcio", "tennis", "lettura"]`
- d) Una stringa concatenata "calcio,tennis,lettura"

---

### 🟢 Domanda 6

Quali metodi HTTP sono gestiti di default da `HttpServlet`? (Seleziona tutti)

- a) GET
- b) POST
- c) PUT
- d) DELETE
- e) HEAD
- f) OPTIONS

---

### 💻 Domanda 7

Analizza questa gestione di upload di file:

```java
@WebServlet("/file-upload")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // 5MB
public class FileUploadServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Part filePart = request.getPart("document");
        String fileName = filePart.getSubmittedFileName();
        
        if (fileName != null && !fileName.isEmpty()) {
            String uploadPath = getServletContext().getRealPath("/uploads/");
            filePart.write(uploadPath + fileName);
            
            response.getWriter().println("File caricato: " + fileName);
        }
    }
}
```

Quale annotazione è **obbligatoria** per gestire upload multipart?

- a) `@WebServlet`
- b) `@MultipartConfig`
- c) `@FileUpload`
- d) `@PostConstruct`

---

## 3. Integrazione CDI e Gestione delle Sessioni

### 💻 Domanda 8

Osserva questa Servlet che usa CDI:

```java
@WebServlet("/order/process")
public class OrderProcessingServlet extends HttpServlet {
    
    @Inject
    private OrderService orderService;
    
    @Inject
    private UserSessionBean userSession;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String userId = userSession.getCurrentUserId();
        String productId = request.getParameter("productId");
        
        if (userId == null) {
            response.sendRedirect("/login");
            return;
        }
        
        Order order = orderService.createOrder(userId, productId);
        request.setAttribute("order", order);
        request.getRequestDispatcher("/order-confirmation.jsp").forward(request, response);
    }
}
```

Quale scope CDI è più appropriato per `UserSessionBean`?

- a) `@RequestScoped`
- b) `@SessionScoped`
- c) `@ApplicationScoped`
- d) `@Dependent`

---

### 🔵 Domanda 9

Quale differenza c'è tra `RequestDispatcher.forward()` e `HttpServletResponse.sendRedirect()`?

- a) `forward()` causa una nuova richiesta HTTP, `sendRedirect()` no
- b) `forward()` mantiene la stessa richiesta, `sendRedirect()` causa una nuova richiesta HTTP
- c) Non c'è differenza, sono sinonimi
- d) `forward()` funziona solo con JSP, `sendRedirect()` solo con HTML

---

### 💻 Domanda 10

Analizza questa gestione di sessione:

```java
@WebServlet("/cart/add")
public class ShoppingCartServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        List<String> cart = (List<String>) session.getAttribute("cart");
        
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        
        String productId = request.getParameter("productId");
        cart.add(productId);
        
        session.setMaxInactiveInterval(30 * 60); // 30 minuti
        
        response.getWriter().println("Prodotto aggiunto al carrello");
    }
}
```

Cosa succede se si chiama `request.getSession(false)` quando non esiste una sessione?

- a) Viene creata una nuova sessione
- b) Viene restituito `null`
- c) Viene lanciata un'eccezione
- d) Viene restituita la sessione predefinita

---

## 4. Gestione degli Errori

### 🔵 Domanda 11

In `web.xml`, quale configurazione ha **priorità** più alta?

```xml
<error-page>
    <error-code>404</error-code>
    <location>/error-404.jsp</location>
</error-page>

<error-page>
    <exception-type>java.lang.Exception</exception-type>
    <location>/error-generic.jsp</location>
</error-page>
```

- a) La configurazione per il codice di errore 404
- b) La configurazione per l'eccezione generica
- c) Dipende dall'ordine nel file XML
- d) Entrambe vengono applicate contemporaneamente

---

### 💻 Domanda 12

Osserva questa gestione di errori personalizzata:

```java
@WebServlet("/data/user")
public class UserDataServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String userId = request.getParameter("id");
            if (userId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Parametro 'id' mancante");
                return;
            }
            
            User user = findUser(userId);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Utente non trovato");
                return;
            }
            
            // Risposta normale...
            
        } catch (DatabaseException e) {
            log("Errore database per utente: " + request.getParameter("id"), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Errore interno del server");
        }
    }
}
```

Quale codice di stato HTTP viene inviato se il parametro `id` è presente ma l'utente non esiste?

- a) 400 Bad Request
- b) 404 Not Found
- c) 500 Internal Server Error
- d) 200 OK

---

### 🟢 Domanda 13

Quali delle seguenti sono **best practices** per la gestione degli errori? (Seleziona tutte)

- a) Non mostrare mai stack trace agli utenti finali
- b) Loggare sempre i dettagli degli errori per il debugging
- c) Usare codici di stato HTTP appropriati
- d) Restituire sempre codice 200 e gestire errori in JavaScript
- e) Creare pagine di errore personalizzate user-friendly

---

## 5. ServletContext e Condivisione di Dati

### 💻 Domanda 14

Analizza questo uso del ServletContext:

```java
@WebServlet("/admin/stats")
public class StatsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ServletContext context = getServletContext();
        
        // Increment visit counter
        Integer visitCount = (Integer) context.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        visitCount++;
        context.setAttribute("visitCount", visitCount);
        
        // Get application info
        String appVersion = context.getInitParameter("app.version");
        String serverInfo = context.getServerInfo();
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("Visite totali: " + visitCount);
        out.println("Versione app: " + appVersion);
        out.println("Server: " + serverInfo);
    }
}
```

I dati memorizzati in `ServletContext` sono condivisi tra:

- a) Solo le richieste della stessa sessione
- b) Solo le richieste dello stesso utente
- c) Tutte le richieste di tutte le Servlet dell'applicazione
- d) Solo le richieste della stessa Servlet

---

### 🔵 Domanda 15

Quale metodo del `ServletContext` viene utilizzato per leggere parametri di inizializzazione configurati in `web.xml`?

- a) `getAttribute(String name)`
- b) `getInitParameter(String name)`
- c) `getParameter(String name)`
- d) `getContextParameter(String name)`

---

### 💻 Domanda 16

Osserva questo listener per monitorare il ciclo di vita dell'applicazione:

```java
@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        // Inizializzazione risorse globali
        DatabasePool pool = new DatabasePool();
        context.setAttribute("dbPool", pool);
        
        context.log("Applicazione inizializzata con DB pool");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        DatabasePool pool = (DatabasePool) context.getAttribute("dbPool");
        
        if (pool != null) {
            pool.shutdown();
        }
        
        context.log("Applicazione terminata, risorse rilasciate");
    }
}
```

Quando viene chiamato il metodo `contextInitialized()`?

- a) All'avvio di ogni Servlet
- b) All'avvio dell'applicazione web
- c) Ad ogni richiesta HTTP
- d) Quando viene creata una nuova sessione

---

### 💻 Domanda 17

Analizza questo listener per il monitoraggio delle sessioni:

```java
@WebListener
public class SessionMonitoringListener implements HttpSessionListener, 
                                                  HttpSessionAttributeListener {
    
    private static int activeSessions = 0;
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions++;
        ServletContext context = se.getSession().getServletContext();
        context.setAttribute("activeSessions", activeSessions);
        context.log("Nuova sessione creata. Totale: " + activeSessions);
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions--;
        ServletContext context = se.getSession().getServletContext();
        context.setAttribute("activeSessions", activeSessions);
        context.log("Sessione distrutta. Totale: " + activeSessions);
    }
    
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            ServletContext context = event.getSession().getServletContext();
            context.log("Utente loggato: " + event.getValue());
        }
    }
    
    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName())) {
            ServletContext context = event.getSession().getServletContext();
            context.log("Utente sloggato: " + event.getValue());
        }
    }
    
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        // Attributo sessione modificato
    }
}
```

Quando viene chiamato il metodo `attributeReplaced()`?

- a) Quando viene aggiunto un nuovo attributo alla sessione
- b) Quando un attributo esistente viene sostituito con un nuovo valore
- c) Quando un attributo viene rimosso dalla sessione
- d) Quando la sessione viene invalidata

---

### 🟢 Domanda 18

Quali delle seguenti interfacce listener sono disponibili per le Servlet? (Seleziona tutte)

- a) `ServletContextListener`
- b) `HttpSessionListener`
- c) `ServletRequestListener`
- d) `HttpSessionAttributeListener`
- e) `ServletContextAttributeListener`
- f) `HttpServletResponseListener`

---

### 💻 Domanda 19

Osserva questo listener per il monitoraggio delle richieste:

```java
@WebListener
public class RequestTrackingListener implements ServletRequestListener {
    
    private static final AtomicLong requestCounter = new AtomicLong(0);
    
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        long requestId = requestCounter.incrementAndGet();
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());
        
        System.out.println("Richiesta #" + requestId + " iniziata: " + 
                          request.getMethod() + " " + request.getRequestURI());
    }
    
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        Long requestId = (Long) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute("startTime");
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Richiesta #" + requestId + " completata in " + 
                              duration + "ms");
        }
    }
}
```

Quale informazione viene tracciata da questo listener?

- a) Solo il numero di richieste totali
- b) Solo l'URL delle richieste
- c) Il numero progressivo e il tempo di elaborazione di ogni richiesta
- d) Solo gli errori nelle richieste

---

### 🔵 Domanda 20

Quale listener viene utilizzato per essere notificati quando il `ServletContext` viene modificato?

- a) `ServletContextListener`
- b) `ServletContextAttributeListener`
- c) `ServletRequestListener`
- d) `HttpSessionListener`

---

### 💻 Domanda 21

Analizza questo listener per il binding/unbinding di oggetti:

```java
public class UserSessionData implements HttpSessionBindingListener {
    
    private String username;
    private Date loginTime;
    
    public UserSessionData(String username) {
        this.username = username;
        this.loginTime = new Date();
    }
    
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        System.out.println("UserSessionData per " + username + 
                          " aggiunto alla sessione " + event.getSession().getId());
        
        // Registra login in audit log
        AuditLogger.logUserLogin(username, event.getSession().getId());
    }
    
    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        System.out.println("UserSessionData per " + username + 
                          " rimosso dalla sessione " + event.getSession().getId());
        
        // Registra logout in audit log
        AuditLogger.logUserLogout(username, event.getSession().getId());
    }
    
    // getters and setters...
}
```

Quando viene chiamato il metodo `valueUnbound()`?

- a) Solo quando l'oggetto viene esplicitamente rimosso con `removeAttribute()`
- b) Solo quando la sessione scade
- c) Sia quando l'oggetto viene rimosso che quando la sessione viene invalidata
- d) Solo quando l'applicazione viene chiusa

---

### 🟢 Domanda 22

Quali delle seguenti sono **best practices** per i Listener? (Seleziona tutte)

- a) Usare l'annotazione `@WebListener` invece della configurazione XML
- b) Evitare operazioni lunghe nei metodi dei listener
- c) Gestire sempre le eccezioni nei listener per non compromettere l'applicazione
- d) Memorizzare stato globale nelle variabili statiche dei listener
- e) Utilizzare logging appropriato per tracciare gli eventi

---

## 6. Sicurezza e Filtri

### 💻 Domanda 23

Analizza questo filtro di sicurezza:

```java
@WebFilter("/admin/*")
public class AdminSecurityFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        Boolean isAdmin = (session != null) ? 
            (Boolean) session.getAttribute("isAdmin") : false;
        
        if (isAdmin != null && isAdmin) {
            chain.doFilter(request, response); // Procedi
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                "Accesso negato: privilegi amministratore richiesti");
        }
    }
}
```

In quale ordine vengono eseguiti i componenti per una richiesta a `/admin/users`?

- a) Servlet → Filter
- b) Filter → Servlet
- c) Servlet e Filter in parallelo
- d) Dipende dalla configurazione

---

### 🔵 Domanda 24

Quale annotazione viene utilizzata per configurare un **filtro** in Servlet 3.0+?

- a) `@WebServlet`
- b) `@WebFilter`
- c) `@Filter`
- d) `@ServletFilter`

---

### 🟢 Domanda 25

Quali delle seguenti sono caratteristiche dei **Filtri** nelle Servlet? (Seleziona tutte)

- a) Possono modificare la richiesta prima che raggiunga la Servlet
- b) Possono modificare la risposta dopo che lascia la Servlet
- c) Vengono eseguiti in una catena (chain)
- d) Sono sempre opzionali per il funzionamento dell'applicazione
- e) Possono bloccare completamente una richiesta

---

## 7. Gestione Asincrona (Servlet 3.0+)

### 💻 Domanda 26

Osserva questa Servlet asincrona:

```java
@WebServlet(value = "/async-process", asyncSupported = true)
public class AsyncProcessingServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(30000); // 30 secondi
        
        asyncContext.start(new Runnable() {
            @Override
            public void run() {
                try {
                    // Operazione lunga (es. chiamata a servizio esterno)
                    Thread.sleep(5000);
                    
                    HttpServletResponse asyncResponse = 
                        (HttpServletResponse) asyncContext.getResponse();
                    asyncResponse.setContentType("text/plain");
                    asyncResponse.getWriter().println("Elaborazione completata!");
                    
                    asyncContext.complete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
```

Quale vantaggio principale offre il processing asincrono?

- a) Migliori performance per operazioni veloci
- b) Liberare thread del server durante operazioni lunghe
- c) Sicurezza migliorata
- d) Gestione automatica degli errori

---

### 🔵 Domanda 27

Quale metodo **deve** essere chiamato per completare un'operazione asincrona?

- a) `asyncContext.finish()`
- b) `asyncContext.complete()`
- c) `asyncContext.end()`
- d) `asyncContext.close()`

---

## 8. Pattern e Best Practices

### 💻 Domanda 28

Osserva questo pattern MVC implementato con Servlet:

```java
@WebServlet("/mvc/user/list")
public class UserListController extends HttpServlet {
    
    @Inject
    private UserService userService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Model: recupera i dati
            List<User> users = userService.getAllUsers();
            request.setAttribute("users", users);
            request.setAttribute("totalUsers", users.size());
            
            // View: forward alla JSP per la presentazione
            RequestDispatcher dispatcher = 
                request.getRequestDispatcher("/WEB-INF/views/user-list.jsp");
            dispatcher.forward(request, response);
            
        } catch (ServiceException e) {
            request.setAttribute("errorMessage", "Errore nel caricamento utenti");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp")
                   .forward(request, response);
        }
    }
}
```

Quale ruolo svolge la Servlet in questo pattern MVC?

- a) Model (gestione dati)
- b) View (presentazione)
- c) Controller (coordinamento)
- d) Tutti e tre i ruoli

---

### 🟢 Domanda 29

Quali delle seguenti sono **best practices** per le Servlet? (Seleziona tutte)

- a) Mantenere la logica di business nei metodi `doGet`/`doPost`
- b) Usare CDI per iniettare dipendenze
- c) Sempre chiamare `super.init()` nei metodi di inizializzazione personalizzati
- d) Evitare di memorizzare stato nelle variabili di istanza della Servlet
- e) Usare RequestDispatcher per forward interno e sendRedirect per URL esterni

---

### 💻 Domanda 30

Analizza questo pattern di validazione:

```java
@WebServlet("/user/register")
public class UserRegistrationServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        List<String> errors = new ArrayList<>();
        
        if (username == null || username.length() < 3) {
            errors.add("Username deve essere di almeno 3 caratteri");
        }
        
        if (email == null || !email.contains("@")) {
            errors.add("Email non valida");
        }
        
        if (password == null || password.length() < 8) {
            errors.add("Password deve essere di almeno 8 caratteri");
        }
        
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } else {
            // Registrazione riuscita
            response.sendRedirect("/welcome");
        }
    }
}
```

Perché si usa `forward()` in caso di errori e `sendRedirect()` in caso di successo?

- a) Non c'è differenza, è una scelta arbitraria
- b) `forward()` mantiene i dati del form per mostrarli di nuovo, `sendRedirect()` evita il re-submit
- c) `forward()` è più veloce, `sendRedirect()` è più sicuro
- d) `forward()` funziona solo con POST, `sendRedirect()` solo con GET

---

## 9. Performance e Ottimizzazione

### 💻 Domanda 31

Osserva questa Servlet che implementa caching:

```java
@WebServlet("/api/products")
public class ProductAPIServlet extends HttpServlet {
    
    private static final Map<String, CacheEntry> cache = 
        new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minuti
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String category = request.getParameter("category");
        String cacheKey = "products_" + category;
        
        CacheEntry entry = cache.get(cacheKey);
        String jsonData;
        
        if (entry == null || entry.isExpired()) {
            // Cache miss - carica dal database
            jsonData = loadProductsFromDatabase(category);
            cache.put(cacheKey, new CacheEntry(jsonData, System.currentTimeMillis()));
        } else {
            // Cache hit
            jsonData = entry.getData();
        }
        
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "max-age=300"); // 5 minuti client cache
        response.getWriter().println(jsonData);
    }
    
    static class CacheEntry {
        private final String data;
        private final long timestamp;
        
        // constructor, getters, isExpired() method...
    }
}
```

Quale tipo di caching implementa questo codice?

- a) Solo caching lato client (browser)
- b) Solo caching lato server (applicazione)
- c) Caching sia lato server che lato client
- d) Nessun tipo di caching

---

### 🔵 Domanda 32

Per ottimizzare le performance di una Servlet con molte richieste concorrenti, quale approccio è **sconsigliato**?

- a) Usare connection pooling per il database
- b) Implementare caching dei dati frequentemente richiesti
- c) Memorizzare stato condiviso in variabili di istanza della Servlet
- d) Usare CDI per iniettare dipendenze

---

## 10. Integrazione con altre Tecnologie Java EE

### 💻 Domanda 33

Osserva questa integrazione Servlet + EJB:

```java
@WebServlet("/order/process")
public class OrderProcessingServlet extends HttpServlet {
    
    @EJB
    private OrderServiceBean orderService;
    
    @EJB
    private NotificationServiceBean notificationService;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String customerId = request.getParameter("customerId");
        String productId = request.getParameter("productId");
        
        try {
            // Transazione gestita automaticamente dall'EJB
            Long orderId = orderService.createOrder(customerId, productId);
            notificationService.sendOrderConfirmation(customerId, orderId);
            
            response.setContentType("application/json");
            response.getWriter().println("{\"orderId\":" + orderId + "}");
            
        } catch (OrderException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
```

Quale vantaggio offre l'uso di EJB invece di CDI bean semplici?

- a) Migliori performance
- b) Gestione automatica delle transazioni e pooling
- c) Codice più semplice
- d) Compatibilità con tutti i server

---

### 🔵 Domanda 34

In una Servlet, quale annotazione viene utilizzata per iniettare un **EJB**?

- a) `@Inject`
- b) `@EJB`
- c) `@Resource`
- d) `@Autowired`

---

### 💻 Domanda 35

Analizza questa integrazione con JMS:

```java
@WebServlet("/message/send")
public class MessageSenderServlet extends HttpServlet {
    
    @Resource(lookup = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Resource(lookup = "java:/jms/queue/notifications")
    private Queue notificationQueue;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String message = request.getParameter("message");
        String recipient = request.getParameter("recipient");
        
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            
            MessageProducer producer = session.createProducer(notificationQueue);
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setStringProperty("recipient", recipient);
            
            producer.send(textMessage);
            
            response.getWriter().println("Messaggio inviato alla coda");
            
        } catch (JMSException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Errore nell'invio del messaggio");
        }
    }
}
```

Quale annotazione viene utilizzata per iniettare risorse JMS?

- a) `@Inject`
- b) `@EJB`
- c) `@Resource`
- d) `@JMS`

---

### 🔵 Domanda 36

Quale scope CDI è **inappropriato** per un bean iniettato in una Servlet?

- a) `@RequestScoped`
- b) `@SessionScoped`
- c) `@ApplicationScoped`
- d) `@ConversationScoped`

---

---

## Risposte Corrette

### 1. **b)** Una sola volta, all'avvio dell'applicazione

Il metodo `init()` viene chiamato una sola volta durante il ciclo di vita della Servlet, dopo la creazione dell'istanza.

### 2. **a, b, c)** Una sola istanza per classe, service() in thread separati, destroy() prima dello shutdown

Gli oggetti request/response non sono thread-safe e il costruttore viene chiamato una sola volta.

### 3. **b)** La Servlet viene inizializzata all'avvio dell'applicazione con priorità 1

`loadOnStartup` specifica che la Servlet deve essere caricata all'avvio, il numero indica la priorità.

### 4. **c)** `<load-on-startup>`

`load-on-startup` è opzionale, mentre gli altri elementi sono necessari per una configurazione completa.

### 5. **c)** `["calcio", "tennis", "lettura"]`

`getParameterValues()` restituisce un array contenente tutti i valori per un parametro con nomi multipli.

### 6. **a, b, c, d, e, f)** GET, POST, PUT, DELETE, HEAD, OPTIONS

`HttpServlet` fornisce implementazioni di default per tutti i metodi HTTP standard.

### 7. **b)** `@MultipartConfig`

`@MultipartConfig` è obbligatoria per abilitare il supporto per upload multipart nelle Servlet.

### 8. **b)** `@SessionScoped`

Per mantenere informazioni utente attraverso multiple richieste nella stessa sessione.

### 9. **b)** `forward()` mantiene la stessa richiesta, `sendRedirect()` causa una nuova richiesta HTTP

`forward()` è interno al server, `sendRedirect()` causa una nuova richiesta dal client.

### 10. **b)** Viene restituito `null`

`getSession(false)` restituisce `null` se non esiste una sessione, invece di crearne una nuova.

### 11. **a)** La configurazione per il codice di errore 404

Le configurazioni specifiche per codici di errore hanno priorità più alta rispetto a quelle per tipi di eccezione.

### 12. **b)** 404 Not Found

Il codice viene inviato quando l'utente è presente ma non trovato nel sistema.

### 13. **a, b, c, e)** Non mostrare stack trace, loggare dettagli, codici appropriati, pagine personalizzate

Restituire sempre 200 non è una best practice per la gestione degli errori REST.

### 14. **c)** Tutte le richieste di tutte le Servlet dell'applicazione

`ServletContext` è condiviso a livello di applicazione, non per sessione o utente.

### 15. **b)** `getInitParameter(String name)`

`getInitParameter()` legge parametri di configurazione definiti in `web.xml` o nelle annotazioni.

### 16. **b)** All'avvio dell'applicazione web

`contextInitialized()` viene chiamato una volta quando l'applicazione web viene avviata.

### 17. **b)** Quando un attributo esistente viene sostituito con un nuovo valore

`attributeReplaced()` viene chiamato quando si imposta un nuovo valore per un attributo di sessione esistente.

### 18. **a, b, c, d, e)** Tutti i listener elencati sono disponibili

Java EE fornisce listener per contesto, sessioni, richieste e attributi. `HttpServletResponseListener` non esiste.

### 19. **c)** Il numero progressivo e il tempo di elaborazione di ogni richiesta

Il listener traccia un ID univoco per ogni richiesta e calcola il tempo di elaborazione totale.

### 20. **b)** `ServletContextAttributeListener`

`ServletContextAttributeListener` viene notificato quando attributi vengono aggiunti, rimossi o modificati nel `ServletContext`.

### 21. **c)** Sia quando l'oggetto viene rimosso che quando la sessione viene invalidata

`HttpSessionBindingListener.valueUnbound()` viene chiamato in entrambi i casi.

### 22. **a, b, c, e)** Usare @WebListener, evitare operazioni lunghe, gestire eccezioni, logging appropriato

Memorizzare stato globale in variabili statiche può causare problemi di concorrenza.

### 23. **b)** Filter → Servlet

I filtri vengono sempre eseguiti prima delle Servlet nella catena di processing.

### 24. **b)** `@WebFilter`

`@WebFilter` è l'annotazione standard per configurare filtri in Servlet 3.0+.

### 25. **a, b, c, e)** Modificare request/response, catena di esecuzione, possono bloccare richieste

I filtri non sono sempre opzionali - spesso implementano funzionalità critiche.

### 26. **b)** Liberare thread del server durante operazioni lunghe

Il processing asincrono permette di non bloccare thread durante operazioni I/O lunghe.

### 27. **b)** `asyncContext.complete()`

`complete()` è il metodo standard per terminare un'operazione asincrona.

### 28. **c)** Controller (coordinamento)

La Servlet coordina tra Model (servizi) e View (JSP), tipico pattern MVC.

### 29. **b, c, d, e)** Usare CDI, chiamare super.init(), evitare stato, pattern forward/redirect

La logica di business dovrebbe stare nei servizi, non nelle Servlet.

### 30. **b)** `forward()` mantiene i dati del form per mostrarli di nuovo, `sendRedirect()` evita il re-submit

Pattern POST-redirect-GET per evitare re-submit accidentali dopo operazioni di modifica.

### 31. **c)** Caching sia lato server che lato client

Implementa cache in memoria lato server e header Cache-Control per il client.

### 32. **c)** Memorizzare stato condiviso in variabili di istanza della Servlet

Le Servlet sono singleton e condivise tra thread - le variabili di istanza causano race condition.

### 33. **b)** Gestione automatica delle transazioni e pooling

Gli EJB offrono servizi enterprise come transazioni dichiarative e pooling delle istanze.

### 34. **b)** `@EJB`

`@EJB` è l'annotazione specifica per iniettare Enterprise JavaBeans.

### 35. **c)** `@Resource`

`@Resource` viene utilizzata per iniettare risorse JNDI come ConnectionFactory e Queue.

### 36. **d)** `@ConversationScoped`

`@ConversationScoped` è specifico per JSF e non appropriato per Servlet standard.
