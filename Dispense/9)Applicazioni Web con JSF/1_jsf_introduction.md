# Introduzione a JavaServer Faces (JSF)

**JavaServer Faces (JSF)** è il framework standard di Java EE per la costruzione di interfacce utente (UI) web basate su componenti. Adotta un'architettura **Model-View-Controller (MVC)** e un modello di programmazione **event-driven**, simile a quello delle applicazioni desktop tradizionali. La versione di riferimento in Java EE 7 è **JSF 2.2**.

L'obiettivo di JSF è semplificare lo sviluppo di UI web astraendo la complessità di HTTP e HTML, permettendo agli sviluppatori di concentrarsi sulla logica dei componenti e degli eventi.

## Caratteristiche Principali di JSF

- **Modello a Componenti**: La UI è costruita come un albero di componenti (es. campi di input, pulsanti, tabelle) che vengono renderizzati come HTML.
- **Stateful**: JSF è un framework stateful, il che significa che può gestire lo stato dei componenti sul server tra una richiesta e l'altra, semplificando la gestione di form complessi.
- **Ciclo di Vita Definito (Lifecycle)**: Ogni richiesta JSF segue un ciclo di vita ben definito che gestisce la conversione dei dati, la validazione, l'aggiornamento del modello e l'invocazione della logica di business.
- **Backing Beans**: La logica e i dati della UI sono gestiti da classi Java chiamate "backing bean", tipicamente bean CDI (`@Named`).
- **Navigazione Semplificata**: La navigazione tra le pagine può essere gestita in modo dichiarativo o programmatico.
- **Supporto AJAX Integrato**: JSF fornisce un supporto di prima classe per le richieste AJAX, permettendo di aggiornare parti della pagina senza un refresh completo.

## Architettura e Componenti Fondamentali

### 1. Facelets: Le Viste

Le pagine JSF (le "viste") sono scritte usando **Facelets**, una tecnologia di templating basata su XHTML. Queste pagine contengono un mix di HTML standard e tag speciali JSF che rappresentano i componenti della UI.

Le librerie di tag più comuni sono:

- `h:` (HTML): Fornisce i componenti UI di base. Esempi: `h:form`, `h:inputText`, `h:commandButton`, `h:dataTable`.
- `f:` (Core): Fornisce funzionalità core di JSF. Esempi: `f:ajax`, `f:convertDateTime`, `f:validator`.
- `ui:` (Templating): Fornisce tag per la creazione di template riutilizzabili. Esempi: `ui:composition`, `ui:define`, `ui:insert`.

### 2. Backing Beans: Il Modello e il Controller

I backing bean sono classi Java (tipicamente bean CDI) che fungono sia da **modello** (contenendo i dati da visualizzare) sia da **controller** (contenendo la logica da eseguire in risposta agli eventi, come il click di un pulsante).

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("utenteBean") // Rende il bean accessibile via EL con il nome "utenteBean"
@RequestScoped      // Il bean vive per la durata di una singola richiesta HTTP
public class UtenteBean {

    private String nome;
    private String password;

    // Getter e Setter sono fondamentali per il data binding

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Metodo d'azione (action method)
    public String login() {
        if ("admin".equals(nome) && "1234".equals(password)) {
            return "success"; // Outcome di navigazione
        } else {
            return "failure"; // Outcome di navigazione
        }
    }
}
```

### 3. Expression Language (EL): Il Collante

L'Expression Language (EL) è il linguaggio usato nelle pagine Facelets per collegare i componenti UI alle proprietà e ai metodi dei backing bean.

- **Value Binding**: Collega il valore di un componente a una proprietà del bean.

    ```xhtml
    <h:inputText value="#{utenteBean.nome}" />
    ```

- **Method Binding**: Collega un'azione (es. un click) a un metodo del bean.

    ```xhtml
    <h:commandButton value="Login" action="#{utenteBean.login}" />
    ```

### Esempio di Pagina di Login (`login.xhtml`)

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html">
<h:head>
    <title>Login</title>
</h:head>
<h:body>
    <h:form>
        <h:panelGrid columns="2">
            Nome Utente: <h:inputText id="username" value="#{utenteBean.nome}" required="true" />
            Password: <h:inputSecret id="password" value="#{utenteBean.password}" required="true" />
        </h:panelGrid>
        <h:commandButton value="Login" action="#{utenteBean.login}" />
        <br/>
        <h:messages style="color:red;" />
    </h:form>
</h:body>
</html>
```

## Scopes dei Backing Bean

Lo **scope** di un backing bean definisce il suo ciclo di vita, ovvero per quanto tempo l'istanza del bean esisterà e sarà accessibile. La scelta dello scope corretto è fondamentale per il corretto funzionamento e l'efficienza dell'applicazione. Con l'integrazione di CDI, si usano le annotazioni di scope di CDI.

| Scope                 | Annotazione CDI         | Durata                                                                                             | Uso Tipico                                                              |
| --------------------- | ----------------------- | -------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| **Request Scope**     | `@RequestScoped`        | L'istanza del bean vive per la durata di una singola richiesta HTTP.                               | Azioni effimere, form semplici che non necessitano di stato tra le richieste. |
| **View Scope**        | `@ViewScoped`           | L'istanza vive finché l'utente interagisce con la stessa pagina (vista) JSF, anche attraverso postback AJAX. | Form complessi, tabelle dati con paginazione/ordinamento. Molto comune in JSF. |
| **Session Scope**     | `@SessionScoped`        | L'istanza persiste per l'intera sessione dell'utente (più richieste e più pagine).                  | Dati dell'utente loggato, carrello della spesa.                         |
| **Application Scope** | `@ApplicationScoped`    | Esiste una sola istanza del bean per l'intera applicazione web.                                    | Dati di configurazione, cache a livello di applicazione.                |
| **Flow Scope**        | `@FlowScoped`           | (JSF 2.2+) L'istanza vive per la durata di un "flusso" definito di pagine (es. un wizard).          | Wizard di registrazione, processi multi-step.                           |
| **Conversation Scope**| `@ConversationScoped`   | (CDI) Permette un controllo programmatico fine sull'inizio e la fine dello scope.                  | Flussi di lavoro complessi che non si adattano agli altri scope.        |

## Facelets Templating

Facelets, la tecnologia di vista di JSF, include un potente sistema di templating che permette di creare layout di pagina riutilizzabili, evitando la duplicazione di codice per elementi comuni come header, footer e menu di navigazione.

**Componenti chiave:**

- **Template (`template.xhtml`)**: Un file XHTML che definisce la struttura generale della pagina, con punti di inserimento definiti da `<ui:insert>`.
- **Client del Template (`pagina.xhtml`)**: Un file XHTML che usa il template tramite `<ui:composition>` e fornisce il contenuto specifico per i punti di inserimento tramite `<ui:define>`.

### Esempio di Template (`template.xhtml`)

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
<h:head>
    <title><ui:insert name="title">Titolo Default</ui:insert></title>
    <h:outputStylesheet name="css/style.css" />
</h:head>
<h:body>
    <div id="header">
        <ui:insert name="header"><h1>Header di Default</h1></ui:insert>
    </div>
    <div id="content">
        <ui:insert name="content">
            <p>Contenuto di default. Verrà sostituito.</p>
        </ui:insert>
    </div>
    <div id="footer">
        <p>&copy; 2025 La Mia Applicazione</p>
    </div>
</h:body>
</html>
```

### Esempio di Client (`home.xhtml`)

```xhtml
<ui:composition template="/template.xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:h="http://xmlns.jcp.org/jsf/html">

    <ui:define name="title">
        Home Page
    </ui:define>

    <ui:define name="header">
        <h1>Benvenuto nella Home Page</h1>
    </ui:define>

    <ui:define name="content">
        <p>Questo è il contenuto specifico della home page.</p>
        <h:form>
            <h:commandButton value="Vai a un'altra pagina" action="altraPagina" />
        </h:form>
    </ui:define>

</ui:composition>
```

## AJAX in JSF

JSF 2.x ha un supporto di prima classe per **AJAX (Asynchronous JavaScript and XML)**, che permette di aggiornare parti di una pagina web senza doverla ricaricare completamente. Questo rende le applicazioni più reattive e migliora l'esperienza utente.

Il cuore del supporto AJAX in JSF è il tag `<f:ajax>`. Inserendolo all'interno di un componente UI (come un campo di input o un pulsante), si può definire un comportamento asincrono.

**Attributi chiave di `<f:ajax>`:**

- `event`: Specifica l'evento del client che scatena la richiesta AJAX (es. `click`, `keyup`, `change`).
- `render`: Una lista di ID di componenti (separati da spazi) che devono essere ri-renderizzati (aggiornati) dopo che la richiesta AJAX è completata. Si possono usare anche parole chiave come `@this`, `@form`, `@all`, `@none`.
- `execute`: Una lista di ID di componenti che devono essere processati sul server durante la richiesta AJAX (ovvero, le fasi del ciclo di vita JSF vengono eseguite solo per loro). Le parole chiave sono le stesse di `render`. Di default è `@this`.

### Esempio: Aggiornamento Live di un Campo

Questo esempio mostra come aggiornare un campo di testo in tempo reale mentre l'utente digita.

```xhtml
<h:form>
    <h:panelGrid columns="2">
        Il tuo nome:
        <h:inputText value="#{bean.name}">
            <f:ajax event="keyup" render="nameOutput" />
        </h:inputText>

        Ciao, <h:outputText id="nameOutput" value="#{bean.name}" />
    </h:panelGrid>
</h:form>
```

In questo codice:

1. Il tag `<f:ajax>` è annidato dentro `h:inputText`.
2. `event="keyup"` fa partire una richiesta AJAX a ogni pressione di un tasto.
3. `render="nameOutput"` dice a JSF di aggiornare solo il componente con `id="nameOutput"` una volta che la richiesta è tornata.

JSF gestisce tutta la complessità della chiamata JavaScript e dell'aggiornamento del DOM.

### Librerie di Componenti AJAX

Oltre al supporto nativo, esistono librerie di componenti JSF di terze parti che offrono un set molto ricco di componenti AJAX pronti all'uso, come **PrimeFaces**, **RichFaces** e **ICEfaces**. Queste librerie semplificano ulteriormente la creazione di interfacce web dinamiche e complesse.

## Localizzazione (i18n/L10N)

La **localizzazione (L10N)** è il processo di adattamento di un'applicazione a una specifica lingua e regione (locale). L'**internazionalizzazione (i18n)** è la progettazione dell'applicazione in modo che possa essere localizzata facilmente. JSF ha un eccellente supporto integrato per entrambe.

I meccanismi principali sono:

- **Resource Bundle**: Sono file di proprietà (`.properties`) che contengono le traduzioni delle stringhe (messaggi, etichette) usate nell'interfaccia. Si crea un file per ogni lingua supportata, ad esempio:
  - `messaggi_it.properties` (Italiano)
  - `messaggi_en.properties` (Inglese)
  - `messaggi.properties` (Default)

- **Configurazione**: Il resource bundle viene registrato nel file `faces-config.xml` o caricato on-demand in una pagina con il tag `<f:loadBundle>`.

    ```xml
    <!-- In faces-config.xml -->
    <application>
        <resource-bundle>
            <base-name>com.miosito.i18n.messaggi</base-name>
            <var>msg</var>
        </resource-bundle>
    </application>
    ```

- **Utilizzo nella Vista**: Le stringhe localizzate vengono richiamate tramite EL, usando la variabile definita (`msg` nell'esempio sopra).

    ```xhtml
    <h:outputText value="#{msg.benvenuto}" />
    ```

JSF seleziona automaticamente il file `.properties` corretto in base alla `Locale` del browser dell'utente, che può essere ispezionata o modificata tramite il `FacesContext`.

## JSF vs. JSP

È importante capire la relazione tra JSF e JSP (JavaServer Pages), un'altra tecnologia di vista di Java EE.

- **Astrazione**: JSF è un framework a componenti che opera a un livello di astrazione superiore rispetto a JSP. Mentre JSP è orientato alla generazione di testo (tipicamente HTML), JSF è orientato alla gestione di un albero di componenti UI.
- **Tecnologia di Vista**: Storicamente, JSF 1.x poteva usare JSP come tecnologia per renderizzare i suoi componenti. Tuttavia, a partire da **JSF 2.0**, la tecnologia di vista predefinita e raccomandata è **Facelets (XHTML)**.
- **Vantaggi di Facelets**: Facelets è superiore a JSP per le applicazioni JSF perché offre un'integrazione migliore con il ciclo di vita di JSF, un potente sistema di templating e facilita la creazione di componenti personalizzati e compositi.
- **Uso Moderno**: Nelle applicazioni moderne Java EE, JSP è raramente usato in combinazione con JSF. La pratica standard è usare JSF con Facelets.

## Il Ciclo di Vita di JSF (JSF Lifecycle)

Ogni richiesta JSF (in particolare un "postback", ovvero l'invio di un form) attraversa una serie di fasi ben definite:

1. **Restore View**: JSF costruisce (o ripristina dalla sessione) l'albero dei componenti della UI per la pagina richiesta.
2. **Apply Request Values**: I valori inviati nella richiesta HTTP (es. i dati del form) vengono estratti e applicati ai rispettivi componenti. In questa fase avviene la **conversione** dei dati da stringa al tipo atteso (es. `String` -> `Date`). Se la conversione fallisce, il ciclo salta direttamente alla fase `Render Response`.
3. **Process Validations**: Vengono eseguiti i validatori associati ai componenti. Se una validazione fallisce, il ciclo salta direttamente alla fase `Render Response`.
4. **Update Model Values**: Se tutte le validazioni hanno avuto successo, i valori dei componenti vengono trasferiti alle proprietà dei backing bean (invocando i metodi `set`).
5. **Invoke Application**: Viene eseguito il metodo d'azione specificato nel componente che ha scatenato l'evento (es. il metodo `login()` del `h:commandButton`).
6. **Render Response**: La vista finale viene renderizzata come HTML e inviata al client. Se si sono verificati errori di conversione o validazione, la pagina viene ri-renderizzata mostrando i messaggi di errore.

## Navigazione

Il risultato di un metodo d'azione (un `outcome`, come la stringa `"success"`) viene usato da JSF per decidere quale pagina mostrare successivamente.

- **Navigazione Implicita**: Se il metodo restituisce `"pagina"`, JSF cercherà una vista chiamata `pagina.xhtml`.
- **Navigazione Esplicita**: È possibile definire regole di navigazione complesse nel file `faces-config.xml`.

## Conversione e Validazione

JSF fornisce un framework robusto per la conversione e la validazione dei dati di input.

### Conversione

- **Converter Standard**: JSF ha converter integrati per tipi comuni (numeri, date). Si usano con tag come `<f:convertDateTime>` e `<f:convertNumber>`.
- **Converter Personalizzati**: Si può creare un converter personalizzato implementando l'interfaccia `javax.faces.convert.Converter`.

### Validazione

- **Attributi Semplici**: Come `required="true"`.
- **Validator Standard**: Tag come `<f:validateLength>`, `<f:validateRegex>`.
- **Validator Personalizzati**: Implementando l'interfaccia `javax.faces.validator.Validator`.
- **Bean Validation (JSR 303/380)**: JSF si integra nativamente con Bean Validation. Se una proprietà di un bean è annotata con `@NotNull`, `@Size`, etc., JSF userà queste annotazioni per validare automaticamente l'input.

## Messaggi di Errore (`FacesMessage`)

Quando una conversione o una validazione fallisce, JSF crea un `FacesMessage` e lo associa al componente corrispondente. Il tag `<h:message for="componentId">` mostra il messaggio per un singolo componente, mentre `<h:messages />` mostra tutti i messaggi della pagina.

## Glossario dei Termini Importanti

| Termine                 | Definizione                                                                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Facelets**            | La tecnologia di templating standard di JSF per la creazione delle viste, basata su XHTML.                                             |
| **Componente UI**       | Un elemento riutilizzabile della UI (es. un campo di testo, un pulsante) rappresentato da un tag JSF e da un oggetto Java sul server.     |
| **Backing Bean**        | Una classe Java (tipicamente un bean CDI `@Named`) che contiene i dati e la logica per una o più viste JSF.                               |
| **Scope**               | Definisce il ciclo di vita di un backing bean, ovvero per quanto tempo la sua istanza esiste.                                            |
| **`@ViewScoped`**       | Uno scope in cui il bean vive finché l'utente interagisce con la stessa vista JSF, anche attraverso richieste parziali (AJAX).          |
| **Facelets Templating** | Il meccanismo di Facelets per creare layout di pagina riutilizzabili (`<ui:composition>`, `<ui:define>`, `<ui:insert>`).                 |
| **AJAX**                | Acronimo di Asynchronous JavaScript and XML. Permette di aggiornare parti di una pagina web senza un refresh completo.                   |
| **`<f:ajax>`**          | Il tag standard di JSF per aggiungere funzionalità AJAX a un componente UI, specificando cosa eseguire e cosa renderizzare.              |
| **Localizzazione (L10N)** | Il processo di adattamento di un'applicazione a una specifica lingua e regione (locale).                                               |
| **Resource Bundle**     | Un insieme di file `.properties` che contengono le stringhe tradotte per supportare l'internazionalizzazione (i18n).                     |
| **Data Binding**        | Il meccanismo, basato su EL, che collega le proprietà dei componenti UI alle proprietà dei backing bean.                                  |
| **Ciclo di Vita (Lifecycle)** | La sequenza di fasi che JSF esegue per processare una richiesta e renderizzare una risposta.                                        |
| **Postback**            | Una richiesta HTTP POST inviata dalla pagina JSF a se stessa, tipicamente scatenata dall'invio di un form.                               |
| **Outcome**             | Una stringa restituita da un metodo d'azione che JSF usa per determinare la regola di navigazione da seguire.                             |
| **`FacesContext`**      | Un oggetto, accessibile in ogni fase del ciclo di vita, che contiene tutte le informazioni contestuali relative alla richiesta corrente.    |
| **Converter**           | Un oggetto responsabile della conversione dei dati tra la rappresentazione testuale (HTTP) e il tipo di dato Java (modello).            |
| **Validator**           | Un oggetto che controlla se i dati inseriti dall'utente rispettano determinate regole di business.                                        |
