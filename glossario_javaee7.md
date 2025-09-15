# 📘 Glossario Completo Java EE 7 -- Certificazione 1Z0-900

## Architettura e Deploy

- **Application Server**
  Software che implementa le specifiche Java EE e fornisce container per eseguire componenti (es. GlassFish, WildFly, WebLogic).

- **Container-Managed vs. Bean-Managed**
  Modalità di gestione delle risorse. Nel primo caso è il container a gestire (transazioni, sicurezza, lifecycle), nel secondo è lo sviluppatore.

- **Deployment Descriptor (web.xml, ejb-jar.xml)**
  File XML che definisce configurazioni di deploy. Con Java EE 7 molte configurazioni sono sostituite da annotazioni.

- **POJO (Plain Old Java Object)**
  Oggetto Java semplice, non vincolato da framework specifici, che contiene campi (con eventuali getter/setter), costruttori semplici e nessuna ereditarietà o annotazione speciale obbligata. Serve a rappresentare dati in modo pulito e indipendente dall’infrastruttura.

---

## JPA -- Java Persistence API

- **JPA (Java Persistence API)**
  Specifica standard per l'ORM (Object-Relational Mapping) in Java. Permette di mappare oggetti Java a tabelle di database. Versione in Java EE 7: JPA 2.1.

- **Entity**
  Un POJO annotato con `@Entity` che rappresenta una tabella nel database.

- **EntityManager**
  Interfaccia principale per interagire con il database (salvare, cercare, aggiornare, eliminare entità). Viene iniettato con `@PersistenceContext`.

- **Persistence Unit**
  Configurazione definita in `persistence.xml` che raggruppa entità, data source e proprietà del provider JPA.

- **Ciclo di Vita dell'Entità**
  Gli stati di un'entità:
  - `New/Transient`: L'oggetto è appena stato creato e non è gestito da JPA.
  - `Managed`: L'oggetto è associato al contesto di persistenza e le sue modifiche sono tracciate.
  - `Detached`: L'oggetto era managed ma non è più associato al contesto.
  - `Removed`: L'oggetto è marcato per la rimozione dal database.

- **JPQL (Java Persistence Query Language)**
  Linguaggio di query simile a SQL ma orientato agli oggetti, che opera su entità e loro proprietà.

- **Annotazioni di Mapping**
  - `@Id`: Definisce la chiave primaria.
  - `@GeneratedValue`: Specifica come viene generata la chiave primaria.
  - `@Table`: Specifica il nome della tabella.
  - `@Column`: Mappa un campo a una colonna specifica.
  - `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`: Definiscono le relazioni tra entità.

---

## EJB -- Enterprise JavaBeans

- **EJB (Enterprise JavaBeans)**
  Componenti server-side gestiti dal container che incapsulano la logica di business. Versione in Java EE 7: EJB 3.2.

- **Tipi di Session Bean**
  - `@Stateless`: Non mantiene stato tra le chiamate. Ideale per operazioni atomiche e scalabili (es. servizi). Il container gestisce un pool di istanze.
  - `@Stateful`: Mantiene lo stato conversazionale per un singolo client. Utile per processi multi-step (es. carrello della spesa).
  - `@Singleton`: Esiste una sola istanza per tutta l'applicazione. Utile per gestire stato condiviso o cache.

- **Ciclo di Vita EJB**
  Gestito dal container con callback:
  - `@PostConstruct`: Chiamato dopo la creazione del bean.
  - `@PreDestroy`: Chiamato prima della distruzione.
  - `@PrePassivate` / `@PostActivate`: Usati negli stateful bean per la passivazione/attivazione.

- **Transazioni EJB (CMT)**
  Le transazioni sono gestite dal container (Container-Managed Transactions). L'annotazione `@TransactionAttribute` definisce il comportamento (es. `REQUIRED`, `REQUIRES_NEW`).

- **@Asynchronous**
  Annotazione che permette a un metodo di essere eseguito in un thread separato, restituendo immediatamente il controllo al chiamante. Il metodo può restituire `void` o `Future<V>`.

- **Timer Service (@Schedule)**
  Servizio per eseguire operazioni pianificate (cron job). L'annotazione `@Schedule` definisce quando un metodo deve essere eseguito automaticamente.

---

## JMS -- Java Message Service

- **JMS (Java Message Service)**
  API standard per la messaggistica asincrona. Permette a componenti disaccoppiati di comunicare tramite messaggi. Versione in Java EE 7: JMS 2.0.

- **Modelli di Messaggistica**
  - **Queue (Point-to-Point)**: Un messaggio viene inviato a una coda e consumato da un solo ricevente.
  - **Topic (Publish/Subscribe)**: Un messaggio viene pubblicato su un topic e ricevuto da tutti i sottoscrittori.

- **Componenti Principali (JMS 2.0)**
  - `JMSContext`: Interfaccia semplificata per creare producer e consumer.
  - `JMSProducer`: Oggetto per inviare messaggi.
  - `JMSConsumer`: Oggetto per ricevere messaggi.
  - `Destination`: La destinazione dei messaggi (una `Queue` o un `Topic`).

- **Message-Driven Bean (MDB)**
  Un tipo di EJB che agisce come un consumer di messaggi. Il metodo `onMessage` viene invocato automaticamente dal container quando un messaggio arriva sulla destinazione specificata. Gli MDB sono transazionali e concorrenti di default.

- **Sottoscrizioni (per Topic)**
  - `Non-Durable`: Riceve messaggi solo se il consumer è attivo.
  - `Durable`: Riceve i messaggi anche se il consumer era offline al momento dell'invio.

---

## Web Services (SOAP & REST)

- **JAX-WS (Java API for XML-Web Services)**
  Specifica per creare web service basati su SOAP e XML.
  - `@WebService`: Definisce una classe come un endpoint di un servizio SOAP.
  - `@WebMethod`: Definisce un metodo come un'operazione del servizio.
  - `WSDL (Web Services Description Language)`: Contratto XML che descrive il servizio.

- **JAXB (Java Architecture for XML Binding)**
  Tecnologia per il mapping tra oggetti Java e XML, usata da JAX-WS per serializzare/deserializzare i dati.

- **JAX-RS (Java API for RESTful Web Services)**
  Specifica per creare web service basati sullo stile architetturale REST. Versione in Java EE 7: JAX-RS 2.0.
  - `@Path`: Mappa una classe o un metodo a un URI.
  - `@GET`, `@POST`, `@PUT`, `@DELETE`: Mappano un metodo a un verbo HTTP.
  - `@Produces`, `@Consumes`: Definiscono i tipi di media (es. JSON, XML) prodotti e consumati.
  - `@PathParam`, `@QueryParam`, `@HeaderParam`: Iniettano parametri dall'URI, dalla query string o dagli header.

- **REST (REpresentational State Transfer)**
  Stile architetturale basato sui principi del web (HTTP, URI). I principi chiave includono statelessness, interfaccia uniforme e manipolazione di risorse tramite rappresentazioni (es. JSON).

---

## Web Tier (Servlet, JSP, JSF)

- **Servlet**
  Componenti Java che gestiscono richieste HTTP. Sono la base delle tecnologie web in Java EE. Il ciclo di vita (`init`, `service`, `destroy`) è gestito dal Web Container.

- **JSP (JavaServer Pages)**
  Tecnologia per creare pagine web dinamiche. Una pagina JSP viene tradotta e compilata in una Servlet dal container. Permette di mescolare HTML con codice Java.

- **EL (Expression Language)**
  Linguaggio (`${...}`) per accedere e manipolare dati (JavaBean) dalle pagine JSP e JSF, riducendo la necessità di codice Java (scriptlet).

- **JSTL (JSP Standard Tag Library)**
  Libreria di tag (`<c:if>`, `<c:forEach>`) che fornisce logica condizionale, iterazione e altre funzioni comuni nelle pagine JSP, promuovendo un codice più pulito.

- **JSF (JavaServer Faces)**
  Framework basato su componenti per costruire interfacce utente web.
  - **Component-Based**: La UI è un albero di componenti (es. `<h:inputText>`).
  - **Backing Bean**: Bean CDI (`@Named`) che contengono la logica e i dati della UI.
  - **Facelets**: La tecnologia di templating (XHTML) usata per creare le viste JSF.
  - **Ciclo di Vita JSF**: Processo a più fasi (es. Restore View, Apply Request Values, Validations, Update Model, Invoke Application) che gestisce le richieste.

---

## WebSocket (JSR 356)

- **WebSocket**
  Protocollo che permette una comunicazione full-duplex (bidirezionale) su una singola connessione TCP. Ideale per applicazioni in tempo reale (chat, notifiche).

- **Handshake**
  Processo iniziale basato su HTTP in cui un client chiede di "aggiornare" la connessione al protocollo WebSocket. Il server risponde con `101 Switching Protocols`.

- **Endpoint (`@ServerEndpoint`)**
  Una classe Java che gestisce le interazioni WebSocket. L'annotazione `@ServerEndpoint` la mappa a un URI (es. `/chat`).

- **Annotazioni del Ciclo di Vita**
  - `@OnOpen`: Chiamato all'apertura di una connessione.
  - `@OnMessage`: Chiamato alla ricezione di un messaggio.
  - `@OnClose`: Chiamato alla chiusura della connessione.
  - `@OnError`: Chiamato in caso di errore.

- **Session (`javax.websocket.Session`)**
  Oggetto che rappresenta la connessione con un singolo client. Permette di inviare messaggi (`getBasicRemote().sendText(...)`) e di memorizzare stato (`getUserProperties()`).

- **Encoder/Decoder**
  Meccanismo per convertire messaggi WebSocket (testo o binari) in oggetti Java personalizzati e viceversa, permettendo una gestione dei dati più strutturata.

---

## CDI -- Contexts and Dependency Injection

- **CDI (Contexts and Dependency Injection)**
  Standard per iniettare dipendenze e gestire il ciclo di vita dei bean. Versione in Java EE 7: CDI 1.1.

- **@Inject**
  Annotazione per iniettare una dipendenza.

- **@Qualifier**
  Permette di distinguere più implementazioni della stessa interfaccia.

- **@Named**
  Rende un bean accessibile in EL (Expression Language), utile in JSP/JSF.

- **Scope Annotations**
  Definiscono il ciclo di vita dei bean:
  - `@RequestScoped`: vive per la durata della richiesta.
  - `@SessionScoped`: vive per la sessione HTTP.
  - `@ApplicationScoped`: condiviso tra tutti gli utenti.
  - `@ConversationScoped`: ciclo di vita personalizzabile.

- **Interceptors (@Interceptor)**
  Permettono di eseguire logica trasversale (logging, sicurezza) attorno ai metodi.

- **Decorators (@Decorator)**
  Estendono il comportamento di un bean implementando la stessa interfaccia.

---

## Sicurezza

- **JAAS (Java Authentication and Authorization Service)**
  Framework di autenticazione/autorizzazione in Java EE.

- **Declarative Security**
  Sicurezza configurata con annotazioni o descrittori XML (`@RolesAllowed`, `@PermitAll`, `@DenyAll`).

- **Programmatic Security**
  Sicurezza gestita via codice (es. `request.isUserInRole("admin")`).

- **Principal**
  Rappresenta l'identità dell'utente autenticato.

- **Realm**
  Repository di utenti/ruoli (es. file, DB, LDAP).

---

## Transazioni

- **JTA (Java Transaction API)**
  API standard per la gestione delle transazioni distribuite. Versione in Java EE 7: JTA 1.2.

- **CMT (Container-Managed Transactions)**
  Transazioni gestite dal container. Annotazioni: `@Transactional`, `@TransactionAttribute`.

- **BMT (Bean-Managed Transactions)**
  Lo sviluppatore gestisce manualmente le transazioni tramite `UserTransaction`.

- **Propagation**
  Definisce come un metodo entra in una transazione esistente o ne crea una nuova (es. `REQUIRED`, `REQUIRES_NEW`, `MANDATORY`).

---

## Validazione

- **Bean Validation (JSR 349)**
  Framework per validazione di oggetti. Integrato con JPA, JSF, CDI. Versione in Java EE 7: Bean Validation 1.1.

- **Constraint Annotations**
  - `@NotNull` -- il campo non può essere nullo
  - `@Size(min, max)` -- lunghezza di stringhe/collezioni
  - `@Pattern(regex)` -- espressioni regolari
  - `@Past` / `@Future` -- validazione date

- **Custom Constraint**
  Creazione di validazioni personalizzate con `@Constraint` e un `ConstraintValidator`.

---

## Concorrenza e Timer

- **Concurrency Utilities (JSR 236)**
  API standard per concorrenza in Java EE. Fornisce thread pool gestiti dal container.

- **ManagedExecutorService**
  Interfaccia per eseguire task in thread gestiti.

- **@Asynchronous**
  Permette metodi asincroni su EJB o CDI bean.

- **Timer Service**
  Permette scheduling di operazioni tramite annotazioni come `@Schedule` o timer programmatici.

---

## Risorse e Configurazione

- **JNDI (Java Naming and Directory Interface)**
  API per cercare e registrare risorse (DB, JMS, EJB) tramite nomi logici.

- **DataSource**
  Oggetto JNDI per la connessione a DB gestito dal container.

- **Resource Injection (@Resource, @EJB, @PersistenceContext)**
  Meccanismo per iniettare risorse dichiarate in JNDI.

---

## Interoperabilità

- **JSON-P (Java API for JSON Processing)**
  API per parsing e scrittura JSON (streaming e object model).

- **Batch Processing (JSR 352)**
  API per job batch (lettura, elaborazione, scrittura di grandi volumi di dati). Basata su step e job XML.

- **Connector Architecture (JCA)**
  Standard per collegare Java EE a sistemi legacy o enterprise esterni (ERP, mainframe).

---

## Packaging e Deployment

- **META-INF / WEB-INF**
  Directory speciali per configurazioni (es. `web.xml`, `persistence.xml`).

- **Classloader Hierarchy**
  Struttura che definisce come le classi vengono caricate in un EAR (moduli isolati vs. condivisi).

- **Portable JNDI Names**
  Standard introdotto in Java EE 6/7 per rendere nomi JNDI portabili tra application server.

---

## Strumenti e Testing

- **Arquillian**
  Framework per testare applicazioni Java EE in container reali.

- **Embedded Container**
  Modalità di esecuzione leggera per testing senza deploy completo.
