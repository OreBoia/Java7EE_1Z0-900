# Filtri (Filters) in Java EE

## Introduzione ai Filtri

I **Filtri** (`javax.servlet.Filter`) sono componenti della specifica Servlet che permettono di intercettare e processare le richieste HTTP prima che raggiungano la servlet di destinazione, e opzionalmente post-processare le risposte.

### Caratteristiche Principali

- **Intercettazione**: I filtri intercettano le richieste prima che arrivino alla servlet
- **Post-processing**: Possono elaborare le risposte dopo l'esecuzione della servlet
- **Configurazione**: Simile alle servlet (annotazioni `@WebFilter` o configurazione in `web.xml`)
- **Scope**: Funzionano su tutte le risorse che corrispondono al loro pattern (servlet, JSP, file statici)

## Configurazione dei Filtri

### 1. Configurazione con Annotazioni

```java
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = "/*", filterName = "LoggingFilter")
public class LoggingFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("LoggingFilter inizializzato");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        // Logica di pre-processing
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        System.out.println("Richiesta ricevuta: " + httpRequest.getRequestURI());
        
        // Continua la catena dei filtri
        chain.doFilter(request, response);
        
        // Logica di post-processing
        System.out.println("Risposta inviata per: " + httpRequest.getRequestURI());
    }
    
    @Override
    public void destroy() {
        System.out.println("LoggingFilter distrutto");
    }
}
```

### 2. Configurazione in web.xml

```xml
<web-app>
    <filter>
        <filter-name>LoggingFilter</filter-name>
        <filter-class>com.example.filters.LoggingFilter</filter-class>
        <init-param>
            <param-name>logLevel</param-name>
            <param-value>INFO</param-value>
        </init-param>
    </filter>
    
    <filter-mapping>
        <filter-name>LoggingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```

## Ciclo di Vita dei Filtri

1. **init()**: Chiamato una volta all'inizializzazione del filtro
2. **doFilter()**: Chiamato per ogni richiesta che corrisponde al pattern
3. **destroy()**: Chiamato quando il filtro viene rimosso dalla memoria

## Esempi Pratici di Filtri

### 1. Filtro di Logging

```java
@WebFilter(urlPatterns = "/*")
public class LoggingFilter implements Filter {
    
    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();
        
        // Log della richiesta in arrivo
        logger.info(String.format("Richiesta: %s %s da %s", 
            httpRequest.getMethod(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()));
        
        try {
            // Continua la catena
            chain.doFilter(request, response);
        } finally {
            // Log del tempo di esecuzione
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info(String.format("Richiesta completata in %d ms", executionTime));
        }
    }
}
```

### 2. Filtro di Autenticazione

```java
@WebFilter(urlPatterns = {"/admin/*", "/secure/*"})
public class AuthenticationFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Verifica se l'utente è autenticato
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        
        if (isLoggedIn) {
            // L'utente è autenticato, continua
            chain.doFilter(request, response);
        } else {
            // L'utente non è autenticato, reindirizza al login
            String loginURL = httpRequest.getContextPath() + "/login";
            httpResponse.sendRedirect(loginURL);
        }
    }
}
```

### 3. Filtro di Compressione

```java
@WebFilter(urlPatterns = "*.html")
public class CompressionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Verifica se il client supporta la compressione gzip
        String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
        
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            // Crea un wrapper per la risposta che comprime il contenuto
            CompressionResponseWrapper wrappedResponse = 
                new CompressionResponseWrapper(httpResponse);
            
            // Imposta l'header per indicare la compressione
            httpResponse.setHeader("Content-Encoding", "gzip");
            
            // Continua con la risposta compressa
            chain.doFilter(request, wrappedResponse);
            
            // Finalizza la compressione
            wrappedResponse.finishResponse();
        } else {
            // Nessuna compressione richiesta
            chain.doFilter(request, response);
        }
    }
}
```

### 4. Filtro per la Gestione delle Encoding

```java
@WebFilter(urlPatterns = "/*")
public class EncodingFilter implements Filter {
    
    private String encoding = "UTF-8";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null) {
            encoding = encodingParam;
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        // Imposta l'encoding per la richiesta
        request.setCharacterEncoding(encoding);
        
        // Imposta l'encoding per la risposta
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=" + encoding);
        
        chain.doFilter(request, response);
    }
}
```

## Wrapper per Request e Response

### Request Wrapper Example

```java
public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private Map<String, String> customHeaders;
    
    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }
    
    public void addHeader(String name, String value) {
        customHeaders.put(name, value);
    }
    
    @Override
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }
    
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(customHeaders.keySet());
        Enumeration<String> e = super.getHeaderNames();
        while (e.hasMoreElements()) {
            set.add(e.nextElement());
        }
        return Collections.enumeration(set);
    }
}
```

### Response Wrapper Example

```java
public class CustomHttpServletResponseWrapper extends HttpServletResponseWrapper {
    
    private ByteArrayOutputStream buffer;
    private PrintWriter writer;
    
    public CustomHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        buffer = new ByteArrayOutputStream();
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
        }
        return writer;
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                buffer.write(b);
            }
            
            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Implementation per async processing
            }
        };
    }
    
    public byte[] getBuffer() {
        if (writer != null) {
            writer.flush();
        }
        return buffer.toByteArray();
    }
}
```

## Catena dei Filtri (Filter Chain)

### Ordine di Esecuzione

L'ordine dei filtri può essere controllato in diversi modi:

1. **Con web.xml**: L'ordine dipende dall'ordine di dichiarazione
2. **Con annotazioni**: Utilizzare `@Order` se supportato dal container

```java
@WebFilter(urlPatterns = "/*")
@Order(1)
public class FirstFilter implements Filter {
    // Questo filtro viene eseguito per primo
}

@WebFilter(urlPatterns = "/*")
@Order(2)  
public class SecondFilter implements Filter {
    // Questo filtro viene eseguito per secondo
}
```

### Esempio di Catena Completa

```java
// Filtro 1: Logging delle richieste
@WebFilter(urlPatterns = "/*")
@Order(1)
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        System.out.println("1. Request ricevuta");
        chain.doFilter(request, response);
        System.out.println("1. Response inviata");
    }
}

// Filtro 2: Autenticazione
@WebFilter(urlPatterns = "/secure/*")
@Order(2)
public class SecurityFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        System.out.println("2. Controllo sicurezza");
        // Logica di autenticazione
        chain.doFilter(request, response);
        System.out.println("2. Sicurezza completata");
    }
}

// Filtro 3: Compressione
@WebFilter(urlPatterns = "*.html")
@Order(3)
public class CompressionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletResponse {
        System.out.println("3. Inizio compressione");
        chain.doFilter(request, response);
        System.out.println("3. Fine compressione");
    }
}
```

**Output della catena:**

```text
1. Request ricevuta
2. Controllo sicurezza
3. Inizio compressione
[Esecuzione Servlet]
3. Fine compressione
2. Sicurezza completata
1. Response inviata
```

## Filtri Avanzati

### Filtro per Cross-Origin Resource Sharing (CORS)

```java
@WebFilter(urlPatterns = "/api/*")
public class CORSFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Imposta gli header CORS
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Gestisce le richieste OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

### Filtro per Rate Limiting

```java
@WebFilter(urlPatterns = "/api/*")
public class RateLimitingFilter implements Filter {
    
    private Map<String, RateLimiter> clientLimiters = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIP = httpRequest.getRemoteAddr();
        RateLimiter limiter = clientLimiters.computeIfAbsent(clientIP, 
            k -> new RateLimiter(100, TimeUnit.MINUTES)); // 100 richieste al minuto
        
        if (limiter.tryAcquire()) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Rate limit exceeded");
        }
    }
}
```

## Best Practices

### 1. Gestione delle Eccezioni

```java
@WebFilter(urlPatterns = "/*")
public class ErrorHandlingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            // Log dell'errore
            logger.error("Errore durante l'elaborazione della richiesta", e);
            
            // Gestione dell'errore
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            if (!httpResponse.isCommitted()) {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Errore interno del server");
            }
        }
    }
}
```

### 2. Performance Monitoring

```java
@WebFilter(urlPatterns = "/*")
public class PerformanceFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        long startTime = System.nanoTime();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.nanoTime() - startTime;
            double milliseconds = duration / 1_000_000.0;
            
            // Log delle performance
            if (milliseconds > 1000) { // Log se richiesta > 1 secondo
                logger.warn(String.format("Richiesta lenta: %s took %.2f ms", 
                    httpRequest.getRequestURI(), milliseconds));
            }
        }
    }
}
```

### 3. Configurazione Dinamica

```java
@WebFilter(urlPatterns = "/*")
public class ConfigurableFilter implements Filter {
    
    private boolean enabled;
    private String logLevel;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Legge parametri di configurazione
        enabled = Boolean.parseBoolean(
            filterConfig.getInitParameter("enabled", "true"));
        logLevel = filterConfig.getInitParameter("logLevel", "INFO");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }
        
        // Logica del filtro basata sulla configurazione
        if ("DEBUG".equals(logLevel)) {
            // Logging dettagliato
        }
        
        chain.doFilter(request, response);
    }
}
```

## Considerazioni Importanti

1. **Ordine dei Filtri**: L'ordine è cruciale per il corretto funzionamento
2. **Performance**: I filtri vengono eseguiti per ogni richiesta, ottimizzare il codice
3. **Thread Safety**: I filtri devono essere thread-safe
4. **Gestione delle Risorse**: Chiudere sempre le risorse nei wrapper
5. **Debugging**: Utilizzare logging appropriato per tracciare l'esecuzione

## Conclusioni

I filtri sono uno strumento potente per implementare funzionalità trasversali nelle applicazioni web Java EE. Permettono di separare le preoccupazioni e implementare aspetti come sicurezza, logging, compressione e caching in modo modulare e riutilizzabile.

La loro natura intercettiva li rende ideali per implementare pattern come:

- **Aspect-Oriented Programming (AOP)**
- **Decorator Pattern**
- **Chain of Responsibility Pattern**

Utilizzati correttamente, i filtri migliorano significativamente l'architettura e la manutenibilità delle applicazioni web.