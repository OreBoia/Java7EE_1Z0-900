# Elementi Sintattici di una Pagina JSP

Una pagina JSP è un file di testo che combina markup statico (come HTML) con elementi dinamici specifici di JSP. Il container JSP interpreta questi elementi per generare una Servlet. Comprendere la sintassi di questi elementi è fondamentale per scrivere pagine JSP efficaci.

## 1. Direttive JSP (`<%@ ... %>`)

Le direttive sono istruzioni per il container JSP che influenzano il processo di traduzione della pagina. Non producono un output diretto, ma configurano il comportamento della Servlet generata.

La sintassi generale è `<%@ direttiva attributo="valore" ... %>`.

- **`page`**: La direttiva più comune, usata per configurare attributi a livello di pagina.
- `import`: Specifica le classi Java da importare (es. `java.util.*`).
- `contentType`: Definisce il tipo MIME e il charset della risposta (es. `text/html;charset=UTF-8`).
- `errorPage`: Specifica una pagina a cui inoltrare la richiesta in caso di eccezioni non gestite.
- `isErrorPage`: Se `true`, indica che la pagina corrente è una pagina di errore, rendendo disponibile l'oggetto `exception`.
- `session`: Se `false`, disabilita la creazione automatica della sessione (`HttpSession`).

    **Esempio:**

    ```jsp
    <%@ page import="java.util.Date, com.example.model.User" contentType="text/html;charset=UTF-8" %>
    ```

- **`include`**: Include il contenuto di un altro file (HTML, JSP, ecc.) **al momento della traduzione**. Il contenuto del file incluso viene fuso con la pagina principale prima che questa venga compilata in una Servlet. È utile per elementi statici e riutilizzabili come header e footer.

    **Esempio:**

    ```jsp
    <%@ include file="/WEB-INF/jspf/header.jspf" %>
    ```

- **`taglib`**: Dichiara una libreria di tag (come JSTL) che può essere utilizzata nella pagina, associandola a un prefisso.

    **Esempio (per JSTL Core):**

    ```jsp
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    ```

## 2. Elementi di Scripting

Questi elementi permettono di inserire codice Java direttamente nella pagina. **Nelle pratiche moderne, il loro uso è fortemente scoraggiato in favore di Expression Language (EL) e JSTL**, che promuovono una migliore separazione tra logica e presentazione.

- **Scriptlet (`<% ... %>`)**: Un blocco di codice Java che viene inserito nel metodo `_jspService()` della Servlet generata.

    **Esempio:**

    ```jsp
    <%
        String username = request.getParameter("user");
        if (username != null) {
            out.println("Ciao, " + username);
        }
    %>
    ```

- **Espressione (`<%= ... %>`)**: Valuta un'espressione Java, la converte in stringa e la scrive direttamente nell'output della risposta. È una scorciatoia per `out.print(...)`.

    **Esempio:**

    ```jsp
    <p>L'ora corrente è: <%= new java.util.Date() %></p>
    <p>Benvenuto, <%= username %>!</p>
    ```

- **Dichiarazione (`<%! ... %>`)**: Dichiara variabili di istanza (campi) o metodi a livello della classe della Servlet generata.
    **Attenzione**: Le variabili dichiarate qui sono condivise tra tutte le richieste, quindi il loro uso deve essere gestito con estrema cautela per evitare problemi di thread-safety.

    **Esempio:**

    ```jsp
    <%!
        private int hitCounter = 0;

        public synchronized void incrementCounter() {
            hitCounter++;
        }
    %>
    <%
        incrementCounter();
    %>
    <p>Questa pagina è stata visitata <%= hitCounter %> volte.</p>
    ```

## 3. Commenti

- **Commento JSP (`<%-- ... --%>`)**: Questo commento viene elaborato dal container e rimosso durante la fase di traduzione. **Non è visibile** nel sorgente HTML inviato al client.

- **Commento HTML (`<!-- ... -->`)**: Questo è un commento standard HTML. Viene inviato al client e **è visibile** nel sorgente della pagina nel browser.

## 4. Contenuto Statico

Qualsiasi testo o markup nella pagina JSP che non è un elemento JSP viene trattato come contenuto statico e scritto direttamente nell'output della risposta.

## Tabella Riassuntiva degli Elementi

| Elemento | Sintassi | Scopo | Visibile nel Client |
| :--- | :--- | :--- | :--- |
| **Direttiva `page`** | `<%@ page ... %>` | Configura la pagina (import, content type, ecc.). | No |
| **Direttiva `include`** | `<%@ include ... %>` | Include un file al momento della traduzione. | Sì (il contenuto) |
| **Direttiva `taglib`** | `<%@ taglib ... %>` | Dichiara una libreria di tag. | No |
| **Scriptlet** | `<% ... %>` | Esegue codice Java. (Uso sconsigliato). | No (l'output sì) |
| **Espressione** | `<%= ... %>` | Stampa il risultato di un'espressione Java. | Sì (il risultato) |
| **Dichiarazione** | `<%! ... %>` | Dichiara campi e metodi della classe Servlet. | No |
| **Commento JSP** | `<%-- ... --%>` | Commento lato server, rimosso dal container. | No |
| **Commento HTML** | `<!-- ... -->` | Commento standard HTML, inviato al client. | Sì |
| **Contenuto Statico** | `(testo/HTML)` | Markup statico inviato così com'è. | Sì |
