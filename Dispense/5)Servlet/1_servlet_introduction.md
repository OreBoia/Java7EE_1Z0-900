# Introduzione alle Servlet in Java EE 7

Le **Servlet** sono componenti Java lato server che implementano la specifica Java EE Web (Servlet API, versione 3.1 in Java EE 7). Una Servlet riceve richieste HTTP da un client (tipicamente un browser) e produce risposte dinamiche (HTML, JSON, dati binari, ecc.).

In Java EE, le Servlet costituiscono la base di tutte le tecnologie web: sia JSP che JSF, ad un livello più basso, vengono compilati o si appoggiano a Servlet. Capire le Servlet significa comprendere il fondamento della gestione delle richieste HTTP in Java.

## Ciclo di Vita di una Servlet

Il ciclo di vita di una Servlet è gestito dal **Web Container** (come Tomcat, WildFly, GlassFish). Il container è responsabile di creare, inizializzare, invocare e distruggere le istanze delle Servlet.

1. **Creazione dell'Istanza**:
    * All'avvio dell'applicazione (o al primo accesso, se la Servlet è configurata come *lazy*), il container crea un'istanza della classe Servlet.
    * La classe deve avere un costruttore pubblico senza argomenti.
    * **Viene creata una sola istanza per ogni dichiarazione di Servlet**. Questa singola istanza gestirà tutte le richieste concorrenti.

2. **Inizializzazione (`init` method)**:
    * Subito dopo la creazione, il container chiama il metodo `init(ServletConfig)` **una sola volta**.
    * Questo metodo è usato per operazioni di inizializzazione costose, come leggere parametri di configurazione (`init-param`), aprire connessioni a database o caricare risorse condivise.

3. **Gestione delle Richieste (`service` method)**:
    * Per ogni richiesta HTTP destinata alla Servlet, il container invoca il metodo `service(HttpServletRequest, HttpServletResponse)` su un thread prelevato da un pool.
    * Se la nostra Servlet estende `HttpServlet` (il caso più comune), il metodo `service` di default smista la richiesta ai metodi `doGet()`, `doPost()`, `doPut()`, ecc., in base al metodo HTTP della richiesta.
    * All'interno di `doGet`/`doPost`, l'applicazione elabora la richiesta: legge parametri, gestisce la sessione, interagisce con la logica di business e infine scrive la risposta usando l'oggetto `HttpServletResponse`.

4. **Distruzione (`destroy` method)**:
    * Quando l'applicazione viene fermata o la Servlet viene ricaricata, il container chiama il metodo `destroy()` **una sola volta**.
    * Questo metodo serve a rilasciare le risorse acquisite durante l'inizializzazione (es. chiudere connessioni al database, terminare thread in background).
    * Dopo la chiamata a `destroy()`, l'istanza della Servlet è eleggibile per la garbage collection.

## Esempio di Codice: Una Semplice Servlet

Questa Servlet risponde a richieste GET con un semplice messaggio HTML.

```java
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// L'annotazione @WebServlet mappa questa classe all'URL "/hello"
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Questo metodo viene chiamato una sola volta all'inizializzazione
        System.out.println("HelloServlet è stata inizializzata!");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Imposta il tipo di contenuto della risposta
        response.setContentType("text/html;charset=UTF-t");
        
        // Ottiene il writer per scrivere la risposta testuale
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet HelloServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Ciao dal mondo delle Servlet!</h1>");
            // Legge un parametro dalla richiesta, es. /hello?name=John
            String name = request.getParameter("name");
            if (name != null && !name.isEmpty()) {
                out.println("<p>Saluti, " + name + "!</p>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    public void destroy() {
        // Questo metodo viene chiamato una sola volta alla distruzione
        System.out.println("HelloServlet è stata distrutta!");
    }
}
```

## Tabella dei Termini Chiave

| Termine | Descrizione |
| :--- | :--- |
| **Servlet** | Un componente Java che elabora richieste e genera risposte dinamiche in un'applicazione web. |
| **Web Container** | La parte del server applicativo che gestisce il ciclo di vita delle Servlet (es. Tomcat, WildFly). |
| **`init()`** | Metodo del ciclo di vita chiamato una sola volta per inizializzare la Servlet. |
| **`service()`** | Metodo del ciclo di vita che riceve tutte le richieste HTTP e le smista ai metodi `doGet`, `doPost`, ecc. |
| **`doGet()` / `doPost()`** | Metodi che gestiscono specificamente le richieste HTTP GET e POST. |
| **`destroy()`** | Metodo del ciclo di vita chiamato una sola volta per rilasciare le risorse prima che la Servlet venga distrutta. |
| **`HttpServletRequest`** | Oggetto che rappresenta la richiesta HTTP in arrivo. Contiene informazioni come parametri, header e sessione. |
| **`HttpServletResponse`** | Oggetto che rappresenta la risposta HTTP da inviare al client. Si usa per impostare header, status code e scrivere il corpo della risposta. |
| **`@WebServlet`** | Annotazione (introdotta in Servlet 3.0) per dichiarare una Servlet e mapparla a uno o più URL, senza bisogno di configurazione in `web.xml`. |
| **Web Application Archive (WAR)** | Il formato file (`.war`) utilizzato per impacchettare e distribuire un'applicazione web Java. |
