# Quiz Avanzato su JavaServer Faces (JSF) - Domande Miste con Codice

Questo quiz avanzato copre i concetti di JavaServer Faces (JSF) in Java EE 7 con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Introduzione e Architettura JSF

### 💻 Domanda 1

Osserva questo backing bean e la sua configurazione:

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("userBean")
@RequestScoped
public class UserBean {
    private String username;
    private String email;
    private int loginAttempts = 0;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getLoginAttempts() { return loginAttempts; }
    
    public String login() {
        loginAttempts++;
        System.out.println("Tentativi: " + loginAttempts);
        if ("admin".equals(username)) {
            return "success";
        }
        return "failure";
    }
}
```

Se lo stesso utente prova a fare login 3 volte consecutive, quale valore verrà stampato nell'ultimo tentativo?

- a) 1 (ogni richiesta ha un nuovo bean)
- b) 3 (il counter viene incrementato ad ogni tentativo)
- c) 0 (il counter viene resettato)
- d) Dipende dalla configurazione del server

---

### 🔵 Domanda 2

Quale delle seguenti **NON** è una caratteristica principale di JSF?

- a) Modello a componenti stateful
- b) Ciclo di vita definito per ogni richiesta
- c) Architettura REST-based
- d) Supporto AJAX integrato

---

### 🟢 Domanda 3

Quali delle seguenti sono **librerie di tag JSF** standard? (Seleziona tutte)

- a) `h:` (HTML)
- b) `f:` (Core)
- c) `ui:` (Templating)
- d) `c:` (JSTL Core)
- e) `jsf:` (Components)

---

## 2. Integrazione JSF con CDI

### 💻 Domanda 4

Analizza questo codice di integrazione JSF-CDI:

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("productBean")
@RequestScoped
public class ProductBean {
    private String productName;
    private double price;
    
    @Inject
    private ProductService productService;
    
    @Inject
    private UserSession userSession;
    
    public String saveProduct() {
        Product p = new Product(productName, price);
        productService.save(p);
        userSession.addActivity("Saved product: " + productName);
        return "success?faces-redirect=true";
    }
    
    // Getters e Setters...
}

@ApplicationScoped
public class ProductService {
    public void save(Product p) {
        System.out.println("Saving: " + p.getName());
    }
}

@SessionScoped
public class UserSession implements Serializable {
    private List<String> activities = new ArrayList<>();
    
    public void addActivity(String activity) {
        activities.add(activity);
    }
}
```

Quale annotazione rende il bean `ProductBean` accessibile tramite Expression Language nella vista?

- a) `@RequestScoped`
- b) `@Inject`
- c) `@Named("productBean")`
- d) `@ManagedBean`

---

### 🔵 Domanda 5

Nel codice precedente, per quanto tempo vive l'istanza di `ProductService`?

- a) Per una singola richiesta HTTP
- b) Per tutta la sessione dell'utente
- c) Per tutta la vita dell'applicazione
- d) Viene ricreato ad ogni iniezione

---

### 💻 Domanda 6

Osserva questa configurazione di scope:

```java
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("orderDetailsBean")
@ViewScoped
public class OrderDetailsBean implements Serializable {
    private int orderId;
    private Order order;
    private List<OrderItem> items;
    
    public void loadOrder() {
        // Carica l'ordine dal database
        this.order = orderService.findById(orderId);
        this.items = orderService.findItems(orderId);
    }
    
    public void updateQuantity(OrderItem item, int newQty) {
        item.setQuantity(newQty);
        // Il bean mantiene lo stato
    }
    
    public String save() {
        orderService.update(order, items);
        return "orders?faces-redirect=true";
    }
}
```

Perché `OrderDetailsBean` implementa `Serializable`?

- a) Per migliorare le performance
- b) Perché `@ViewScoped` richiede bean serializzabili per la passivazione
- c) Per permettere l'iniezione CDI
- d) È obbligatorio per tutti i bean JSF

---

### 🟢 Domanda 7

Quali sono i **vantaggi** dell'uso di CDI in JSF rispetto a `@ManagedBean`? (Seleziona tutti)

- a) Dependency Injection più potente con `@Inject`
- b) Supporto per eventi CDI con `@Observes`
- c) Interceptor e decoratori
- d) Migliori performance
- e) Integrazione con altri componenti Java EE

---

## 3. Expression Language (EL) in JSF

### 💻 Domanda 8

Analizza questo uso di Expression Language:

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>User Profile</title>
</h:head>
<h:body>
    <h:form>
        <h1>Benvenuto, #{userProfile.fullName}!</h1>
        
        <h:panelGrid columns="2">
            Nome: <h:inputText value="#{userProfile.firstName}" />
            Cognome: <h:inputText value="#{userProfile.lastName}" />
            Età: <h:inputText value="#{userProfile.age}" />
        </h:panelGrid>
        
        <h:outputText value="Maggiorenne" 
                      rendered="#{userProfile.age >= 18}" 
                      style="color: green;" />
        <h:outputText value="Minorenne" 
                      rendered="#{userProfile.age lt 18}" 
                      style="color: red;" />
        
        <h:commandButton value="Salva" 
                         action="#{userProfile.save}"
                         disabled="#{empty userProfile.firstName}" />
    </h:form>
</h:body>
</html>
```

Cosa fa l'attributo `rendered="#{userProfile.age >= 18}"`?

- a) Disabilita il componente se l'età è minore di 18
- b) Renderizza il componente solo se l'età è >= 18
- c) Valida che l'età sia maggiore di 18
- d) Imposta lo stile del componente

---

### 🔵 Domanda 9

Nel codice precedente, quale operatore EL è equivalente a `>=`?

- a) `gt`
- b) `ge`
- c) `gte`
- d) `greater`

---

### 💻 Domanda 10

Osserva questo uso avanzato di EL:

```xhtml
<h:form>
    <h:dataTable value="#{productBean.products}" var="product">
        <h:column>
            <f:facet name="header">Nome</f:facet>
            #{product.name}
        </h:column>
        <h:column>
            <f:facet name="header">Prezzo</f:facet>
            <h:outputText value="#{product.price}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </h:column>
        <h:column>
            <f:facet name="header">Disponibilità</f:facet>
            <h:outputText value="Disponibile" 
                          rendered="#{product.stock > 0}"
                          style="color: green;" />
            <h:outputText value="Esaurito" 
                          rendered="#{product.stock <= 0}"
                          style="color: red;" />
        </h:column>
        <h:column>
            <h:commandButton value="Aggiungi al carrello"
                             action="#{cartBean.addProduct(product)}"
                             disabled="#{product.stock == 0}" />
        </h:column>
    </h:dataTable>
    
    <h:outputText value="Carrello vuoto" rendered="#{empty cartBean.items}" />
    <h:outputText value="#{cartBean.itemCount} articoli nel carrello" 
                  rendered="#{not empty cartBean.items}" />
</h:form>
```

Cosa restituisce `#{empty cartBean.items}` se il carrello contiene 5 elementi?

- a) `true`
- b) `false`
- c) `5`
- d) Un errore

---

## 4. Componenti UI e Value Binding

### 💻 Domanda 11

Analizza questa pagina di registrazione:

```xhtml
<h:form id="registrationForm">
    <h:panelGrid columns="3">
        <h:outputLabel for="username" value="Username:" />
        <h:inputText id="username" 
                     value="#{registerBean.username}" 
                     required="true"
                     requiredMessage="Username obbligatorio"
                     validator="#{registerBean.validateUsername}" />
        <h:message for="username" style="color: red;" />
        
        <h:outputLabel for="email" value="Email:" />
        <h:inputText id="email" 
                     value="#{registerBean.email}" 
                     required="true"
                     validatorMessage="Email non valida">
            <f:validateRegex pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}" />
        </h:inputText>
        <h:message for="email" style="color: red;" />
        
        <h:outputLabel for="birthdate" value="Data di nascita:" />
        <h:inputText id="birthdate" 
                     value="#{registerBean.birthDate}" 
                     required="true">
            <f:convertDateTime pattern="dd/MM/yyyy" />
        </h:inputText>
        <h:message for="birthdate" style="color: red;" />
        
        <h:outputLabel for="password" value="Password:" />
        <h:inputSecret id="password" 
                       value="#{registerBean.password}" 
                       required="true"
                       validatorMessage="La password deve essere di almeno 6 caratteri">
            <f:validateLength minimum="6" />
        </h:inputSecret>
        <h:message for="password" style="color: red;" />
    </h:panelGrid>
    
    <h:commandButton value="Registra" action="#{registerBean.register}" />
    <h:messages globalOnly="true" style="color: red;" />
</h:form>
```

Qual è la differenza tra `<h:message>` e `<h:messages>`?

- a) Non c'è differenza, sono sinonimi
- b) `<h:message>` mostra messaggi per un componente specifico, `<h:messages>` per tutti
- c) `<h:message>` è per errori, `<h:messages>` per warning
- d) `<h:message>` è deprecato in JSF 2.2

---

### 🔵 Domanda 12

Nel codice precedente, quando viene eseguito il validator `#{registerBean.validateUsername}`?

- a) Durante la fase "Apply Request Values"
- b) Durante la fase "Process Validations"
- c) Durante la fase "Update Model Values"
- d) Durante la fase "Invoke Application"

---

### 💻 Domanda 13

Osserva questo uso di converter personalizzato:

```java
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("categoryConverter")
public class CategoryConverter implements Converter {
    
    @Inject
    private CategoryService categoryService;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return categoryService.findById(Integer.parseInt(value));
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Category) {
            return String.valueOf(((Category) value).getId());
        }
        return "";
    }
}
```

```xhtml
<h:form>
    <h:selectOneMenu value="#{productBean.selectedCategory}" 
                     converter="categoryConverter">
        <f:selectItems value="#{productBean.categories}" 
                       var="cat"
                       itemValue="#{cat}"
                       itemLabel="#{cat.name}" />
    </h:selectOneMenu>
    <h:commandButton value="Filtra" action="#{productBean.filter}" />
</h:form>
```

A cosa serve il metodo `getAsObject` del converter?

- a) Converte l'oggetto Category in stringa per la visualizzazione
- b) Converte la stringa (ID) ricevuta dalla vista in oggetto Category
- c) Valida che l'oggetto sia di tipo Category
- d) Serializza l'oggetto per la sessione

---

## 5. Navigazione in JSF

### 💻 Domanda 14

Analizza questo sistema di navigazione:

```java
@Named("loginBean")
@RequestScoped
public class LoginBean {
    private String username;
    private String password;
    
    @Inject
    private AuthenticationService authService;
    
    public String login() {
        User user = authService.authenticate(username, password);
        if (user != null) {
            if (user.isAdmin()) {
                return "admin-dashboard?faces-redirect=true";
            } else {
                return "user-home?faces-redirect=true";
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Login fallito", "Credenziali non valide"));
            return null; // Rimane sulla stessa pagina
        }
    }
    
    public String cancel() {
        return "index?faces-redirect=true";
    }
}
```

```xhtml
<h:form>
    Username: <h:inputText value="#{loginBean.username}" />
    Password: <h:inputSecret value="#{loginBean.password}" />
    
    <h:commandButton value="Login" action="#{loginBean.login}" />
    <h:commandButton value="Annulla" action="#{loginBean.cancel}" immediate="true" />
    
    <h:messages />
</h:form>
```

Cosa fa il parametro `?faces-redirect=true` nell'outcome?

- a) Migliora le performance del redirect
- b) Esegue un HTTP redirect invece di un forward, aggiornando l'URL nel browser
- c) Abilita il caching della pagina di destinazione
- d) È obbligatorio per la navigazione in JSF

---

### 🔵 Domanda 15

Nel codice precedente, perché il pulsante "Annulla" ha `immediate="true"`?

- a) Per renderlo più veloce
- b) Per saltare la validazione e navigare immediatamente
- c) Per renderizzarlo prima degli altri componenti
- d) È deprecato in JSF 2.2

---

### 💻 Domanda 16

Osserva questa configurazione di navigazione esplicita:

```xml
<!-- faces-config.xml -->
<faces-config version="2.2"
              xmlns="http://xmlns.jcp.org/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
                                  http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd">
    
    <navigation-rule>
        <from-view-id>/login.xhtml</from-view-id>
        <navigation-case>
            <from-outcome>success</from-outcome>
            <to-view-id>/secure/home.xhtml</to-view-id>
            <redirect />
        </navigation-case>
        <navigation-case>
            <from-outcome>failure</from-outcome>
            <to-view-id>/login-error.xhtml</to-view-id>
        </navigation-case>
    </navigation-rule>
    
    <navigation-rule>
        <navigation-case>
            <from-outcome>logout</from-outcome>
            <to-view-id>/goodbye.xhtml</to-view-id>
            <redirect />
        </navigation-case>
    </navigation-rule>
</faces-config>
```

Se un metodo di `login.xhtml` restituisce `"success"`, quale pagina viene mostrata?

- a) `success.xhtml`
- b) `/secure/home.xhtml`
- c) `login-error.xhtml`
- d) Rimane su `login.xhtml`

---

### 🟢 Domanda 17

Quali delle seguenti sono **tecniche di navigazione valide** in JSF? (Seleziona tutte)

- a) Navigazione implicita basata su outcome
- b) Navigazione esplicita tramite `faces-config.xml`
- c) Navigazione programmatica con `NavigationHandler`
- d) Navigazione tramite `@Navigation` annotation
- e) Uso di `<redirect />` per HTTP redirect

---

## 6. Ciclo di Vita JSF (JSF Lifecycle)

### 💻 Domanda 18

Analizza questo scenario di ciclo di vita:

```java
@Named("productBean")
@RequestScoped
public class ProductBean {
    private String productName;
    private double price;
    
    public void validatePrice(FacesContext context, UIComponent component, Object value) {
        double priceValue = (Double) value;
        if (priceValue < 0) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Il prezzo non può essere negativo", null));
        }
        if (priceValue > 10000) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Il prezzo è troppo alto", null));
        }
    }
    
    public String save() {
        System.out.println("Salvataggio prodotto: " + productName + " - €" + price);
        // Logica di salvataggio
        return "success";
    }
}
```

```xhtml
<h:form>
    Nome: <h:inputText value="#{productBean.productName}" required="true" />
    Prezzo: <h:inputText value="#{productBean.price}" 
                         required="true"
                         validator="#{productBean.validatePrice}">
        <f:convertNumber type="number" minFractionDigits="2" />
    </h:inputText>
    
    <h:commandButton value="Salva" action="#{productBean.save}" />
    <h:messages />
</h:form>
```

In quale ordine vengono eseguite queste operazioni quando l'utente clicca "Salva"?

- a) Validazione → Conversione → Aggiornamento modello → Invocazione metodo
- b) Conversione → Validazione → Aggiornamento modello → Invocazione metodo
- c) Aggiornamento modello → Validazione → Conversione → Invocazione metodo
- d) Invocazione metodo → Validazione → Conversione → Aggiornamento modello

---

### 🟢 Domanda 19

Quali sono le **fasi del ciclo di vita JSF**? (Seleziona tutte)

- a) Restore View
- b) Apply Request Values
- c) Process Validations
- d) Update Model Values
- e) Invoke Application
- f) Render Response
- g) Execute Business Logic

---

### 💻 Domanda 20

Osserva questo uso di `immediate`:

```xhtml
<h:form>
    <h:panelGrid columns="2">
        Nome: <h:inputText id="name" value="#{userBean.name}" required="true" />
        Email: <h:inputText id="email" value="#{userBean.email}" required="true" />
        Telefono: <h:inputText id="phone" value="#{userBean.phone}" required="true" />
    </h:panelGrid>
    
    <h:commandButton value="Salva" action="#{userBean.save}" />
    <h:commandButton value="Annulla" action="#{userBean.cancel}" immediate="true" />
    <h:commandButton value="Reset" action="#{userBean.reset}" />
    
    <h:messages />
</h:form>
```

```java
@Named("userBean")
@RequestScoped
public class UserBean {
    private String name;
    private String email;
    private String phone;
    
    public String save() {
        System.out.println("Salvataggio...");
        return "success";
    }
    
    public String cancel() {
        System.out.println("Annullamento...");
        return "home?faces-redirect=true";
    }
    
    public String reset() {
        name = null;
        email = null;
        phone = null;
        return null; // Rimane sulla stessa pagina
    }
}
```

Se l'utente lascia tutti i campi vuoti e clicca "Annulla", cosa succede?

- a) Viene mostrato un errore di validazione
- b) Naviga verso "home" senza errori di validazione
- c) Rimane sulla stessa pagina con errori
- d) Genera un'eccezione

---

## 7. AJAX in JSF

### 💻 Domanda 21

Analizza questo uso di AJAX:

```xhtml
<h:form>
    <h:panelGrid columns="2">
        <h:outputLabel for="username" value="Username:" />
        <h:inputText id="username" value="#{registerBean.username}">
            <f:ajax event="blur" 
                    listener="#{registerBean.checkUsername}"
                    render="usernameMessage" />
        </h:inputText>
        <h:outputText id="usernameMessage" 
                      value="#{registerBean.usernameStatus}"
                      style="#{registerBean.usernameAvailable ? 'color: green' : 'color: red'}" />
        
        <h:outputLabel for="country" value="Paese:" />
        <h:selectOneMenu id="country" value="#{registerBean.selectedCountry}">
            <f:selectItems value="#{registerBean.countries}" />
            <f:ajax event="change" 
                    listener="#{registerBean.onCountryChange}"
                    render="cityPanel" />
        </h:selectOneMenu>
        
        <h:panelGroup id="cityPanel">
            <h:outputLabel for="city" value="Città:" />
            <h:selectOneMenu id="city" value="#{registerBean.selectedCity}">
                <f:selectItems value="#{registerBean.cities}" />
            </h:selectOneMenu>
        </h:panelGroup>
    </h:panelGrid>
    
    <h:commandButton value="Registra" action="#{registerBean.register}">
        <f:ajax execute="@form" render="@form" />
    </h:commandButton>
</h:form>
```

```java
@Named("registerBean")
@ViewScoped
public class RegisterBean implements Serializable {
    private String username;
    private String usernameStatus;
    private boolean usernameAvailable;
    
    private String selectedCountry;
    private String selectedCity;
    
    private List<String> countries;
    private List<String> cities;
    
    @Inject
    private UserService userService;
    
    @PostConstruct
    public void init() {
        countries = Arrays.asList("Italia", "Francia", "Germania");
    }
    
    public void checkUsername() {
        if (userService.isUsernameAvailable(username)) {
            usernameStatus = "Username disponibile";
            usernameAvailable = true;
        } else {
            usernameStatus = "Username già in uso";
            usernameAvailable = false;
        }
    }
    
    public void onCountryChange() {
        if ("Italia".equals(selectedCountry)) {
            cities = Arrays.asList("Roma", "Milano", "Napoli");
        } else if ("Francia".equals(selectedCountry)) {
            cities = Arrays.asList("Parigi", "Lione", "Marsiglia");
        } else {
            cities = Arrays.asList("Berlino", "Monaco", "Amburgo");
        }
        selectedCity = null; // Reset della città
    }
    
    public String register() {
        // Logica di registrazione
        return "success?faces-redirect=true";
    }
    
    // Getters e Setters...
}
```

Cosa fa l'attributo `render="usernameMessage"` nel tag `<f:ajax>`?

- a) Disabilita il componente `usernameMessage`
- b) Aggiorna solo il componente `usernameMessage` senza refresh completo
- c) Valida il componente `usernameMessage`
- d) Nasconde il componente `usernameMessage`

---

### 🔵 Domanda 22

Nel codice precedente, quando viene eseguito il listener `#{registerBean.checkUsername}`?

- a) Quando l'utente digita nel campo username
- b) Quando l'utente lascia il campo username (evento blur)
- c) Quando viene inviato il form
- d) Immediatamente al caricamento della pagina

---

### 💻 Domanda 23

Osserva questo uso avanzato di AJAX:

```xhtml
<h:form id="searchForm">
    <h:inputText id="searchTerm" value="#{searchBean.searchTerm}">
        <f:ajax event="keyup" 
                listener="#{searchBean.search}"
                render="resultsPanel"
                delay="500" />
    </h:inputText>
    
    <h:panelGroup id="resultsPanel">
        <h:dataTable value="#{searchBean.results}" var="result" 
                     rendered="#{not empty searchBean.results}">
            <h:column>
                <h:commandLink value="#{result.name}" 
                               action="#{searchBean.selectResult(result)}">
                    <f:ajax render="@none" />
                </h:commandLink>
            </h:column>
        </h:dataTable>
        <h:outputText value="Nessun risultato trovato" 
                      rendered="#{empty searchBean.results and not empty searchBean.searchTerm}" />
    </h:panelGroup>
</h:form>
```

Cosa fa l'attributo `delay="500"` nel tag `<f:ajax>`?

- a) Ritarda l'aggiornamento della vista di 500ms
- b) Attende 500ms prima di inviare la richiesta AJAX, utile per evitare troppe richieste
- c) Imposta il timeout della richiesta a 500ms
- d) È deprecato in JSF 2.2

---

### 🟢 Domanda 24

Quali sono i **valori validi** per l'attributo `execute` di `<f:ajax>`? (Seleziona tutti)

- a) `@this` (solo il componente corrente)
- b) `@form` (tutti i componenti del form)
- c) `@all` (tutti i componenti della vista)
- d) `@none` (nessun componente)
- e) ID specifici di componenti

---

## 8. Templating con Facelets

### 💻 Domanda 25

Analizza questo sistema di template:

```xhtml
<!-- template.xhtml -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
<h:head>
    <title><ui:insert name="title">Default Title</ui:insert></title>
    <h:outputStylesheet library="css" name="style.css" />
    <ui:insert name="head" />
</h:head>
<h:body>
    <div id="header">
        <ui:insert name="header">
            <ui:include src="/WEB-INF/includes/default-header.xhtml" />
        </ui:insert>
    </div>
    
    <div id="menu">
        <ui:insert name="menu">
            <ui:include src="/WEB-INF/includes/menu.xhtml" />
        </ui:insert>
    </div>
    
    <div id="content">
        <ui:insert name="content">
            Default Content
        </ui:insert>
    </div>
    
    <div id="footer">
        <ui:insert name="footer">
            <p>&copy; 2025 My Company</p>
        </ui:insert>
    </div>
</h:body>
</html>
```

```xhtml
<!-- products.xhtml -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
      
<ui:composition template="/WEB-INF/templates/template.xhtml">
    
    <ui:define name="title">
        Lista Prodotti
    </ui:define>
    
    <ui:define name="head">
        <h:outputStylesheet library="css" name="products.css" />
        <h:outputScript library="js" name="products.js" />
    </ui:define>
    
    <ui:define name="content">
        <h1>I Nostri Prodotti</h1>
        <h:form>
            <h:dataTable value="#{productBean.products}" var="product">
                <h:column>
                    <f:facet name="header">Nome</f:facet>
                    #{product.name}
                </h:column>
                <h:column>
                    <f:facet name="header">Prezzo</f:facet>
                    <h:outputText value="#{product.price}">
                        <f:convertNumber type="currency" currencySymbol="€" />
                    </h:outputText>
                </h:column>
            </h:dataTable>
        </h:form>
    </ui:define>
    
</ui:composition>
</html>
```

Cosa succede al contenuto definito in `<ui:define name="header">` se non viene specificato in `products.xhtml`?

- a) Viene mostrato un errore
- b) Viene utilizzato il contenuto di default dal template
- c) La sezione header viene nascosta
- d) Viene mostrata una pagina bianca

---

### 🔵 Domanda 26

Qual è la differenza tra `<ui:include>` e `<ui:composition>`?

- a) Non c'è differenza, sono sinonimi
- b) `<ui:include>` include un frammento, `<ui:composition>` definisce una pagina che usa un template
- c) `<ui:include>` è per HTML, `<ui:composition>` per XHTML
- d) `<ui:include>` è deprecato in JSF 2.2

---

### 💻 Domanda 27

Osserva questo uso di parametri nei template:

```xhtml
<!-- custom-panel.xhtml (template parametrizzato) -->
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:h="http://xmlns.jcp.org/jsf/html">
    
    <div class="panel">
        <div class="panel-header" style="background-color: #{panelColor}">
            <h3>#{panelTitle}</h3>
        </div>
        <div class="panel-body">
            <ui:insert name="content" />
        </div>
    </div>
    
</ui:composition>
```

```xhtml
<!-- usage-page.xhtml -->
<ui:composition template="/WEB-INF/templates/main.xhtml"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:h="http://xmlns.jcp.org/jsf/html">
    
    <ui:define name="content">
        
        <ui:include src="/WEB-INF/includes/custom-panel.xhtml">
            <ui:param name="panelTitle" value="Informazioni Utente" />
            <ui:param name="panelColor" value="#3498db" />
        </ui:include>
        
        <ui:include src="/WEB-INF/includes/custom-panel.xhtml">
            <ui:param name="panelTitle" value="Statistiche" />
            <ui:param name="panelColor" value="#e74c3c" />
        </ui:include>
        
    </ui:define>
    
</ui:composition>
```

A cosa serve `<ui:param>` in questo contesto?

- a) Valida i parametri del template
- b) Passa parametri al frammento incluso
- c) Definisce variabili globali
- d) È deprecato in JSF 2.2

---

## 9. Gestione degli Eventi

### 💻 Domanda 28

Analizza questo sistema di eventi:

```java
@Named("userBean")
@ViewScoped
public class UserBean implements Serializable {
    private String username;
    private String email;
    
    public void usernameChanged(ValueChangeEvent event) {
        String oldValue = (String) event.getOldValue();
        String newValue = (String) event.getNewValue();
        
        System.out.println("Username cambiato da '" + oldValue + "' a '" + newValue + "'");
        
        // Logica aggiuntiva
        if (newValue != null && newValue.length() < 3) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Username troppo corto", "Username deve essere almeno 3 caratteri"));
        }
    }
    
    public void preRenderView() {
        System.out.println("Pre-rendering della vista");
        // Caricamento dati, inizializzazioni
    }
    
    public String save() {
        System.out.println("Salvataggio utente: " + username);
        return "success";
    }
    
    // Getters e Setters...
}
```

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>User Form</title>
</h:head>
<h:body>
    <f:metadata>
        <f:viewParam name="userId" value="#{userBean.userId}" />
        <f:event type="preRenderView" listener="#{userBean.preRenderView}" />
    </f:metadata>
    
    <h:form>
        <h:panelGrid columns="2">
            <h:outputLabel for="username" value="Username:" />
            <h:inputText id="username" 
                         value="#{userBean.username}"
                         valueChangeListener="#{userBean.usernameChanged}"
                         onchange="submit()">
                <f:ajax event="change" render="@form" />
            </h:inputText>
            
            <h:outputLabel for="email" value="Email:" />
            <h:inputText id="email" value="#{userBean.email}" />
        </h:panelGrid>
        
        <h:commandButton value="Salva" action="#{userBean.save}" />
        <h:messages />
    </h:form>
</h:body>
</html>
```

Quando viene invocato il metodo `usernameChanged`?

- a) Immediatamente quando l'utente digita
- b) Quando il valore cambia e viene eseguita la fase "Process Validations"
- c) Solo quando viene cliccato il pulsante "Salva"
- d) Mai, perché usa AJAX

---

### 🟢 Domanda 29

Quali sono **tipi di eventi JSF** validi? (Seleziona tutti)

- a) `ActionEvent`
- b) `ValueChangeEvent`
- c) `PhaseEvent`
- d) `SystemEvent` (es. `PreRenderViewEvent`)
- e) `ExceptionEvent`
- f) `AjaxBehaviorEvent`

---

### 💻 Domanda 30

Osserva questo uso di eventi di sistema:

```java
@Named("orderBean")
@ViewScoped
public class OrderBean implements Serializable {
    
    @Inject
    private OrderService orderService;
    
    private int orderId;
    private Order order;
    
    public void loadOrder(ComponentSystemEvent event) {
        if (orderId > 0) {
            order = orderService.findById(orderId);
            if (order == null) {
                FacesContext ctx = FacesContext.getCurrentInstance();
                NavigationHandler nav = ctx.getApplication().getNavigationHandler();
                nav.handleNavigation(ctx, null, "order-not-found?faces-redirect=true");
                ctx.renderResponse(); // Salta al rendering
            }
        }
    }
    
    // Getters e Setters...
}
```

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>Order Details</title>
</h:head>
<h:body>
    <f:metadata>
        <f:viewParam name="orderId" value="#{orderBean.orderId}" />
        <f:event type="preRenderView" listener="#{orderBean.loadOrder}" />
    </f:metadata>
    
    <h:panelGroup rendered="#{not empty orderBean.order}">
        <h1>Ordine ##{orderBean.order.id}</h1>
        <p>Cliente: #{orderBean.order.customerName}</p>
        <p>Totale: 
            <h:outputText value="#{orderBean.order.total}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </p>
    </h:panelGroup>
</h:body>
</html>
```

Qual è il vantaggio di usare `<f:event type="preRenderView">` per caricare i dati?

- a) Migliori performance
- b) I dati vengono caricati prima del rendering, permettendo redirect se necessario
- c) Obbligatorio per bean ViewScoped
- d) Abilita il caching automatico

---

---

## Risposte Corrette

### 1. **a)** 1 (ogni richiesta ha un nuovo bean)

Con `@RequestScoped`, il bean viene ricreato ad ogni richiesta HTTP, quindi `loginAttempts` riparte sempre da 0.

### 2. **c)** Architettura REST-based

JSF è un framework stateful basato su componenti, non REST-based.

### 3. **a, b, c)** `h:`, `f:`, `ui:`

JSTL `c:` non è una libreria specifica di JSF, e `jsf:` non è una libreria standard.

### 4. **c)** `@Named("productBean")`

`@Named` rende il bean accessibile tramite EL con il nome specificato.

### 5. **c)** Per tutta la vita dell'applicazione

`@ApplicationScoped` mantiene un'unica istanza per tutta l'applicazione.

### 6. **b)** Perché `@ViewScoped` richiede bean serializzabili per la passivazione

I bean ViewScoped possono essere serializzati per la passivazione, quindi devono implementare `Serializable`.

### 7. **a, b, c, e)** Tutti tranne "Migliori performance"

CDI offre DI potente, eventi, interceptor e integrazione, ma non necessariamente performance migliori.

### 8. **b)** Renderizza il componente solo se l'età è >= 18

`rendered` controlla se il componente viene renderizzato nell'HTML.

### 9. **b)** `ge`

`ge` è "greater than or equal" in EL, equivalente a `>=`.

### 10. **b)** `false`

`empty` restituisce `false` per collezioni non vuote.

### 11. **b)** `<h:message>` mostra messaggi per un componente specifico, `<h:messages>` per tutti

`<h:message>` è legato a un componente via `for`, `<h:messages>` mostra tutti i messaggi.

### 12. **b)** Durante la fase "Process Validations"

I validator vengono eseguiti durante la fase di validazione del ciclo di vita JSF.

### 13. **b)** Converte la stringa (ID) ricevuta dalla vista in oggetto Category

`getAsObject` converte dalla rappresentazione stringa all'oggetto.

### 14. **b)** Esegue un HTTP redirect invece di un forward, aggiornando l'URL nel browser

`faces-redirect=true` causa un redirect HTTP invece di un forward interno.

### 15. **b)** Per saltare la validazione e navigare immediatamente

`immediate="true"` bypassa la validazione, utile per azioni di cancellazione.

### 16. **b)** `/secure/home.xhtml`

La navigazione esplicita mappa l'outcome "success" a `/secure/home.xhtml`.

### 17. **a, b, c, e)** Tutti tranne `@Navigation` annotation

`@Navigation` non è una tecnica di navigazione JSF valida.

### 18. **b)** Conversione → Validazione → Aggiornamento modello → Invocazione metodo

Questo è l'ordine corretto delle fasi del ciclo di vita JSF.

### 19. **a, b, c, d, e, f)** Tutte le fasi standard

"Execute Business Logic" non è una fase ufficiale del ciclo di vita JSF.

### 20. **b)** Naviga verso "home" senza errori di validazione

`immediate="true"` salta la validazione per il pulsante Annulla.

### 21. **b)** Aggiorna solo il componente `usernameMessage` senza refresh completo

`render` specifica quali componenti aggiornare via AJAX.

### 22. **b)** Quando l'utente lascia il campo username (evento blur)

L'evento `blur` si verifica quando il campo perde il focus.

### 23. **b)** Attende 500ms prima di inviare la richiesta AJAX, utile per evitare troppe richieste

`delay` introduce un debounce per ridurre il numero di richieste AJAX.

### 24. **a, b, c, d, e)** Tutti

Tutti questi sono valori validi per l'attributo `execute`.

### 25. **b)** Viene utilizzato il contenuto di default dal template

Se non viene fornito un `<ui:define>`, viene usato il contenuto di default da `<ui:insert>`.

### 26. **b)** `<ui:include>` include un frammento, `<ui:composition>` definisce una pagina che usa un template

`<ui:include>` include contenuto riutilizzabile, `<ui:composition>` è per usare template.

### 27. **b)** Passa parametri al frammento incluso

`<ui:param>` permette di passare parametri ai frammenti inclusi.

### 28. **b)** Quando il valore cambia e viene eseguita la fase "Process Validations"

I `ValueChangeListener` vengono invocati durante la fase di validazione.

### 29. **a, b, c, d, f)** Tutti tranne `ExceptionEvent`

`ExceptionEvent` non è un tipo di evento JSF standard.

### 30. **b)** I dati vengono caricati prima del rendering, permettendo redirect se necessario

`preRenderView` permette di caricare dati e fare redirect prima del rendering della vista.

---

## Riepilogo Punteggi

- **25-30 risposte corrette**: Eccellente! Ottima conoscenza di JSF
- **20-24 risposte corrette**: Buono! Solida comprensione di JSF
- **15-19 risposte corrette**: Discreto, ma richiede approfondimento
- **Meno di 15**: Necessario studiare di più i concetti di JSF
