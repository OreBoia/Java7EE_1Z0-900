# Quiz Avanzato su Business Logic EJB - Domande Miste con Codice

Questo quiz avanzato copre i concetti degli Enterprise JavaBeans (EJB) con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Tipi di Session Bean

### 🔵 Domanda 1

Osserva il seguente codice:

```java
@Stateless
public class CalcolatriceService {
    private int counter = 0;
    
    public int incrementa() {
        return ++counter;
    }
}
```

Cosa accadrà se due client diversi chiamano `incrementa()` consecutivamente?

- a) Il primo client otterrà 1, il secondo otterrà 2
- b) Entrambi i client potrebbero ottenere 1
- c) Si verificherà un errore di concorrenza
- d) Il comportamento è indefinito e non sicuro

---

### 🟢 Domanda 2

Quali delle seguenti affermazioni sui **Stateless Session Bean** sono corrette? (Seleziona tutte quelle corrette)

- a) Mantengono lo stato conversazionale per ogni client
- b) Sono gestiti in un pool di istanze dal container
- c) Sono thread-safe per natura
- d) Ogni client ha la propria istanza dedicata
- e) Sono ideali per servizi di accesso ai dati

---

### 💻 Domanda 3

Analizza il seguente Stateful Session Bean:

```java
@Stateful
public class CarrelloSpesa {
    private List<Prodotto> prodotti = new ArrayList<>();
    
    public void aggiungiProdotto(Prodotto p) {
        prodotti.add(p);
    }
    
    public List<Prodotto> getProdotti() {
        return prodotti;
    }
    
    @Remove
    public void finalizzaOrdine() {
        // Processo dell'ordine
        System.out.println("Ordine finalizzato con " + prodotti.size() + " prodotti");
    }
}
```

Cosa succede quando il client chiama `finalizzaOrdine()`?

- a) Il metodo viene eseguito e l'istanza rimane disponibile
- b) Il metodo viene eseguito e l'istanza viene distrutta
- c) Il carrello viene svuotato ma l'istanza rimane attiva
- d) Si verifica un errore perché @Remove non può essere usato sui metodi

---

### 🔵 Domanda 4

Quale tipo di Session Bean è più appropriato per implementare un **wizard multi-step**?

- a) Stateless Session Bean
- b) Stateful Session Bean
- c) Singleton Session Bean
- d) Message-Driven Bean

---

## 2. Ciclo di Vita e Callback

### 💻 Domanda 5

Osserva questo codice e identifica l'ordine di esecuzione dei callback:

```java
@Stateless
public class ServizioConfig {
    private Properties config;
    
    @PostConstruct
    public void init() {
        System.out.println("1. Inizializzazione");
        config = new Properties();
        // Caricamento configurazione
    }
    
    public String getProperty(String key) {
        System.out.println("2. Metodo business");
        return config.getProperty(key);
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("3. Pulizia");
        config = null;
    }
}
```

Per una nuova istanza nel pool, quale sarà l'ordine di output al primo utilizzo?

- a) 1 → 2 → 3
- b) 2 → 1 → 3
- c) 1 → 2
- d) 2 → 3

---

### 🟢 Domanda 6

Quali dei seguenti callback sono disponibili per gli **Stateful Session Bean**? (Seleziona tutti)

- a) `@PostConstruct`
- b) `@PreDestroy`
- c) `@PrePassivate`
- d) `@PostActivate`
- e) `@PreUpdate`

---

### 💻 Domanda 7

Analizza questo Stateful Session Bean e identifica quando viene chiamato `@PrePassivate`:

```java
@Stateful
public class SessioneUtente {
    private String username;
    private Date lastActivity;
    
    @PostConstruct
    public void init() {
        lastActivity = new Date();
    }
    
    @PrePassivate
    public void preparaPassivazione() {
        System.out.println("Bean passivato per: " + username);
        // Serializzazione preparazione
    }
    
    @PostActivate
    public void dopoAttivazione() {
        System.out.println("Bean riattivato per: " + username);
    }
    
    public void updateActivity() {
        lastActivity = new Date();
    }
}
```

Quando viene invocato `preparaPassivazione()`?

- a) Prima della distruzione definitiva del bean
- b) Prima che il bean venga serializzato su disco per liberare memoria
- c) Ad ogni chiamata di metodo business
- d) Solo quando il client chiama esplicitamente un metodo @Remove

---

## 3. Gestione delle Transazioni

### 🔵 Domanda 8

Quale `TransactionAttributeType` rappresenta il comportamento **di default** per i metodi EJB?

- a) `NEVER`
- b) `REQUIRED`
- c) `REQUIRES_NEW`
- d) `SUPPORTS`

---

### 💻 Domanda 9

Analizza questo codice transazionale:

```java
@Stateless
public class ServizioContabilita {
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void trasferisciDenaro(Long daAccount, Long aAccount, BigDecimal importo) {
        // Operazione 1: Prelievo
        prelieva(daAccount, importo);
        
        // Operazione 2: Deposito  
        deposita(aAccount, importo);
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logOperazione(String operazione) {
        // Log dell'operazione
        System.out.println("Log: " + operazione);
    }
}
```

Se `deposita()` lancia un'eccezione in `trasferisciDenaro()`, cosa succede?

- a) Solo il deposito viene annullato, il prelievo rimane
- b) Sia prelievo che deposito vengono annullati (rollback completo)
- c) L'operazione continua ignorando l'errore
- d) Solo il prelievo viene annullato

---

### 🟢 Domanda 10

Quali dei seguenti `TransactionAttributeType` **NON richiedono** una transazione attiva dal chiamante? (Seleziona tutti)

- a) `REQUIRED`
- b) `REQUIRES_NEW`
- c) `MANDATORY`
- d) `NOT_SUPPORTED`
- e) `NEVER`

---

### 💻 Domanda 11

Osserva questo scenario di gestione eccezioni:

```java
@Stateless
public class ServizioOrdini {
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processaOrdine(Ordine ordine) throws BusinessException {
        try {
            validaOrdine(ordine);
            salvaOrdine(ordine);
            inviaMail(ordine);
        } catch (ValidationException e) {
            // Exception checked - business logic error
            throw new BusinessException("Ordine non valido", e);
        } catch (RuntimeException e) {
            // Sistema error - re-throw
            throw e;
        }
    }
}
```

Se `inviaMail()` lancia una `RuntimeException`, cosa succede alla transazione?

- a) La transazione viene committata perché l'eccezione è gestita
- b) La transazione viene annullata automaticamente (rollback)
- c) La transazione rimane in stato sospeso
- d) Dipende dal tipo specifico di RuntimeException

---

## 4. Bean-Managed Transactions (BMT)

### 💻 Domanda 12

Analizza questo codice BMT:

```java
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ServizioComplesso {
    
    @Resource
    private UserTransaction userTransaction;
    
    public void operazioneComplessa() throws Exception {
        userTransaction.begin();
        
        try {
            // Operazione 1
            operazioneA();
            
            // Checkpoint intermedio
            userTransaction.commit();
            userTransaction.begin();
            
            // Operazione 2
            operazioneB();
            
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
    }
}
```

Quale vantaggio offre questo approccio rispetto al CMT?

- a) Migliori performance automatiche
- b) Controllo granulare su più transazioni in un singolo metodo
- c) Gestione automatica degli errori
- d) Thread-safety migliorata

---

## 5. Metodi Asincroni

### 🔵 Domanda 13

Osserva questo metodo asincrono:

```java
@Stateless
public class ServizioElaborazione {
    
    @Asynchronous
    public void processaBatch() {
        // Elaborazione lunga 10 minuti
        System.out.println("Elaborazione completata");
    }
}
```

Quando torna il controllo al client che chiama `processaBatch()`?

- a) Dopo 10 minuti quando l'elaborazione è completata
- b) Immediatamente, prima che inizi l'elaborazione
- c) Solo se l'elaborazione ha successo
- d) Dipende dal tipo di client

---

### 💻 Domanda 14

Analizza questo servizio asincrono che restituisce un Future:

```java
@Stateless
public class ServizioReporting {
    
    @Asynchronous
    public Future<String> generaReport(int giorni) {
        try {
            // Simulazione elaborazione
            Thread.sleep(giorni * 1000);
            String report = "Report per " + giorni + " giorni generato";
            return new AsyncResult<>(report);
        } catch (InterruptedException e) {
            return new AsyncResult<>(null);
        }
    }
}

// Client usage
Future<String> futureReport = servizio.generaReport(5);
String result = futureReport.get(); // Questo blocca il client
```

Cosa fa `futureReport.get()`?

- a) Restituisce immediatamente null se il task non è completato
- b) Blocca il thread corrente fino al completamento del task
- c) Avvia l'esecuzione del metodo asincrono
- d) Annulla l'operazione in corso

---

### 🟢 Domanda 15

Quali dei seguenti tipi di ritorno sono validi per metodi `@Asynchronous`? (Seleziona tutti)

- a) `void`
- b) `Future<String>`
- c) `String`
- d) `Future<Integer>`
- e) `CompletableFuture<Object>`

---

## 6. Timer Service

### 💻 Domanda 16

Osserva questo EJB Timer:

```java
@Stateless
public class ServizioManutenzioneProgrammata {
    
    @Resource
    private TimerService timerService;
    
    @PostConstruct
    public void init() {
        // Crea un timer che si attiva ogni ora
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("*").minute("0").second("0");
        
        timerService.createCalendarTimer(schedule, 
            new TimerConfig("pulizia-cache", false));
    }
    
    @Timeout
    public void eseguiPulizia(Timer timer) {
        System.out.println("Pulizia cache eseguita: " + timer.getInfo());
    }
}
```

Quando viene eseguito il metodo `eseguiPulizia()`?

- a) Una sola volta al primo minuto di ogni ora
- b) Ogni minuto
- c) Al primo secondo di ogni ora
- d) Solo quando l'applicazione viene avviata

---

### 🔵 Domanda 17

Quale annotazione identifica il metodo che viene chiamato quando scade un timer EJB?

- a) `@Schedule`
- b) `@Timer`
- c) `@Timeout`
- d) `@Trigger`

---

### 💻 Domanda 18

Analizza questo timer programmatico:

```java
@Stateless
public class ServizioPromemoria {
    
    @Resource
    private TimerService timerService;
    
    public void creaPromemoria(String messaggio, long millisecondi) {
        TimerConfig config = new TimerConfig(messaggio, false);
        timerService.createSingleActionTimer(millisecondi, config);
    }
    
    @Timeout
    public void promemoria(Timer timer) {
        String messaggio = (String) timer.getInfo();
        System.out.println("Promemoria: " + messaggio);
    }
}
```

Se il client chiama `creaPromemoria("Meeting", 5000)`, cosa succede?

- a) Il messaggio viene stampato immediatamente
- b) Il messaggio viene stampato dopo 5 secondi, poi il timer si ripete ogni 5 secondi
- c) Il messaggio viene stampato una sola volta dopo 5 secondi
- d) Si verifica un errore perché il timer non è persistente

---

## 7. Interceptor

### 💻 Domanda 19

Osserva questo interceptor:

```java
@Interceptor
@Logged
public class LoggingInterceptor {
    
    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        
        try {
            System.out.println("Inizio chiamata: " + ctx.getMethod().getName());
            Object result = ctx.proceed();
            System.out.println("Fine chiamata: " + ctx.getMethod().getName());
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("Durata: " + duration + "ms");
        }
    }
}

@Stateless
@Logged
public class ServizioUtente {
    public void creaUtente(String nome) {
        System.out.println("Creazione utente: " + nome);
    }
}
```

Quale sarà l'output quando si chiama `creaUtente("Mario")`?

- a) Solo "Creazione utente: Mario"
- b) "Inizio chiamata: creaUtente" → "Creazione utente: Mario" → "Fine chiamata: creaUtente" → "Durata: XYZms"
- c) "Creazione utente: Mario" → "Durata: XYZms"
- d) L'interceptor non viene eseguito

---

### 🟢 Domanda 20

Quali tipi di interceptor sono disponibili in EJB? (Seleziona tutti)

- a) `@AroundInvoke`
- b) `@PostConstruct`
- c) `@PreDestroy`  
- d) `@AroundTimeout`
- e) `@AroundConstruct`

---

## 8. Integrazione EJB-CDI

### 💻 Domanda 21

Analizza questa integrazione EJB-CDI:

```java
@Stateless
public class ServizioNotifica {
    
    @Inject
    @Email
    private NotificationChannel emailChannel;
    
    @Inject
    @SMS  
    private NotificationChannel smsChannel;
    
    public void inviaNotifia(String messaggio, TipoNotifica tipo) {
        switch(tipo) {
            case EMAIL:
                emailChannel.send(messaggio);
                break;
            case SMS:
                smsChannel.send(messaggio);
                break;
        }
    }
}

@Email
@ApplicationScoped
public class EmailNotificationChannel implements NotificationChannel {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
```

Quale vantaggio offre l'uso di CDI qualifiers (`@Email`, `@SMS`) in questo scenario?

- a) Migliori performance di runtime
- b) Dependency injection type-safe con disambiguazione
- c) Gestione automatica delle transazioni
- d) Thread-safety migliorata

---

### 🔵 Domanda 22

In un EJB, quale scope CDI è più appropriato per un bean che deve mantenere stato tra chiamate dello stesso client?

- a) `@RequestScoped`
- b) `@SessionScoped`
- c) `@ApplicationScoped`
- d) `@ConversationScoped`

---

## 9. Sicurezza negli EJB

### 💻 Domanda 23

Osserva questa configurazione di sicurezza:

```java
@Stateless
@RolesAllowed({"admin", "manager"})
public class ServizioAmministrazione {
    
    @PermitAll
    public String getInfo() {
        return "Informazioni generali";
    }
    
    @RolesAllowed("admin")
    public void eliminaUtente(Long userId) {
        // Solo admin può eliminare utenti
    }
    
    @DenyAll
    public void metodoProibito() {
        // Nessuno può chiamare questo metodo
    }
}
```

Se un utente con ruolo "manager" chiama `eliminaUtente()`, cosa succede?

- a) Il metodo viene eseguito con successo
- b) Viene lanciata una `SecurityException`
- c) Il metodo viene eseguito ma genera un warning
- d) L'accesso viene negato silenziosamente

---

### 🟢 Domanda 24

Quali annotazioni di sicurezza sono disponibili per gli EJB? (Seleziona tutte)

- a) `@RolesAllowed`
- b) `@PermitAll`
- c) `@DenyAll`
- d) `@Secured`
- e) `@RunAs`

---

## 10. Lookup e Dependency Injection

### 💻 Domanda 25

Analizza questi modi diversi di accedere agli EJB:

```java
// Opzione 1: Injection
@EJB
private CalcolatriceService calc1;

// Opzione 2: JNDI Lookup
@Resource
private SessionContext sessionContext;

public void metodo1() {
    try {
        CalcolatriceService calc2 = (CalcolatriceService) 
            sessionContext.lookup("java:module/CalcolatriceService");
    } catch (Exception e) {
        // gestione errore
    }
}

// Opzione 3: CDI Injection
@Inject
private CalcolatriceService calc3;
```

Quale approccio è più appropriato per un'applicazione Java EE moderna?

- a) Opzione 1 (@EJB injection) - è lo standard EJB
- b) Opzione 2 (JNDI lookup) - offre massima flessibilità
- c) Opzione 3 (@Inject CDI) - è l'approccio moderno raccomandato
- d) Tutti e tre sono equivalenti in termini di performance

---

## 11. Session Bean Remoti vs Locali

### 🔵 Domanda 26

Osserva questa configurazione:

```java
@Remote
public interface CalcolatriceRemote {
    int somma(int a, int b);
}

@Local
public interface CalcolatriceLocal {
    int moltiplicazione(int a, int b);
}

@Stateless
public class CalcolatriceImpl implements CalcolatriceRemote, CalcolatriceLocal {
    
    public int somma(int a, int b) {
        return a + b;
    }
    
    public int moltiplicazione(int a, int b) {
        return a * b;
    }
}
```

Quale differenza principale esiste tra le chiamate remote e locali?

- a) Le chiamate remote sono sempre più veloci
- b) Le chiamate locali passano parametri per riferimento, le remote per valore
- c) Le chiamate locali non supportano transazioni
- d) Non c'è differenza funzionale

---

## 12. Gestione delle Eccezioni

### 💻 Domanda 27

Analizza questo codice di gestione eccezioni:

```java
@Stateless
public class ServizioValidazione {
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void validaEProcessa(Documento doc) throws ValidationException {
        try {
            if (!doc.isValid()) {
                throw new ValidationException("Documento non valido");
            }
            
            salvaDocumento(doc);
            
        } catch (SQLException e) {
            // Database error - system exception
            throw new EJBException("Errore di sistema", e);
        }
    }
}
```

Quale eccezione causa il rollback automatico della transazione?

- a) Solo `ValidationException` perché è checked
- b) Solo `EJBException` perché è runtime
- c) Entrambe le eccezioni
- d) Nessuna delle due

---

## 13. Performance e Best Practices

### 🟢 Domanda 28

Quali delle seguenti sono **best practices** per la performance degli EJB? (Seleziona tutte)

- a) Usare Stateless Session Bean quando possibile
- b) Minimizzare il numero di chiamate remote
- c) Usare @Asynchronous per operazioni lunghe
- d) Evitare transazioni lunghe
- e) Usare sempre Bean-Managed Transactions per controllo migliore

---

### 💻 Domanda 29

Osserva questo pattern di Service Facade:

```java
@Stateless
public class OrderServiceFacade {
    
    @EJB
    private ProductService productService;
    
    @EJB  
    private CustomerService customerService;
    
    @EJB
    private InventoryService inventoryService;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderResult processOrder(OrderRequest request) {
        // Un'unica transazione per tutta l'operazione
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Product product = productService.findProduct(request.getProductId());
        
        boolean available = inventoryService.checkAvailability(
            request.getProductId(), request.getQuantity());
            
        if (available) {
            inventoryService.reserve(request.getProductId(), request.getQuantity());
            return new OrderResult(true, "Order processed");
        } else {
            return new OrderResult(false, "Product not available");
        }
    }
}
```

Quale pattern architetturale implementa questo codice?

- a) Data Access Object (DAO)
- b) Service Facade
- c) Business Delegate
- d) Session Facade

---

### 🔵 Domanda 30

In una applicazione con molti EJB Stateful, quale problema di performance può emergere?

- a) Memory leak dovuto al pooling inadeguato
- b) Contesa sui thread per l'accesso concorrente
- c) Consumo eccessivo di memoria per mantenere lo stato
- d) Deadlock nelle transazioni distribuite

---

## 11. Domande Avanzate - Scenari Complessi

### 💻 Domanda 31

Analizza questo scenario di EJB con Timer e Transazioni:

```java
@Singleton
@Startup
public class ServizioMonitoraggio {
    
    @Resource
    private TimerService timerService;
    
    @EJB
    private ServizioLog logService;
    
    @PostConstruct
    public void init() {
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.second("*/30"); // Ogni 30 secondi
        timerService.createCalendarTimer(schedule);
    }
    
    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void monitoraSystem(Timer timer) {
        try {
            // Controllo sistema
            SystemStatus status = checkSystemHealth();
            logService.logStatus(status);
        } catch (Exception e) {
            // Se il log fallisce, non vogliamo interrompere il monitoraggio
            System.err.println("Errore nel log: " + e.getMessage());
        }
    }
}
```

Perché è stato usato `REQUIRES_NEW` per il metodo `@Timeout`?

- a) Per migliorare le performance del timer
- b) Per isolare gli errori di log dal ciclo di monitoraggio principale
- c) È obbligatorio per i metodi @Timeout
- d) Per permettere l'accesso concorrente al timer

---

### 💻 Domanda 32

Osserva questo pattern di comunicazione asincrona tra EJB:

```java
@Stateless
public class OrderProcessor {
    
    @EJB
    private PaymentService paymentService;
    
    @EJB
    private NotificationService notificationService;
    
    @Asynchronous
    public Future<OrderResult> processOrderAsync(Order order) {
        try {
            // Step 1: Process payment
            PaymentResult payment = paymentService.processPayment(order);
            
            if (payment.isSuccessful()) {
                // Step 2: Send notification (fire-and-forget)
                notificationService.sendConfirmationAsync(order);
                
                return new AsyncResult<>(new OrderResult(true, "Order completed"));
            } else {
                return new AsyncResult<>(new OrderResult(false, "Payment failed"));
            }
        } catch (Exception e) {
            return new AsyncResult<>(new OrderResult(false, "Error: " + e.getMessage()));
        }
    }
}

@Stateless
public class NotificationService {
    
    @Asynchronous
    public void sendConfirmationAsync(Order order) {
        // Send email confirmation (può fallire senza impattare l'ordine)
        try {
            emailService.send(order.getCustomerEmail(), "Order confirmed");
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
```

Quale vantaggio offre questo design asincrono?

- a) Il processo dell'ordine non viene bloccato dall'invio dell'email
- b) Migliore gestione delle transazioni distribuite
- c) Riduzione del consumo di memoria
- d) Thread-safety automatica

---

---

## Risposte Corrette

### 1. **b)** Entrambi i client potrebbero ottenere 1

In un Stateless Bean, le istanze sono condivise da un pool. La variabile `counter` non è affidabile per mantenere stato tra client diversi.

### 2. **b, c, e)** Sono gestiti in un pool, sono thread-safe, ideali per servizi dati

I Stateless Bean non mantengono stato conversazionale e non hanno istanze dedicate per client.

### 3. **b)** Il metodo viene eseguito e l'istanza viene distrutta

`@Remove` indica che dopo l'esecuzione del metodo, l'istanza del Stateful Bean deve essere distrutta.

### 4. **b)** Stateful Session Bean

Un wizard multi-step richiede di mantenere lo stato tra le varie fasi, caratteristica dei Stateful Bean.

### 5. **c)** 1 → 2

Per i Stateless Bean, `@PostConstruct` viene chiamato alla creazione, poi i metodi business. `@PreDestroy` solo alla distruzione.

### 6. **a, b, c, d)** `@PostConstruct`, `@PreDestroy`, `@PrePassivate`, `@PostActivate`

`@PreUpdate` non esiste nel ciclo di vita EJB.

### 7. **b)** Prima che il bean venga serializzato su disco per liberare memoria

`@PrePassivate` viene chiamato quando il container passiva il bean per gestire la memoria.

### 8. **b)** `REQUIRED`

`REQUIRED` è il comportamento transazionale di default per tutti i metodi EJB.

### 9. **b)** Sia prelievo che deposito vengono annullati (rollback completo)

Con `REQUIRED`, tutto il metodo è in una singola transazione, quindi l'eccezione causa rollback completo.

### 10. **a, b, d, e)** `REQUIRED`, `REQUIRES_NEW`, `NOT_SUPPORTED`, `NEVER`

`MANDATORY` richiede una transazione esistente dal chiamante.

### 11. **b)** La transazione viene annullata automaticamente (rollback)

Le RuntimeException causano automaticamente il rollback delle transazioni CMT.

### 12. **b)** Controllo granulare su più transazioni in un singolo metodo

BMT permette di gestire manualmente transazioni multiple all'interno dello stesso metodo.

### 13. **b)** Immediatamente, prima che inizi l'elaborazione

I metodi `@Asynchronous` restituiscono immediatamente il controllo al client.

### 14. **b)** Blocca il thread corrente fino al completamento del task

`Future.get()` è una chiamata bloccante che attende il risultato.

### 15. **a, b, d)** `void`, `Future<String>`, `Future<Integer>`

I metodi asincroni possono restituire solo `void` o `Future<T>`. `CompletableFuture` non è supportato direttamente.

### 16. **c)** Al primo secondo di ogni ora

`hour("*").minute("0").second("0")` significa ogni ora al minuto 0, secondo 0.

### 17. **c)** `@Timeout`

`@Timeout` identifica il metodo callback per i timer EJB.

### 18. **c)** Il messaggio viene stampato una sola volta dopo 5 secondi

`createSingleActionTimer` crea un timer che si attiva una sola volta.

### 19. **b)** "Inizio chiamata: creaUtente" → "Creazione utente: Mario" → "Fine chiamata: creaUtente" → "Durata: XYZms"

L'interceptor wrappa la chiamata al metodo con il logging.

### 20. **a, b, c, d)** `@AroundInvoke`, `@PostConstruct`, `@PreDestroy`, `@AroundTimeout`

`@AroundConstruct` non esiste negli interceptor EJB.

### 21. **b)** Dependency injection type-safe con disambiguazione

I qualifiers CDI permettono di iniettare l'implementazione corretta in modo type-safe.

### 22. **b)** `@SessionScoped`

Per mantenere stato tra chiamate dello stesso client si usa `@SessionScoped`.

### 23. **b)** Viene lanciata una `SecurityException`

Il metodo richiede ruolo "admin" ma l'utente ha solo "manager".

### 24. **a, b, c, e)** `@RolesAllowed`, `@PermitAll`, `@DenyAll`, `@RunAs`

`@Secured` è un'annotazione Spring Security, non Java EE.

### 25. **c)** Opzione 3 (@Inject CDI) - è l'approccio moderno raccomandato

CDI è lo standard moderno per dependency injection in Java EE.

### 26. **b)** Le chiamate locali passano parametri per riferimento, le remote per valore

Questa è la differenza fondamentale tra interfacce locali e remote.

### 27. **b)** Solo `EJBException` perché è runtime

Solo le RuntimeException (incluse EJBException) causano rollback automatico in CMT.

### 28. **a, b, c, d)** Usare Stateless quando possibile, minimizzare chiamate remote, usare @Asynchronous, evitare transazioni lunghe

BMT non è necessariamente migliore per performance.

### 29. **d)** Session Facade

Il pattern coordina più servizi in un'unica operazione transazionale.

### 30. **c)** Consumo eccessivo di memoria per mantenere lo stato

I Stateful Bean mantengono istanze dedicate che consumano memoria.

### 31. **b)** Per isolare gli errori di log dal ciclo di monitoraggio principale

`REQUIRES_NEW` garantisce che gli errori nel log non interrompano il timer di monitoraggio.

### 32. **a)** Il processo dell'ordine non viene bloccato dall'invio dell'email

L'asincronismo permette di completare l'ordine senza attendere operazioni secondarie come le notifiche.
