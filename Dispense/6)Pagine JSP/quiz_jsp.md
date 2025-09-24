# Quiz Avanzato su Pagine JSP - Domande Miste con Codice

Questo quiz avanzato copre i concetti delle Pagine JSP in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Ciclo di Vita e Traduzione JSP

### 💻 Domanda 1

Osserva questo file JSP e la sua configurazione:

```jsp
<%-- welcome.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date" %>
<%!
    private int visitCount = 0;
    
    public void jspInit() {
        System.out.println("JSP inizializzata");
    }
    
    public void jspDestroy() {
        System.out.println("JSP distrutta, visite totali: " + visitCount);
    }
%>
<html>
<body>
    <h1>Benvenuto! Visita numero: <%= ++visitCount %></h1>
    <p>Data: <%= new Date() %></p>
</body>
</html>
```

Cosa succede alla variabile `visitCount` quando due utenti visitano contemporaneamente la pagina?

- a) Ogni utente ha il suo counter separato
- b) Il counter è condiviso tra tutti gli utenti
- c) Il counter viene resettato ad ogni richiesta
- d) Si verifica un errore di compilazione

---

### 🔵 Domanda 2

Durante quale fase del ciclo di vita JSP viene eseguita la traduzione da `.jsp` a `.java`?

- a) Ad ogni richiesta HTTP
- b) Solo alla prima richiesta (o se il file JSP è modificato)
- c) Durante lo startup del server
- d) Mai, le JSP vengono interpretate direttamente

---

### 💻 Domanda 3

Analizza questa configurazione JSP:

```jsp
<%@ page session="false" errorPage="/error.jsp" isThreadSafe="false" %>
<%@ page import="java.util.*, java.text.*" %>

<html>
<body>
    <h1>Configurazione Test</h1>
    <%
        // Codice che potrebbe generare eccezioni
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = sdf.parse(request.getParameter("date"));
    %>
    <p>Data parsata: <%= today %></p>
</body>
</html>
```

Cosa significa `isThreadSafe="false"`?

- a) La JSP non può essere eseguita in ambiente multi-thread
- b) Il container deve sincronizzare l'accesso alla JSP
- c) La JSP viene eseguita in un thread separato
- d) È un parametro obsoleto che non ha effetto

---

## 2. Elementi Sintattici JSP

### 🟢 Domanda 4

Quali delle seguenti sono **direttive JSP** valide? (Seleziona tutte)

- a) `<%@ page %>`
- b) `<%@ include %>`
- c) `<%@ taglib %>`
- d) `<%@ import %>`
- e) `<%@ forward %>`

---

### 💻 Domanda 5

Osserva questi diversi elementi JSP:

```jsp
<%@ page contentType="text/html" %>
<%-- Questo è un commento JSP --%>
<!-- Questo è un commento HTML -->

<%! 
    String globalMessage = "Messaggio globale";
    
    public String formatMessage(String msg) {
        return msg.toUpperCase();
    }
%>

<%
    String localMessage = "Messaggio locale";
    localMessage = formatMessage(localMessage);
%>

<p>Globale: <%= globalMessage %></p>
<p>Locale: <%= localMessage %></p>
<p>Diretto: <%= formatMessage("test") %></p>
```

Quale differenza c'è tra `<%! %>` e `<% %>`?

- a) Non c'è differenza, sono sinonimi
- b) `<%! %>` dichiara membri della classe, `<% %>` contiene codice del metodo service
- c) `<%! %>` è per commenti, `<% %>` per codice
- d) `<%! %>` è deprecato in JSP 2.3

---

### 🔵 Domanda 6

Nel codice precedente, quale commento appare nel sorgente HTML generato?

- a) Solo il commento JSP `<%-- --%>`
- b) Solo il commento HTML `<!-- -->`
- c) Entrambi i commenti
- d) Nessuno dei due commenti

---

## 3. Oggetti Impliciti JSP

### 💻 Domanda 7

Analizza questo utilizzo di oggetti impliciti:

```jsp
<%@ page contentType="text/html" %>
<html>
<body>
    <h1>Informazioni Richiesta</h1>
    
    <p>Metodo HTTP: <%= request.getMethod() %></p>
    <p>URI richiesta: <%= request.getRequestURI() %></p>
    <p>User Agent: <%= request.getHeader("User-Agent") %></p>
    
    <p>Sessione ID: <%= session.getId() %></p>
    <p>Sessione nuova: <%= session.isNew() %></p>
    
    <p>Context Path: <%= application.getContextPath() %></p>
    <p>Server Info: <%= application.getServerInfo() %></p>
    
    <%
        pageContext.setAttribute("pageVar", "Valore pagina");
        request.setAttribute("requestVar", "Valore richiesta");
        session.setAttribute("sessionVar", "Valore sessione");
        application.setAttribute("appVar", "Valore applicazione");
    %>
    
    <p>Page scope: <%= pageContext.getAttribute("pageVar") %></p>
    <p>Da pageContext: <%= pageContext.findAttribute("requestVar") %></p>
</body>
</html>
```

Quale oggetto implicito ha lo scope più ampio?

- a) `request`
- b) `session`
- c) `application`
- d) `pageContext`

---

### 🟢 Domanda 8

Quali degli seguenti sono **oggetti impliciti** validi in JSP? (Seleziona tutti)

- a) `request`
- b) `response`
- c) `session`
- d) `context`
- e) `out`
- f) `page`
- g) `config`

---

### 💻 Domanda 9

Osserva questo codice per il redirect condizionale:

```jsp
<%
    String userRole = (String) session.getAttribute("role");
    
    if (userRole == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    if (!"admin".equals(userRole)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato");
        return;
    }
%>

<html>
<body>
    <h1>Area Amministratore</h1>
    <p>Benvenuto, amministratore!</p>
</body>
</html>
```

Perché è importante usare `return` dopo `sendRedirect()` e `sendError()`?

- a) Per migliorare le performance
- b) Per evitare che il resto della pagina venga processato
- c) È obbligatorio per la sintassi JSP
- d) Per rilasciare la memoria utilizzata

---

## 4. Expression Language (EL)

### 💻 Domanda 10

Analizza questo uso di Expression Language:

```jsp
<%@ page contentType="text/html" %>
<%
    User user = new User("Mario", "Rossi", 30);
    request.setAttribute("currentUser", user);
    
    String[] hobbies = {"calcio", "lettura", "cinema"};
    request.setAttribute("userHobbies", hobbies);
    
    Map<String, String> preferences = new HashMap<>();
    preferences.put("theme", "dark");
    preferences.put("language", "it");
    request.setAttribute("userPrefs", preferences);
%>

<html>
<body>
    <h1>Profilo Utente</h1>
    
    <p>Nome: ${currentUser.nome}</p>
    <p>Cognome: ${currentUser.cognome}</p>
    <p>Età: ${currentUser.eta}</p>
    
    <p>Primo hobby: ${userHobbies[0]}</p>
    <p>Hobby totali: ${userHobbies.length}</p>
    
    <p>Tema: ${userPrefs.theme}</p>
    <p>Lingua: ${userPrefs['language']}</p>
    
    <p>Maggiorenne: ${currentUser.eta >= 18}</p>
    <p>Adulto: ${currentUser.eta gt 21}</p>
</body>
</html>
```

Quale sintassi EL è **equivalente** a `${userPrefs.theme}`?

- a) `${userPrefs.get("theme")}`
- b) `${userPrefs["theme"]}`
- c) `${userPrefs->theme}`
- d) `${userPrefs::theme}`

---

### 🔵 Domanda 11

Nel codice precedente, cosa stampa `${currentUser.eta gt 21}`?

- a) `true` se l'età è maggiore di 21
- b) `false` se l'età è maggiore di 21
- c) Il valore numerico dell'età
- d) Un errore di sintassi

---

### 💻 Domanda 12

Osserva questo uso avanzato di EL:

```jsp
<%
    List<Product> products = Arrays.asList(
        new Product("Laptop", 999.99, true),
        new Product("Mouse", 29.99, false),
        new Product("Keyboard", 79.99, true)
    );
    request.setAttribute("products", products);
%>

<p>Parametro categoria: ${param.category}</p>
<p>Header Accept: ${header.Accept}</p>
<p>Cookie JSESSIONID: ${cookie.JSESSIONID.value}</p>

<p>Lista prodotti vuota: ${empty products}</p>
<p>Prodotti disponibili: ${not empty products}</p>

<p>Primo prodotto disponibile: ${products[0].available}</p>
<p>Prezzo con sconto: ${products[0].price * 0.9}</p>

<p>Context path: ${pageContext.request.contextPath}</p>
<p>Session ID: ${pageContext.session.id}</p>
```

Cosa restituisce `${empty products}` se la lista contiene 3 elementi?

- a) `true`
- b) `false`
- c) `3`
- d) Un errore

---

## 5. JSTL - JSP Standard Tag Library

### 💻 Domanda 13

Analizza questo codice JSTL:

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    List<Order> orders = getOrdersFromDatabase();
    request.setAttribute("orderList", orders);
    
    BigDecimal total = new BigDecimal("1299.50");
    request.setAttribute("totalAmount", total);
%>

<html>
<body>
    <h1>Lista Ordini</h1>
    
    <c:choose>
        <c:when test="${empty orderList}">
            <p>Nessun ordine trovato.</p>
        </c:when>
        <c:otherwise>
            <table>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Importo</th>
                    <th>Data</th>
                </tr>
                <c:forEach items="${orderList}" var="order" varStatus="status">
                    <tr class="${status.index % 2 == 0 ? 'even' : 'odd'}">
                        <td>${order.id}</td>
                        <td>${order.customerName}</td>
                        <td><fmt:formatNumber value="${order.amount}" type="currency"/></td>
                        <td><fmt:formatDate value="${order.date}" pattern="dd/MM/yyyy"/></td>
                    </tr>
                </c:forEach>
            </table>
            
            <p>Totale ordini: <fmt:formatNumber value="${totalAmount}" type="currency"/></p>
        </c:otherwise>
    </c:choose>
    
    <c:if test="${param.debug == 'true'}">
        <div class="debug">
            <h3>Debug Info</h3>
            <p>Numero ordini: ${orderList.size()}</p>
        </div>
    </c:if>
</body>
</html>
```

A cosa serve `varStatus="status"` nel tag `<c:forEach>`?

- a) Contiene informazioni sul ciclo corrente (indice, primo/ultimo elemento, ecc.)
- b) Contiene lo stato dell'oggetto iterato
- c) È obbligatorio per il funzionamento del ciclo
- d) Serve per il debugging del ciclo

---

### 🟢 Domanda 14

Quali delle seguenti sono **librerie JSTL** standard? (Seleziona tutte)

- a) Core (`c`)
- b) Formatting (`fmt`)
- c) Functions (`fn`)
- d) SQL (`sql`)
- e) Validation (`val`)
- f) XML (`x`)

---

### 💻 Domanda 15

Osserva questo uso di JSTL Functions:

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String message = "Benvenuto nel nostro sito web!";
    request.setAttribute("welcomeMessage", message);
    
    List<String> tags = Arrays.asList("java", "jsp", "jstl", "web");
    request.setAttribute("tagList", tags);
%>

<p>Messaggio: ${welcomeMessage}</p>
<p>Lunghezza: ${fn:length(welcomeMessage)}</p>
<p>Maiuscolo: ${fn:toUpperCase(welcomeMessage)}</p>
<p>Sottostringhe: ${fn:substring(welcomeMessage, 0, 10)}</p>
<p>Contiene 'sito': ${fn:contains(welcomeMessage, 'sito')}</p>

<p>Numero di tag: ${fn:length(tagList)}</p>
<p>Tag uniti: ${fn:join(tagList, ', ')}</p>

<c:set var="searchTerm" value="jsp"/>
<p>Tag che contengono '${searchTerm}':</p>
<c:forEach items="${tagList}" var="tag">
    <c:if test="${fn:contains(tag, searchTerm)}">
        <span class="highlight">${tag}</span>
    </c:if>
</c:forEach>
```

Cosa restituisce `${fn:length(tagList)}`?

- a) La lunghezza della stringa "tagList"
- b) Il numero di elementi nella lista (4)
- c) La lunghezza del primo elemento della lista
- d) Un errore perché fn:length funziona solo con stringhe

---

## 6. Include e Forward Actions

### 💻 Domanda 16

Analizza questa differenza tra inclusioni:

```jsp
<%-- main.jsp --%>
<%@ page contentType="text/html" %>
<%@ include file="header.jspf" %>

<html>
<body>
    <h1>Pagina Principale</h1>
    
    <jsp:include page="dynamic-content.jsp">
        <jsp:param name="section" value="homepage"/>
        <jsp:param name="userId" value="${sessionScope.userId}"/>
    </jsp:include>
    
    <jsp:include page="sidebar.jsp"/>
    
    <%@ include file="footer.jspf" %>
</body>
</html>
```

```jsp
<%-- dynamic-content.jsp --%>
<div class="content">
    <h2>Contenuto per: ${param.section}</h2>
    <p>Utente: ${param.userId}</p>
    <p>Generato alle: <%= new java.util.Date() %></p>
</div>
```

Qual è la **principale differenza** tra `<%@ include %>` e `<jsp:include>`?

- a) `<%@ include %>` è per file HTML, `<jsp:include>` per JSP
- b) `<%@ include %>` include a tempo di traduzione, `<jsp:include>` a runtime
- c) Non c'è differenza, sono sinonimi
- d) `<%@ include %>` è più veloce ma meno flessibile

---

### 🔵 Domanda 17

Nel codice precedente, quale vantaggio offre `<jsp:param>`?

- a) Migliori performance
- b) Possibilità di passare parametri alla risorsa inclusa
- c) Caching automatico del contenuto
- d) Validazione automatica dei parametri

---

### 💻 Domanda 18

Osserva questo uso di forward action:

```jsp
<%-- controller.jsp --%>
<%
    String action = request.getParameter("action");
    String targetPage = null;
    
    if ("login".equals(action)) {
        targetPage = "login-form.jsp";
    } else if ("logout".equals(action)) {
        session.invalidate();
        targetPage = "goodbye.jsp";
    } else if ("profile".equals(action)) {
        if (session.getAttribute("user") != null) {
            targetPage = "user-profile.jsp";
        } else {
            targetPage = "login-form.jsp";
        }
    } else {
        targetPage = "home.jsp";
    }
%>

<jsp:forward page="<%= targetPage %>">
    <jsp:param name="source" value="controller"/>
    <jsp:param name="timestamp" value="<%= System.currentTimeMillis() %>"/>
</jsp:forward>
```

Cosa succede al contenuto HTML dopo il tag `<jsp:forward>`?

- a) Viene incluso insieme al contenuto della pagina di destinazione
- b) Viene ignorato e non inviato al client
- c) Causa un errore di compilazione
- d) Viene inviato prima del forward

---

## 7. Gestione degli Errori in JSP

### 💻 Domanda 19

Analizza questa configurazione di error handling:

```xml
<!-- web.xml -->
<error-page>
    <error-code>404</error-code>
    <location>/WEB-INF/errors/404.jsp</location>
</error-page>

<error-page>
    <exception-type>java.lang.NullPointerException</exception-type>
    <location>/WEB-INF/errors/npe-error.jsp</location>
</error-page>

<error-page>
    <exception-type>java.lang.Exception</exception-type>
    <location>/WEB-INF/errors/generic-error.jsp</location>
</error-page>
```

```jsp
<%-- generic-error.jsp --%>
<%@ page isErrorPage="true" contentType="text/html" %>
<html>
<body>
    <h1>Si è verificato un errore</h1>
    
    <p>Codice errore: ${pageContext.errorData.statusCode}</p>
    <p>URI richiesta: ${pageContext.errorData.requestURI}</p>
    <p>Messaggio: ${pageContext.errorData.throwable.message}</p>
    
    <c:if test="${not empty exception}">
        <h3>Dettagli eccezione:</h3>
        <p>Tipo: ${exception.class.name}</p>
        <p>Messaggio: ${exception.message}</p>
    </c:if>
</body>
</html>
```

Perché si usa `isErrorPage="true"` nella pagina di errore?

- a) Per abilitare l'oggetto implicito `exception`
- b) Per migliorare le performance
- c) Per prevenire il caching della pagina
- d) È obbligatorio per tutte le pagine JSP

---

### 🔵 Domanda 20

Se si verifica una `NullPointerException`, quale pagina di errore viene utilizzata?

- a) `404.jsp`
- b) `npe-error.jsp`
- c) `generic-error.jsp`
- d) La prima trovata in web.xml

---

## 8. JavaBeans e JSP Actions

### 💻 Domanda 21

Osserva questo uso di JavaBeans con JSP:

```java
// User.java
public class User {
    private String username;
    private String email;
    private int age;
    private boolean active;
    
    // Costruttori, getter e setter standard
    public User() {}
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

```jsp
<%-- form-processing.jsp --%>
<jsp:useBean id="user" class="com.example.User" scope="request"/>
<jsp:setProperty name="user" property="*"/>

<html>
<body>
    <h1>Dati Utente Ricevuti</h1>
    
    <p>Username: <jsp:getProperty name="user" property="username"/></p>
    <p>Email: <jsp:getProperty name="user" property="email"/></p>
    <p>Età: <jsp:getProperty name="user" property="age"/></p>
    <p>Attivo: <jsp:getProperty name="user" property="active"/></p>
    
    <%-- Equivalente con EL --%>
    <h2>Con Expression Language:</h2>
    <p>Username: ${user.username}</p>
    <p>Email: ${user.email}</p>
    <p>Età: ${user.age}</p>
    <p>Attivo: ${user.active}</p>
</body>
</html>
```

Cosa fa `<jsp:setProperty name="user" property="*"/>`?

- a) Imposta tutte le proprietà del bean con i parametri della richiesta corrispondenti
- b) Resetta tutte le proprietà del bean a null
- c) Copia tutte le proprietà da un altro bean
- d) Valida tutte le proprietà del bean

---

### 🔵 Domanda 22

Quale dei seguenti **NON** è un requisito per un JavaBean?

- a) Deve avere un costruttore pubblico senza parametri
- b) Le proprietà devono essere private
- c) Deve implementare l'interfaccia Serializable
- d) Deve avere metodi getter e setter pubblici

---

### 💻 Domanda 23

Analizza questo ciclo di vita del bean:

```jsp
<%-- page1.jsp --%>
<jsp:useBean id="counter" class="com.example.Counter" scope="session"/>
<jsp:setProperty name="counter" property="value" value="1"/>

<p>Valore iniziale: <jsp:getProperty name="counter" property="value"/></p>
<a href="page2.jsp">Vai alla pagina 2</a>
```

```jsp
<%-- page2.jsp --%>
<jsp:useBean id="counter" class="com.example.Counter" scope="session"/>
<%
    counter.increment();
%>

<p>Valore incrementato: <jsp:getProperty name="counter" property="value"/></p>
<a href="page3.jsp">Vai alla pagina 3</a>
```

```jsp
<%-- page3.jsp --%>
<jsp:useBean id="counter" class="com.example.Counter" scope="session"/>

<p>Valore finale: ${counter.value}</p>
```

Se un utente visita page1, poi page2, poi page3, quale valore mostrerà page3?

- a) 1
- b) 2
- c) 3
- d) Dipende dall'implementazione di Counter

---

## 9. Tag Libraries Personalizzate

### 💻 Domanda 24

Osserva questa definizione di tag personalizzato:

```java
// FormatDateTag.java
public class FormatDateTag extends SimpleTagSupport {
    private Date date;
    private String pattern = "dd/MM/yyyy";
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    @Override
    public void doTag() throws JspException, IOException {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            getJspContext().getOut().write(sdf.format(date));
        }
    }
}
```

```xml
<!-- WEB-INF/tags.tld -->
<taglib>
    <tlib-version>1.0</tlib-version>
    <short-name>custom</short-name>
    <uri>http://example.com/tags</uri>
    
    <tag>
        <name>formatDate</name>
        <tag-class>com.example.FormatDateTag</tag-class>
        <body-content>empty</body-content>
        <attribute>
            <name>date</name>
            <required>true</required>
            <rtexprvalue>true</rtexprvalue>
            <type>java.util.Date</type>
        </attribute>
        <attribute>
            <name>pattern</name>
            <required>false</required>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
    </tag>
</taglib>
```

```jsp
<%-- usage.jsp --%>
<%@ taglib prefix="custom" uri="http://example.com/tags" %>
<%
    Date now = new Date();
    request.setAttribute("currentDate", now);
%>

<p>Data default: <custom:formatDate date="${currentDate}"/></p>
<p>Data custom: <custom:formatDate date="${currentDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
```

Cosa significa `rtexprvalue="true"` nella definizione TLD?

- a) L'attributo è obbligatorio
- b) L'attributo può contenere espressioni runtime (EL, scriptlet)
- c) L'attributo viene validato a runtime
- d) L'attributo può essere null

---

### 🔵 Domanda 25

Quale classe è la **base più semplice** per creare tag personalizzati?

- a) `TagSupport`
- b) `BodyTagSupport`
- c) `SimpleTagSupport`
- d) `CustomTagSupport`

---

## 10. Performance e Best Practices

### 💻 Domanda 26

Analizza queste due implementazioni per mostrare una lista:

```jsp
<%-- Approccio A: Solo scriptlet --%>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    if (products != null && !products.isEmpty()) {
        out.println("<ul>");
        for (Product p : products) {
            out.println("<li>" + p.getName() + " - €" + p.getPrice() + "</li>");
        }
        out.println("</ul>");
    } else {
        out.println("<p>Nessun prodotto disponibile.</p>");
    }
%>
```

```jsp
<%-- Approccio B: JSTL + EL --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:choose>
    <c:when test="${not empty products}">
        <ul>
            <c:forEach items="${products}" var="product">
                <li>${product.name} - <fmt:formatNumber value="${product.price}" type="currency"/></li>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
        <p>Nessun prodotto disponibile.</p>
    </c:otherwise>
</c:choose>
```

Quale approccio è **preferibile** e perché?

- a) Approccio A: è più veloce ed efficiente
- b) Approccio B: migliore separazione tra logica e presentazione
- c) Sono equivalenti in termini di performance e manutenibilità
- d) Approccio A: più facile da debuggare

---

### 🟢 Domanda 27

Quali delle seguenti sono **best practices** per le JSP? (Seleziona tutte)

- a) Evitare scriptlet Java nelle pagine
- b) Usare EL invece di espressioni Java quando possibile
- c) Utilizzare JSTL per logica di controllo
- d) Mettere la logica di business direttamente nella JSP
- e) Usare JavaBeans per incapsulare i dati

---

### 💻 Domanda 28

Osserva questa ottimizzazione per il caching:

```jsp
<%@ page contentType="text/html" %>
<%@ page buffer="16kb" autoFlush="false" %>

<%
    response.setHeader("Cache-Control", "public, max-age=3600");
    response.setDateHeader("Expires", System.currentTimeMillis() + 3600000);
%>

<html>
<body>
    <h1>Contenuto Semi-Statico</h1>
    
    <%-- Contenuto che cambia raramente --%>
    <div class="static-content">
        <jsp:include page="/WEB-INF/includes/company-info.jsp"/>
    </div>
    
    <%-- Contenuto dinamico minimo --%>
    <div class="dynamic-content">
        <p>Ultima visualizzazione: <%= new java.util.Date() %></p>
    </div>
</body>
</html>
```

Cosa fa l'attributo `buffer="16kb" autoFlush="false"`?

- a) Imposta la dimensione del buffer di output e disabilita il flush automatico
- b) Limita la dimensione massima della pagina generata
- c) Abilita il caching lato server della pagina
- d) Comprime automaticamente l'output HTML

---

### 🔵 Domanda 29

Quale delle seguenti pratiche **peggiora** le performance delle JSP?

- a) Usare JSTL invece di scriptlet
- b) Includere molti file con `<%@ include %>`
- c) Eseguire query SQL direttamente negli scriptlet
- d) Usare Expression Language per accedere ai dati

---

### 💻 Domanda 30

Analizza questo pattern di organizzazione:

```jsp
<%-- main-template.jsp --%>
<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>${pageTitle}</title>
    <jsp:include page="/WEB-INF/includes/head-resources.jsp"/>
</head>
<body>
    <jsp:include page="/WEB-INF/includes/header.jsp"/>
    
    <main class="content">
        <jsp:include page="${contentPage}"/>
    </main>
    
    <jsp:include page="/WEB-INF/includes/footer.jsp"/>
    
    <jsp:include page="/WEB-INF/includes/scripts.jsp"/>
</body>
</html>
```

```java
// TemplateServlet.java
@WebServlet("/page/*")
public class TemplateServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String page = request.getPathInfo().substring(1);
        
        request.setAttribute("pageTitle", "Titolo per " + page);
        request.setAttribute("contentPage", "/WEB-INF/pages/" + page + ".jsp");
        
        request.getRequestDispatcher("/WEB-INF/templates/main-template.jsp")
               .forward(request, response);
    }
}
```

Quale pattern architetturale implementa questo codice?

- a) Model-View-Controller (MVC)
- b) Template Method Pattern
- c) Front Controller Pattern
- d) Page Template Pattern

---

---

## Risposte Corrette

### 1. **b)** Il counter è condiviso tra tutti gli utenti

Le variabili dichiarate in `<%! %>` sono variabili di istanza della servlet e quindi condivise.

### 2. **b)** Solo alla prima richiesta (o se il file JSP è modificato)

La traduzione avviene quando necessario, non ad ogni richiesta.

### 3. **b)** Il container deve sincronizzare l'accesso alla JSP

`isThreadSafe="false"` indica che la JSP non è thread-safe e richiede sincronizzazione.

### 4. **a, b, c)** `page`, `include`, `taglib`

`import` e `forward` non sono direttive JSP validi.

### 5. **b)** `<%! %>` dichiara membri della classe, `<% %>` contiene codice del metodo service

`<%! %>` per dichiarazioni, `<% %>` per logica procedurale.

### 6. **b)** Solo il commento HTML `<!-- -->`

I commenti JSP `<%-- --%>` non appaiono nell'output HTML.

### 7. **c)** `application`

`application` (ServletContext) ha scope a livello di intera applicazione.

### 8. **a, b, c, e, f, g)** `request`, `response`, `session`, `out`, `page`, `config`

`context` non è un oggetto implicito valido (si usa `application`).

### 9. **b)** Per evitare che il resto della pagina venga processato

Dopo redirect/error, il processing deve fermarsi per evitare output aggiuntivo.

### 10. **b)** `${userPrefs["theme"]}`

La notazione con parentesi quadre è equivalente alla dot notation per le mappe.

### 11. **a)** `true` se l'età è maggiore di 21

`gt` è l'operatore "greater than" in EL.

### 12. **b)** `false`

`empty` restituisce `false` per collezioni non vuote.

### 13. **a)** Contiene informazioni sul ciclo corrente (indice, primo/ultimo elemento, ecc.)

`varStatus` fornisce metadati sul ciclo forEach.

### 14. **a, b, c, d, f)** Core, Formatting, Functions, SQL, XML

`val` (Validation) non è una libreria JSTL standard.

### 15. **b)** Il numero di elementi nella lista (4)

`fn:length` funziona sia con stringhe che con collezioni.

### 16. **b)** `<%@ include %>` include a tempo di traduzione, `<jsp:include>` a runtime

Include statica vs include dinamica.

### 17. **b)** Possibilità di passare parametri alla risorsa inclusa

`<jsp:param>` permette di passare parametri alle azioni JSP.

### 18. **b)** Viene ignorato e non inviato al client

`<jsp:forward>` interrompe il processing della pagina corrente.

### 19. **a)** Per abilitare l'oggetto implicito `exception`

`isErrorPage="true"` rende disponibile l'oggetto `exception`.

### 20. **b)** `npe-error.jsp`

Le eccezioni specifiche hanno priorità su quelle generiche.

### 21. **a)** Imposta tutte le proprietà del bean con i parametri della richiesta corrispondenti

`property="*"` fa il mapping automatico di tutti i parametri.

### 22. **c)** Deve implementare l'interfaccia Serializable

Serializable non è obbligatorio per i JavaBeans.

### 23. **b)** 2

Il bean è in scope session, quindi il valore viene incrementato da 1 a 2.

### 24. **b)** L'attributo può contenere espressioni runtime (EL, scriptlet)

`rtexprvalue="true"` abilita le espressioni runtime nell'attributo.

### 25. **c)** `SimpleTagSupport`

`SimpleTagSupport` è la classe base più semplice per tag personalizzati.

### 26. **b)** Approccio B: migliore separazione tra logica e presentazione

JSTL+EL promuove la separazione tra logica e presentazione.

### 27. **a, b, c, e)** Evitare scriptlet, usare EL, usare JSTL, usare JavaBeans

La logica di business non dovrebbe stare nella JSP.

### 28. **a)** Imposta la dimensione del buffer di output e disabilita il flush automatico

`buffer` imposta la dimensione del buffer, `autoFlush="false"` disabilita il flush automatico.

### 29. **c)** Eseguire query SQL direttamente negli scriptlet

Le query SQL nella JSP violano la separazione delle responsabilità e peggiorano le performance.

### 30. **d)** Page Template Pattern

Implementa un template riutilizzabile con contenuto dinamico.
