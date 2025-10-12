# Oggetti Impliciti in JSP (Implicit Objects)

Una delle maggiori comodità offerte dalle JSP è la disponibilità di un set di **oggetti impliciti**. Si tratta di variabili predefinite che il container JSP rende automaticamente disponibili all'interno di scriptlet ed espressioni, senza che lo sviluppatore debba dichiararle o istanziarle.

Questi oggetti forniscono un accesso diretto alle componenti chiave di una richiesta web.

## Elenco degli Oggetti Impliciti

Ecco i 9 oggetti impliciti definiti dalla specifica JSP:

1. **`request`**:
    * **Tipo**: `javax.servlet.http.HttpServletRequest`
    * **Descrizione**: Rappresenta la richiesta HTTP corrente. È lo stesso oggetto `request` che si riceve nel metodo `doGet`/`doPost` di una Servlet. Permette di accedere a parametri, header, cookie, ecc.

2. **`response`**:
    * **Tipo**: `javax.servlet.http.HttpServletResponse`
    * **Descrizione**: Rappresenta la risposta HTTP corrente. Permette di impostare header, aggiungere cookie, reindirizzare, ecc.

3. **`session`**:
    * **Tipo**: `javax.servlet.http.HttpSession`
    * **Descrizione**: Rappresenta la sessione dell'utente. È disponibile solo se la direttiva `<%@ page session="true" %>` è impostata (è il default). Se la sessione non esiste, viene creata.

4. **`application`**:
    * **Tipo**: `javax.servlet.ServletContext`
    * **Descrizione**: Rappresenta l'intera applicazione web (il contesto). È condiviso tra tutte le richieste e sessioni. Utile per parametri globali e attributi a livello di applicazione.

5. **`out`**:
    * **Tipo**: `javax.servlet.jsp.JspWriter`
    * **Descrizione**: È uno stream di output bufferizzato, simile a un `PrintWriter`, usato per scrivere contenuto nella risposta. Le espressioni JSP (`<%= ... %>`) usano questo oggetto implicitamente.

6. **`config`**:
    * **Tipo**: `javax.servlet.ServletConfig`
    * **Descrizione**: Fornisce l'accesso ai parametri di configurazione della Servlet generata dalla JSP (definiti con `<init-param>` in `web.xml`).

7. **`pageContext`**:
    * **Tipo**: `javax.servlet.jsp.PageContext`
    * **Descrizione**: Fornisce un punto di accesso centralizzato a tutti gli altri oggetti impliciti e agli attributi nei vari scope (page, request, session, application). È un oggetto molto potente, usato spesso dalle librerie di tag.

8. **`page`**:
    * **Tipo**: `java.lang.Object` (ma in pratica è un riferimento alla Servlet generata)
    * **Descrizione**: Rappresenta l'istanza della Servlet generata dalla pagina JSP. È l'equivalente di `this`. Il suo uso è raro.

9. **`exception`**:
    * **Tipo**: `java.lang.Throwable`
    * **Descrizione**: Questo oggetto è disponibile **solo** nelle pagine di errore, ovvero quelle designate con la direttiva `<%@ page isErrorPage="true" %>`. Contiene l'eccezione che ha causato l'errore.

## Esempio di Codice: Utilizzo degli Oggetti Impliciti

Questa pagina JSP mostra come accedere ad alcuni degli oggetti impliciti più comuni.

**`implicit-objects-demo.jsp`**

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Demo Oggetti Impliciti</title>
</head>
<body>
    <h1>Accesso agli Oggetti Impliciti</h1>

    <h2>Request Info</h2>
    <ul>
        <li><b>User Agent:</b> <%= request.getHeader("User-Agent") %></li>
        <li><b>Metodo HTTP:</b> <%= request.getMethod() %></li>
    </ul>

    <h2>Session Info</h2>
    <%
        // Usa l'oggetto 'session' per gestire un contatore di visite
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 1;
        } else {
            visitCount++;
        }
        session.setAttribute("visitCount", visitCount);
    %>
    <p>ID della sessione: <%= session.getId() %></p>
    <p>Numero di visite in questa sessione: <%= visitCount %></p>

    <h2>Application Info</h2>
    <p>Nome del Server: <%= application.getServerInfo() %></p>
    <p>Versione Servlet API: <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></p>

    <h2>Output con 'out'</h2>
    <%
        out.println("<p>Questo paragrafo è stato scritto usando l'oggetto 'out'.</p>");
    %>

</body>
</html>
```

## Perché Evitare Codice Java nelle Pagine JSP (Best Practice)

Storicamente, le JSP permettevano di inserire logica di business complessa direttamente nella pagina tramite gli scriptlet (`<% ... %>`). Tuttavia, questa pratica è oggi considerata una **cattiva pratica** per diversi motivi:

* **Violazione del Principio di Separazione delle Competenze (Separation of Concerns)**: Mescolare codice Java (logica di business) con markup HTML (logica di presentazione) rende le pagine difficili da leggere, manutenere e testare.
* **Difficoltà di Manutenzione**: Modificare la logica richiede di intervenire su file che contengono anche la struttura della UI, aumentando il rischio di introdurre errori.
* **Scarsa Riusabilità**: Il codice Java intrappolato in una JSP non può essere facilmente riutilizzato in altre parti dell'applicazione.
* **Collaborazione Difficile**: Rende difficile la collaborazione tra sviluppatori front-end (che si occupano dell'HTML/CSS) e sviluppatori back-end (che si occupano della logica Java).

La **best practice** moderna, promossa dall'architettura MVC (Model-View-Controller), prevede di:

1. **Spostare tutta la logica di business in componenti lato server** come Servlet (Controller) e CDI/EJB beans (Model).
2. **Mantenere le JSP il più "stupide" possibile**, relegandole al solo ruolo di **View** (vista). Il loro unico compito dovrebbe essere quello di presentare i dati preparati dal controller.

Per raggiungere questo obiettivo e eliminare quasi del tutto la necessità di scriptlet, sono state introdotte due tecnologie fondamentali:

* **Expression Language (EL)**: Per accedere e visualizzare i dati (attributi di request, session, ecc.) con una sintassi pulita (es. `${user.name}`).
* **JSP Standard Tag Library (JSTL)**: Una libreria di tag per eseguire operazioni logiche comuni come cicli (`<c:forEach>`) e condizionali (`<c:if>`) senza scrivere codice Java.

## Tabella Riassuntiva degli Oggetti Impliciti

| Variabile | Tipo | Scope | Descrizione |
| :--- | :--- | :--- | :--- |
| **`request`** | `HttpServletRequest` | Request | La richiesta HTTP corrente. |
| **`response`** | `HttpServletResponse` | Page | La risposta HTTP corrente. |
| **`session`** | `HttpSession` | Session | La sessione dell'utente. |
| **`application`** | `ServletContext` | Application | Il contesto dell'intera applicazione. |
| **`out`** | `JspWriter` | Page | Lo stream di output per la risposta. |
| **`config`** | `ServletConfig` | Page | La configurazione della Servlet/JSP. |
| **`pageContext`** | `PageContext` | Page | Contesto della pagina, dà accesso a tutti gli scope. |
| **`page`** | `Object` | Page | L'istanza della Servlet generata (`this`). |
| **`exception`** | `Throwable` | Page | L'eccezione catturata (solo nelle pagine di errore). |
