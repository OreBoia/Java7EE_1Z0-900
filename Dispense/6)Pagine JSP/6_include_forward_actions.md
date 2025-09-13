# Includere Contenuto e Gestire il Flusso con le Azioni JSP

Le **Azioni Standard JSP** (JSP Standard Actions) sono tag simili a XML che forniscono funzionalità utili per controllare il comportamento del motore JSP. Permettono di includere file, inoltrare richieste e interagire con i componenti JavaBean.

A differenza delle direttive (es. `<%@ include %>`), che vengono processate durante la fase di traduzione della JSP in servlet, le azioni vengono eseguite a **runtime**, ovvero ogni volta che la pagina viene richiesta.

## Inclusione di Contenuto

### `<jsp:include>`: Inclusione Dinamica

L'azione `<jsp:include>` permette di includere il contenuto di un'altra risorsa (come un'altra JSP, una servlet o un file HTML) nella pagina corrente al momento della richiesta.

**Caratteristiche:**

- **Inclusione a Runtime**: La risorsa specificata viene eseguita e il suo output viene incluso nella pagina chiamante.
- **Dinamicità**: Poiché l'inclusione avviene a runtime, se la risorsa inclusa cambia, la pagina principale mostrerà il contenuto aggiornato alla richiesta successiva senza bisogno di ricompilazione.
- **Passaggio di Parametri**: È possibile passare parametri alla risorsa inclusa tramite il tag `<jsp:param>`.

#### Esempio di Codice

Supponiamo di avere un file `header.jsp` riutilizzabile:

```jsp
<%-- header.jsp --%>
<header>
    <h1>${param.titolo}</h1>
    <p>Benvenuto nel nostro sito!</p>
</header>
```

E una pagina principale `home.jsp` che lo include:

```jsp
<%-- home.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Home Page</title>
</head>
<body>
    <jsp:include page="header.jsp">
        <jsp:param name="titolo" value="Pagina Principale Dinamica"/>
    </jsp:include>

    <main>
        <p>Questo è il contenuto principale della pagina.</p>
    </main>

    <jsp:include page="footer.html" />
</body>
</html>
```

In questo esempio, `header.jsp` viene eseguito e il suo output (che include il parametro `titolo`) viene inserito nel punto in cui si trova `<jsp:include>`.

### Differenza tra `<jsp:include>` (Azione) e `<%@ include %>` (Direttiva)

È fondamentale non confondere l'azione di inclusione con la direttiva di inclusione.

| Caratteristica      | `<jsp:include>` (Azione)                                  | `<%@ include %>` (Direttiva)                               |
| ------------------- | --------------------------------------------------------- | ----------------------------------------------------------- |
| **Momento**         | **Runtime** (al momento della richiesta)                  | **Compile-time** (al momento della traduzione della JSP)    |
| **Tipo**            | **Dinamica**: Include l'output della risorsa eseguita.    | **Statica**: Incolla il codice sorgente del file incluso.   |
| **Risorse Incluse** | JSP, Servlet, HTML. La risorsa è un'entità separata.      | File di testo (JSPF, HTML, etc.). Il codice diventa parte della servlet principale. |
| **Performance**     | Leggermente più lenta perché richiede una chiamata interna. | Più veloce, perché il risultato è un'unica grande servlet. |
| **Uso Tipico**      | Per includere componenti dinamici (es. un carrello).      | Per includere layout statici (header, footer, menu).        |

## Inoltro delle Richieste (`forward`)

### `<jsp:forward>`

Questa azione inoltra la richiesta (request) e la risposta (response) a un'altra risorsa sul server (un'altra JSP, una servlet, etc.). Il controllo della richiesta non torna più alla pagina originale.

**Caratteristiche:**

- **Server-side**: L'inoltro è trasparente per il client (l'URL nel browser non cambia).
- **Buffer Svuotato**: Qualsiasi contenuto già scritto nel buffer della pagina corrente viene cancellato prima dell'inoltro.
- **Passaggio di Parametri**: Anche qui si possono aggiungere parametri con `<jsp:param>`.

#### Esempio di Codice

```jsp
<%-- dispatcher.jsp --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${param.sezione == 'profilo'}">
        <jsp:forward page="/WEB-INF/jsp/profilo.jsp" />
    </c:when>
    <c:when test="${param.sezione == 'ordini'}">
        <jsp:forward page="/WEB-INF/jsp/listaOrdini.jsp" />
    </c:when>
    <c:otherwise>
        <jsp:forward page="home.jsp" />
    </c:otherwise>
</c:choose>
```

## Azioni per i JavaBean (Legacy)

Esistono anche azioni per manipolare i JavaBean, anche se oggi il loro uso è diminuito in favore dell'Expression Language (EL) e di JSTL, che offrono un approccio più pulito.

- **`<jsp:useBean>`**: Cerca o istanzia un oggetto JavaBean.
- **`<jsp:setProperty>`**: Imposta il valore di una proprietà di un bean.
- **`<jsp:getProperty>`**: Ottiene e stampa il valore di una proprietà di un bean.

Questi tag erano il modo principale per collegare i dati ai form HTML prima che l'EL diventasse lo standard.

## Tabella Riassuntiva dei Comandi

| Azione              | Descrizione                                                                                             |
| ------------------- | ------------------------------------------------------------------------------------------------------- |
| `<jsp:include>`     | Include dinamicamente (a runtime) l'output di un'altra risorsa.                                         |
| `<jsp:forward>`     | Inoltra la richiesta e la risposta a un'altra risorsa sul server. Il controllo non torna alla pagina JSP. |
| `<jsp:param>`       | Usato all'interno di `include` o `forward` per passare parametri alla risorsa di destinazione.            |
| `<jsp:useBean>`     | (Legacy) Trova o crea un'istanza di un JavaBean in un determinato scope.                                |
| `<jsp:setProperty>` | (Legacy) Imposta una proprietà di un JavaBean, spesso usando i parametri della richiesta.               |
| `<jsp:getProperty>` | (Legacy) Recupera il valore di una proprietà da un JavaBean e lo scrive nell'output.                     |

## Glossario dei Termini Importanti

| Termine           | Definizione                                                                                                                            |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Azione JSP**    | Un tag XML eseguito a runtime che invoca una funzionalità predefinita del container JSP, come includere o inoltrare.                     |
| **Inclusione Dinamica** | Un'inclusione che avviene al momento della richiesta, dove la risorsa inclusa viene eseguita separatamente e il suo output viene aggiunto alla pagina chiamante. |
| **Inclusione Statica**  | Un'inclusione che avviene al momento della compilazione, dove il codice sorgente del file incluso viene fuso con quello della pagina JSP principale. |
| **Forward (Inoltro)** | Un trasferimento di controllo da una risorsa web a un'altra all'interno dello stesso server, invisibile al browser dell'utente.      |
