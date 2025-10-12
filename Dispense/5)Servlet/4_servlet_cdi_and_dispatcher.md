# Integrazione di CDI e Invocazione di Risorse Web nelle Servlet

Le Servlet non operano in isolamento. In un'applicazione Java EE moderna, si integrano con altre tecnologie come CDI per la logica di business e JSP per la presentazione, agendo spesso come "controllori" in un'architettura MVC (Model-View-Controller).

## Utilizzo di CDI Beans nelle Servlet

Grazie alla profonda integrazione in Java EE 7, il Web Container è un contesto in cui CDI (Contexts and Dependency Injection) è pienamente supportato. Questo significa che le Servlet possono sfruttare la dependency injection per ottenere riferimenti a CDI beans o EJB in modo pulito e dichiarativo, senza ricorrere a lookup JNDI manuali.

L'annotazione chiave è `@Inject`.

### Esempio di Codice: Servlet che Inietta un Servizio

**1. Il Servizio (CDI Bean)**

```java
// Un semplice bean CDI con scope di default (@Dependent)
public class PersonService {
    public String getPersonName(int id) {
        // In un'app reale, qui ci sarebbe la logica per accedere a un database
        return "John Doe (ID: " + id + ")";
    }
}
```

**2. La Servlet che usa il Servizio**

```java
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/person")
public class PersonServlet extends HttpServlet {

    // 1. Iniezione della dipendenza
    @Inject
    private PersonService personService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 2. Utilizzo del servizio iniettato
        String personName = personService.getPersonName(123);

        response.setContentType("text/plain");
        response.getWriter().write("Nome recuperato: " + personName);
    }
}
```

## Invocare Altre Risorse Web con `RequestDispatcher`

Una Servlet spesso agisce come un **controller**: elabora la richiesta, prepara i dati (il "modello") e poi delega la presentazione della risposta a una **view**, che è tipicamente una pagina JSP (JavaServer Pages) o un altro file HTML.

Questo meccanismo di delega è gestito dall'interfaccia `RequestDispatcher`.

Si ottiene un `RequestDispatcher` dalla richiesta:
`RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/my-view.jsp");`

`RequestDispatcher` ha due metodi principali:

1. **`forward(request, response)`**:
    * Trasferisce completamente il controllo della richiesta e della risposta a un'altra risorsa **sul server**.
    * L'URL nel browser del client non cambia. Il client non sa che è avvenuto un "forward".
    * Qualsiasi output già scritto nella risposta dalla Servlet chiamante viene cancellato.

2. **`include(request, response)`**:
    * Include il contenuto di un'altra risorsa nella risposta della Servlet corrente.
    * Il controllo ritorna alla Servlet originale dopo che la risorsa inclusa ha terminato la sua esecuzione.
    * È utile per creare layout modulari (es. includere un header e un footer comuni).

### Esempio di Codice: Servlet come Controller che fa un `forward` a una JSP

**1. La Servlet (Controller)**

```java
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/user-list")
public class UserListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Prepara i dati (il modello)
        List<String> users = Arrays.asList("Alice", "Bob", "Charlie");

        // 2. Salva i dati nello scope della richiesta per renderli accessibili alla JSP
        request.setAttribute("userList", users);

        // 3. Ottiene il dispatcher per la JSP (la view)
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user-view.jsp");

        // 4. Inoltra la richiesta e la risposta alla JSP
        dispatcher.forward(request, response);
    }
}
```

**2. La Pagina JSP (View) - `user-view.jsp`**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Lista Utenti</title>
</head>
<body>
    <h1>Lista degli Utenti</h1>
    <ul>
        <%-- La JSP legge l'attributo 'userList' e itera su di esso --%>
        <c:forEach var="user" items="${userList}">
            <li>${user}</li>
        </c:forEach>
    </ul>
</body>
</html>
```

## Tabella dei Termini Chiave

| Termine | Descrizione |
| :--- | :--- |
| **CDI (Contexts and Dependency Injection)** | La specifica standard di Java EE per la dependency injection, che permette di "iniettare" dipendenze (come altri bean o servizi) in una classe. |
| **`@Inject`** | L'annotazione CDI usata per marcare un punto di iniezione. |
| **`RequestDispatcher`** | Un'interfaccia che definisce un oggetto in grado di inoltrare una richiesta a un'altra risorsa sul server o di includerne il contenuto. |
| **`forward()`** | Metodo di `RequestDispatcher` che trasferisce la richiesta a un'altra risorsa. È un'operazione lato server, trasparente per il client. |
| **`include()`** | Metodo di `RequestDispatcher` che include l'output di un'altra risorsa nella risposta corrente. |
| **MVC (Model-View-Controller)** | Un pattern architetturale che separa la logica di business (Model), la presentazione (View) e la gestione dell'input (Controller). |
| **Request Scope** | L'ambito di vita degli oggetti associati a una singola `HttpServletRequest`. Gli attributi impostati con `request.setAttribute()` sono disponibili in questo scope. |
| **JSP (JavaServer Pages)** | Una tecnologia per creare pagine web dinamiche. Le JSP vengono compilate in Servlet dal container. |
