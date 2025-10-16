# Gestione degli Eventi in JSF

JSF utilizza un modello di programmazione event-driven simile alle applicazioni desktop tradizionali. Gli eventi permettono ai componenti UI di comunicare con i backing bean e di reagire alle interazioni dell'utente.

---

## Tipi di Eventi in JSF

JSF definisce diversi tipi di eventi che coprono varie fasi del ciclo di vita e interazioni dell'utente.

### 1. Eventi di Azione (`ActionEvent`)

Generati da componenti di comando quando l'utente li attiva.

**Componenti che generano ActionEvent:**

- `<h:commandButton>`
- `<h:commandLink>`

**Esempio:**

```xhtml
<h:commandButton value="Salva" action="#{userBean.save}">
    <f:actionListener type="com.example.LoggingActionListener" />
</h:commandButton>
```

```java
@Named
@RequestScoped
public class UserBean {
    
    // Action method - restituisce un outcome per la navigazione
    public String save() {
        System.out.println("Salvando l'utente...");
        // Logica di salvataggio
        return "success"; // Outcome per la navigazione
    }
}
```

### 2. Eventi di Cambio Valore (`ValueChangeEvent`)

Generati quando il valore di un componente di input cambia.

**Componenti che generano ValueChangeEvent:**

- `<h:inputText>`
- `<h:inputSecret>`
- `<h:inputTextarea>`
- `<h:selectOneMenu>`
- `<h:selectOneRadio>`
- `<h:selectBooleanCheckbox>`
- `<h:selectManyCheckbox>`

**Esempio:**

```xhtml
<h:form>
    <h:selectOneMenu value="#{countryBean.selectedCountry}" 
                     valueChangeListener="#{countryBean.onCountryChange}"
                     onchange="submit()">
        <f:selectItems value="#{countryBean.countries}" />
    </h:selectOneMenu>
    
    <h:outputText value="Hai selezionato: #{countryBean.selectedCountry}" />
</h:form>
```

```java
import javax.faces.event.ValueChangeEvent;

@Named
@ViewScoped
public class CountryBean implements Serializable {
    private String selectedCountry;
    private List<String> countries;
    
    @PostConstruct
    public void init() {
        countries = Arrays.asList("Italia", "Francia", "Germania", "Spagna");
    }
    
    public void onCountryChange(ValueChangeEvent event) {
        String oldValue = (String) event.getOldValue();
        String newValue = (String) event.getNewValue();
        
        System.out.println("Paese cambiato da '" + oldValue + "' a '" + newValue + "'");
        
        // Logica aggiuntiva basata sul nuovo valore
        if ("Italia".equals(newValue)) {
            // Carica le città italiane
        }
    }
    
    // Getters e Setters...
}
```

**Nota:** Il `ValueChangeListener` viene invocato durante la fase **Process Validations** del ciclo di vita JSF, solo se il valore è effettivamente cambiato.

### 3. Eventi AJAX (`AjaxBehaviorEvent`)

Generati durante richieste AJAX con `<f:ajax>`.

**Esempio:**

```xhtml
<h:form>
    <h:inputText id="username" value="#{registerBean.username}">
        <f:ajax event="blur" 
                listener="#{registerBean.checkUsernameAvailability}"
                render="usernameMsg" />
    </h:inputText>
    <h:outputText id="usernameMsg" value="#{registerBean.usernameMessage}" 
                  style="#{registerBean.usernameAvailable ? 'color:green' : 'color:red'}" />
</h:form>
```

```java
import javax.faces.event.AjaxBehaviorEvent;

@Named
@ViewScoped
public class RegisterBean implements Serializable {
    private String username;
    private String usernameMessage;
    private boolean usernameAvailable;
    
    @Inject
    private UserService userService;
    
    public void checkUsernameAvailability(AjaxBehaviorEvent event) {
        System.out.println("Verificando disponibilità username: " + username);
        
        if (username != null && !username.isEmpty()) {
            usernameAvailable = userService.isUsernameAvailable(username);
            
            if (usernameAvailable) {
                usernameMessage = "✓ Username disponibile";
            } else {
                usernameMessage = "✗ Username già in uso";
            }
        } else {
            usernameMessage = "";
        }
    }
    
    // Getters e Setters...
}
```

### 4. Eventi di Sistema (`SystemEvent`)

Eventi legati al ciclo di vita della vista e dell'applicazione.

**Principali SystemEvent:**

| Evento | Quando viene generato |
|--------|----------------------|
| `PreRenderViewEvent` | Prima del rendering della vista |
| `PostConstructViewMapEvent` | Dopo la creazione della ViewMap |
| `PreDestroyViewMapEvent` | Prima della distruzione della ViewMap |
| `PreRenderComponentEvent` | Prima del rendering di un componente |
| `PostAddToViewEvent` | Dopo l'aggiunta di un componente alla vista |
| `PreValidateEvent` | Prima della validazione di un componente |
| `PostValidateEvent` | Dopo la validazione di un componente |

**Esempio con PreRenderViewEvent:**

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>Order Details</title>
</h:head>
<h:body>
    <!-- Metadata della vista -->
    <f:metadata>
        <f:viewParam name="orderId" value="#{orderBean.orderId}" />
        <f:event type="preRenderView" listener="#{orderBean.loadOrder}" />
    </f:metadata>
    
    <h:panelGroup rendered="#{not empty orderBean.order}">
        <h1>Ordine ##{orderBean.order.id}</h1>
        <p>Cliente: #{orderBean.order.customerName}</p>
        <p>Data: 
            <h:outputText value="#{orderBean.order.orderDate}">
                <f:convertDateTime pattern="dd/MM/yyyy" />
            </h:outputText>
        </p>
        <p>Totale: 
            <h:outputText value="#{orderBean.order.total}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </p>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{empty orderBean.order}">
        <p>Ordine non trovato.</p>
    </h:panelGroup>
</h:body>
</html>
```

```java
import javax.faces.event.ComponentSystemEvent;
import javax.faces.application.NavigationHandler;

@Named
@ViewScoped
public class OrderBean implements Serializable {
    
    @Inject
    private OrderService orderService;
    
    private int orderId;
    private Order order;
    
    public void loadOrder(ComponentSystemEvent event) {
        System.out.println("PreRenderView: Caricamento ordine ID " + orderId);
        
        if (orderId > 0) {
            order = orderService.findById(orderId);
            
            if (order == null) {
                // Ordine non trovato, redirect alla lista
                FacesContext ctx = FacesContext.getCurrentInstance();
                NavigationHandler nav = ctx.getApplication().getNavigationHandler();
                nav.handleNavigation(ctx, null, "orders?faces-redirect=true");
                ctx.renderResponse(); // Salta le fasi rimanenti
            }
        } else {
            // ID non valido
            FacesContext ctx = FacesContext.getCurrentInstance();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "ID ordine non valido", null));
        }
    }
    
    // Getters e Setters...
}
```

**Vantaggi di PreRenderView:**

- Permette di caricare dati prima del rendering
- Permette di fare redirect se necessario (es. risorsa non trovata)
- Viene eseguito sia per richieste GET che per postback

### 5. Eventi di Fase (`PhaseEvent`)

Eventi generati all'inizio e alla fine di ogni fase del ciclo di vita JSF.

**Esempio di PhaseListener:**

```java
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class PerformancePhaseListener implements PhaseListener {
    
    private long startTime;
    
    @Override
    public void beforePhase(PhaseEvent event) {
        startTime = System.currentTimeMillis();
        System.out.println("=== INIZIO " + event.getPhaseId() + " ===");
    }
    
    @Override
    public void afterPhase(PhaseEvent event) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("=== FINE " + event.getPhaseId() + " (durata: " + duration + "ms) ===");
        
        // Esempio: Log delle fasi lente
        if (duration > 500) {
            System.err.println("ATTENZIONE: Fase " + event.getPhaseId() + 
                             " ha impiegato " + duration + "ms");
        }
    }
    
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE; // Intercetta tutte le fasi
        // Oppure: PhaseId.RENDER_RESPONSE per una fase specifica
    }
}
```

**Registrazione in `faces-config.xml`:**

```xml
<faces-config>
    <lifecycle>
        <phase-listener>com.example.PerformancePhaseListener</phase-listener>
    </lifecycle>
</faces-config>
```

---

## Action Methods vs Action Listeners

È importante comprendere la differenza tra **action methods** e **action listeners**.

### Action Method

- Definito con l'attributo `action`
- Restituisce un **outcome** (String) per la navigazione
- Eseguito durante la fase **Invoke Application**
- Usato per logica di business principale

```xhtml
<h:commandButton value="Salva" action="#{userBean.save}" />
```

```java
public String save() {
    // Logica di business
    userService.save(user);
    return "success"; // Outcome per navigazione
}
```

### Action Listener

- Definito con l'attributo `actionListener` o il tag `<f:actionListener>`
- **Non restituisce** nulla (void)
- Eseguito **prima** dell'action method
- Usato per logica accessoria (logging, notifiche, ecc.)

```xhtml
<h:commandButton value="Salva" 
                 actionListener="#{userBean.logAction}"
                 action="#{userBean.save}" />
```

```java
import javax.faces.event.ActionEvent;

public void logAction(ActionEvent event) {
    System.out.println("Pulsante premuto da " + getCurrentUser());
    // Nessun valore di ritorno
}

public String save() {
    userService.save(user);
    return "success";
}
```

**Ordine di esecuzione:**

1. Action Listeners (se presenti)
2. Action Method

---

## `<f:event>` - Tag per Eventi di Sistema

Il tag `<f:event>` permette di collegare listener a eventi di sistema.

### Sintassi

```xhtml
<f:event type="eventType" listener="#{bean.listenerMethod}" />
```

### Eventi Supportati

```xhtml
<!-- PreRenderView: Prima del rendering della vista -->
<f:event type="preRenderView" listener="#{bean.init}" />

<!-- PostAddToView: Dopo l'aggiunta di un componente -->
<f:event type="postAddToView" listener="#{bean.componentAdded}" />

<!-- PreValidate: Prima della validazione -->
<f:event type="preValidate" listener="#{bean.beforeValidation}" />

<!-- PostValidate: Dopo la validazione -->
<f:event type="postValidate" listener="#{bean.afterValidation}" />
```

### Esempio Completo: Inizializzazione con Parametri

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>User Profile</title>
</h:head>
<h:body>
    <f:metadata>
        <!-- Cattura parametri dalla query string -->
        <f:viewParam name="userId" value="#{userProfileBean.userId}" />
        <f:viewParam name="tab" value="#{userProfileBean.activeTab}" />
        
        <!-- Inizializza il bean prima del rendering -->
        <f:event type="preRenderView" listener="#{userProfileBean.init}" />
    </f:metadata>
    
    <h:panelGroup rendered="#{not empty userProfileBean.user}">
        <h1>#{userProfileBean.user.name}</h1>
        
        <h:form>
            <h:commandLink value="Info" action="#{userProfileBean.showInfo}">
                <f:param name="tab" value="info" />
            </h:commandLink>
            
            <h:commandLink value="Ordini" action="#{userProfileBean.showOrders}">
                <f:param name="tab" value="orders" />
            </h:commandLink>
            
            <h:panelGroup rendered="#{userProfileBean.activeTab == 'info'}">
                <!-- Tab Info -->
            </h:panelGroup>
            
            <h:panelGroup rendered="#{userProfileBean.activeTab == 'orders'}">
                <!-- Tab Ordini -->
            </h:panelGroup>
        </h:form>
    </h:panelGroup>
</h:body>
</html>
```

```java
@Named
@ViewScoped
public class UserProfileBean implements Serializable {
    
    @Inject
    private UserService userService;
    
    private int userId;
    private String activeTab = "info"; // Tab di default
    private User user;
    
    public void init(ComponentSystemEvent event) {
        System.out.println("Inizializzando profilo per userId: " + userId);
        
        if (userId > 0) {
            user = userService.findById(userId);
            
            if (user == null) {
                // User non trovato, redirect
                FacesContext ctx = FacesContext.getCurrentInstance();
                ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, 
                    "Utente non trovato", null));
                
                NavigationHandler nav = ctx.getApplication().getNavigationHandler();
                nav.handleNavigation(ctx, null, "users?faces-redirect=true");
                ctx.renderResponse();
            }
        }
    }
    
    public String showInfo() {
        activeTab = "info";
        return null; // Rimane sulla stessa pagina
    }
    
    public String showOrders() {
        activeTab = "orders";
        return null;
    }
    
    // Getters e Setters...
}
```

---

## `<f:viewParam>` e `<f:viewAction>`

### `<f:viewParam>` - Parametri della Vista

Cattura parametri dalla query string e li inietta nel bean.

```xhtml
<f:metadata>
    <f:viewParam name="id" value="#{productBean.productId}" />
    <f:viewParam name="category" value="#{productBean.category}" />
</f:metadata>

<!-- URL: product.xhtml?id=123&category=electronics -->
```

**Con validazione:**

```xhtml
<f:metadata>
    <f:viewParam name="id" 
                 value="#{productBean.productId}" 
                 required="true"
                 requiredMessage="ID prodotto obbligatorio">
        <f:validateLongRange minimum="1" />
    </f:viewParam>
</f:metadata>
```

### `<f:viewAction>` - Azioni della Vista (JSF 2.2+)

Esegue un'azione durante il ciclo di vita della vista.

```xhtml
<f:metadata>
    <f:viewParam name="id" value="#{productBean.productId}" />
    <f:viewAction action="#{productBean.loadProduct}" />
</f:metadata>
```

```java
public String loadProduct() {
    product = productService.findById(productId);
    
    if (product == null) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage("Prodotto non trovato"));
        return "products?faces-redirect=true";
    }
    
    return null; // Rimane sulla pagina corrente
}
```

**Differenza tra `<f:event type="preRenderView">` e `<f:viewAction>`:**

- `<f:event type="preRenderView">` viene eseguito per **ogni richiesta** (GET e POST)
- `<f:viewAction>` può essere configurato per eseguirsi solo per specifiche fasi (es. solo GET iniziali)

```xhtml
<!-- Solo per GET iniziali (non postback) -->
<f:viewAction action="#{bean.init}" onPostback="false" />

<!-- Sempre (default) -->
<f:viewAction action="#{bean.init}" />
```

---

## Ordine di Esecuzione degli Eventi

Quando ci sono più eventi e listener, l'ordine di esecuzione è importante.

### Per un Componente di Comando

```xhtml
<h:commandButton value="Salva"
                 actionListener="#{bean.listener1}"
                 action="#{bean.action}">
    <f:actionListener type="com.example.CustomActionListener" />
    <f:ajax listener="#{bean.ajaxListener}" />
</h:commandButton>
```

**Ordine di esecuzione:**

1. `AjaxBehaviorListener` (se AJAX)
2. `ActionListener` da tag `<f:actionListener>`
3. `actionListener` attribute
4. `action` method

### Per un Componente di Input

```xhtml
<h:inputText value="#{bean.value}"
             valueChangeListener="#{bean.onChange}">
    <f:valueChangeListener type="com.example.CustomValueChangeListener" />
    <f:ajax listener="#{bean.ajaxListener}" />
</h:inputText>
```

**Ordine di esecuzione:**

1. Conversione
2. Validazione
3. `AjaxBehaviorListener` (se AJAX)
4. `ValueChangeListener` da tag
5. `valueChangeListener` attribute
6. Update model values

---

## Gestione degli Eventi con CDI

Con CDI, puoi usare eventi asincroni per comunicazione tra bean.

### Definizione di un Evento Custom

```java
public class UserRegisteredEvent {
    private User user;
    private Date registrationDate;
    
    public UserRegisteredEvent(User user) {
        this.user = user;
        this.registrationDate = new Date();
    }
    
    // Getters
}
```

### Firing dell'Evento

```java
import javax.enterprise.event.Event;
import javax.inject.Inject;

@Named
@RequestScoped
public class RegistrationBean {
    
    @Inject
    private Event<UserRegisteredEvent> userRegisteredEvent;
    
    @Inject
    private UserService userService;
    
    private User user = new User();
    
    public String register() {
        userService.save(user);
        
        // Fire del evento CDI
        userRegisteredEvent.fire(new UserRegisteredEvent(user));
        
        return "welcome?faces-redirect=true";
    }
    
    // Getters e Setters...
}
```

### Osservazione dell'Evento

```java
import javax.enterprise.event.Observes;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationService {
    
    public void onUserRegistered(@Observes UserRegisteredEvent event) {
        User user = event.getUser();
        
        System.out.println("Nuovo utente registrato: " + user.getEmail());
        
        // Invia email di benvenuto
        sendWelcomeEmail(user);
        
        // Log dell'evento
        logRegistration(user);
    }
    
    private void sendWelcomeEmail(User user) {
        // Logica invio email
    }
    
    private void logRegistration(User user) {
        // Logica logging
    }
}
```

---

## Best Practices per la Gestione degli Eventi

### 1. Usa PreRenderView per Inizializzazione

```xhtml
<f:metadata>
    <f:event type="preRenderView" listener="#{bean.init}" />
</f:metadata>
```

Invece di `@PostConstruct` quando hai bisogno di parametri dalla query string.

### 2. Separa Logica di Business e Logica di Presentazione

```java
// ✅ Buono - Logica separata
public String save() {
    try {
        userService.save(user); // Business logic nel service
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage("Utente salvato"));
        return "success";
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        return null;
    }
}

// ❌ Cattivo - Tutto nel bean
public String save() {
    user.validate();
    database.connect();
    database.save(user);
    database.close();
    // ... troppa logica nel backing bean
}
```

### 3. Usa CDI Events per Disaccoppiamento

Preferisci eventi CDI per comunicazione tra moduli diversi dell'applicazione.

### 4. Gestisci gli Errori Appropriatamente

```java
public void listener(AjaxBehaviorEvent event) {
    try {
        // Logica
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Errore: " + e.getMessage(), null));
    }
}
```

### 5. Non Abusare di ValueChangeListener

Per aggiornamenti semplici, usa AJAX invece di ValueChangeListener con submit.

```xhtml
<!-- ✅ Preferibile con AJAX -->
<h:inputText value="#{bean.value}">
    <f:ajax render="output" />
</h:inputText>

<!-- ❌ Meno efficiente -->
<h:inputText value="#{bean.value}" 
             valueChangeListener="#{bean.onChange}"
             onchange="submit()" />
```

---

## Glossario

| Termine | Descrizione |
|---------|-------------|
| **Event** | Un'occorrenza che può scatenare l'esecuzione di codice |
| **Listener** | Un metodo o classe che reagisce a un evento specifico |
| **ActionEvent** | Evento generato da componenti di comando |
| **ValueChangeEvent** | Evento generato quando cambia il valore di un input |
| **AjaxBehaviorEvent** | Evento generato durante una richiesta AJAX |
| **SystemEvent** | Evento legato al ciclo di vita della vista/applicazione |
| **PhaseEvent** | Evento generato all'inizio/fine di ogni fase del ciclo di vita |
| **Action Method** | Metodo che esegue logica e restituisce un outcome |
| **Action Listener** | Metodo che reagisce a un ActionEvent senza restituire outcome |
| **PreRenderView** | Evento che si verifica prima del rendering della vista |
| **ComponentSystemEvent** | Classe base per eventi legati ai componenti |
| **`<f:event>`** | Tag per collegare listener a eventi di sistema |
| **`<f:viewParam>`** | Tag per catturare parametri dalla query string |
| **`<f:viewAction>`** | Tag per eseguire azioni durante il ciclo di vita della vista |
| **`<f:metadata>`** | Contenitore per metadati della vista (viewParam, event, ecc.) |

---

Con una solida comprensione della gestione degli eventi in JSF, puoi costruire applicazioni reattive e ben strutturate che rispondono elegantemente alle interazioni dell'utente!
