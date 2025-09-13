# Modello dei Componenti UI di JSF (UI Component Model)

Il cuore di JavaServer Faces (JSF) è il suo modello di componenti dell'interfaccia utente (UI). Ogni elemento in una pagina JSF è un componente che vive sul server e ha uno stato. Questo approccio basato su componenti semplifica lo sviluppo di interfacce web complesse.

## Caratteristiche Principali dei Componenti UI

Ogni componente dell'interfaccia utente (ad esempio, `<h:inputText>`, `<h:commandButton>`) possiede diverse caratteristiche chiave:

- **Value Binding**: Ogni componente di input può essere collegato a una proprietà di un managed bean (come un bean CDI) tramite l'attributo `value`. Questo crea un collegamento bidirezionale: il valore della proprietà viene visualizzato nel componente e il valore inserito dall'utente aggiorna la proprietà del bean.
- **Converter**: I componenti possono avere un `converter` associato per convertire i dati tra il formato del modello (oggetti Java) e il formato della vista (stringhe). Ad esempio, per convertire una stringa in un oggetto `Date`.
- **Validator**: È possibile associare uno o più `validator` a un componente per garantire che i dati inseriti dall'utente siano validi prima che il modello venga aggiornato.
- **Attributi**: I componenti hanno attributi specifici che ne controllano il comportamento, come `required="true"` per rendere un campo obbligatorio.
- **Stato Locale**: I componenti mantengono il proprio stato (ad esempio, il valore corrente) durante il ciclo di vita della richiesta JSF.

## Eventi e Listener

JSF definisce un modello di eventi per gestire le interazioni dell'utente. I tipi di eventi più comuni sono:

- **`actionEvent`**: Generato da componenti di comando (come `<h:commandButton>`) quando l'utente li attiva. Viene tipicamente usato per eseguire logica di business.
- **`valueChangeEvent`**: Generato da componenti di input quando il loro valore viene modificato dall'utente.

È possibile collegare metodi *listener* a questi eventi per eseguire codice specifico in risposta all'interazione dell'utente.

## Attributo `immediate`

L'attributo `immediate="true"` modifica il ciclo di vita standard di JSF. Quando impostato su `true` per un componente (come un `UICommand` o un `UIInput`), l'evento associato viene elaborato durante la fase *Apply Request Values*, saltando le fasi di validazione e aggiornamento del modello per gli altri componenti nella stessa form.

**Nota**: L'uso di `immediate="true"` è utile per azioni di navigazione o cancellazione che non richiedono la validazione dei dati del form.

---

## Esempi di Codice

### Esempio 1: Value Binding e ActionEvent

Questo esempio mostra un semplice form di login con `value-binding` e un `actionEvent` gestito dal metodo `login` del bean.

**`login.xhtml`**

```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html">
<h:head>
    <title>Login</title>
</h:head>
<h:body>
    <h:form>
        <h:panelGrid columns="2">
            <h:outputLabel for="username" value="Username:" />
            <h:inputText id="username" value="#{loginBean.username}" required="true" />

            <h:outputLabel for="password" value="Password:" />
            <h:inputSecret id="password" value="#{loginBean.password}" required="true" />
        </h:panelGrid>
        
        <h:commandButton value="Login" action="#{loginBean.login}" />
    </h:form>
</h:body>
</html>
```

**`LoginBean.java`**

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class LoginBean {
    private String username;
    private String password;

    // Getters and Setters

    public String login() {
        System.out.println("Username: " + username + ", Password: " + password);
        // Logica di business per il login
        if ("admin".equals(username) && "password".equals(password)) {
            return "welcome?faces-redirect=true"; // Navigazione a welcome.xhtml
        } else {
            return "login";
        }
    }
}
```

### Esempio 2: ValueChangeEvent

Questo esempio mostra come usare un `valueChangeListener` per reagire al cambiamento di valore di un menu a tendina.

**`page.xhtml`**

```xml
<h:form>
    <h:selectOneMenu value="#{countryBean.selectedCountry}" 
                     valueChangeListener="#{countryBean.onCountryChange}"
                     onchange="submit()">
        <f:selectItems value="#{countryBean.countries}" />
    </h:selectOneMenu>
    <h:outputText value="Paese selezionato: #{countryBean.selectedCountry}" />
</h:form>
```

**`CountryBean.java`**

```java
// ...
public void onCountryChange(ValueChangeEvent event) {
    String newCountry = event.getNewValue().toString();
    System.out.println("Il paese è cambiato in: " + newCountry);
    // Logica aggiuntiva, es. caricare le città del nuovo paese
}
// ...
```

### Esempio 3: `immediate="true"`

Un pulsante "Annulla" che riporta l'utente alla pagina principale senza validare gli input del form.

**`editUser.xhtml`**

```xml
<h:form>
    <h:outputLabel for="name" value="Nome:" />
    <h:inputText id="name" value="#{userBean.user.name}" required="true" />
    <h:message for="name" style="color:red" />

    <h:commandButton value="Salva" action="#{userBean.save}" />
    <h:commandButton value="Annulla" action="home?faces-redirect=true" immediate="true" />
</h:form>
```
Se l'utente clicca "Annulla", la validazione (`required="true"`) sul campo `name` viene saltata e avviene la navigazione immediata.

---

## Tabella dei Termini e Attributi Chiave

| Termine / Attributo | Descrizione |
|---------------------|-------------|
| **Componente UI**   | Un oggetto lato server che rappresenta un elemento dell'interfaccia utente (es. `<h:inputText>`). |
| **Value Binding**   | Il meccanismo (`#{bean.property}`) che collega un componente a una proprietà di un bean. |
| `value`             | L'attributo usato per specificare il value binding di un componente. |
| `converter`         | Un oggetto che converte i dati tra il formato della vista (stringa) e quello del modello (oggetto). |
| `validator`         | Un oggetto che esegue controlli di validità sui dati inseriti dall'utente. |
| `required`          | Un attributo booleano che, se `true`, richiede che l'utente fornisca un valore per il componente. |
| **`actionEvent`**   | Evento generato da componenti di comando (es. `h:commandButton`) per avviare un'azione. |
| `action`            | Attributo che definisce il metodo del bean (action method) da invocare o la regola di navigazione. |
| **`valueChangeEvent`**| Evento generato quando il valore di un componente di input viene modificato. |
| `valueChangeListener`| Attributo che collega un metodo listener a un `valueChangeEvent`. |
| `immediate`         | Attributo booleano che, se `true`, anticipa l'elaborazione dell'evento alla fase *Apply Request Values*. |
