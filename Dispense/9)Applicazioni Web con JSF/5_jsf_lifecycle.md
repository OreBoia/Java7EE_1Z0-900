# Ciclo di Vita JSF (JSF Lifecycle)

Il **ciclo di vita JSF (JSF Lifecycle)** è una sequenza ben definita di fasi attraverso le quali ogni richiesta HTTP passa quando viene processata da un'applicazione JSF. Comprendere questo ciclo è fondamentale per costruire applicazioni JSF robuste e per diagnosticare problemi.

Il ciclo di vita JSF è particolarmente rilevante per i **postback**, ovvero quando un form JSF viene inviato alla stessa pagina (o a un'altra pagina JSF) per essere processato. Durante questo processo, JSF deve:

1. Ricostruire lo stato dei componenti UI
2. Applicare i valori inviati dall'utente
3. Convertire e validare i dati
4. Aggiornare il modello (i backing bean)
5. Invocare la logica di business
6. Renderizzare la risposta

---

## Le Sei Fasi del Ciclo di Vita JSF

Il ciclo di vita standard di JSF è composto da **sei fasi**:

### 1. Restore View (Ripristino della Vista)

**Cosa succede:**

- JSF ricostruisce (o ripristina dalla cache) l'albero dei componenti UI della pagina richiesta, chiamato **Component Tree** o **View**.
- Se è la prima richiesta (initial request, non un postback), JSF costruisce un nuovo albero vuoto.
- Se è un postback (l'utente ha inviato un form), JSF ripristina l'albero dei componenti salvato dalla richiesta precedente, recuperando lo stato dei componenti.

**Dove viene salvato lo stato:**

- Lo stato può essere salvato lato **server** (nella sessione HTTP) o lato **client** (nascosto nel form HTML come campo `javax.faces.ViewState`).
- Questo è configurabile tramite il parametro `javax.faces.STATE_SAVING_METHOD` nel `web.xml`.

**Quando si salta alla fase Render Response:**

- Se è una richiesta GET iniziale (non postback), JSF salta direttamente alla fase **Render Response**.

---

### 2. Apply Request Values (Applicazione dei Valori della Richiesta)

**Cosa succede:**

- JSF attraversa l'albero dei componenti e per ogni componente di input (es. `<h:inputText>`, `<h:selectOneMenu>`), estrae il valore corrispondente dai parametri della richiesta HTTP.
- Questi valori vengono memorizzati come **submitted values** (valori grezzi, ancora stringhe) nei componenti.
- I componenti non aggiornano ancora le proprietà dei backing bean; semplicemente memorizzano i valori ricevuti.

**Gestione di `immediate="true"`:**

- Se un componente ha `immediate="true"`, la sua conversione, validazione ed evento vengono processati **in questa fase** invece che nelle fasi successive.
- Questo è utile per componenti come pulsanti "Annulla" che devono bypassare la validazione dei campi del form.

**Conversione iniziale:**

- Se un componente ha un `Converter` associato, JSF tenta una conversione di base in questa fase.
- Se la conversione fallisce (es. l'utente inserisce "abc" in un campo numerico), JSF aggiunge un messaggio di errore e **salta** alle fasi di validazione e aggiornamento, andando direttamente a **Render Response** per mostrare l'errore.

---

### 3. Process Validations (Elaborazione delle Validazioni)

**Cosa succede:**

- JSF attraversa nuovamente l'albero dei componenti.
- Per ogni componente di input, JSF:
  1. **Converte** il valore (se non è già stato convertito): il `Converter` associato trasforma la stringa ricevuta nel tipo di dato Java appropriato (es. `String` → `Date`, `String` → `Integer`).
  2. **Valida** il valore convertito: vengono eseguiti tutti i `Validator` associati al componente (es. `<f:validateLength>`, `@NotNull` da Bean Validation, validator personalizzati).

**Cosa succede in caso di errore:**

- Se la conversione o la validazione fallisce per qualsiasi componente, JSF:
  - Aggiunge un `FacesMessage` di errore al contesto.
  - Marca il componente come invalido.
  - **Salta** le fasi successive (Update Model Values, Invoke Application) e passa direttamente a **Render Response** per mostrare gli errori all'utente.

**Attributo `required`:**

- Se un componente ha `required="true"` e il valore inviato è vuoto, JSF genera un errore di validazione in questa fase.

**Gestione di `immediate="true"` per i componenti:**

- I componenti con `immediate="true"` sono già stati validati nella fase precedente (Apply Request Values), quindi non vengono rivalidati qui.

---

### 4. Update Model Values (Aggiornamento dei Valori del Modello)

**Cosa succede:**

- Se tutte le conversioni e validazioni hanno avuto **successo**, JSF procede con l'aggiornamento del modello.
- Per ogni componente di input, JSF invoca il **metodo setter** della proprietà del backing bean associata tramite value binding.
  
  ```java
  // Esempio: se il componente è <h:inputText value="#{userBean.name}" />
  // JSF chiama: userBean.setName(valoreDalComponente);
  ```

- I dati convertiti e validati vengono quindi trasferiti dai componenti UI ai backing bean.

**Quando si salta questa fase:**

- Se nella fase precedente ci sono stati errori di conversione o validazione, questa fase viene **saltata** e JSF va direttamente a **Render Response**.

**Ordine di aggiornamento:**

- L'ordine in cui i setter vengono invocati dipende dall'ordine dei componenti nell'albero UI, non necessariamente dall'ordine visivo nella pagina.

---

### 5. Invoke Application (Invocazione dell'Applicazione)

**Cosa succede:**

- JSF invoca il **metodo d'azione (action method)** specificato nel componente che ha scatenato l'evento (tipicamente un `<h:commandButton>` o `<h:commandLink>`).
- Questo è il momento in cui viene eseguita la **logica di business** dell'applicazione (es. salvare dati nel database, effettuare un login).

**Restituzione dell'outcome:**

- Il metodo d'azione può restituire una **stringa (outcome)** che JSF usa per determinare la **navigazione** verso la pagina successiva.
- Se il metodo restituisce `null` o `void`, l'utente rimane sulla stessa pagina.

**Esempi:**

```java
public String save() {
    // Logica di salvataggio
    database.save(this.product);
    return "success"; // Naviga alla pagina success.xhtml
}

public void update() {
    // Logica di aggiornamento
    database.update(this.user);
    // Rimane sulla stessa pagina (nessun outcome restituito)
}
```

**Eventi di azione:**

- In questa fase vengono elaborati gli `ActionEvent` dei componenti di comando.
- È possibile definire `ActionListener` che vengono eseguiti prima del metodo d'azione.

---

### 6. Render Response (Renderizzazione della Risposta)

**Cosa succede:**

- JSF attraversa l'albero dei componenti e genera l'output HTML finale da inviare al browser.
- Ogni componente viene "renderizzato" chiamando il suo `Renderer` che produce il markup HTML corrispondente.
- Se ci sono stati errori nelle fasi precedenti, i messaggi di errore (`FacesMessage`) vengono inclusi nella pagina renderizzata (tramite `<h:message>` o `<h:messages>`).

**Salvataggio dello stato:**

- Prima di inviare la risposta, JSF salva lo stato corrente della vista (l'albero dei componenti) per poterlo ripristinare nella prossima richiesta.
- Lo stato viene salvato lato server o lato client a seconda della configurazione.

**Richieste AJAX:**

- Per le richieste AJAX, JSF non renderizza l'intera pagina, ma solo i componenti specificati nell'attributo `render` del tag `<f:ajax>`.
- L'output parziale viene inviato al client in formato XML e il JavaScript di JSF aggiorna solo le porzioni necessarie del DOM.

---

## Diagramma del Ciclo di Vita JSF

```
┌─────────────────────────────────────────────────────────────┐
│                    INIZIO RICHIESTA                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
          ┌────────────────────────┐
          │   1. RESTORE VIEW      │
          │  (Ripristino Vista)    │
          └────────┬───────────────┘
                   │
                   │ GET Request? ──────────────┐
                   │                            │
                   ▼ No (Postback)              │
          ┌────────────────────────┐            │
          │ 2. APPLY REQUEST VALUES│            │
          │ (Applica Valori)       │            │
          └────────┬───────────────┘            │
                   │                            │
                   │ Errore conversione? ───┐   │
                   │                        │   │
                   ▼ No                     │   │
          ┌────────────────────────┐        │   │
          │ 3. PROCESS VALIDATIONS │        │   │
          │ (Valida)               │        │   │
          └────────┬───────────────┘        │   │
                   │                        │   │
                   │ Errore validazione? ───┤   │
                   │                        │   │
                   ▼ No                     │   │
          ┌────────────────────────┐        │   │
          │ 4. UPDATE MODEL VALUES │        │   │
          │ (Aggiorna Modello)     │        │   │
          └────────┬───────────────┘        │   │
                   │                        │   │
                   ▼                        │   │
          ┌────────────────────────┐        │   │
          │ 5. INVOKE APPLICATION  │        │   │
          │ (Invoca Azione)        │        │   │
          └────────┬───────────────┘        │   │
                   │                        │   │
                   └────────────────────────┼───┤
                                            │   │
                                            ▼   ▼
                              ┌────────────────────────┐
                              │ 6. RENDER RESPONSE     │
                              │ (Renderizza Risposta)  │
                              └────────┬───────────────┘
                                       │
                                       ▼
                              ┌────────────────────────┐
                              │     FINE RICHIESTA     │
                              └────────────────────────┘
```

---

## Casi Speciali e Shortcut del Ciclo di Vita

### 1. Richiesta GET Iniziale (Initial Request)

Quando un utente accede per la prima volta a una pagina JSF (senza postback):

1. **Restore View**: JSF crea un nuovo albero di componenti vuoto.
2. JSF **salta** tutte le fasi intermedie (Apply Request Values, Process Validations, Update Model Values, Invoke Application).
3. **Render Response**: La pagina viene renderizzata direttamente.

### 2. Errori di Conversione

Se durante **Apply Request Values** un converter fallisce:

1. JSF aggiunge un messaggio di errore al contesto.
2. **Salta** Process Validations, Update Model Values e Invoke Application.
3. Va direttamente a **Render Response** per mostrare l'errore.

### 3. Errori di Validazione

Se durante **Process Validations** un validator fallisce:

1. JSF aggiunge un messaggio di errore.
2. **Salta** Update Model Values e Invoke Application.
3. Va direttamente a **Render Response**.

### 4. Componenti con `immediate="true"`

Un componente (tipicamente un pulsante) con `immediate="true"`:

1. La sua conversione, validazione ed evento vengono processati durante **Apply Request Values** invece che nelle fasi successive.
2. Questo permette di bypassare la validazione degli altri componenti del form.

**Esempio tipico: Pulsante "Annulla"**

```xhtml
<h:form>
    Nome: <h:inputText value="#{userBean.name}" required="true" />
    Email: <h:inputText value="#{userBean.email}" required="true" />
    
    <h:commandButton value="Salva" action="#{userBean.save}" />
    <h:commandButton value="Annulla" action="home" immediate="true" />
</h:form>
```

Se l'utente lascia i campi vuoti e clicca "Annulla", il pulsante con `immediate="true"` bypassa la validazione `required="true"` e naviga direttamente alla home.

### 5. Richieste AJAX

Con `<f:ajax>`:

- Solo i componenti specificati in `execute` passano attraverso le fasi del ciclo di vita.
- Solo i componenti specificati in `render` vengono ri-renderizzati nella fase Render Response.
- Questo rende il processo molto più efficiente per aggiornamenti parziali della pagina.

---

## L'Attributo `immediate` in Dettaglio

L'attributo `immediate` modifica il comportamento standard del ciclo di vita e può essere applicato a:

### Su Componenti di Input (`UIInput`)

Quando `immediate="true"` è impostato su un componente di input:

- La **conversione** e la **validazione** del componente avvengono durante la fase **Apply Request Values** (fase 2) invece che durante **Process Validations** (fase 3).
- Se la conversione/validazione fallisce, il ciclo salta direttamente a **Render Response**.
- Questo è raramente usato sui componenti di input; è più comune sui componenti di comando.

### Su Componenti di Comando (`UICommand`)

Quando `immediate="true"` è impostato su un componente di comando (es. `<h:commandButton>`):

- L'**action event** viene elaborato durante la fase **Apply Request Values** (fase 2) invece che durante **Invoke Application** (fase 5).
- Questo significa che l'azione viene eseguita **prima** della validazione degli altri componenti del form.
- **Caso d'uso principale**: Pulsanti di "Annulla", "Indietro" o navigazione che non devono triggerare la validazione del form.

**Esempio pratico:**

```xhtml
<h:form>
    <h:panelGrid columns="2">
        Username: <h:inputText value="#{loginBean.username}" required="true" />
        Password: <h:inputSecret value="#{loginBean.password}" required="true" />
    </h:panelGrid>
    
    <h:commandButton value="Login" action="#{loginBean.login}" />
    <h:commandButton value="Registrati" action="register?faces-redirect=true" immediate="true" />
</h:form>
```

Se l'utente clicca "Registrati" senza compilare i campi:

- **Senza** `immediate="true"`: JSF validerebbe i campi, troverebbe errori (required) e rimarrebbe sulla pagina mostrando gli errori.
- **Con** `immediate="true"`: JSF esegue l'azione di navigazione subito, saltando la validazione, e l'utente viene reindirizzato alla pagina di registrazione.

---

## PhaseListener: Intercettare il Ciclo di Vita

JSF permette di intercettare e modificare il comportamento del ciclo di vita tramite **PhaseListener**. Un `PhaseListener` è un'interfaccia che può essere implementata per eseguire codice prima e dopo ogni fase del ciclo di vita.

### Interfaccia PhaseListener

```java
public interface PhaseListener extends EventListener {
    void afterPhase(PhaseEvent event);
    void beforePhase(PhaseEvent event);
    PhaseId getPhaseId(); // Specifica quale fase intercettare
}
```

### Esempio di PhaseListener

```java
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class LoggingPhaseListener implements PhaseListener {
    
    @Override
    public void beforePhase(PhaseEvent event) {
        System.out.println("PRIMA della fase: " + event.getPhaseId());
    }
    
    @Override
    public void afterPhase(PhaseEvent event) {
        System.out.println("DOPO la fase: " + event.getPhaseId());
    }
    
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE; // Intercetta tutte le fasi
        // Oppure: PhaseId.RENDER_RESPONSE per una fase specifica
    }
}
```

### Registrazione del PhaseListener

Il `PhaseListener` può essere registrato nel `faces-config.xml`:

```xml
<faces-config>
    <lifecycle>
        <phase-listener>com.example.LoggingPhaseListener</phase-listener>
    </lifecycle>
</faces-config>
```

### Casi d'Uso per PhaseListener

- **Logging e debugging**: Tracciare il flusso delle richieste attraverso il ciclo di vita.
- **Security checks**: Verificare l'autenticazione/autorizzazione prima di determinate fasi.
- **Performance monitoring**: Misurare il tempo impiegato in ogni fase.
- **Manipolazione del contesto**: Modificare il `FacesContext` o aggiungere dati al modello prima del rendering.

---

## FacesContext: Il Cuore del Ciclo di Vita

Il **`FacesContext`** è un oggetto centrale in JSF che rappresenta il contesto della richiesta corrente. È disponibile in ogni fase del ciclo di vita e fornisce accesso a:

### Principali Funzionalità di FacesContext

```java
// Ottenere l'istanza corrente
FacesContext context = FacesContext.getCurrentInstance();

// Accesso agli oggetti impliciti
HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
HttpSession session = (HttpSession) context.getExternalContext().getSession(true);

// Aggiungere messaggi
context.addMessage(null, new FacesMessage("Operazione completata con successo"));
context.addMessage("formId:inputId", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Valore non valido", null));

// Verificare errori di validazione
boolean hasErrors = context.isValidationFailed();

// Forzare il rendering immediato (saltare le fasi rimanenti)
context.renderResponse();

// Accedere alla vista corrente
UIViewRoot viewRoot = context.getViewRoot();

// Navigazione programmatica
NavigationHandler navHandler = context.getApplication().getNavigationHandler();
navHandler.handleNavigation(context, null, "success?faces-redirect=true");
```

### Metodi Chiave per Controllare il Flusso

- **`renderResponse()`**: Salta tutte le fasi rimanenti e va direttamente a Render Response. Utile quando si vuole interrompere il processing dopo aver rilevato un errore.
- **`responseComplete()`**: Indica che la risposta è stata completamente gestita e JSF non deve fare più nulla. Usato quando si inviano risposte personalizzate (es. download di file, redirect manuale).

---

## Conversione e Validazione nel Ciclo di Vita

### Ordine di Esecuzione

Durante la fase **Process Validations**, per ogni componente di input, JSF esegue nell'ordine:

1. **Conversione** (tramite `Converter`)
2. **Validazione richiesta** (attributo `required="true"`)
3. **Validazioni custom** (validator specifici del componente)
4. **Bean Validation** (annotazioni JSR 303/380 come `@NotNull`, `@Size`)

### Esempio Completo

```xhtml
<h:form>
    <h:inputText id="birthdate" 
                 value="#{userBean.birthDate}" 
                 required="true"
                 requiredMessage="La data di nascita è obbligatoria"
                 validatorMessage="La data non è valida">
        <f:convertDateTime pattern="dd/MM/yyyy" />
        <f:validator validatorId="futureDateValidator" />
    </h:inputText>
    <h:message for="birthdate" />
    
    <h:commandButton value="Salva" action="#{userBean.save}" />
</h:form>
```

**Sequenza di validazione:**

1. **Conversione**: JSF usa `convertDateTime` per convertire la stringa "25/12/2000" in un oggetto `Date`.
   - Se la conversione fallisce (formato errato), JSF mostra un errore e salta le fasi successive.
2. **Required check**: Verifica che il valore non sia vuoto.
3. **Custom validator**: Esegue `futureDateValidator` per verificare che la data non sia nel futuro.
4. **Bean Validation**: Se la proprietà `birthDate` nel bean ha annotazioni (es. `@Past`), vengono validate.

Se tutte le validazioni passano, nella fase **Update Model Values** JSF chiama:

```java
userBean.setBirthDate(dataConvertita);
```

---

## AJAX e il Ciclo di Vita Parziale

Quando si usa `<f:ajax>`, JSF esegue un **ciclo di vita parziale**:

### Attributi Chiave di `<f:ajax>`

- **`execute`**: Specifica quali componenti devono essere processati durante il ciclo di vita.
  - `@this`: Solo il componente corrente
  - `@form`: Tutti i componenti del form
  - `@all`: Tutti i componenti della vista
  - `@none`: Nessun componente
  - ID specifici: `"input1 input2"`

- **`render`**: Specifica quali componenti devono essere ri-renderizzati.
  - Stesse opzioni di `execute`

### Esempio

```xhtml
<h:form>
    <h:inputText id="username" value="#{registerBean.username}">
        <f:ajax event="blur" 
                listener="#{registerBean.checkUsername}"
                execute="@this"
                render="usernameMsg" />
    </h:inputText>
    <h:outputText id="usernameMsg" value="#{registerBean.usernameStatus}" />
    
    <h:inputText id="email" value="#{registerBean.email}" />
    
    <h:commandButton value="Registra" action="#{registerBean.register}">
        <f:ajax execute="@form" render="@form" />
    </h:commandButton>
</h:form>
```

**Cosa succede quando l'utente lascia il campo username (evento blur):**

1. Viene inviata una richiesta AJAX al server.
2. **Execute @this**: Solo il componente `username` passa attraverso tutte le fasi del ciclo di vita (conversione, validazione, update model).
3. Il listener `checkUsername()` viene invocato.
4. **Render usernameMsg**: Solo il componente `usernameMsg` viene ri-renderizzato e aggiornato nel browser.
5. Il campo `email` non viene toccato.

---

## Best Practices e Consigli

### 1. Comprendere quando usare `immediate="true"`

- **Pulsanti di navigazione/cancellazione**: Usare `immediate="true"` per bypassare la validazione.
- **Input fields**: Raramente necessario; può causare comportamenti inaspettati.

### 2. Gestire gli errori appropriatamente

```java
public String save() {
    try {
        service.saveUser(user);
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Utente salvato con successo", null));
        return "success?faces-redirect=true";
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Errore durante il salvataggio: " + e.getMessage(), null));
        return null; // Rimane sulla stessa pagina
    }
}
```

### 3. Usare AJAX per migliorare l'esperienza utente

- Validazione in tempo reale dei campi
- Aggiornamento di liste senza refresh della pagina
- Autocomplete e ricerche dinamiche

### 4. Debug del ciclo di vita

Per debuggare problemi del ciclo di vita:

1. Implementare un `PhaseListener` che logga ogni fase.
2. Verificare i messaggi di errore con `<h:messages globalOnly="true" />`.
3. Usare gli strumenti di sviluppo del browser per ispezionare le richieste AJAX.

### 5. Attenzione allo stato della vista

- Lo stato può crescere rapidamente, impattando le performance.
- Considerare l'uso di `@ViewScoped` per mantenere lo stato durante interazioni AJAX sulla stessa pagina.
- Valutare se salvare lo stato lato client o server in base alle esigenze.

---

## Glossario dei Termini Chiave

| Termine | Descrizione |
|---------|-------------|
| **Ciclo di Vita (Lifecycle)** | La sequenza di sei fasi che JSF esegue per processare una richiesta e generare una risposta. |
| **Postback** | Una richiesta HTTP (tipicamente POST) che una pagina JSF invia a se stessa per processare i dati del form. |
| **Component Tree** | L'albero gerarchico di componenti UI che rappresenta la struttura della vista JSF. |
| **View State** | Lo stato serializzato dell'albero dei componenti, salvato tra le richieste per poter ricostruire la vista. |
| **Restore View** | Fase 1: Ripristino dell'albero dei componenti dalla richiesta precedente. |
| **Apply Request Values** | Fase 2: Estrazione dei valori dalla richiesta HTTP e memorizzazione nei componenti. |
| **Process Validations** | Fase 3: Conversione e validazione dei valori dei componenti. |
| **Update Model Values** | Fase 4: Trasferimento dei valori validati dai componenti ai backing bean (invocazione dei setter). |
| **Invoke Application** | Fase 5: Esecuzione del metodo d'azione (action method) e della logica di business. |
| **Render Response** | Fase 6: Generazione dell'HTML finale e invio al client. |
| **`immediate="true"`** | Attributo che anticipa l'elaborazione di un componente alla fase Apply Request Values, bypassando la validazione normale. |
| **`FacesContext`** | Oggetto che rappresenta il contesto della richiesta corrente, fornendo accesso a tutte le informazioni e funzionalità di JSF. |
| **`PhaseListener`** | Interfaccia per intercettare e reagire alle diverse fasi del ciclo di vita JSF. |
| **`PhaseEvent`** | Evento generato all'inizio e alla fine di ogni fase del ciclo di vita. |
| **Ciclo di vita parziale** | Ciclo di vita applicato solo a un sottoinsieme di componenti durante una richiesta AJAX. |
| **`renderResponse()`** | Metodo di `FacesContext` che forza il salto diretto alla fase Render Response. |
| **`responseComplete()`** | Metodo di `FacesContext` che indica che la risposta è stata completamente gestita e JSF non deve fare più nulla. |
| **Submitted Value** | Il valore grezzo (stringa) estratto dalla richiesta HTTP prima della conversione. |
| **Converted Value** | Il valore dopo la conversione dal formato stringa al tipo Java appropriato. |
| **Validated Value** | Il valore che ha superato tutti i controlli di validazione ed è pronto per aggiornare il modello. |

---

## Esempio Completo: Tracciare il Ciclo di Vita

Ecco un esempio completo che mostra come i valori fluiscono attraverso il ciclo di vita:

### Backing Bean

```java
@Named("productBean")
@RequestScoped
public class ProductBean {
    
    private String productName;
    private Double price;
    private String message;
    
    // Getters e Setters
    
    public void validatePrice(FacesContext context, UIComponent component, Object value) {
        System.out.println("FASE 3 - Validazione custom del prezzo: " + value);
        Double priceValue = (Double) value;
        if (priceValue < 0) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Il prezzo non può essere negativo", null));
        }
        if (priceValue > 10000) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Il prezzo è troppo alto (max 10000)", null));
        }
    }
    
    public String save() {
        System.out.println("FASE 5 - Invocazione dell'azione save()");
        System.out.println("Nome prodotto: " + productName);
        System.out.println("Prezzo: " + price);
        
        // Simulazione salvataggio
        message = "Prodotto salvato: " + productName + " - €" + price;
        
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
        
        return null; // Rimane sulla stessa pagina
    }
    
    public String cancel() {
        System.out.println("FASE 2 - Azione cancel() con immediate=true");
        return "home?faces-redirect=true";
    }
}
```

### Vista XHTML

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>Test Ciclo di Vita JSF</title>
</h:head>
<h:body>
    <h1>Inserisci Prodotto</h1>
    
    <h:form id="productForm">
        <h:panelGrid columns="3">
            <h:outputLabel for="name" value="Nome Prodotto:" />
            <h:inputText id="name" 
                         value="#{productBean.productName}" 
                         required="true"
                         requiredMessage="Il nome è obbligatorio">
                <f:ajax event="blur" render="nameMsg" />
            </h:inputText>
            <h:message id="nameMsg" for="name" style="color: red;" />
            
            <h:outputLabel for="price" value="Prezzo (€):" />
            <h:inputText id="price" 
                         value="#{productBean.price}" 
                         required="true"
                         validator="#{productBean.validatePrice}">
                <f:convertNumber type="number" minFractionDigits="2" maxFractionDigits="2" />
            </h:inputText>
            <h:message for="price" style="color: red;" />
        </h:panelGrid>
        
        <h:panelGroup>
            <h:commandButton value="Salva" action="#{productBean.save}" />
            <h:commandButton value="Annulla" action="#{productBean.cancel}" immediate="true" />
        </h:panelGroup>
        
        <h:messages globalOnly="true" style="color: green; font-weight: bold;" />
    </h:form>
    
    <hr />
    <h3>Cosa succede durante il ciclo di vita:</h3>
    <ol>
        <li><strong>FASE 1 - Restore View:</strong> JSF ripristina l'albero dei componenti.</li>
        <li><strong>FASE 2 - Apply Request Values:</strong> JSF estrae i valori dal form e li applica ai componenti. 
            Se clicchi "Annulla" (immediate=true), l'azione viene eseguita qui.</li>
        <li><strong>FASE 3 - Process Validations:</strong> JSF converte "123.45" in Double e valida con il validator custom. 
            Se il prezzo è negativo o troppo alto, viene mostrato un errore.</li>
        <li><strong>FASE 4 - Update Model Values:</strong> Se la validazione ha successo, JSF chiama 
            <code>setProductName()</code> e <code>setPrice()</code>.</li>
        <li><strong>FASE 5 - Invoke Application:</strong> JSF esegue il metodo <code>save()</code>.</li>
        <li><strong>FASE 6 - Render Response:</strong> La pagina viene renderizzata con i messaggi di successo o errore.</li>
    </ol>
</h:body>
</html>
```

### Console Output (esempio di esecuzione)

**Scenario 1: Utente inserisce dati validi e clicca "Salva"**

```
FASE 1 - Restore View
FASE 2 - Apply Request Values: name="Laptop", price="999.99"
FASE 3 - Conversione: "999.99" → Double(999.99)
FASE 3 - Validazione custom del prezzo: 999.99
FASE 4 - Update Model: setProductName("Laptop"), setPrice(999.99)
FASE 5 - Invocazione dell'azione save()
Nome prodotto: Laptop
Prezzo: 999.99
FASE 6 - Render Response: Messaggio "Prodotto salvato: Laptop - €999.99"
```

**Scenario 2: Utente inserisce prezzo negativo e clicca "Salva"**

```
FASE 1 - Restore View
FASE 2 - Apply Request Values: name="Mouse", price="-10.00"
FASE 3 - Conversione: "-10.00" → Double(-10.0)
FASE 3 - Validazione custom del prezzo: -10.0
ERRORE: Il prezzo non può essere negativo
FASE 6 - Render Response: Mostra errore di validazione
```

**Scenario 3: Utente lascia i campi vuoti e clicca "Annulla"**

```
FASE 1 - Restore View
FASE 2 - Apply Request Values
FASE 2 - Azione cancel() con immediate=true
NAVIGAZIONE: home?faces-redirect=true
FASE 6 - Render Response: Redirect alla home
```

---

## Conclusione

Il ciclo di vita JSF è il meccanismo fondamentale che permette a JSF di:

1. Gestire lo stato dei componenti UI tra le richieste
2. Convertire e validare i dati dell'utente in modo robusto
3. Aggiornare il modello (backing bean) solo quando i dati sono validi
4. Eseguire la logica di business e gestire la navigazione
5. Renderizzare una risposta appropriata con messaggi di errore o successo

Comprendere il ciclo di vita è essenziale per:

- Costruire applicazioni JSF robuste e manutenibili
- Debuggare problemi di conversione, validazione e binding
- Ottimizzare le performance con AJAX e cicli di vita parziali
- Personalizzare il comportamento con `PhaseListener` e `immediate`

Con questa conoscenza, sei pronto per affrontare qualsiasi scenario di sviluppo JSF e per rispondere alle domande della certificazione 1Z0-900 relative al ciclo di vita JSF!
