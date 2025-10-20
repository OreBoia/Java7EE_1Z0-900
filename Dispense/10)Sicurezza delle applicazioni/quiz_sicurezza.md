# Quiz: Sicurezza delle Applicazioni Java EE 7

## Domanda 1

Quali sono i due approcci principali per implementare la sicurezza in Java EE?

A) Sicurezza Client-Side e Server-Side  
B) Sicurezza Dichiarativa e Programmatica  
C) Sicurezza Statica e Dinamica  
D) Sicurezza BASIC e FORM

**Risposta:** B

**Spiegazione:** Java EE supporta due modelli di sicurezza: **Sicurezza Dichiarativa** (regole definite con annotazioni o XML) e **Sicurezza Programmatica** (logica implementata direttamente nel codice). Questi approcci possono anche coesistere.

---

## Domanda 2

Quale annotazione si usa per proteggere un metodo EJB consentendo l'accesso solo a specifici ruoli?

A) `@Secured`  
B) `@RolesAllowed`  
C) `@SecurityConstraint`  
D) `@AllowedRoles`

**Risposta:** B

**Spiegazione:** L'annotazione `@RolesAllowed` permette di specificare uno o più ruoli che possono accedere a un metodo EJB. Esempio: `@RolesAllowed({"ADMIN", "MANAGER"})`.

---

## Domanda 3

Quale metodo di autenticazione usa una pagina HTML/JSP personalizzata per il login?

A) BASIC  
B) DIGEST  
C) FORM  
D) CLIENT-CERT

**Risposta:** C

**Spiegazione:** Il metodo **FORM** permette di utilizzare una pagina di login personalizzata (HTML/JSP/JSF) per raccogliere le credenziali dell'utente, offrendo un'esperienza utente migliore rispetto a BASIC.

---

## Domanda 4

Nel metodo FORM-based authentication, quali sono i nomi standard dei campi del form che il container si aspetta?

A) `username` e `password`  
B) `user` e `pass`  
C) `j_username` e `j_password`  
D) `login` e `pwd`

**Risposta:** C

**Spiegazione:** Il container Java EE richiede che il form usi i campi standard `j_username` e `j_password`, e l'action `j_security_check` per gestire automaticamente l'autenticazione.

---

## Domanda 5

Quale elemento XML si usa in `web.xml` per definire quali URL sono protetti e da quali ruoli?

A) `<auth-constraint>`  
B) `<security-constraint>`  
C) `<url-protection>`  
D) `<role-mapping>`

**Risposta:** B

**Spiegazione:** L'elemento `<security-constraint>` in `web.xml` permette di specificare quali URL sono protetti (`<url-pattern>`), quali ruoli possono accedervi (`<auth-constraint>`), e i requisiti di trasporto (`<user-data-constraint>`).

---

## Domanda 6

Dato il seguente codice:

```java
@WebServlet("/admin/dashboard")
@ServletSecurity(
    @HttpConstraint(rolesAllowed = "ADMIN")
)
public class AdminDashboardServlet extends HttpServlet {
    // ...
}
```

Chi può accedere a questo servlet?

A) Tutti gli utenti autenticati  
B) Solo gli utenti con ruolo ADMIN  
C) Tutti gli utenti, anche quelli non autenticati  
D) Nessuno, il servlet è completamente bloccato

**Risposta:** B

**Spiegazione:** L'annotazione `@ServletSecurity` con `rolesAllowed = "ADMIN"` permette l'accesso solo agli utenti autenticati che hanno il ruolo ADMIN.

---

## Domanda 7

Quale metodo di `HttpServletRequest` permette di verificare se l'utente corrente appartiene a un ruolo specifico?

A) `checkRole(String role)`  
B) `hasRole(String role)`  
C) `isUserInRole(String role)`  
D) `verifyRole(String role)`

**Risposta:** C

**Spiegazione:** Il metodo `isUserInRole(String role)` è l'API standard per verificare programmaticamente se l'utente autenticato appartiene a un determinato ruolo.

---

## Domanda 8

Come si esegue un login programmatico in un Servlet?

A) `request.authenticate(username, password)`  
B) `request.login(username, password)`  
C) `SecurityContext.login(username, password)`  
D) `HttpSession.login(username, password)`

**Risposta:** B

**Spiegazione:** Il metodo `request.login(username, password)` su `HttpServletRequest` permette di eseguire un'autenticazione programmatica. Se il login ha successo, l'utente è autenticato per la sessione corrente.

---

## Domanda 9

Quale annotazione consente l'accesso a un metodo a TUTTI gli utenti, anche quelli non autenticati?

A) `@AllowAll`  
B) `@Public`  
C) `@PermitAll`  
D) `@Unrestricted`

**Risposta:** C

**Spiegazione:** L'annotazione `@PermitAll` permette l'accesso a un metodo o risorsa a tutti gli utenti, indipendentemente dal fatto che siano autenticati o meno.

---

## Domanda 10

Quale annotazione NEGA l'accesso a un metodo a TUTTI gli utenti?

A) `@DenyAll`  
B) `@NoAccess`  
C) `@Forbidden`  
D) `@RestrictAll`

**Risposta:** A

**Spiegazione:** L'annotazione `@DenyAll` blocca completamente l'accesso a un metodo, indipendentemente dal ruolo dell'utente. È utile per disabilitare temporaneamente metodi o per metodi in fase di sviluppo.

---

## Domanda 11

Quale valore di `<transport-guarantee>` forza l'uso di HTTPS?

A) `SECURE`  
B) `CONFIDENTIAL`  
C) `ENCRYPTED`  
D) `SSL`

**Risposta:** B

**Spiegazione:** Impostando `<transport-guarantee>` a `CONFIDENTIAL` in un `<user-data-constraint>`, si obbliga il client a usare una connessione sicura HTTPS. Il container reindirizza automaticamente le richieste HTTP a HTTPS.

---

## Domanda 12

Dato il seguente XML:

```xml
<security-constraint>
    <web-resource-collection>
        <url-pattern>/admin/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>ADMIN</role-name>
    </auth-constraint>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

Cosa succede quando un utente cerca di accedere a `/admin/users` via HTTP?

A) Riceve un errore 403  
B) Riceve un errore 404  
C) Viene reindirizzato a HTTPS  
D) L'accesso è consentito normalmente

**Risposta:** C

**Spiegazione:** Con `<transport-guarantee>CONFIDENTIAL</transport-guarantee>`, il container reindirizza automaticamente la richiesta HTTP a HTTPS prima di verificare l'autenticazione e autorizzazione.

---

## Domanda 13

In un EJB, quale oggetto si usa per verificare programmaticamente i ruoli?

A) `SecurityContext`  
B) `EJBContext`  
C) `Principal`  
D) `SessionContext`

**Risposta:** B

**Spiegazione:** All'interno di un EJB, si usa `EJBContext` (iniettato con `@Resource`) per verificare i ruoli con `isCallerInRole(String role)` e ottenere il principal con `getCallerPrincipal()`.

---

## Domanda 14

Cosa rappresenta un **Principal** nella sicurezza Java EE?

A) Un ruolo assegnato a un utente  
B) L'identità di un utente autenticato  
C) Una policy di sicurezza  
D) Un certificato di sicurezza

**Risposta:** B

**Spiegazione:** Un **Principal** rappresenta l'identità di un'entità autenticata (solitamente un utente). Dopo il login, il Principal contiene tipicamente il nome utente e viene usato per le verifiche di autorizzazione.

---

## Domanda 15

Cosa fa l'annotazione `@RunAs` su un EJB?

A) Esegue il bean in modo asincrono  
B) Esegue il bean con l'identità di un ruolo specifico  
C) Definisce il timeout di esecuzione  
D) Specifica su quale server eseguire il bean

**Risposta:** B

**Spiegazione:** `@RunAs` permette a un EJB di essere eseguito con l'identità di un ruolo specifico, diverso da quello del chiamante. Questo è utile per la propagazione dell'identità tra chiamate a diversi EJB.

---

## Domanda 16

Dato il seguente codice:

```java
@Stateless
@DeclareRoles({"ADMIN", "SYSTEM_AUDITOR"})
@RunAs("SYSTEM_AUDITOR")
public class ReportingService {

    @EJB
    private AuditService auditService;

    @RolesAllowed("ADMIN")
    public void generateReport() {
        auditService.log("Report generato");
    }
}
```

Con quale ruolo viene eseguito il metodo `auditService.log()`?

A) ADMIN  
B) SYSTEM_AUDITOR  
C) Entrambi  
D) Nessuno, genera un errore

**Risposta:** B

**Spiegazione:** Grazie a `@RunAs("SYSTEM_AUDITOR")`, quando il `ReportingService` chiama altri EJB, lo fa con l'identità del ruolo `SYSTEM_AUDITOR`, anche se il chiamante originale ha il ruolo `ADMIN`.

---

## Domanda 17

Quale framework sta alla base del sistema di sicurezza di Java EE?

A) Spring Security  
B) JAAS (Java Authentication and Authorization Service)  
C) Apache Shiro  
D) OAuth

**Risposta:** B

**Spiegazione:** **JAAS** è il framework standard Java che i container Java EE usano "dietro le quinte" per gestire l'autenticazione e l'autorizzazione in modo pluggable e configurabile.

---

## Domanda 18

Cos'è un **Realm** nel contesto della sicurezza Java EE?

A) Un insieme di regole di autorizzazione  
B) Una sorgente di dati per utenti, password e ruoli (es. database, LDAP)  
C) Un certificato di sicurezza  
D) Un protocollo di autenticazione

**Risposta:** B

**Spiegazione:** Un **Realm** è una sorgente di dati configurata a livello di server applicativo che contiene le informazioni su utenti, password e ruoli. Può essere un file, un database, un server LDAP, ecc.

---

## Domanda 19

Quale metodo permette di ottenere il nome dell'utente autenticato in un Servlet?

A) `request.getUserName()`  
B) `request.getPrincipal().getName()`  
C) `request.getUserPrincipal().getName()`  
D) `request.getUser()`

**Risposta:** C

**Spiegazione:** Il metodo `request.getUserPrincipal()` restituisce il `Principal` dell'utente autenticato, e chiamando `getName()` su di esso si ottiene il nome utente (username).

---

## Domanda 20

Quale annotazione si usa per dichiarare formalmente i ruoli che un EJB intende usare?

A) `@SecurityRoles`  
B) `@DeclareRoles`  
C) `@DefineRoles`  
D) `@Roles`

**Risposta:** B

**Spiegazione:** `@DeclareRoles` dichiara formalmente i ruoli di sicurezza che saranno usati nel componente, rendendoli noti al container. È particolarmente utile quando si usano controlli programmatici con `isCallerInRole()`.

---

## Domanda 21

Quale metodo di autenticazione usa certificati digitali X.509?

A) BASIC  
B) DIGEST  
C) FORM  
D) CLIENT-CERT

**Risposta:** D

**Spiegazione:** **CLIENT-CERT** è il metodo di autenticazione basato su certificati digitali SSL/TLS. L'utente si autentica presentando un certificato client, offrendo un livello di sicurezza molto elevato.

---

## Domanda 22

Come si effettua un logout programmatico in un Servlet?

A) `request.logout()`  
B) `session.invalidate()`  
C) `SecurityContext.logout()`  
D) `request.removeAuthentication()`

**Risposta:** A

**Spiegazione:** Il metodo `request.logout()` su `HttpServletRequest` invalida la sessione dell'utente e rimuove l'autenticazione. In alternativa, `session.invalidate()` può essere usato ma è meno specifico.

---

## Domanda 23

In quale directory deve trovarsi il file `web.xml` di un'applicazione web?

A) `META-INF/`  
B) `WEB-INF/`  
C) `src/main/resources/`  
D) Nella root dell'applicazione

**Risposta:** B

**Spiegazione:** Il file `web.xml` (deployment descriptor) deve essere posizionato nella directory `WEB-INF/` dell'applicazione web. È il descrittore standard per configurare servlet, filtri, sicurezza, ecc.

---

## Domanda 24

Quale delle seguenti affermazioni sulla sicurezza dichiarativa è VERA?

A) È più flessibile della sicurezza programmatica  
B) Richiede sempre la ricompilazione del codice per modifiche  
C) Separa la logica di sicurezza da quella di business  
D) Non può essere usata insieme alla sicurezza programmatica

**Risposta:** C

**Spiegazione:** La **sicurezza dichiarativa** separa le regole di sicurezza dalla logica di business, rendendola più manutenibile. Le regole possono essere modificate senza ricompilare il codice, e può coesistere con la sicurezza programmatica.

---

## Domanda 25

Dato il seguente codice:

```java
if (request.getUserPrincipal() != null) {
    if (request.isUserInRole("FINANCE") || request.isUserInRole("ADMIN")) {
        // Mostra dati finanziari
    } else {
        // Mostra dati limitati
    }
} else {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
}
```

Questo è un esempio di quale tipo di sicurezza?

A) Sicurezza Dichiarativa  
B) Sicurezza Programmatica  
C) Sicurezza Basata su Annotazioni  
D) Sicurezza Automatica

**Risposta:** B

**Spiegazione:** Questo è un esempio di **sicurezza programmatica**, dove la logica di autorizzazione è implementata direttamente nel codice usando le API `getUserPrincipal()` e `isUserInRole()`.

---

## Domanda 26

Quale metodo di autenticazione invia le credenziali come hash invece che in chiaro?

A) BASIC  
B) DIGEST  
C) FORM  
D) Tutti i metodi inviano in chiaro

**Risposta:** B

**Spiegazione:** **DIGEST** authentication invia un hash delle credenziali invece delle credenziali in Base64 (come BASIC), offrendo maggiore sicurezza. Tuttavia, HTTPS rimane la soluzione preferita per proteggere le credenziali.

---

## Domanda 27

Cosa deve contenere un form di login per l'autenticazione FORM-based?

A) Action: `login`, campi: `user` e `password`  
B) Action: `j_security_check`, campi: `j_username` e `j_password`  
C) Action: `authenticate`, campi: `username` e `password`  
D) Action: `security`, campi: `login` e `pwd`

**Risposta:** B

**Spiegazione:** Per il metodo FORM, il form deve avere `action="j_security_check"`, con i campi `j_username` e `j_password`. Questi sono standard richiesti dal container Java EE.

---

## Domanda 28

Quale annotazione si usa per proteggere un servizio REST JAX-RS?

A) `@Secured`  
B) `@RolesAllowed`  
C) `@RestSecurity`  
D) `@Protected`

**Risposta:** B

**Spiegazione:** Anche per i servizi REST JAX-RS, si usa l'annotazione standard `@RolesAllowed` per specificare quali ruoli possono accedere a una risorsa o metodo REST.

---

## Domanda 29

In che ordine avviene il processo di sicurezza quando un utente accede a una risorsa protetta?

A) Autorizzazione → Autenticazione → Accesso  
B) Autenticazione → Autorizzazione → Accesso  
C) Accesso → Autenticazione → Autorizzazione  
D) Non c'è un ordine specifico

**Risposta:** B

**Spiegazione:** Il processo standard è: 1) **Autenticazione** (verificare chi è l'utente), 2) **Autorizzazione** (verificare cosa può fare), 3) **Accesso** alla risorsa. Prima bisogna identificare l'utente, poi verificare i suoi permessi.

---

## Domanda 30

Quale delle seguenti è una caratteristica di JAAS?

A) È un framework specifico per Java EE  
B) È pluggable, permettendo di cambiare il meccanismo di autenticazione senza modificare il codice  
C) Gestisce solo l'autorizzazione  
D) È stato deprecato in Java EE 7

**Risposta:** B

**Spiegazione:** **JAAS** è un framework pluggable che permette di cambiare il meccanismo di autenticazione (es. da file a LDAP) a livello di configurazione del server, senza modificare il codice dell'applicazione. Gestisce sia autenticazione che autorizzazione.
