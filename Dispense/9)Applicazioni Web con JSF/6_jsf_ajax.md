# AJAX in JSF - Approfondimento

AJAX (Asynchronous JavaScript and XML) è una tecnica fondamentale per creare interfacce web moderne e reattive. JSF 2.x fornisce un supporto nativo eccellente per AJAX, permettendo agli sviluppatori di aggiornare porzioni specifiche della pagina senza ricaricarla completamente.

---

## Il Tag `<f:ajax>`

Il cuore del supporto AJAX in JSF è il tag **`<f:ajax>`**, che può essere inserito all'interno di quasi tutti i componenti UI per aggiungere comportamenti asincroni.

### Sintassi Base

```xhtml
<h:inputText value="#{bean.property}">
    <f:ajax event="blur" render="outputComponent" />
</h:inputText>
```

---

## Attributi Principali di `<f:ajax>`

### 1. `event`

Specifica quale evento del componente scatena la richiesta AJAX.

**Eventi comuni:**

| Componente | Eventi Disponibili |
|------------|-------------------|
| `<h:inputText>` | `blur`, `change`, `click`, `keyup`, `keydown`, `keypress` |
| `<h:commandButton>` | `click`, `action` (default) |
| `<h:selectOneMenu>` | `change`, `valueChange` |
| `<h:selectBooleanCheckbox>` | `change`, `click` |

**Esempi:**

```xhtml
<!-- Validazione in tempo reale quando l'utente lascia il campo -->
<h:inputText value="#{userBean.email}">
    <f:ajax event="blur" render="emailMsg" />
</h:inputText>

<!-- Ricerca mentre l'utente digita -->
<h:inputText value="#{searchBean.query}">
    <f:ajax event="keyup" render="results" delay="300" />
</h:inputText>

<!-- Caricamento dinamico al cambio di selezione -->
<h:selectOneMenu value="#{bean.country}">
    <f:selectItems value="#{bean.countries}" />
    <f:ajax event="change" listener="#{bean.onCountryChange}" render="cityPanel" />
</h:selectOneMenu>
```

### 2. `render`

Specifica quali componenti devono essere aggiornati (ri-renderizzati) dopo che la richiesta AJAX è completata.

**Valori speciali:**

- `@this` - Solo il componente corrente
- `@form` - Tutti i componenti del form
- `@all` - Tutti i componenti della pagina
- `@none` - Nessun componente (solo esecuzione lato server)

**ID multipli:**

```xhtml
<f:ajax render="output1 output2 output3" />
```

**Esempi:**

```xhtml
<!-- Aggiorna solo un campo di output -->
<h:inputText value="#{bean.name}">
    <f:ajax render="nameDisplay" />
</h:inputText>
<h:outputText id="nameDisplay" value="#{bean.name}" />

<!-- Aggiorna un intero pannello -->
<h:selectOneMenu value="#{bean.category}">
    <f:ajax render="productPanel" />
</h:selectOneMenu>

<h:panelGroup id="productPanel">
    <!-- Contenuto dinamico basato sulla categoria -->
</h:panelGroup>
```

### 3. `execute`

Specifica quali componenti devono essere processati (sottoposti al ciclo di vita JSF) durante la richiesta AJAX.

**Valori speciali:**

- `@this` (default) - Solo il componente corrente
- `@form` - Tutti i componenti del form
- `@all` - Tutti i componenti della pagina
- `@none` - Nessun componente

**Esempio pratico:**

```xhtml
<h:form>
    <h:inputText id="field1" value="#{bean.field1}" />
    <h:inputText id="field2" value="#{bean.field2}" />
    <h:inputText id="field3" value="#{bean.field3}" />
    
    <!-- Invia solo field1 e field2 al server -->
    <h:commandButton value="Salva Parziale" action="#{bean.savePartial}">
        <f:ajax execute="field1 field2" render="message" />
    </h:commandButton>
    
    <!-- Invia tutti i campi del form -->
    <h:commandButton value="Salva Tutto" action="#{bean.saveAll}">
        <f:ajax execute="@form" render="@form" />
    </h:commandButton>
</h:form>
```

### 4. `listener`

Specifica un metodo del backing bean da invocare durante il processing della richiesta AJAX.

**Firma del metodo:**

```java
public void listenerMethod(AjaxBehaviorEvent event) {
    // Logica da eseguire
}
```

**Esempio:**

```xhtml
<h:inputText value="#{registerBean.username}">
    <f:ajax event="blur" 
            listener="#{registerBean.checkUsername}"
            render="usernameStatus" />
</h:inputText>
<h:outputText id="usernameStatus" value="#{registerBean.message}" />
```

```java
@Named
@ViewScoped
public class RegisterBean implements Serializable {
    private String username;
    private String message;
    
    public void checkUsername(AjaxBehaviorEvent event) {
        if (username != null && !username.isEmpty()) {
            if (userService.isAvailable(username)) {
                message = "✓ Username disponibile";
            } else {
                message = "✗ Username già in uso";
            }
        }
    }
    
    // Getters e Setters...
}
```

### 5. `delay`

Specifica un ritardo (in millisecondi) prima di inviare la richiesta AJAX. Utile per eventi ad alta frequenza come `keyup`.

**Esempio - Ricerca con debounce:**

```xhtml
<h:form>
    <h:inputText id="searchBox" value="#{searchBean.query}">
        <f:ajax event="keyup" 
                listener="#{searchBean.search}"
                render="results"
                delay="500" />
    </h:inputText>
    
    <h:panelGroup id="results">
        <h:dataTable value="#{searchBean.results}" var="item">
            <h:column>#{item.name}</h:column>
        </h:dataTable>
    </h:panelGroup>
</h:form>
```

**Spiegazione:**

- L'utente digita "laptop"
- Ogni carattere triggerebbe una richiesta AJAX
- Con `delay="500"`, JSF aspetta 500ms dopo l'ultimo carattere digitato
- Se l'utente continua a digitare entro 500ms, il timer viene resettato
- La richiesta parte solo quando l'utente smette di digitare per almeno 500ms

### 6. `disabled`

Disabilita temporaneamente il comportamento AJAX (boolean).

```xhtml
<h:inputText value="#{bean.value}">
    <f:ajax render="output" disabled="#{bean.ajaxDisabled}" />
</h:inputText>
```

### 7. `onevent`

Specifica una funzione JavaScript da chiamare durante le fasi della richiesta AJAX.

**Fasi:**

- `begin` - Prima di inviare la richiesta
- `complete` - Quando la richiesta è completata
- `success` - Quando l'aggiornamento è avvenuto con successo

```xhtml
<h:commandButton value="Salva" action="#{bean.save}">
    <f:ajax render="@form" onevent="handleAjaxEvent" />
</h:commandButton>

<script>
function handleAjaxEvent(data) {
    var status = data.status;
    if (status === "begin") {
        // Mostra loading spinner
        document.getElementById("spinner").style.display = "block";
    } else if (status === "complete") {
        // Nascondi loading spinner
        document.getElementById("spinner").style.display = "none";
    } else if (status === "success") {
        // Operazione completata con successo
        console.log("AJAX request successful");
    }
}
</script>
```

### 8. `onerror`

Specifica una funzione JavaScript da chiamare in caso di errore AJAX.

```xhtml
<h:commandButton value="Salva" action="#{bean.save}">
    <f:ajax render="@form" onerror="handleAjaxError" />
</h:commandButton>

<script>
function handleAjaxError(data) {
    var status = data.status;
    var errorName = data.errorName;
    var errorMessage = data.errorMessage;
    
    alert("Errore AJAX: " + errorMessage);
}
</script>
```

---

## Pattern ed Esempi Pratici

### 1. Validazione in Tempo Reale

```xhtml
<h:form>
    <h:panelGrid columns="3">
        <h:outputLabel for="username" value="Username:" />
        <h:inputText id="username" value="#{userBean.username}">
            <f:ajax event="blur" 
                    listener="#{userBean.validateUsername}"
                    render="usernameMsg" />
        </h:inputText>
        <h:message id="usernameMsg" for="username" style="color:red" />
        
        <h:outputLabel for="email" value="Email:" />
        <h:inputText id="email" value="#{userBean.email}">
            <f:ajax event="blur" 
                    listener="#{userBean.validateEmail}"
                    render="emailMsg" />
            <f:validateRegex pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}" />
        </h:inputText>
        <h:message id="emailMsg" for="email" style="color:red" />
    </h:panelGrid>
    
    <h:commandButton value="Registra" action="#{userBean.register}" />
</h:form>
```

### 2. Liste Dinamiche (Master-Detail)

```xhtml
<h:form>
    <!-- Lista Paesi -->
    <h:selectOneMenu id="country" value="#{locationBean.selectedCountry}">
        <f:selectItems value="#{locationBean.countries}" />
        <f:ajax listener="#{locationBean.onCountryChange}" 
                render="cityPanel" />
    </h:selectOneMenu>
    
    <!-- Lista Città (aggiornata dinamicamente) -->
    <h:panelGroup id="cityPanel">
        <h:selectOneMenu value="#{locationBean.selectedCity}" 
                         disabled="#{empty locationBean.cities}">
            <f:selectItems value="#{locationBean.cities}" />
        </h:selectOneMenu>
    </h:panelGroup>
</h:form>
```

```java
@Named
@ViewScoped
public class LocationBean implements Serializable {
    private String selectedCountry;
    private String selectedCity;
    
    private List<String> countries;
    private List<String> cities = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        countries = Arrays.asList("Italia", "Francia", "Germania");
    }
    
    public void onCountryChange() {
        cities.clear();
        
        switch (selectedCountry) {
            case "Italia":
                cities = Arrays.asList("Roma", "Milano", "Napoli");
                break;
            case "Francia":
                cities = Arrays.asList("Parigi", "Lione", "Marsiglia");
                break;
            case "Germania":
                cities = Arrays.asList("Berlino", "Monaco", "Amburgo");
                break;
        }
        
        selectedCity = null; // Reset della selezione
    }
    
    // Getters e Setters...
}
```

### 3. Ricerca con Autocomplete

```xhtml
<h:form>
    <h:inputText id="search" value="#{searchBean.query}">
        <f:ajax event="keyup" 
                listener="#{searchBean.search}"
                render="suggestions"
                delay="300" />
    </h:inputText>
    
    <h:panelGroup id="suggestions">
        <ul>
            <ui:repeat value="#{searchBean.suggestions}" var="suggestion">
                <li>
                    <h:commandLink value="#{suggestion}" 
                                   action="#{searchBean.select(suggestion)}">
                        <f:ajax render="search @form" />
                    </h:commandLink>
                </li>
            </ui:repeat>
        </ul>
    </h:panelGroup>
</h:form>
```

### 4. Aggiornamento Live di un Totale

```xhtml
<h:form>
    <h:dataTable value="#{cartBean.items}" var="item">
        <h:column>
            <f:facet name="header">Prodotto</f:facet>
            #{item.name}
        </h:column>
        <h:column>
            <f:facet name="header">Prezzo</f:facet>
            <h:outputText value="#{item.price}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </h:column>
        <h:column>
            <f:facet name="header">Quantità</f:facet>
            <h:inputText value="#{item.quantity}" style="width: 50px;">
                <f:ajax event="change" 
                        listener="#{cartBean.updateTotal}"
                        render="totalPanel" />
            </h:inputText>
        </h:column>
        <h:column>
            <f:facet name="header">Subtotale</f:facet>
            <h:outputText value="#{item.price * item.quantity}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </h:column>
    </h:dataTable>
    
    <h:panelGroup id="totalPanel">
        <h3>
            Totale: 
            <h:outputText value="#{cartBean.total}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </h3>
    </h:panelGroup>
</h:form>
```

### 5. Pulsante con Feedback Visivo

```xhtml
<h:form>
    <h:panelGrid columns="2">
        <h:outputLabel for="name" value="Nome:" />
        <h:inputText id="name" value="#{userBean.name}" />
        
        <h:outputLabel for="email" value="Email:" />
        <h:inputText id="email" value="#{userBean.email}" />
    </h:panelGrid>
    
    <h:commandButton id="saveBtn" value="Salva" action="#{userBean.save}">
        <f:ajax execute="@form" 
                render="@form message" 
                onevent="handleSaveEvent" />
    </h:commandButton>
    
    <h:panelGroup id="message">
        <h:messages globalOnly="true" />
    </h:panelGroup>
    
    <div id="loadingSpinner" style="display:none;">
        Salvataggio in corso...
    </div>
</h:form>

<script>
function handleSaveEvent(data) {
    var spinner = document.getElementById("loadingSpinner");
    var button = document.getElementById("saveBtn");
    
    if (data.status === "begin") {
        spinner.style.display = "block";
        button.disabled = true;
    } else if (data.status === "complete") {
        spinner.style.display = "none";
        button.disabled = false;
    }
}
</script>
```

---

## AJAX Grouping: `<f:ajax>` Wrapper

Puoi raggruppare più componenti sotto un singolo tag `<f:ajax>` per applicare lo stesso comportamento a tutti.

```xhtml
<h:form>
    <f:ajax render="output">
        <h:inputText id="field1" value="#{bean.field1}" />
        <h:inputText id="field2" value="#{bean.field2}" />
        <h:inputText id="field3" value="#{bean.field3}" />
    </f:ajax>
    
    <h:outputText id="output" value="#{bean.combinedValue}" />
</h:form>
```

Ogni `inputText` triggererà automaticamente AJAX (evento default `valueChange`) e aggiornerà `output`.

---

## AJAX Programmatico con JavaScript

JSF fornisce l'API JavaScript `jsf.ajax.request()` per invocare richieste AJAX programmaticamente.

**Sintassi:**

```javascript
jsf.ajax.request(source, event, options);
```

**Esempio:**

```xhtml
<h:form id="myForm">
    <h:inputText id="input1" value="#{bean.value}" />
    <h:outputText id="output1" value="#{bean.result}" />
    
    <button type="button" onclick="sendAjax();">Invia AJAX Manuale</button>
</h:form>

<script>
function sendAjax() {
    var input = document.getElementById("myForm:input1");
    jsf.ajax.request(input, null, {
        execute: "myForm:input1",
        render: "myForm:output1"
    });
}
</script>
```

---

## Best Practices per AJAX in JSF

### 1. Usa `@ViewScoped` per bean con AJAX

I bean `@ViewScoped` mantengono lo stato durante le richieste AJAX alla stessa vista, evitando la necessità di ricaricare dati.

```java
@Named
@ViewScoped // Mantiene lo stato durante le chiamate AJAX
public class ProductBean implements Serializable {
    private List<Product> products;
    
    @PostConstruct
    public void init() {
        products = productService.findAll(); // Caricato una sola volta
    }
    
    public void filter() {
        // Filtra la lista esistente senza ricaricare
    }
}
```

### 2. Limita l'ambito di `render` e `execute`

Evita di usare `@all` quando non necessario; specifica solo i componenti che devono essere aggiornati.

```xhtml
<!-- ❌ Non efficiente -->
<f:ajax execute="@all" render="@all" />

<!-- ✅ Efficiente -->
<f:ajax execute="@this" render="specificOutput" />
```

### 3. Usa `delay` per eventi ad alta frequenza

```xhtml
<!-- ✅ Buona pratica: evita troppe richieste -->
<h:inputText value="#{bean.search}">
    <f:ajax event="keyup" render="results" delay="300" />
</h:inputText>
```

### 4. Gestisci gli errori appropriatamente

```xhtml
<f:ajax onerror="handleError" onevent="handleEvent" />

<script>
function handleError(data) {
    console.error("Errore AJAX:", data.errorMessage);
    alert("Si è verificato un errore. Riprova.");
}
</script>
```

### 5. Fornisci feedback visivo all'utente

Mostra spinner o messaggi di "Caricamento..." durante le operazioni AJAX lunghe.

---

## Debugging AJAX

### 1. Console del Browser

Le richieste AJAX JSF appaiono nella console degli strumenti per sviluppatori:

```
Network -> XHR -> /myapp/mypage.xhtml
```

### 2. Logging Lato Server

```java
public void ajaxListener(AjaxBehaviorEvent event) {
    System.out.println("AJAX request ricevuta");
    System.out.println("Componente: " + event.getComponent().getClientId());
}
```

### 3. Verifica del Rendering

```xhtml
<!-- Aggiungi un bordo per vedere cosa viene ri-renderizzato -->
<h:panelGroup id="debugPanel" style="border: 2px solid red;">
    <h:outputText value="#{bean.value}" />
</h:panelGroup>
```

---

## Limitazioni e Considerazioni

### 1. JavaScript deve essere abilitato

AJAX richiede JavaScript; assicurati di avere un fallback per utenti senza JS.

### 2. Gestione dello stato

Con `@ViewScoped`, lo stato cresce; monitora l'uso della memoria per viste complesse.

### 3. SEO

I contenuti caricati via AJAX potrebbero non essere indicizzati dai motori di ricerca.

### 4. Accessibilità

Assicurati che gli aggiornamenti AJAX siano accessibili agli screen reader usando ARIA.

---

## Glossario

| Termine | Descrizione |
|---------|-------------|
| **AJAX** | Asynchronous JavaScript and XML - Tecnica per aggiornare parti di una pagina web senza ricaricarla completamente. |
| **`<f:ajax>`** | Tag JSF per aggiungere comportamento AJAX ai componenti UI. |
| **`event`** | Attributo che specifica quale evento scatena la richiesta AJAX (es. `blur`, `change`, `keyup`). |
| **`render`** | Attributo che specifica quali componenti devono essere aggiornati dopo la richiesta AJAX. |
| **`execute`** | Attributo che specifica quali componenti devono essere processati durante la richiesta AJAX. |
| **`listener`** | Metodo del backing bean invocato durante il processing della richiesta AJAX. |
| **`delay`** | Ritardo in millisecondi prima di inviare la richiesta AJAX (debouncing). |
| **`@this`** | Riferimento al componente corrente. |
| **`@form`** | Riferimento a tutti i componenti del form corrente. |
| **`@all`** | Riferimento a tutti i componenti della vista. |
| **`@none`** | Nessun componente. |
| **`AjaxBehaviorEvent`** | Evento generato durante una richiesta AJAX JSF. |
| **Debouncing** | Tecnica per ritardare l'esecuzione di una funzione fino a quando non sono passati X millisecondi dall'ultima invocazione. |
| **Partial Rendering** | Aggiornamento di solo una porzione della pagina invece dell'intera pagina. |

---

Con AJAX in JSF, puoi creare interfacce utente moderne, reattive e user-friendly, mantenendo la semplicità e la robustezza del framework JSF!
