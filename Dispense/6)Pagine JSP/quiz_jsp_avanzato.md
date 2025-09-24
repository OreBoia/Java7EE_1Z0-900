# Quiz Avanzato su Pagine JSP - Volume 2

Questo quiz avanzato copre i concetti delle Pagine JSP in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Ciclo di Vita e Traduzione JSP

### 💻 Domanda 1

Considera la seguente direttiva in una pagina JSP:

```jsp
<%@ page isThreadSafe="true" %>
```

Cosa implica questa dichiarazione per il container JSP?

- a) Il container non impone alcuna sincronizzazione sull'accesso all'istanza della JSP, lasciando la responsabilità allo sviluppatore.
- b) Il container crea una nuova istanza della JSP per ogni richiesta.
- c) Il container serializza tutte le richieste alla pagina, mettendole in coda.
- d) La pagina non può accedere a oggetti in scope `session`.

---

### 🔵 Domanda 2

Se modifichi un file `.jspf` incluso staticamente (`<%@ include file="..." %>`) in diverse pagine JSP, cosa succede alla successiva richiesta?

- a) Solo il file `.jspf` viene ricompilato.
- b) Nessuna pagina viene ricompilata finché non viene modificata direttamente.
- c) Tutte le pagine JSP che includono quel file `.jspf` vengono ricompilate.
- d) Viene generato un errore di runtime.

---

## 2. Elementi Sintattici JSP

### 💻 Domanda 3

Analizza i seguenti commenti in una pagina JSP:

```jsp
<%-- Commento 1: Visibile solo agli sviluppatori --%>
<!-- Commento 2: Visibile nel sorgente HTML del client -->
<%
    // Commento 3: All'interno di uno scriptlet Java
%>
```

Quali di questi commenti vengono completamente ignorati durante la fase di traduzione da JSP a Servlet Java?

- a) Solo il Commento 1
- b) Solo il Commento 2
- c) Il Commento 1 e il Commento 3
- d) Tutti e tre i commenti

---

### 🟢 Domanda 4

Quali dei seguenti sono elementi di scripting JSP validi? (Seleziona tutte)

- a) `<%! int count = 0; %>` (Dichiarazione)
- b) `<% out.println("Hello"); %>` (Scriptlet)
- c) `<%= new java.util.Date() %>` (Espressione)
- d) `<%@ page language="java" %>` (Direttiva)
- e) `<jsp:forward page="next.jsp" />` (Azione)

---

## 3. Oggetti Impliciti JSP

### 💻 Domanda 5

Osserva il seguente codice che utilizza l'oggetto implicito `pageContext`:

```jsp
<%
    pageContext.setAttribute("myVar", "Page Scope", PageContext.PAGE_SCOPE);
    request.setAttribute("myVar", "Request Scope");
    session.setAttribute("myVar", "Session Scope");
%>

<p>Valore 1: ${myVar}</p>
<p>Valore 2: ${pageScope.myVar}</p>
<p>Valore 3: ${sessionScope.myVar}</p>
```

Quali valori verranno stampati?

- a) Valore 1: Request Scope, Valore 2: Page Scope, Valore 3: Session Scope
- b) Valore 1: Page Scope, Valore 2: Page Scope, Valore 3: Session Scope
- c) Valore 1: Session Scope, Valore 2: Page Scope, Valore 3: Session Scope
- d) Tutti e tre mostreranno "Page Scope".

---

### 🔵 Domanda 6

Qual è lo scopo principale dell'oggetto implicito `config` (`javax.servlet.ServletConfig`)?

- a) Fornire accesso ai parametri di configurazione dell'intera applicazione web (`web.xml`).
- b) Fornire accesso ai parametri di inizializzazione specifici della servlet JSP, definiti in `web.xml`.
- c) Gestire la configurazione della sessione utente.
- d) Contenere le informazioni di configurazione della pagina stessa, come il `contentType`.

---

## 4. Expression Language (EL)

### 💻 Domanda 7

Dato il seguente bean e la pagina JSP:

```java
public class Product {
    private String name;
    private double price;
    // getters e setters
}
```

```jsp
<%
    Product p = null;
    request.setAttribute("product", p);
%>

<p>Nome: ${product.name}</p>
<p>Vuoto: ${empty product}</p>
```

Cosa verrà visualizzato nella pagina?

- a) Verrà lanciata una `NullPointerException`.
- b) Nome: , Vuoto: true
- c) Nome: null, Vuoto: true
- d) La pagina non verrà compilata.

---

### 🟢 Domanda 8

Quali dei seguenti operatori EL sono validi per testare l'uguaglianza? (Seleziona tutte)

- a) `==`
- b) `eq`
- c) `equals`
- d) `=`

---

## 5. JSTL - JSP Standard Tag Library

### 💻 Domanda 9

Analizza il seguente codice JSTL per l'URL rewriting:

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="productUrl" value="/products.jsp">
    <c:param name="id" value="123"/>
    <c:param name="lang" value="it"/>
</c:url>

<a href="${productUrl}">Vedi Prodotto</a>
```

Quale URL verrà generato per il link, supponendo che il context path sia `/myApp`?

- a) `/products.jsp?id=123&lang=it`
- b) `/myApp/products.jsp?id=123&lang=it`
- c) `http://localhost:8080/myApp/products.jsp?id=123&lang=it`
- d) Dipende se la sessione è tracciata tramite URL. In tal caso, potrebbe includere un `jsessionid`.

---

### 🔵 Domanda 10

A cosa serve il tag `<c:catch>` della libreria Core di JSTL?

- a) A definire un blocco `try-catch` per il codice Java negli scriptlet.
- b) A catturare e gestire le eccezioni lanciate dalle azioni JSTL annidate al suo interno.
- c) A creare un punto di "cattura" per eventi custom.
- d) È un tag deprecato e non dovrebbe essere usato.

---

## 6. Include e Forward Actions

### 💻 Domanda 11

Considera `main.jsp` che inoltra una richiesta a `target.jsp`:

```jsp
<%-- main.jsp --%>
<h1>Titolo in main.jsp</h1>
<% response.flushBuffer(); %>
<jsp:forward page="target.jsp" />
```

```jsp
<%-- target.jsp --%>
<h2>Titolo in target.jsp</h2>
```

Cosa succede quando `main.jsp` viene richiesta?

- a) Il client riceve "Titolo in main.jsp" seguito da "Titolo in target.jsp".
- b) Il client riceve solo "Titolo in target.jsp".
- c) Viene lanciata una `IllegalStateException` perché il buffer è già stato svuotato prima del forward.
- d) Viene lanciato un errore di compilazione.

---

### 🔵 Domanda 12

Qual è un vantaggio chiave dell'utilizzo di `<jsp:include>` (azione dinamica) rispetto a `<%@ include %>` (direttiva statica)?

- a) Le performance sono significativamente migliori con `<jsp:include>`.
- b) `<jsp:include>` permette di includere risorse esterne al context dell'applicazione web.
- c) La risorsa inclusa con `<jsp:include>` viene processata come una richiesta separata, permettendo di usare logica condizionale per decidere quale pagina includere.
- d) `<%@ include %>` non può includere file con estensione `.jspf`.

---

## 7. Gestione degli Errori in JSP

### 💻 Domanda 13

In una pagina di errore configurata con `<%@ page isErrorPage="true" %>`, quale oggetto implicito ti permette di accedere ai dettagli dell'eccezione che ha causato l'errore?

```jsp
<%@ page isErrorPage="true" %>
<h1>Oops! Si è verificato un errore.</h1>
<p>Messaggio: ${pageContext.exception.message}</p>
```

- a) `error`
- b) `throwable`
- c) `exception`
- d) `pageContext.errorData`

---

### 🔵 Domanda 14

Se in `web.xml` definisci una pagina di errore per `java.io.IOException` e una generica per `java.lang.Throwable`, quale pagina verrà mostrata se si verifica una `FileNotFoundException` (che estende `IOException`)?

- a) La pagina definita per `java.lang.Throwable`.
- b) La pagina definita per `java.io.IOException`.
- c) La pagina di errore di default del server.
- d) Nessuna, l'errore non verrà gestito.

---

## 8. JavaBeans e JSP Actions

### 💻 Domanda 15

Analizza l'uso di `<jsp:useBean>`:

```jsp
<jsp:useBean id="user" class="com.example.User" scope="session" />
```

Cosa succede se un bean con `id="user"` esiste già nella sessione quando questa riga viene eseguita?

- a) Viene creato un nuovo bean `user` che sovrascrive quello esistente.
- b) Viene lanciata un'eccezione perché l'ID è già in uso.
- c) L'azione non fa nulla e la variabile `user` non viene creata.
- d) L'azione recupera il bean esistente dalla sessione e lo assegna alla variabile `user`.

---
---

## Risposte Corrette

### 1. **a)** Il container non impone alcuna sincronizzazione sull'accesso all'istanza della JSP

`isThreadSafe="true"` (il default) indica che lo sviluppatore garantisce che il codice (specialmente le dichiarazioni `<%! %>`) sia thread-safe. Il container non applicherà blocchi.

### 2. **c)** Tutte le pagine JSP che includono quel file `.jspf` vengono ricompilate

Con l'inclusione statica, il contenuto del file `.jspf` viene fuso nel file JSP principale prima della traduzione. Una modifica al frammento richiede la ricompilazione di tutte le pagine che lo contengono.

### 3. **a)** Solo il Commento 1

Il commento JSP (`<%-- --%>`) è l'unico che viene scartato durante la fase di traduzione. Il commento HTML viene passato al client e il commento Java rimane nel codice del metodo `_jspService`.

### 4. **a, b, c)**

Le direttive e le azioni non sono considerate "elementi di scripting". Gli elementi di scripting sono dichiarazioni, scriptlet ed espressioni.

### 5. **b)** Valore 1: Page Scope, Valore 2: Page Scope, Valore 3: Session Scope

Quando EL (`${myVar}`) non specifica uno scope, cerca la variabile partendo dallo scope più ristretto (page) a quello più ampio (application). Quindi `${myVar}` trova il valore in `pageScope`.

### 6. **b)** Fornire accesso ai parametri di inizializzazione specifici della servlet JSP

L'oggetto `config` (`ServletConfig`) contiene i parametri `<init-param>` definiti per quella specifica servlet nel `web.xml`, non i parametri di contesto (`<context-param>`), per i quali si usa `application`.

### 7. **b)** Nome: , Vuoto: true

EL è "null-safe". Se `product` è `null`, `${product.name}` non lancia una `NullPointerException` ma restituisce una stringa vuota. L'operatore `empty` restituisce `true` per `null`.

### 8. **a, b)** `==` e `eq`

Sono gli unici due operatori di uguaglianza validi in Expression Language.

### 9. **d)** Dipende se la sessione è tracciata tramite URL. In tal caso, potrebbe includere un `jsessionid`

Il tag `<c:url>` gestisce correttamente il context path e, cosa più importante, aggiunge automaticamente il `jsessionid` all'URL se i cookie sono disabilitati e il tracciamento della sessione via URL è attivo.

### 10. **b)** A catturare e gestire le eccezioni lanciate dalle azioni JSTL annidate al suo interno

`<c:catch>` è utile per gestire errori che possono verificarsi durante l'esecuzione di altri tag JSTL (es. parsing di numeri, accesso a risorse) senza interrompere il rendering della pagina.

### 11. **c)** Viene lanciata una `IllegalStateException`

`<jsp:forward>` deve essere eseguito prima che qualsiasi parte del corpo della risposta venga inviata al client. `response.flushBuffer()` svuota il buffer, rendendo impossibile un forward successivo.

### 12. **c)** La risorsa inclusa con `<jsp:include>` viene processata come una richiesta separata

Questo permette di usare logica per decidere dinamicamente quale pagina includere a runtime e di passare parametri tramite `<jsp:param>`.

### 13. **c)** `exception`

L'attributo `isErrorPage="true"` rende disponibile l'oggetto implicito `exception` (di tipo `java.lang.Throwable`) che contiene l'eccezione originale.

### 14. **b)** La pagina definita per `java.io.IOException`

Il container cerca la corrispondenza più specifica nella gerarchia delle eccezioni. Poiché `FileNotFoundException` è una sottoclasse di `IOException`, viene scelta la pagina di errore per `IOException`.

### 15. **d)** L'azione recupera il bean esistente dalla sessione e lo assegna alla variabile `user`

`<jsp:useBean>` crea una nuova istanza solo se non ne trova una esistente con lo stesso `id` e `scope`. Altrimenti, riutilizza quella esistente.
