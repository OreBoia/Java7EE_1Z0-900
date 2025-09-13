# Tag Libraries e JSTL (JSP Standard Tag Library)

Per evitare di inserire codice Java (scriptlet) direttamente nelle pagine JSP, rendendole più pulite e manutenibili, sono state introdotte le **Tag Libraries**. Una tag library è una collezione di "tag" personalizzati che eseguono determinate azioni.

**JSTL (JSP Standard Tag Library)** è la libreria di tag standard più importante e utilizzata. Fornisce un ricco set di tag per i compiti più comuni nello sviluppo di pagine web, come l'iterazione su collezioni, la logica condizionale, la formattazione dei dati e molto altro.

## Come usare JSTL

Per utilizzare una tag library in una pagina JSP, è necessario prima dichiararla all'inizio del file tramite la direttiva `<%@ taglib %>`, specificando un prefisso (es. `c`, `fmt`) e un URI che identifica univocamente la libreria.

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
```

Una volta dichiarata, si possono usare i tag della libreria utilizzando la sintassi XML `prefix:tagName`.

## Librerie JSTL Principali

JSTL è suddivisa in diverse librerie, ognuna con un proprio prefisso e scopo.

| Prefisso | URI                                     | Descrizione                                                                                                |
| :------- | :-------------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| `c`      | `http://java.sun.com/jsp/jstl/core`     | **Core Library**: Fornisce tag per la logica, il controllo di flusso (if, forEach), la gestione delle URL e altro. È la libreria più usata. |
| `fmt`    | `http://java.sun.com/jsp/jstl/fmt`      | **Formatting Library**: Fornisce tag per la formattazione e il parsing di numeri e date, e per l'internazionalizzazione (I18N). |
| `sql`    | `http://java.sun.com/jsp/jstl/sql`      | **SQL Library**: Permette di eseguire query SQL direttamente dalla JSP. **Considerata una cattiva pratica** in applicazioni reali, poiché mescola la logica di accesso ai dati con la presentazione. |
| `xml`    | `http://java.sun.com/jsp/jstl/xml`      | **XML Library**: Fornisce tag per la manipolazione e il parsing di dati XML.                               |
| `fn`     | `http://java.sun.com/jsp/jstl/functions`| **Functions Library**: Fornisce una serie di funzioni EL per manipolare le stringhe, controllare la lunghezza delle collezioni, etc. |

## Esempi di Codice

### Core Library (`c:`)

**`<c:if>`**: Esegue il corpo del tag solo se la condizione è vera.

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${utente.isAdmin}">
    <p>Benvenuto, amministratore!</p>
</c:if>
```

**`<c:forEach>`**: Itera su una collezione (es. `List`, `Map`, `Array`).

```jsp
<%-- Supponendo che 'listaProdotti' sia una List<Prodotto> nella request --%>
<ul>
    <c:forEach items="${listaProdotti}" var="prodotto">
        <li>${prodotto.nome} - € ${prodotto.prezzo}</li>
    </c:forEach>
</ul>
```

**`<c:choose>`, `<c:when>`, `<c:otherwise>`**: Equivalente a uno `switch` o a un `if-else if-else`.

```jsp
<c:choose>
    <c:when test="${utente.livello == 'admin'}">
        <p>Accesso completo.</p>
    </c:when>
    <c:when test="${utente.livello == 'moderatore'}">
        <p>Accesso limitato.</p>
    </c:when>
    <c:otherwise>
        <p>Accesso base.</p>
    </c:otherwise>
</c:choose>
```

### Formatting Library (`fmt:`)

**`<fmt:formatDate>`**: Formatta una data secondo un pattern specifico.

```jsp
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Date" %>

<% request.setAttribute("dataDiOggi", new Date()); %>

<p>Data formattata: <fmt:formatDate value="${dataDiOggi}" pattern="dd/MM/yyyy HH:mm" /></p>
```

**`<fmt:formatNumber>`**: Formatta un numero, ad esempio come valuta.

```jsp
<p>Prezzo: <fmt:formatNumber value="${prodotto.prezzo}" type="currency" currencySymbol="€" /></p>
```

## Glossario dei Termini Importanti

| Termine             | Definizione                                                                                                                            |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Tag Library**     | Una collezione di tag personalizzati che estendono le funzionalità di JSP, permettendo di incapsulare logica riutilizzabile.             |
| **JSTL**            | La libreria di tag standard per JSP, che fornisce un set di tag per compiti comuni, riducendo la necessità di scriptlet Java.           |
| **Direttiva `taglib`** | La direttiva JSP (`<%@ taglib ... %>`) usata per dichiarare una tag library e associarla a un prefisso per l'uso nella pagina.          |
| **Prefisso**        | Un alias breve (es. `c`, `fmt`) che viene associato all'URI di una tag library e usato per invocare i tag di quella libreria.            |
| **URI**             | Un identificatore univoco per la tag library. Il container JSP lo usa per localizzare il descrittore della libreria (TLD).              |
| **TLD (Tag Library Descriptor)** | Un file XML che descrive una tag library, elencando tutti i tag che contiene e i loro attributi. Il container lo usa per validare i tag. |
