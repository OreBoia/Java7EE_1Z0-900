# Introduzione a JavaServer Pages (JSP)

JavaServer Pages (JSP) è una tecnologia Java EE progettata per semplificare la creazione di pagine web con contenuto dinamico. Mentre è possibile generare HTML direttamente da una Servlet usando `PrintWriter`, questo approccio mescola la logica di presentazione con la logica di business, rendendo il codice difficile da leggere e mantenere.

Le JSP risolvono questo problema permettendo agli sviluppatori di scrivere pagine HTML (o altri formati testuali) e di inserire al loro interno frammenti di codice Java o tag speciali per generare le parti dinamiche.

In Java EE 7, le versioni di riferimento sono **JSP 2.3** e **JSTL 1.2** (JSP Standard Tag Library).

## Il Concetto Fondamentale: da JSP a Servlet

Una JSP non è altro che un modo più conveniente per scrivere una Servlet. Il Web Container, dietro le quinte, esegue un processo di **traduzione e compilazione** per trasformare un file `.jsp` in una vera e propria Servlet.

## Ciclo di Vita di una JSP

Il ciclo di vita di una JSP si articola in due fasi principali: la fase di traduzione (che avviene una sola volta) e la fase di richiesta (che segue il ciclo di vita di una Servlet).

1. **Fase di Traduzione e Compilazione** (gestita dal container):
    * **Traduzione**: Quando una JSP viene richiesta per la prima volta (o all'avvio dell'applicazione, se precompilata), il container traduce il file `.jsp` in un file sorgente Java (`.java`). Durante questo processo, tutto il markup statico (HTML) viene convertito in istruzioni `out.println(...)` all'interno della Servlet generata, mentre gli elementi dinamici (scriptlet, espressioni) vengono integrati come codice Java.
    * **Compilazione**: Il file `.java` generato viene poi compilato in un file bytecode Java (`.class`).

2. **Fase di Esecuzione** (ciclo di vita della Servlet generata):
    * **Inizializzazione**: Il container carica la classe della Servlet generata, ne crea un'istanza e chiama il suo metodo `init()` (che corrisponde a `jspInit()` in una JSP). Questo avviene una sola volta.
    * **Gestione delle Richieste**: Per ogni richiesta successiva, il container invoca il metodo `_jspService()` (che è l'equivalente del metodo `service()` di una Servlet). Questo metodo esegue il codice della pagina per generare la risposta dinamica.
    * **Distruzione**: Quando l'applicazione viene fermata, il container chiama il metodo `destroy()` (`jspDestroy()` in una JSP) per rilasciare le risorse.

Gli sviluppatori di solito non interagiscono direttamente con la Servlet generata (che si trova nelle cartelle di lavoro del server, es. la directory `work` di Tomcat), ma è fondamentale capire che una JSP è, a tutti gli effetti, una Servlet.

## Esempio di Codice: Una Semplice Pagina JSP

Questa pagina JSP mostra l'ora corrente e saluta un utente il cui nome è passato come parametro.

**`welcome.jsp`**

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date" %>
<html>
<head>
    <title>Pagina di Benvenuto JSP</title>
</head>
<body>
    <h1>Benvenuto nella nostra pagina JSP!</h1>

    <p>
        <%-- Questo è un commento JSP, non visibile nel sorgente HTML --%>
        <%-- Le direttive (sopra) importano classi o definiscono attributi della pagina --%>
    </p>

    <p>
        <strong>Ora corrente del server:</strong>
        <%-- Questa è un'espressione JSP. Il risultato viene stampato nella risposta. --%>
        <%= new Date() %>
    </p>

    <p>
        <%-- Questo è uno scriptlet. Contiene codice Java che viene eseguito. --%>
        <%
            String username = request.getParameter("user");
            if (username == null || username.trim().isEmpty()) {
                username = "Ospite";
            }
        %>
        Saluti, <strong><%= username %></strong>!
    </p>
</body>
</html>
```

## Tabella dei Termini Chiave

| Termine | Descrizione |
| :--- | :--- |
| **JSP (JavaServer Pages)** | Una tecnologia lato server per creare pagine web dinamiche mescolando markup statico con codice Java o tag speciali. |
| **Ciclo di Vita JSP** | Il processo in più fasi (traduzione, compilazione, inizializzazione, servizio, distruzione) che una pagina JSP attraversa. |
| **Traduzione** | La fase in cui il container converte un file `.jsp` in un file sorgente di una Servlet (`.java`). |
| **Compilazione** | La fase in cui la Servlet generata viene compilata in bytecode Java (`.class`). |
| **Servlet Generata** | La classe Servlet creata automaticamente dal container a partire da un file JSP. |
| **Scriptlet (`<% ... %>`)** | Un blocco di codice Java inserito in una pagina JSP che viene eseguito durante la gestione della richiesta. (Il suo uso è scoraggiato in favore di JSTL e EL). |
| **Espressione (`<%= ... %>`)** | Un'espressione Java il cui risultato viene convertito in stringa e scritto direttamente nell'output della risposta. |
| **Direttiva (`<%@ ... %>`)** | Un'istruzione per il container JSP, come `page` (per importare classi, definire il content type) o `taglib` (per importare librerie di tag). |
| **JSTL (JSP Standard Tag Library)** | Una libreria di tag standard che fornisce funzionalità comuni (iterazioni, condizionali, ecc.) per evitare l'uso di scriptlet. |
