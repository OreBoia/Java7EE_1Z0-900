# Modello di Sicurezza in Java EE 7

La piattaforma Java EE 7 fornisce un modello di sicurezza robusto e flessibile, gestito direttamente dal container applicativo (come WildFly, GlassFish, ecc.). Questo approccio solleva gli sviluppatori dal compito di implementare logiche complesse di autenticazione e autorizzazione, permettendo di concentrarsi sulla logica di business.

Il container si interpone tra l'utente e l'applicazione, intercettando le richieste per verificare l'identità dell'utente ( **autenticazione** ) e controllare se ha i permessi necessari per accedere a una risorsa ( **autorizzazione** ).

Java EE supporta due modelli di sicurezza principali, che possono anche coesistere:

1. **Sicurezza Dichiarativa**: Le regole di sicurezza vengono definite esternamente al codice, tramite annotazioni o descrittori di deployment (come `web.xml`).
2. **Sicurezza Programmatica**: La logica di sicurezza viene implementata direttamente nel codice sorgente, utilizzando API specifiche.

Internamente, il meccanismo su cui si basano i container Java EE è **JAAS (Java Authentication and Authorization Service)**, uno standard Java per l'autenticazione e l'autorizzazione pluggable.

---

## 1. Sicurezza Dichiarativa

Questo è l'approccio preferito perché separa la logica di sicurezza da quella di business, rendendo l'applicazione più manutenibile. Le regole possono essere modificate senza dover ricompilare il codice.

### Protezione di Risorse Web (`web.xml`)

Il file `web.xml` è il descrittore di deployment standard per le applicazioni web. Qui si possono definire:

- **Security Constraint**: Specifica quali URL sono protetti e da quali ruoli.
- **Login Config**: Definisce il metodo di autenticazione (es. `FORM`, `BASIC`).
- **Security Role**: Dichiara i ruoli usati nell'applicazione.

**Esempio `web.xml`:**

```xml
<web-app ...>
    <!-- 1. Definizione del vincolo di sicurezza -->
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>Area Riservata agli Admin</web-resource-name>
            <url-pattern>/admin/*</url-pattern> <!-- Protegge tutte le URL sotto /admin/ -->
        </web-resource-collection>
        <auth-constraint>
            <role-name>ADMIN</role-name> <!-- Solo gli utenti con ruolo ADMIN possono accedere -->
        </auth-constraint>
    </security-constraint>

    <!-- 2. Configurazione del metodo di login -->
    <login-config>
        <auth-method>FORM</auth-method>
        <form-login-config>
            <form-login-page>/login.jsp</form-login-page>
            <form-error-page>/login-error.jsp</form-error-page>
        </form-login-config>
    </login-config>

    <!-- 3. Dichiarazione dei ruoli di sicurezza -->
    <security-role>
        <role-name>ADMIN</role-name>
    </security-role>
    <security-role>
        <role-name>USER</role-name>
    </security-role>
</web-app>
```

### Protezione con Annotazioni

Java EE 7 favorisce l'uso di annotazioni per la sicurezza dichiarativa.

- **Per le Servlet (`@ServletSecurity`)**:

    ```java
    @WebServlet("/admin/dashboard")
    @ServletSecurity(
        @HttpConstraint(rolesAllowed = "ADMIN")
    )
    public class AdminDashboardServlet extends HttpServlet {
        // ...
    }
    ```

- **Per gli EJB (`@RolesAllowed`)**:

    ```java
    import javax.annotation.security.RolesAllowed;
    import javax.ejb.Stateless;

    @Stateless
    public class BillingService {
        
        @RolesAllowed({"ADMIN", "MANAGER"})
        public void generateInvoices() {
            // Solo ADMIN e MANAGER possono eseguire questo metodo
        }

        @RolesAllowed("USER")
        public void viewMyBills() {
            // Tutti gli utenti con ruolo USER possono vederlo
        }
    }
    ```

---

## 2. Sicurezza Programmatica

A volte la logica di sicurezza è troppo complessa per essere espressa in modo dichiarativo. In questi casi, si usano le API di sicurezza direttamente nel codice.

### Esempio con Servlet (`HttpServletRequest`)

È possibile gestire il login e controllare i ruoli programmaticamente.

```java
@WebServlet("/api/data")
public class DataServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Controlla se l'utente ha il ruolo "ANALYST"
        if (request.isUserInRole("ANALYST")) {
            // ... fornisci dati dettagliati
        } else {
            // ... fornisci dati generici
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // Esempio di login programmatico
        try {
            request.login(request.getParameter("username"), request.getParameter("password"));
            // Se il login ha successo, l'utente è autenticato per la sessione
            response.sendRedirect(request.getContextPath() + "/profile");
        } catch (ServletException e) {
            // Login fallito
            response.sendRedirect(request.getContextPath() + "/login-error");
        }
    }
}
```

### Esempio con EJB (`EJBContext`)

All'interno di un EJB, si può usare `EJBContext` per ottenere informazioni sull'utente chiamante.

```java
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;

@Stateless
public class ReportService {

    @Resource
    private EJBContext ejbContext;

    public void generateSensitiveReport() {
        // Ottiene il nome dell'utente autenticato
        String callerName = ejbContext.getCallerPrincipal().getName();
        
        // Controlla se l'utente ha il ruolo "SUPERVISOR"
        if (ejbContext.isCallerInRole("SUPERVISOR")) {
            System.out.println("Generazione report per il supervisore: " + callerName);
            // ... logica per generare il report
        } else {
            throw new SecurityException("Accesso negato");
        }
    }
}
```

---

## Tabella dei Termini Chiave

| Termine | Descrizione |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Autenticazione** | Il processo di verifica dell'identità di un utente (chi sei?). |
| **Autorizzazione** | Il processo di verifica se un utente autenticato ha i permessi per accedere a una risorsa (cosa puoi fare?). |
| **Principal** | Un'entità che può essere autenticata. Dopo il login, rappresenta l'identità dell'utente (es. il suo username). |
| **Ruolo (Role)** | Un raggruppamento di permessi. Un utente (Principal) viene mappato a uno o più ruoli (es. "ADMIN", "USER") per definire le sue autorizzazioni. |
| **Sicurezza Dichiarativa** | Le regole di sicurezza sono definite fuori dal codice (in `web.xml` o tramite annotazioni). È l'approccio preferito. |
| **Sicurezza Programmatica** | La logica di sicurezza è scritta direttamente nel codice Java usando API specifiche come `isUserInRole()`. |
| **JAAS** | *Java Authentication and Authorization Service*. È il framework sottostante usato dai container Java EE per gestire la sicurezza in modo pluggable. |
| `web.xml` | Descrittore di deployment per applicazioni web dove si possono definire `security-constraint`, `login-config` e `security-role`. |
| `@RolesAllowed` | Annotazione (per EJB, JAX-RS) per specificare quali ruoli possono accedere a un metodo. |
| `@ServletSecurity` | Annotazione per le Servlet per definire vincoli di sicurezza in modo dichiarativo. |
| `HttpServletRequest.login()`| Metodo per eseguire un'autenticazione programmatica in un contesto web. |
| `EJBContext.isCallerInRole()`| Metodo per verificare programmaticamente il ruolo del chiamante all'interno di un EJB. |
