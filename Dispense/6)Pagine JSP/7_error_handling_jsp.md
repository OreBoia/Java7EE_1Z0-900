# Gestione degli Errori in JSP (Error Handling)

La gestione degli errori è una parte cruciale di qualsiasi applicazione web. Nelle applicazioni Java EE basate su Servlet e JSP, esistono meccanismi robusti, sia dichiarativi che programmatici, per catturare e gestire le eccezioni in modo controllato, presentando all'utente pagine di errore amichevoli invece di stack trace incomprensibili.

## 1. Gestione Dichiarativa tramite `web.xml`

L'approccio preferito e più pulito per la gestione degli errori è quello dichiarativo, configurato nel descrittore di deployment dell'applicazione (`web.xml`). Questo metodo permette di centralizzare la logica di gestione degli errori senza sporcare il codice delle singole pagine o servlet.

Si possono mappare due tipi di condizioni di errore a specifiche risorse (come una pagina JSP o una servlet):

- **Codici di stato HTTP**: Come 404 (Not Found) o 500 (Internal Server Error).
- **Tipi di eccezioni Java**: Come `java.io.IOException` o eccezioni custom.

### Configurazione in `web.xml`

All'interno del file `web.xml`, si utilizzano i tag `<error-page>`.

```xml
<web-app ...>

    <!-- Mapping per codice di stato HTTP -->
    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/error/404.jsp</location>
    </error-page>

    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/error/error.jsp</location>
    </error-page>

    <!-- Mapping per tipo di eccezione Java -->
    <error-page>
        <exception-type>java.lang.Throwable</exception-type>
        <location>/WEB-INF/error/error.jsp</location>
    </error-page>
    
    <error-page>
        <exception-type>com.myapp.MyCustomException</exception-type>
        <location>/WEB-INF/error/customError.jsp</location>
    </error-page>

</web-app>
```

Quando un'eccezione non gestita si verifica in una servlet o in una JSP, o quando viene impostato un codice di errore sulla response, il container delle servlet cerca una corrispondenza in `web.xml` ed esegue un **forward** interno verso la risorsa specificata nel tag `<location>`.

## 2. Creazione della Pagina di Errore JSP

Una pagina JSP designata per gestire gli errori deve essere contrassegnata con una direttiva specifica per poter accedere ai dettagli dell'errore.

### La Direttiva `<%@ page isErrorPage="true" %>`

Questa direttiva, posta all'inizio della pagina JSP, fa due cose fondamentali:

1. Indica al container che questa è una pagina di errore.
2. Rende disponibile l'**oggetto implicito `exception`**.

L'oggetto `exception` è un'istanza di `java.lang.Throwable` e rappresenta l'eccezione che ha causato il reindirizzamento a questa pagina.

### Esempio di Pagina di Errore (`error.jsp`)

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Errore Inaspettato</title>
    <style>
        body { font-family: sans-serif; }
        .error-details { background-color: #f0f0f0; border: 1px solid #ccc; padding: 10px; margin-top: 20px; }
    </style>
</head>
<body>
    <h1>Oops! Si è verificato un errore.</h1>
    <p>Ci scusiamo per l'inconveniente. Il nostro team è stato notificato e risolverà il problema al più presto.</p>
    <p>Puoi tornare alla <a href="${pageContext.request.contextPath}/">home page</a>.</p>

    <%-- 
        Questa sezione è utile in fase di sviluppo per debuggare.
        Dovrebbe essere rimossa o nascosta in produzione.
    --%>
    <c:if test="${pageContext.servletContext.getInitParameter('environment') == 'development'}">
        <div class="error-details">
            <h2>Dettagli dell'errore (solo per sviluppo)</h2>
            <p><strong>Messaggio:</strong> ${exception.message}</p>
            <p><strong>Tipo di Eccezione:</strong> ${exception.class.name}</p>
            <p><strong>URI della Richiesta:</strong> ${pageContext.errorData.requestURI}</p>
            <pre>
                <%-- Stampa la stack trace in un blocco <pre> per la leggibilità --%>
                <% 
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    exception.printStackTrace(pw);
                    out.print(sw.toString());
                %>
            </pre>
        </div>
    </c:if>

</body>
</html>
```

## 3. Gestione Manuale (Non Consigliata)

È possibile gestire le eccezioni direttamente in una pagina JSP utilizzando scriptlet con blocchi `try-catch`. Tuttavia, questo approccio è **fortemente sconsigliato** perché mescola la logica di business e di controllo degli errori con la vista, violando il principio di separazione delle competenze (Separation of Concerns).

```jsp
<%-- Esempio da evitare --%>
<%
    try {
        // Codice che potrebbe lanciare un'eccezione
        int risultato = 10 / 0;
        out.println("Il risultato è: " + risultato);
    } catch (Exception e) {
        out.println("Si è verificato un errore durante il calcolo.");
    }
%>
```

## Tabella Riassuntiva dei Comandi e Oggetti

| Elemento            | Tipo                | Descrizione                                                                                                                            |
| ------------------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `<error-page>`      | Tag XML (`web.xml`) | Mappa un codice di errore HTTP o un tipo di eccezione Java a una risorsa (es. una JSP) che gestirà l'errore.                          |
| `isErrorPage="true"`| Attributo Direttiva | `<%@ page isErrorPage="true" %>`. Specifica che la JSP è una pagina di errore, rendendo disponibile l'oggetto `exception`.             |
| `exception`         | Oggetto Implicito   | Disponibile solo nelle pagine con `isErrorPage="true"`. Contiene l'eccezione (`Throwable`) che ha causato l'errore.                     |
| `errorData`         | Oggetto `PageContext` | Accessibile tramite `${pageContext.errorData}`, fornisce informazioni contestuali sull'errore, come l'URI della richiesta (`requestURI`). |

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                                                           |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Gestione Dichiarativa**     | Configurare il comportamento dell'applicazione (in questo caso, la gestione degli errori) tramite file di configurazione (es. `web.xml`) invece che nel codice.         |
| **Descrittore di Deployment** | Il file `web.xml`, che descrive come un'applicazione web Java EE deve essere deployata e configurata dal container delle servlet.                                        |
| **Oggetto Implicito `exception`** | Un oggetto disponibile in una pagina di errore JSP che rappresenta l'eccezione che si è verificata.                                                                    |
| **Separation of Concerns (SoC)** | Un principio di progettazione software che promuove la suddivisione di un'applicazione in parti distinte, ognuna con una responsabilità specifica (es. vista, controllo, modello). |
