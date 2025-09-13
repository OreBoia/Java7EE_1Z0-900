# 📘 Glossario Completo Java EE 7 -- Certificazione 1Z0-900

## Architettura e Deploy

- **Application Server**\
    Software che implementa le specifiche Java EE e fornisce container
    per eseguire componenti (es. GlassFish, WildFly, WebLogic).

- **Container-Managed vs. Bean-Managed**\
    Modalità di gestione delle risorse. Nel primo caso è il container a
    gestire (transazioni, sicurezza, lifecycle), nel secondo è lo
    sviluppatore.

- **Deployment Descriptor (web.xml, ejb-jar.xml)**\
    File XML che definisce configurazioni di deploy. Con Java EE 7 molte
    configurazioni sono sostituite da annotazioni.

- **POJO (Plain Old Java Object)**\
    Oggetto Java semplice, non vincolato da framework specifici, che contiene campi (con eventuali getter/setter), costruttori semplici e nessuna ereditarietà o annotazione speciale obbligata. Serve a rappresentare dati in modo pulito e indipendente dall’infrastruttura.

------------------------------------------------------------------------

## CDI -- Contexts and Dependency Injection

- **CDI (Contexts and Dependency Injection)**\
    Standard per iniettare dipendenze e gestire il ciclo di vita dei
    bean. Versione in Java EE 7: CDI 1.1.

- **@Inject**\
    Annotazione per iniettare una dipendenza.

- **@Qualifier**\
    Permette di distinguere più implementazioni della stessa
    interfaccia.

- **@Named**\
    Rende un bean accessibile in EL (Expression Language), utile in
    JSP/JSF.

- **Scope Annotations**\
    Definiscono il ciclo di vita dei bean:

  - `@RequestScoped`: vive per la durata della richiesta.\
  - `@SessionScoped`: vive per la sessione HTTP.\
  - `@ApplicationScoped`: condiviso tra tutti gli utenti.\
  - `@ConversationScoped`: ciclo di vita personalizzabile.

- **Interceptors (@Interceptor)**\
    Permettono di eseguire logica trasversale (logging, sicurezza)
    attorno ai metodi.

- **Decorators (@Decorator)**\
    Estendono il comportamento di un bean implementando la stessa
    interfaccia.

------------------------------------------------------------------------

## Sicurezza

- **JAAS (Java Authentication and Authorization Service)**\
    Framework di autenticazione/autorizzazione in Java EE.

- **Declarative Security**\
    Sicurezza configurata con annotazioni o descrittori XML
    (`@RolesAllowed`, `@PermitAll`, `@DenyAll`).

- **Programmatic Security**\
    Sicurezza gestita via codice (es. `request.isUserInRole("admin")`).

- **Principal**\
    Rappresenta l'identità dell'utente autenticato.

- **Realm**\
    Repository di utenti/ruoli (es. file, DB, LDAP).

------------------------------------------------------------------------

## Transazioni

- **JTA (Java Transaction API)**\
    API standard per la gestione delle transazioni distribuite. Versione
    in Java EE 7: JTA 1.2.

- **CMT (Container-Managed Transactions)**\
    Transazioni gestite dal container. Annotazioni: `@Transactional`,
    `@TransactionAttribute`.

- **BMT (Bean-Managed Transactions)**\
    Lo sviluppatore gestisce manualmente le transazioni tramite
    `UserTransaction`.

- **Propagation**\
    Definisce come un metodo entra in una transazione esistente o ne
    crea una nuova (es. `REQUIRED`, `REQUIRES_NEW`, `MANDATORY`).

------------------------------------------------------------------------

## Validazione

- **Bean Validation (JSR 349)**\
    Framework per validazione di oggetti. Integrato con JPA, JSF, CDI.
    Versione in Java EE 7: Bean Validation 1.1.

- **Constraint Annotations**

  - `@NotNull` -- il campo non può essere nullo\
  - `@Size(min, max)` -- lunghezza di stringhe/collezioni\
  - `@Pattern(regex)` -- espressioni regolari\
  - `@Past` / `@Future` -- validazione date

- **Custom Constraint**\
    Creazione di validazioni personalizzate con `@Constraint` e un
    `ConstraintValidator`.

------------------------------------------------------------------------

## Concorrenza e Timer

- **Concurrency Utilities (JSR 236)**\
    API standard per concorrenza in Java EE. Fornisce thread pool
    gestiti dal container.

- **ManagedExecutorService**\
    Interfaccia per eseguire task in thread gestiti.

- **@Asynchronous**\
    Permette metodi asincroni su EJB o CDI bean.

- **Timer Service**\
    Permette scheduling di operazioni tramite annotazioni come
    `@Schedule` o timer programmatici.

------------------------------------------------------------------------

## Risorse e Configurazione

- **JNDI (Java Naming and Directory Interface)**\
    API per cercare e registrare risorse (DB, JMS, EJB) tramite nomi
    logici.

- **DataSource**\
    Oggetto JNDI per la connessione a DB gestito dal container.

- **Resource Injection (@Resource, @EJB, @PersistenceContext)**\
    Meccanismo per iniettare risorse dichiarate in JNDI.

------------------------------------------------------------------------

## Interoperabilità

- **JSON-P (Java API for JSON Processing)**\
    API per parsing e scrittura JSON (streaming e object model).

- **Batch Processing (JSR 352)**\
    API per job batch (lettura, elaborazione, scrittura di grandi volumi
    di dati). Basata su step e job XML.

- **Connector Architecture (JCA)**\
    Standard per collegare Java EE a sistemi legacy o enterprise esterni
    (ERP, mainframe).

------------------------------------------------------------------------

## Packaging e Deployment

- **META-INF / WEB-INF**\
    Directory speciali per configurazioni (es. `web.xml`,
    `persistence.xml`).

- **Classloader Hierarchy**\
    Struttura che definisce come le classi vengono caricate in un EAR
    (moduli isolati vs. condivisi).

- **Portable JNDI Names**\
    Standard introdotto in Java EE 6/7 per rendere nomi JNDI portabili
    tra application server.

------------------------------------------------------------------------

## Strumenti e Testing

- **Arquillian**\
    Framework per testare applicazioni Java EE in container reali.

- **Embedded Container**\
    Modalità di esecuzione leggera per testing senza deploy completo.
