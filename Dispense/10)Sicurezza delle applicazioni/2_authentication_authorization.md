# Autenticazione e Autorizzazione in Java EE 7

La sicurezza in Java EE si fonda su due pilastri fondamentali: l'**autenticazione** (verificare chi è l'utente) e l'**autorizzazione** (decidere cosa può fare l'utente). Il container Java EE offre meccanismi standard e robusti per gestire entrambi, astraendo la complessità dallo sviluppatore.

---

## 1. Autenticazione (Authentication)

L'autenticazione è il processo con cui un utente prova la propria identità al sistema, solitamente fornendo credenziali come username e password. Java EE supporta diversi metodi di autenticazione gestiti dal container, configurabili in modo dichiarativo.

I principali metodi sono:

- **BASIC**: Il browser apre una finestra di dialogo nativa per chiedere username e password. Le credenziali vengono inviate in Base64 (non crittografate, quindi da usare solo con HTTPS).
- **DIGEST**: Simile a BASIC, ma invia un hash delle credenziali invece che le credenziali in chiaro, offrendo maggiore sicurezza.
- **FORM**: Il metodo più comune per le applicazioni web. Permette di usare una pagina di login personalizzata (un form HTML/JSP/JSF) per raccogliere le credenziali. Il container gestisce la validazione delle credenziali rispetto a un **realm** (un database di utenti, un server LDAP, ecc.) configurato sul server.
- **CLIENT-CERT (X.509)**: Autenticazione basata su certificati digitali SSL/TLS. L'utente si autentica presentando un certificato client, garantendo un livello di sicurezza molto elevato.

### Configurazione dell'Autenticazione

La configurazione avviene tipicamente in modo dichiarativo.

#### Esempio: Autenticazione FORM-based in `web.xml`

Il file `web.xml` permette di specificare il metodo di autenticazione e le pagine per il login e l'errore.

```xml
<web-app ...>
    <!-- ... altri settaggi ... -->

    <login-config>
        <!-- Metodo di autenticazione scelto: FORM -->
        <auth-method>FORM</auth-method>
        
        <!-- Dominio di sicurezza (realm) usato dal server -->
        <realm-name>my-app-realm</realm-name>
        
        <form-login-config>
            <!-- Pagina custom per il login -->
            <form-login-page>/login.xhtml</form-login-page>
            <!-- Pagina da mostrare in caso di login fallito -->
            <form-error-page>/login-error.xhtml</form-error-page>
        </form-login-config>
    </login-config>

    <!-- ... security constraints e roles ... -->
</web-app>
```

Il form nella pagina `login.xhtml` deve usare le action `j_security_check` e i campi `j_username` e `j_password`, gestiti automaticamente dal container.

```html
<form method="POST" action="j_security_check">
    Username: <input type="text" name="j_username" />
    Password: <input type="password" name="j_password" />
    <input type="submit" value="Login" />
</form>
```

#### Esempio: Annotazioni (JASPIC / Java EE 8+)

In Java EE 8 e con lo standard JASPIC, è possibile usare annotazioni per una configurazione più moderna, riducendo la necessità di XML.

```java
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.xhtml",
        errorPage = "/login-error.xhtml"
    )
)
@WebServlet("/my-app")
public class MyApplicationServlet extends HttpServlet {
    // ...
}
```

---

## 2. Autorizzazione (Authorization)

Una volta che l'utente è stato autenticato, il container determina se ha i permessi per accedere alla risorsa richiesta. Questo processo è l'autorizzazione.

L'autorizzazione in Java EE si basa sul **mapping tra il Principal (l'identità dell'utente autenticato) e i ruoli di sicurezza** definiti dall'applicazione.

### Autorizzazione Dichiarativa

È l'approccio più comune e si realizza tramite `web.xml` o annotazioni.

#### Esempio: Vincoli di Sicurezza in `web.xml`

Si definisce un `<security-constraint>` per proteggere un insieme di URL e si specifica quali ruoli (`<auth-constraint>`) possono accedervi.

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Area Amministrativa</web-resource-name>
        <url-pattern>/admin/*</url-pattern> <!-- Protegge tutte le URL sotto /admin/ -->
    </web-resource-collection>
    <auth-constraint>
        <role-name>ADMIN</role-name> <!-- Solo gli utenti con ruolo ADMIN possono accedere -->
    </auth-constraint>
</security-constraint>

<security-role>
    <role-name>ADMIN</role-name>
</security-role>
```

#### Esempio: Annotazioni di Sicurezza

Le annotazioni permettono di applicare le regole di autorizzazione direttamente sul codice.

- `@RolesAllowed`: Specifica un elenco di ruoli che possono accedere a un metodo o a una classe.
- `@PermitAll`: Consente l'accesso a tutti, anche agli utenti non autenticati.
- `@DenyAll`: Nega l'accesso a tutti, indipendentemente dal ruolo.

```java
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;

@Stateless
public class ProductService {

    @RolesAllowed({"ADMIN", "MANAGER"})
    public void addProduct(Product p) {
        // ... logica per aggiungere un prodotto
    }

    @RolesAllowed("USER")
    public void viewProduct(int productId) {
        // ... logica per visualizzare un prodotto
    }

    @PermitAll
    public List<Product> listAllProducts() {
        // Tutti possono vedere la lista dei prodotti
        return null;
    }
}
```

### Autorizzazione Programmatica

Quando la logica di autorizzazione è dinamica, si può usare l'API `isUserInRole()` per verificare i ruoli nel codice.

```java
@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // L'utente è autenticato?
        if (request.getUserPrincipal() != null) {
            
            // Ha il ruolo 'FINANCE' o 'ADMIN'?
            if (request.isUserInRole("FINANCE") || request.isUserInRole("ADMIN")) {
                // Genera report finanziario completo
                response.getWriter().write("Dati finanziari dettagliati...");
            } else {
                // Altrimenti, mostra un report di base
                response.getWriter().write("Dati di base...");
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Accesso non autorizzato");
        }
    }
}
```

---

## 3. Sicurezza Dichiarativa vs. Programmatica

Come accennato, Java EE offre due approcci complementari per implementare la sicurezza.

- **La sicurezza dichiarativa** è l'approccio preferito e più comune. Le regole di sicurezza sono definite esternamente al codice dell'applicazione, utilizzando annotazioni Java o descrittori di deployment XML (come `web.xml`). Questo disaccoppia la logica di sicurezza da quella di business, rendendo l'applicazione più pulita e facile da manutenere. Le regole possono essere modificate senza dover ricompilare il codice.

    *Esempio con annotazione su una Servlet:*

    ```java
    @WebServlet("/user/profile")
    @ServletSecurity(@HttpConstraint(rolesAllowed = {"USER", "ADMIN"}))
    public class UserProfileServlet extends HttpServlet {
        // ...
    }
    ```

- **La sicurezza programmatica** viene utilizzata quando le decisioni di autorizzazione dipendono da logiche complesse o da condizioni che possono essere verificate solo a runtime. Si implementa direttamente nel codice Java utilizzando le API fornite dalla piattaforma. È più flessibile ma lega la sicurezza alla logica di business.

    *API comuni per la sicurezza programmatica:*
  - `HttpServletRequest.login(username, password)`: Per tentare di autenticare un utente.
  - `HttpServletRequest.logout()`: Per invalidare la sessione dell'utente.
  - `HttpServletRequest.isUserInRole(roleName)`: Per verificare se l'utente appartiene a un ruolo.
  - `EJBContext.isCallerInRole(roleName)`: L'equivalente in un contesto EJB.

## 4. Ruoli e Protezione delle Risorse

Un **ruolo** è un nome logico e astratto che rappresenta un'autorizzazione. Invece di dare permessi direttamente ai singoli utenti, si concedono permessi ai ruoli, e poi si assegnano i ruoli agli utenti.

### Definizione e Mapping dei Ruoli

I ruoli usati da un'applicazione (`<security-role>`) vengono dichiarati nei descrittori di deployment (es. `web.xml` o `ejb-jar.xml`). Durante il deployment, l'amministratore del server applicativo si occupa di **mappare** questi ruoli logici agli utenti o gruppi reali definiti nel **realm** del server. Questo permette di separare i ruoli definiti dallo sviluppatore dagli utenti/gruppi gestiti dall'amministratore di sistema.

### Protezione delle Risorse Web e Trasporto Sicuro (HTTPS)

Con l'elemento `<security-constraint>` in `web.xml` è possibile definire vincoli di sicurezza molto precisi per le risorse web.

- **Chi può accedere**: L'`<auth-constraint>` specifica quali ruoli possono accedere a un dato `<url-pattern>`.
- **Come si deve accedere**: Per proteggere i dati in transito, Java EE consente di richiedere la cifratura tramite HTTPS. Il `<user-data-constraint>` definisce i requisiti del trasporto di rete. Impostando `<transport-guarantee>` a `CONFIDENTIAL`, si obbliga il client a usare una connessione sicura (SSL/TLS). Se un utente tenta di accedere alla risorsa tramite HTTP, il container tenterà di reindirizzarlo a HTTPS.

**Esempio: Imporre HTTPS**

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Area Pagamenti</web-resource-name>
        <url-pattern>/checkout/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>CUSTOMER</role-name>
    </auth-constraint>
    
    <!-- Impone che l'accesso a /checkout/* avvenga solo tramite HTTPS -->
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

### Protezione dei Metodi EJB

Come già visto, negli EJB la protezione a livello di metodo è semplice e potente grazie alle annotazioni `@RolesAllowed`, `@PermitAll` e `@DenyAll`, che permettono un controllo granulare su chi può invocare la logica di business.

---

## 5. Configurazioni Avanzate e JAAS

Oltre a `web.xml`, la sicurezza può essere configurata in altri punti. Il meccanismo che sta alla base di tutto è **JAAS (Java Authentication and Authorization Service)**.

### Descrittori, Annotazioni e Realm

Anche i bean EJB e altri componenti possono usare descrittori (es. `ejb-jar.xml`) o annotazioni specifiche per la sicurezza:

- **`@DeclareRoles`**: Dichiara formalmente i ruoli che saranno usati nel codice (es. passati a `isCallerInRole`), rendendoli noti al container.
- **`@RunAs`**: Specifica che un EJB deve essere eseguito con l'identità di un ruolo specifico, utile per la propagazione dell'identità tra chiamate a diversi EJB.

Il container gestisce un **realm di autenticazione**, ovvero una sorgente di dati per utenti, password e gruppi (es. un file, un database, un server LDAP). Questo realm è configurato a livello di server dall'amministratore e non è parte dell'applicazione.

### Esempio di `@RunAs` e `@DeclareRoles`

Immaginiamo uno scenario con due EJB:

1. `ReportingService`: Accessibile solo dagli `ADMIN` per generare report.
2. `AuditService`: Un servizio interno che registra tutte le operazioni sensibili e richiede il ruolo `AUDITOR`.

Un `ADMIN` non ha il ruolo `AUDITOR`, quindi come può `ReportingService` chiamare `AuditService`? La soluzione è usare `@RunAs` per "elevare" temporaneamente i privilegi del `ReportingService`.

```java
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Questo EJB può essere chiamato da un ADMIN, ma quando esegue le sue operazioni,
 * agisce con l'identità del ruolo "SYSTEM_AUDITOR".
 */
@Stateless
@DeclareRoles({"ADMIN", "SYSTEM_AUDITOR"}) // Dichiara i ruoli usati
@RunAs("SYSTEM_AUDITOR") // Esegui come questo ruolo
public class ReportingService {

    @EJB
    private AuditService auditService;

    // Solo gli ADMIN possono chiamare questo metodo
    @RolesAllowed("ADMIN")
    public String generateAdminReport() {
        // Grazie a @RunAs, la chiamata seguente è valida perché
        // il chiamante (ReportingService) ora ha il ruolo "SYSTEM_AUDITOR".
        auditService.log("Generazione report amministrativo richiesta.");
        
        return "Report per ADMIN generato con successo.";
    }
}

@Stateless
public class AuditService {

    // Solo chi ha il ruolo "SYSTEM_AUDITOR" può chiamare questo metodo.
    @RolesAllowed("SYSTEM_AUDITOR")
    public void log(String message) {
        // Logica per scrivere su un file di audit sicuro
        System.out.println("AUDIT: " + message);
    }
}
```

In questo esempio, un utente con ruolo `ADMIN` può invocare `generateAdminReport()`. All'interno di quel metodo, grazie a `@RunAs("SYSTEM_AUDITOR")`, il `ReportingService` assume l'identità del ruolo `SYSTEM_AUDITOR`, permettendogli di chiamare con successo il metodo `log()` dell'`AuditService`, che è protetto e richiede proprio quel ruolo.

### Il Ruolo di JAAS

Il codice applicativo **non interagisce quasi mai direttamente con JAAS**. È il container che lo usa "dietro le quinte".

1. Il container intercetta una richiesta a una risorsa protetta.
2. In base alla configurazione (`<login-config>`, ecc.), attiva i **Login Module** di JAAS appropriati. Un Login Module è un componente pluggable che sa come autenticare gli utenti rispetto a uno specifico sistema (es. `LDAPLoginModule`, `DatabaseLoginModule`).
3. Se l'autenticazione ha successo, il Login Module recupera i dati dell'utente e i suoi ruoli dal realm.
4. JAAS crea un `Subject` che contiene il `Principal` (l'identità dell'utente) e i ruoli associati.
5. Il container usa queste informazioni per le successive verifiche di autorizzazione (es. `isUserInRole`).

Questo modello rende l'applicazione indipendente dal meccanismo di autenticazione specifico, che può essere cambiato a livello di server senza modificare il codice.

---

## Tabella dei Termini Chiave

| Termine | Descrizione |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Authentication** | Il processo di verifica dell'identità di un utente (es. tramite username/password). |
| **Authorization** | Il processo di verifica se un utente autenticato ha i permessi per accedere a una risorsa specifica. |
| **JAAS** | *Java Authentication and Authorization Service*. Framework standard di Java, usato "sotto il cofano" dai container EE per gestire l'autenticazione e l'autorizzazione in modo pluggable. |
| **Login Module** | Un componente di JAAS che contiene la logica per autenticare gli utenti contro una specifica sorgente (es. file, LDAP, DB). |
| **Realm** | Un database o un servizio (es. file, LDAP) configurato a livello di server, che contiene le informazioni su utenti, password e ruoli. |
| **Principal** | L'identità di un utente autenticato (es. il suo username). |
| **Role** | Un'etichetta astratta che rappresenta un livello di autorizzazione (es. "ADMIN", "USER"). Un Principal è associato a uno o più ruoli. |
| **Sicurezza Dichiarativa** | Regole di sicurezza definite fuori dal codice (XML o annotazioni). Approccio preferito per la manutenibilità. |
| **Sicurezza Programmatica** | Logica di sicurezza implementata nel codice tramite API (es. `isUserInRole`) per decisioni complesse o a runtime. |
| `<transport-guarantee>` | Sotto-elemento di `<user-data-constraint>` che impone requisiti sul trasporto di rete. `CONFIDENTIAL` forza l'uso di HTTPS. |
| `@DeclareRoles` | Annotazione per dichiarare i ruoli di sicurezza che un componente intende usare programmaticamente. |
| `@RunAs` | Annotazione (per EJB) che permette a un bean di essere eseguito con l'identità di un ruolo specifico. |
| `isUserInRole("ruolo")` | Metodo programmatico (su `HttpServletRequest` o `EJBContext`) per verificare se l'utente corrente appartiene a un determinato ruolo. |
| `request.login()` / `logout()` | API programmatiche per gestire il ciclo di vita dell'autenticazione utente in un contesto web. |
