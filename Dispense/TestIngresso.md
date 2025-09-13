# Test di ingresso — Java EE 7 (stile 1Z0-900)

## Istruzioni

- Alcune domande richiedono più risposte corrette (indicato).
- Non sono necessarie `import` o `package`, salvo dove indicate nello snippet.
- Scegli la risposta più corretta quando più opzioni sembrano plausibili.

---

## Sezione A — Fondamenti Piattaforma & Packaging

1. **Quale affermazione descrive correttamente Java EE rispetto a Java SE?**
    A. Java EE è un JDK alternativo che sostituisce Java SE
    B. Java EE aggiunge specifiche per sviluppo enterprise (servizi web, componenti, transazioni) basate su Java SE
    C. Java EE è un runtime proprietario non compatibile con Java SE
    D. Java EE è solo un insieme di IDE e plugin

2. **In un archivio `.ear`, quali moduli sono tipicamente contenuti? (seleziona 2)**
    A. Moduli `.war`
    B. Moduli `.jar` EJB
    C. Moduli `.class` sciolti nella root
    D. Moduli `.apk`

3. **Dove si dichiara di norma un DataSource JNDI per un’app Java EE standard?**
    A. Dentro `web.xml` del WAR
    B. Nella configurazione del server/app server e referenziato via JNDI
    C. In un file `.properties` letto da CDI
    D. In un file `manifest.mf` del JAR

4. **Quale tecnologia non è parte di Java EE 7?**
    A. JAX-RS 2.0
    B. WebSocket 1.0
    C. JSP 2.x / Servlet 3.1
    D. Spring MVC

---

## Sezione B — Servlets, JSF, Web tier

1. **In Servlet 3.1, qual è un vantaggio delle annotazioni rispetto a `web.xml`?**
    A. Le annotazioni permettono hot-reload automatico
    B. Consente di dichiarare servlet, filtri e mapping senza `web.xml`
    C. Le annotazioni eseguono la servlet in modalità asincrona per default
    D. Disabilitano i filtri

2. **In JSF 2.x su Java EE 7, un bean con `@Named` e `@RequestScoped` è:**
    A. Un managed bean JSF legacy, non CDI
    B. Un CDI bean accessibile in EL
    C. Necessita sempre di `faces-config.xml`
    D. Non supportato

3. **In JSF, l’attributo `immediate="true"` su un componente di input:**
    A. Disabilita la validazione
    B. Esegue conversione/validazione e eventi prima degli altri componenti
    C. Rimanda la validazione alla prossima richiesta
    D. Esegue l’action listener in fase Render Response

---

## Sezione C — CDI ed EJB

1. **In CDI, i Qualifier servono a:**
    A. Definire nuovi scope
    B. Distinguere più implementazioni dello stesso tipo in injection point
    C. Limitare l’uso di `@Inject` ai soli EJB
    D. Gestire la concorrenza tra thread

2. **Quale combinazione è valida per un produttore CDI?**
    A. `@Produces` su campo o metodo, opzionalmente con Qualifier
    B. `@Produces` può essere usato solo su costruttori
    C. `@Produces` richiede sempre `@Alternative`
    D. `@Produces` è deprecato in EE7

3. **In EJB, quale è vero?**
    A. `@Stateless` non può usare transazioni container-managed
    B. `@Singleton` può avere `@Lock(WRITE)` per sezioni critiche
    C. `@Stateful` non mantiene mai stato tra invocazioni
    D. EJB non supportano injection di `@Resource` DataSource

4. **In EJB Timer Service, per un timer calendar-based si usa:**
    A. `@Schedule(...)`
    B. `@Timeout` su metodo senza schedule
    C. `@Asynchronous`
    D. `@Lock(READ)`

---

## Sezione D — JPA 2.1

1. **In JPA, la chiave primaria generata dal DB si mappa con:**
    A. `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
    B. `@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)` solo
    C. `@Id` senza `@GeneratedValue`
    D. `@GeneratedValue` senza `@Id`

2. **In JPA, la differenza principale tra `EntityManager.persist()` e `merge()` è:**
    A. `persist` salva e detacha, `merge` non salva
    B. `persist` rende managed un’istanza nuova; `merge` copia lo stato in un’istanza managed e restituisce il riferimento managed
    C. `persist` funziona solo fuori transazione
    D. Non c’è differenza

3. **In una relazione OneToMany bidirezionale (Parent-Children), il lato proprietario è di solito:**
    A. Il lato `@OneToMany`
    B. Il lato `@ManyToOne` con la `@JoinColumn`
    C. Entrambi
    D. Nessuno

4. **Quale locking previene aggiornamenti concorrenti sulla stessa riga durante la transazione corrente?**
    A. Optimistic senza versione
    B. Optimistic con `@Version`
    C. Pessimistic `LockModeType.PESSIMISTIC_WRITE`
    D. `LockModeType.NONE`

5. **Dato:**

```java
@Entity
public class User {
  @Id Long id;
  @NotNull String email;
}
```

    **Quando viene applicata la validazione Bean Validation su `email` durante `persist()`?**
    A. In `flush/commit` della transazione (pre-dipersist)
    B. Solo in `merge()`
    C. Mai automaticamente
    D. Solo su chiamata esplicita al `Validator`

---

## Sezione E — JAX-RS 2.0 (REST)

1. **Quale è un client JAX-RS 2.0 valido?**
    A. `Client client = ClientBuilder.newClient();`
    B. `Client client = new Client();`
    C. `Client client = ClientFactory.create();`
    D. `WebTarget target = new WebTarget();`

2. **In una risorsa:**

```java
@Path("/items")
public class ItemResource {
  @GET
  @Path("{id}")
  public Response get(@PathParam("id") long id) { ... }
}
```

    **L’URI per ottenere l’item 5 è:**
    A. `/items?id=5`
    B. `/items/5`
    C. `/items#get/5`
    D. `/items?idPath=5`

3.  **Per inviare/consumare JSON in JAX-RS si usa tipicamente: (seleziona 2)**
    A. `@Produces(MediaType.APPLICATION_JSON)`
    B. `@Consumes(MediaType.APPLICATION_JSON)`
    C. `@FormParam`
    D. `@MatrixParam`

4. **Un filtro client JAX-RS si registra:**
    A. Nel `web.xml`
    B. Tramite `ClientRequestFilter` e `Client.register(...)`
    C. Con `@Provider` solo lato server
    D. Non esistono filtri sul client

---

## Sezione F — WebSocket 1.0

1. **Un endpoint server annotato correttamente è:**
    A. `@ServerEndpoint("/chat")` su una classe POJO con metodi `@OnOpen`, `@OnMessage`, `@OnClose`
    B. `@WebServlet("/chat")`
    C. `@Path("/chat")`
    D. `@MessageDriven("/chat")`

2. **`Session.getAsyncRemote()` serve a:**
    A. Ottenere una connessione REST asincrona
    B. Inviare messaggi WebSocket in modo non bloccante
    C. Aprire la sessione HTTP
    D. Riconnettere automaticamente il client

---

## Sezione G — JMS 2.0 (Simplified API)

1. **La Simplified API JMS 2.0 consente di inviare un messaggio con:**
    A. `context.createProducer().send(queue, "hello");`
    B. `session.createSender(queue).send("hello");`
    C. `producer.send("hello")` senza destinazione
    D. `MessageProducer mp = new MessageProducer("queue"); mp.send("hello");`

2. **Per ricevere in modo sincrono un messaggio testo:**
    A. `consumer.receiveBody(String.class)`
    B. `consumer.onMessage(m -> ...)`
    C. `listener.onText(...)`
    D. `context.subscribe(String.class)`

---

## Sezione H — Bean Validation 1.1

1. **Quale è vero su Bean Validation 1.1 in Java EE 7?**
    A. Si integra con JAX-RS e JPA; le violazioni possono emergere a runtime su invocazioni risorsa e al commit JPA
    B. È disponibile solo fuori dai container
    C. Non supporta validazioni custom
    D. Non è integrabile con CDI

---

## Sezione I — Concurrency Utilities for EE (JSR-236)

1. **Quale affermazione è corretta sui thread in EE?**
    A. È sempre lecito creare thread con `new Thread()` in un WAR
    B. Bisogna usare `ManagedExecutorService`/`ManagedThreadFactory` forniti dal container
    C. Non è possibile eseguire task in background
    D. Solo EJB possono creare thread

2. **Un modo standard per pianificare un task è:**
    A. `ManagedScheduledExecutorService.schedule(...)`
    B. `Timer t = new Timer();`
    C. `Executors.newScheduledThreadPool(1)`
    D. `Thread.sleep(...)`

---

## Sezione J — Batch Applications (JSR-352)

1. **Un job batch è definito in:**
    A. `batch.xml` globale
    B. `META-INF/batch-jobs/<job-name>.xml`
    C. `WEB-INF/jobs.xml`
    D. `jobs.json`

2. **Gli step batch composti da ItemReader/ItemProcessor/ItemWriter sono:**
    A. Chunk-oriented step
    B. Tasklet step
    C. Timer step
    D. Web step

3. **Per avviare un job batch da codice:**
    A. `JobOperator.start("jobName", new Properties());`
    B. `Batch.start("jobName")`
    C. `JobRepository.run("jobName")`
    D. `JobRunner.execute("jobName")`

---

## Sezione K — JSON-P 1.0

1. **Per costruire un JSON oggetto con JSON-P:**
    A. `Json.createObjectBuilder().add("k","v").build()`
    B. `new JSONObject().put("k","v")` (org.json)
    C. `ObjectMapper().createObjectNode()` (Jackson)
    D. `Gson().toJsonTree(...)` (Gson)

2. **Per leggere uno stream JSON con JSON-P si usa:**
    A. `JsonReader`
    B. `ObjectInputStream`
    C. `JsonUnmarshaller` JAX-B
    D. `Scanner` su `InputStream`

---

## Sezione L — Sicurezza (JAAS/JASPIC, web security)

1. **In un’app web Java EE 7, la protezione di una risorsa URL via declarative security avviene in: (seleziona 2)**
    A. `web.xml` con `<security-constraint>`
    B. Annotazione `@ServletSecurity` sulla Servlet
    C. `persistence.xml`
    D. `beans.xml`

2. **JASPIC (Java Authentication SPI for Containers) serve per:**
    A. Definire interceptor REST
    B. Integrare meccanismi di autenticazione custom a livello di container/app server
    C. Configurare HTTPS su connettore
    D. Firmare messaggi JMS

---

## Sezione M — Misc / Best-practice

1. **Quale è consigliato per disaccoppiare componenti e facilitare il testing in Java EE 7?**
    A. Dipendenze hard-coded con `new`
    B. CDI per injection e scoping
    C. Usare singletons statici ovunque
    D. Business logic nelle JSP

---

## Esempi “stile esame” con snippet (breve sezione bonus)

1. **(risposte multiple) Cosa succede chiamando `em.remove(entity)` su un’istanza detached?**
    A. L’entità viene marcata come `REMOVED` e `delete` al commit
    B. Lancia `IllegalArgumentException`
    C. Viene prima eseguito un `merge` implicito
    D. Non succede nulla

2. **Dato un produttore CDI:**

    ```java
    public class Producers {
      @Produces @Fast
      public Service fastService() { ... }
    }
    ```

    **Come inietti correttamente?**
    A. `@Inject Service s;`
    B. `@Inject @Fast Service s;`
    C. `@Resource @Fast Service s;`
    D. `@EJB @Fast Service s;`

3. **In JAX-RS, come forzi il cache control su risposta GET?**
    A. `return Response.ok(data).header("Cache-Control","max-age=60").build();`
    B. `@Cacheable(60)` sull’endpoint
    C. `@Produces("cache/60")`
    D. `ClientConfig.setCache(60)`

4. **In WebSocket, dove non è raccomandato accedere a risorse non thread-safe condivise?**
    A. In `@OnOpen`
    B. In `@OnMessage` senza sincronizzazione/serializzazione
    C. In `@OnClose`
    D. In costruttore

5. **In Batch JSR-352, cosa fa un ItemProcessor?**
    A. Legge i record dalla sorgente
    B. Trasforma/valida ogni item tra lettura e scrittura
    C. Scrive i record a destinazione
    D. Pianifica l’esecuzione del job

---

## Griglia di correzione (chiave risposte)

- **Sezione A:** 1:B, 2:A,B, 3:B, 4:D
- **Sezione B:** 1:B, 2:B, 3:B
- **Sezione C:** 1:B, 2:A, 3:B, 4:A
- **Sezione D:** 1:A, 2:B, 3:B, 4:C, 5:A
- **Sezione E:** 1:A, 2:B, 3:A,B, 4:B
- **Sezione F:** 1:A, 2:B
- **Sezione G:** 1:A, 2:A
- **Sezione H:** 1:A
- **Sezione I:** 1:B, 2:A
- **Sezione J:** 1:B, 2:A, 3:A
- **Sezione K:** 1:A, 2:A
- **Sezione L:** 1:A,B, 2:B
- **Sezione M:** 1:B
- **Bonus:** 1:B, 2:B, 3:A, 4:B, 5:B
