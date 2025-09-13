# Gestione della Navigazione in JSF (Navigation)

In un'applicazione JSF, la "navigazione" è il meccanismo che decide quale pagina visualizzare all'utente in risposta a un'azione, come il click su un pulsante o un link. JSF offre un sistema flessibile per gestire il flusso tra le pagine, basato su regole e convenzioni.

Esistono due approcci principali per gestire la navigazione:

1. **Navigazione Implicita (Implicit Navigation)**
2. **Navigazione Esplicita (Explicit Navigation) tramite `faces-config.xml`**

---

## 1. Navigazione Implicita

La navigazione implicita è il modo più semplice e comune per gestire il flusso di pagine. Si basa su una convenzione: il valore restituito da un metodo di azione (action method) di un bean viene interpretato da JSF come il nome della pagina successiva da visualizzare.

**Come funziona:**

1. Un componente di comando (es. `<h:commandButton>`) invoca un metodo di un managed bean tramite l'attributo `action`.
2. Il metodo esegue la sua logica di business.
3. Il metodo restituisce una `String`. Questa stringa è chiamata **outcome**.
4. JSF cerca una vista (un file `.xhtml`) con un nome che corrisponde all'outcome. Ad esempio, se il metodo restituisce `"success"`, JSF cercherà un file `success.xhtml`.
5. Per eseguire un redirect HTTP (che aggiorna l'URL nel browser), è sufficiente aggiungere `?faces-redirect=true` all'outcome.

### Esempio di Navigazione Implicita

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
        Username: <h:inputText value="#{loginBean.username}" />
        <br/>
        Password: <h:inputSecret value="#{loginBean.password}" />
        <br/>
        <h:commandButton value="Login" action="#{loginBean.doLogin}" />
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

    // Getters and setters...

    public String doLogin() {
        if ("admin".equals(username) && "secret".equals(password)) {
            // Outcome "welcome". JSF cercherà welcome.xhtml
            return "welcome?faces-redirect=true"; 
        } else {
            // Outcome "error". JSF cercherà error.xhtml
            return "error";
        }
    }
}
```

In questo caso, a seconda delle credenziali, il metodo `doLogin` restituisce `"welcome?faces-redirect=true"` o `"error"`, e JSF naviga rispettivamente verso `welcome.xhtml` (con un redirect) o `error.xhtml`.

---

## 2. Navigazione Esplicita

A volte la navigazione implicita non è sufficiente. Potresti voler mappare un outcome generico (come `"success"`) a una pagina specifica (come `home.xhtml`), o definire regole di navigazione complesse e centralizzate. In questi casi, si utilizza la navigazione esplicita, configurata nel file `faces-config.xml`.

**Come funziona:**

1. Nel file `WEB-INF/faces-config.xml`, si definiscono delle `<navigation-rule>`.
2. Ogni `<navigation-rule>` può specificare una pagina di partenza (`<from-view-id>`). Se omessa, la regola è globale.
3. All'interno di una regola, si definiscono uno o più `<navigation-case>`, ognuno dei quali mappa un outcome (`<from-outcome>`) a una pagina di destinazione (`<to-view-id>`).

### Esempio di Navigazione Esplicita

Supponiamo di voler mappare l'outcome `"success"` a `main_page.xhtml` e `"failure"` a `login_error.xhtml`, indipendentemente dalla pagina di partenza.

**`faces-config.xml`**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<faces-config version="2.2"
              xmlns="http://xmlns.jcp.org/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
                                  http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd">

    <!-- Regola di navigazione globale -->
    <navigation-rule>
        <!-- Caso per l'outcome "success" -->
        <navigation-case>
            <from-outcome>success</from-outcome>
            <to-view-id>/main_page.xhtml</to-view-id>
            <redirect /> <!-- Opzionale: esegue un redirect -->
        </navigation-case>

        <!-- Caso per l'outcome "failure" -->
        <navigation-case>
            <from-outcome>failure</from-outcome>
            <to-view-id>/login_error.xhtml</to-view-id>
        </navigation-case>
    </navigation-rule>

</faces-config>
```

Il bean `LoginBean` può ora restituire outcome generici.

**`LoginBean.java`**

```java
// ...
public String doLogin() {
    if ("admin".equals(username) && "secret".equals(password)) {
        return "success"; // Mappato a /main_page.xhtml da faces-config.xml
    } else {
        return "failure"; // Mappato a /login_error.xhtml da faces-config.xml
    }
}
// ...
```

---

## 3. Navigazione con Redirect vs. Forward

Per impostazione predefinita, JSF esegue una **forward** lato server. Questo significa che il controllo viene passato a un'altra pagina sullo stesso server, ma l'URL visualizzato nel browser dell'utente non cambia.

A volte, però, è desiderabile eseguire un **redirect** HTTP. Con un redirect, il server invia una risposta al browser che gli dice di fare una nuova richiesta a un URL diverso. Questo è utile per:

- Evitare problemi di "doppio submit" del form se l'utente ricarica la pagina.
- Mostrare all'utente l'URL corretto della pagina in cui si trova.

Per forzare un redirect, si usano due meccanismi:

- **Nella navigazione implicita**: si aggiunge il parametro `?faces-redirect=true` all'outcome restituito dal metodo di azione.

  ```java
  public String save() {
      // ... logica di salvataggio ...
      return "viewDetails?faces-redirect=true";
  }
  ```

- **Nella navigazione esplicita**: si aggiunge il tag vuoto `<redirect />` all'interno del `<navigation-case>` nel file `faces-config.xml`.

  ```xml
  <navigation-case>
      <from-outcome>success</from-outcome>
      <to-view-id>/main_page.xhtml</to-view-id>
      <redirect />
  </navigation-case>
  ```

## 4. Conditional Navigation

JSF permette di definire regole di navigazione condizionali direttamente nel `faces-config.xml` utilizzando l'elemento `<if>`. Questo permette di centralizzare la logica di navigazione che dipende da certe condizioni, espresse tramite Expression Language (EL).

### Esempio di Navigazione Condizionale

Immaginiamo di voler navigare verso pagine diverse a seconda del ruolo dell'utente dopo il login.

**`faces-config.xml`**

```xml
<navigation-rule>
    <from-view-id>/login.xhtml</from-view-id>
    
    <!-- Caso 1: l'utente è un amministratore -->
    <navigation-case>
        <from-outcome>loginSuccess</from-outcome>
        <if>#{loginBean.role == 'ADMIN'}</if>
        <to-view-id>/admin/dashboard.xhtml</to-view-id>
        <redirect/>
    </navigation-case>

    <!-- Caso 2: l'utente è un utente standard -->
    <navigation-case>
        <from-outcome>loginSuccess</from-outcome>
        <if>#{loginBean.role == 'USER'}</if>
        <to-view-id>/user/home.xhtml</to-view-id>
        <redirect/>
    </navigation-case>
</navigation-rule>
```

In questo esempio, se il metodo di azione restituisce `"loginSuccess"`, JSF valuta le condizioni `<if>` in sequenza e sceglie la prima che risulta `true`.

## 5. Componenti per la Navigazione

I componenti più comuni per avviare la navigazione sono:

- **`<h:commandButton>` e `<h:commandLink>`**:
  - Sono componenti di azione che inviano il form al server.
  - Attivano il ciclo di vita completo di JSF (validazione, aggiornamento del modello, ecc.).
  - Usano l'attributo `action` per specificare il metodo di azione da invocare o direttamente l'outcome per la navigazione.
  - Sono ideali per operazioni che modificano lo stato del server (es. salvare dati, login).

- **`<h:button>` e `<h:link>`**:
  - Sono componenti di navigazione "pura" introdotti in JSF 2.0.
  - **Non** inviano il form e non invocano metodi di azione.
  - Usano l'attributo `outcome` per specificare la destinazione. JSF calcola l'URL corretto.
  - Generano un semplice link `<a>` e sono ideali per la navigazione che non richiede l'elaborazione di dati (es. un link "Home" o "Annulla").

**Esempio:**

```xml
<h:form>
    <!-- Invia il form ed esegue un'azione -->
    <h:commandButton value="Salva" action="#{bean.save}" />
</h:form>

<!-- Navigazione semplice senza inviare il form -->
<h:link value="Torna alla Home" outcome="home" />
```

---

## Tabella dei Termini Chiave

| Termine | Descrizione |
|--------------------------|----------------------------------------------------------------------------------------------------------------|
| **Navigation** | Il processo di determinare la pagina successiva da mostrare all'utente. |
| **Action Method** | Un metodo in un managed bean che viene eseguito in risposta a un'azione dell'utente e restituisce un outcome. |
| **Outcome** | La stringa restituita da un action method, usata per decidere la navigazione. |
| **Implicit Navigation** | Navigazione basata sulla convenzione per cui l'outcome corrisponde al nome del file della vista. |
| **Explicit Navigation** | Navigazione definita tramite regole esplicite nel file `faces-config.xml`. |
| **Conditional Navigation** | Navigazione esplicita che dipende da una condizione EL, definita con il tag `<if>`. |
| `faces-config.xml` | Il file di configurazione di JSF dove possono essere definite le regole di navigazione. |
| `<navigation-rule>` | L'elemento XML in `faces-config.xml` che definisce un insieme di regole di navigazione. |
| `<from-view-id>` | Specifica la pagina di origine a cui si applica una regola di navigazione. Se omesso, la regola è globale. |
| `<navigation-case>` | Definisce la mappatura da un outcome (`<from-outcome>`) a una vista di destinazione (`<to-view-id>`). |
| `<if>` | Elemento usato in un `<navigation-case>` per definire una condizione EL per la navigazione. |
| `?faces-redirect=true` | Un suffisso aggiunto all'outcome per forzare un redirect HTTP. |
| `<redirect />` | Un tag XML in un `<navigation-case>` che indica a JSF di eseguire un redirect. |
| `<h:commandButton>` / `<h:commandLink>` | Componenti di azione che inviano un form e attivano il ciclo di vita JSF per la navigazione. |
| `<h:button>` / `<h:link>` | Componenti di navigazione semplice (non di azione) che generano un link senza inviare un form. |
| `?faces-redirect=true` | Un suffisso aggiunto all'outcome per forzare un redirect HTTP, cambiando l'URL nel browser. |
| `<redirect />` | Un tag XML in un `<navigation-case>` che indica a JSF di eseguire un redirect alla vista di destinazione. |
