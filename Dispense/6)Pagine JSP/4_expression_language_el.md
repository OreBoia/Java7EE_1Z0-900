# Expression Language (EL)

JSP 2.0 ha introdotto l'Expression Language (EL), che oggi è unificata con quella usata in JavaServer Faces (JSF). L'EL fornisce un modo semplice e conciso per accedere ai dati (oggetti Java) e manipolarli nelle pagine JSP, senza la necessità di scrivere codice Java esplicito (scriptlet).

La sintassi di base è `${expression}`. Tutto ciò che si trova all'interno delle parentesi graffe viene valutato come un'espressione EL.

## Caratteristiche Principali

- **Accesso Semplificato ai Dati**: L'EL permette di accedere facilmente alle proprietà dei JavaBean, agli elementi di un array o di una `Map`, e agli attributi memorizzati nei vari scope (page, request, session, application).
- **Operatori**: Supporta operatori aritmetici, logici, relazionali e condizionali.
- **Oggetti Impliciti**: Fornisce un set di oggetti impliciti per accedere a parametri della richiesta, header, cookie, etc.
- **Sicurezza**: Di default, non è possibile chiamare metodi arbitrari con argomenti. L'EL è progettato principalmente per l'accesso alle proprietà (tramite metodi `get` e `set`) e per la logica di presentazione, non per la logica di business complessa.

## Esempi di Codice

### Accesso agli Attributi

Se in una Servlet o in uno scriptlet si imposta un attributo:

```java
// In una Servlet
Utente utente = new Utente("Mario", "Rossi");
request.setAttribute("utente", utente);
```

Nella pagina JSP, si può accedere alle proprietà dell'oggetto `Utente` in questo modo:

```jsp
<p>Benvenuto, ${utente.nome} ${utente.cognome}!</p>
```

L'EL invocherà automaticamente i metodi `getNome()` e `getCognome()` dell'oggetto `utente`.

### Operazioni Aritmetiche e Logiche

```jsp
<p>Risultato: ${5 + 3}</p> <%-- Stampa 8 --%>

<p>Prezzo totale: ${prodotto.prezzo * prodotto.quantita}</p>

<p>Sei un amministratore? ${param.username == 'admin'}</p>
```

### Utilizzo dell'operatore `empty`

L'operatore `empty` è utile per verificare se un valore è nullo o vuoto (es. una stringa vuota o una collezione senza elementi).

```jsp
<c:if test="${not empty listaProdotti}">
    <p>Ci sono prodotti nel carrello.</p>
</c:if>

<c:if test="${empty param.search}">
    <p>Nessun termine di ricerca inserito.</p>
</c:if>
```

### Operatore Ternario

```jsp
<p>Stato: ${utente.attivo ? 'Attivo' : 'Non Attivo'}</p>
```

## Tabella dei Comandi e Termini

### Oggetti Impliciti (Implicit Objects)

Questi oggetti sono sempre disponibili in un'espressione EL.

| Oggetto Implicito  | Descrizione                                                                                             | Esempio                               |
| ------------------ | ------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| `pageScope`        | Una `Map` che contiene tutti gli attributi con scope di pagina.                                         | `${pageScope.nomeAttributo}`          |
| `requestScope`     | Una `Map` che contiene tutti gli attributi con scope di richiesta.                                      | `${requestScope.utente}`              |
| `sessionScope`     | Una `Map` che contiene tutti gli attributi con scope di sessione.                                       | `${sessionScope.carrello}`            |
| `applicationScope` | Una `Map` che contiene tutti gli attributi con scope di applicazione (context).                         | `${applicationScope.contatoreVisite}` |
| `param`            | Una `Map` che contiene i parametri della richiesta HTTP, con chiave singola.                            | `${param.idProdotto}`                 |
| `paramValues`      | Una `Map` che contiene i parametri della richiesta con valori multipli (restituisce un array di String). | `${paramValues.interessi[0]}`         |
| `header`           | Una `Map` che contiene gli header della richiesta HTTP.                                                 | `${header['User-Agent']}`             |
| `headerValues`     | Una `Map` che contiene gli header con valori multipli.                                                  | `${headerValues['Accept-Language']}`   |
| `cookie`           | Una `Map` che contiene gli oggetti `Cookie` inviati dal client.                                         | `${cookie.JSESSIONID.value}`          |
| `initParam`        | Una `Map` che contiene i parametri di inizializzazione del contesto della web application.              | `${initParam.nomeParametro}`          |
| `pageContext`      | L'oggetto `PageContext` della pagina JSP, che dà accesso a tutti gli altri oggetti.                     | `${pageContext.request.contextPath}`  |

### Operatori EL

| Categoria   | Operatori                                                              | Descrizione                               |
| ----------- | ---------------------------------------------------------------------- | ----------------------------------------- |
| Aritmetici  | `+`, `-`, `*`, `/` (o `div`), `%` (o `mod`)                            | Operazioni matematiche di base.           |
| Logici      | `&&` (o `and`), `||` (o `or`),`!` (o `not`)                           | Operazioni booleane.                      |
| Relazionali | `==` (o `eq`), `!=` (o `ne`), `<` (o `lt`), `>` (o `gt`), `<=` (o `le`), `>=` (o `ge`) | Confronto tra valori.                     |
| Speciali    | `empty`, `A ? B : C`                                                   | Verifica se un valore è nullo o vuoto, operatore ternario. |
| Accesso     | `.` , `[]`                                                             | Accesso a proprietà di un bean o elementi di una collezione. |

### Glossario dei Termini Importanti

| Termine                  | Definizione                                                                                                                               |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **Expression Language (EL)** | Un linguaggio di scripting, ispirato a JavaScript e XPath, che permette di accedere a componenti Java (JavaBean) da dentro le pagine JSP. |
| **JavaBean**             | Una classe Java che segue determinate convenzioni: costruttore senza argomenti, proprietà private e metodi `get`/`set` pubblici.          |
| **Scope**                | L'ambito di visibilità e ciclo di vita di un oggetto. In JSP/Servlet ci sono 4 scope: `page`, `request`, `session`, e `application`.        |
| **Oggetto Implicito**    | Un oggetto predefinito che il container JSP/EL rende disponibile automaticamente all'interno di una pagina o di un'espressione.             |
| **Scriptlet**            | Un blocco di codice Java (`<% ... %>`) inserito in una pagina JSP. L'uso di EL e JSTL è preferito rispetto agli scriptlet.                  |
