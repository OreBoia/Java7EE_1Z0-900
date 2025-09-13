# Gestione degli Errori nelle Applicazioni Web (Error Handling)

Una parte fondamentale dello sviluppo di applicazioni web robuste è la gestione degli errori. Un utente non dovrebbe mai vedere una stack trace di Java o una pagina di errore generica del server. La Servlet API fornisce meccanismi sia dichiarativi che programmatici per gestire gli errori in modo controllato e presentare pagine di errore personalizzate.

## 1. Gestione Dichiarativa tramite `web.xml`

L'approccio più comune e robusto è configurare le pagine di errore nel descrittore di deployment (`web.xml`). L'elemento `<error-page>` permette di mappare specifici codici di stato HTTP o tipi di eccezioni Java a una pagina di errore personalizzata (HTML, JSP, ecc.).

Questo metodo ha il vantaggio di centralizzare la gestione degli errori e di funzionare anche per errori generati dal container stesso (es. risorsa non trovata, 404) e non solo dal codice della nostra applicazione.

### Esempio di Configurazione in `web.xml`

```xml
<web-app ...>

    <!-- Mappatura per un codice di errore HTTP (es. 404 Not Found) -->
    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/error-pages/404.jsp</location>
    </error-page>

    <!-- Mappatura per un codice di errore HTTP (es. 500 Internal Server Error) -->
    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/error-pages/500.jsp</location>
    </error-page>

    <!-- Mappatura per un tipo specifico di eccezione Java -->
    <error-page>
        <exception-type>java.lang.NullPointerException</exception-type>
        <location>/WEB-INF/error-pages/npe.jsp</location>
    </error-page>
    
    <!-- Una pagina di errore generica per tutte le altre eccezioni -->
    <error-page>
        <exception-type>java.lang.Throwable</exception-type>
        <location>/WEB-INF/error-pages/generic-error.jsp</location>
    </error-page>

</web-app>
```

*Nota: È buona pratica posizionare le pagine di errore in `/WEB-INF/` per impedire l'accesso diretto da parte degli utenti.*

## 2. Gestione Programmatica all'interno di una Servlet

A volte è necessario gestire gli errori direttamente nel codice della Servlet.

### a) Usare `response.sendError()`

Il metodo `sendError(int sc, String msg)` sull'oggetto `HttpServletResponse` è il modo più semplice per attivare il meccanismo di gestione degli errori del container. Quando si chiama questo metodo, il container interrompe l'esecuzione della Servlet e cerca una pagina di errore configurata in `web.xml` che corrisponda al codice di stato inviato.

```java
@WebServlet("/resource")
public class ResourceServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        if (id == null || findResourceById(id) == null) {
            // Invia un errore 404. Il container mostrerà la pagina mappata in web.xml.
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "La risorsa richiesta non è stata trovata.");
            return; // Importante terminare l'esecuzione del metodo
        }
        
        // ... continua l'elaborazione normale ...
    }
    
    private Object findResourceById(String id) { return null; /* logica fittizia */ }
}
```

### b) Usare `try-catch` e `RequestDispatcher`

Per un controllo più granulare, si può catturare un'eccezione con un blocco `try-catch` e inoltrare manualmente la richiesta a una pagina di errore dedicata usando un `RequestDispatcher`. Questo approccio è utile se si vogliono aggiungere informazioni specifiche sull'errore alla richiesta prima di visualizzare la pagina.

```java
@WebServlet("/process")
public class ProcessingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Simula un'operazione che potrebbe fallire
            performComplexOperation();
        } catch (Exception e) {
            // 1. Logga l'errore per il debug
            System.err.println("Errore durante l'elaborazione: " + e.getMessage());
            
            // 2. Aggiungi informazioni sull'errore alla richiesta
            request.setAttribute("errorMessage", "Si è verificato un errore imprevisto durante l'elaborazione.");
            
            // 3. Inoltra a una pagina di errore personalizzata
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error-pages/processing-error.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    private void performComplexOperation() throws Exception {
        throw new Exception("Operazione fallita!");
    }
}
```

## Tabella dei Termini e Metodi Chiave

| Termine/Elemento | Descrizione |
| :--- | :--- |
| **`<error-page>`** | Elemento in `web.xml` usato per mappare un codice di errore o un tipo di eccezione a una pagina di errore. |
| **`<error-code>`** | Sotto-elemento di `<error-page>` che specifica il codice di stato HTTP (es. 404, 503). |
| **`<exception-type>`** | Sotto-elemento di `<error-page>` che specifica il nome completo di una classe di eccezione Java. |
| **`<location>`** | Sotto-elemento di `<error-page>` che definisce il percorso della pagina di errore da visualizzare. |
| **`response.sendError(int, String)`** | Metodo programmatico per inviare un codice di errore al client, attivando il meccanismo di gestione degli errori del container. |
| **`try-catch` con `RequestDispatcher`** | Approccio programmatico per catturare eccezioni e inoltrare la richiesta a una pagina di errore, permettendo di aggiungere dati contestuali. |
| **Pagina di Errore** | Una pagina (JSP, HTML) che viene mostrata all'utente in caso di errore, fornendo un'esperienza utente migliore rispetto a una stack trace. |
