# Quiz Avanzato su Business Logic EJB - Serie 2

Questo quiz avanzato copre i concetti degli Enterprise JavaBeans (EJB) con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Session Bean e Thread Safety

### 💻 Domanda 1

Osserva il seguente codice di un Stateless Session Bean:

```java
@Stateless
public class DataProcessorService {
    private static Map<String, Object> cache = new ConcurrentHashMap<>();
    private StringBuilder buffer = new StringBuilder();
    
    public String processData(String input) {
        buffer.append(input).append("-processed");
        String result = buffer.toString();
        buffer.setLength(0);
        return result;
    }
    
    public void cacheData(String key, Object value) {
        cache.put(key, value);
    }
}
```

Quale problema presenta questo codice?

- a) La mappa statica non è thread-safe
- b) Il StringBuilder non è thread-safe in un contesto multi-thread
- c) I metodi dovrebbero essere sincronizzati
- d) Non ci sono problemi, il container gestisce automaticamente la concorrenza

---

### 🔵 Domanda 2

In un **Singleton Session Bean**, quale annotazione garantisce che un solo thread alla volta acceda ai metodi dell'istanza?

- a) `@Synchronized`
- b) `@Lock(LockType.WRITE)`
- c) `@AccessTimeout`
- d) `@Concurrency(ConcurrencyManagementType.BEAN)`

---

### 🟢 Domanda 3

Quali delle seguenti affermazioni sui **Singleton Session Bean** sono corrette? (Seleziona tutte quelle corrette)

- a) Esiste una sola istanza per applicazione
- b) Sono thread-safe per default
- c) Possono essere avviati automaticamente con @Startup
- d) Supportano sia letture concorrenti che scritture esclusive
- e) Mantengono stato condiviso tra tutti i client

---

## 2. Dependency Injection e Lookup

### 💻 Domanda 4

Analizza questo scenario di dependency injection:

```java
@Stateless
@Local
public class EmailService {
    public void sendEmail(String to, String message) {
        System.out.println("Email sent to: " + to);
    }
}

@Stateless
public class UserService {
    
    @EJB
    private EmailService emailService;
    
    @EJB(lookup = "java:app/EmailService")
    private EmailService emailServiceExplicit;
    
    public void createUser(String email) {
        // Creazione utente
        emailService.sendEmail(email, "Welcome!");
    }
}
```

Qual è la differenza tra le due iniezioni di `EmailService`?

- a) La prima usa auto-discovery, la seconda usa lookup esplicito JNDI
- b) La prima è locale, la seconda è remota
- c) Non c'è differenza funzionale
- d) La seconda è più performante

---

### 🔵 Domanda 5

Quale eccezione viene lanciata se un EJB referenziato con `@EJB` non viene trovato durante il deployment?

- a) `NullPointerException`
- b) `DeploymentException`
- c) `EJBException`
- d) `NameNotFoundException`

---

## 3. Gestione Avanzata delle Transazioni

### 💻 Domanda 6

Osserva questo codice transazionale complesso:

```java
@Stateless
public class TransferService {
    
    @EJB
    private AccountService accountService;
    
    @EJB
    private AuditService auditService;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transferMoney(Long fromId, Long toId, BigDecimal amount) 
            throws InsufficientFundsException {
        
        accountService.debit(fromId, amount);  // REQUIRED
        accountService.credit(toId, amount);   // REQUIRED
        
        // Log dell'operazione in transazione separata
        auditService.logTransfer(fromId, toId, amount);  // REQUIRES_NEW
    }
}

@Stateless
public class AuditService {
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logTransfer(Long fromId, Long toId, BigDecimal amount) {
        // Registrazione sempre salvata, anche se il transfer fallisce
        System.out.println("Transfer logged: " + fromId + " -> " + toId + " = " + amount);
    }
}
```

Se `credit()` lancia un'eccezione, cosa succede al log dell'audit?

- a) Viene annullato insieme al transfer
- b) Viene salvato perché è in una transazione separata
- c) Dipende dal tipo di eccezione lanciata
- d) Il comportamento è indefinito

---

### 🟢 Domanda 7

Quali dei seguenti attributi transazionali **creano sempre** una nuova transazione? (Seleziona tutti)

- a) `REQUIRED`
- b) `REQUIRES_NEW`
- c) `MANDATORY`
- d) `NOT_SUPPORTED`
- e) `NEVER`

---

### 💻 Domanda 8

Analizza questo scenario di rollback personalizzato:

```java
@Stateless
public class OrderProcessingService {
    
    @Resource
    private SessionContext sessionContext;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderResult processOrder(Order order) throws BusinessException {
        
        try {
            validateOrder(order);
            processPayment(order);
            updateInventory(order);
            
            return new OrderResult(true, "Order processed successfully");
            
        } catch (PaymentDeclinedException e) {
            // Rollback esplicito per errore business
            sessionContext.setRollbackOnly();
            return new OrderResult(false, "Payment declined: " + e.getMessage());
            
        } catch (Exception e) {
            // Rollback automatico per errori di sistema
            throw new EJBException("System error", e);
        }
    }
}
```

Quando viene utilizzato `setRollbackOnly()`?

- a) Per forzare il rollback quando si vuole restituire un risultato controllato invece di lanciare un'eccezione
- b) Solo quando si usa Bean-Managed Transactions
- c) Per migliorare le performance della transazione
- d) È deprecato, si dovrebbe sempre lanciare un'eccezione

---

## 4. Interceptor Avanzati

### 💻 Domanda 9

Osserva questo interceptor per il monitoraggio delle performance:

```java
@Interceptor
@PerformanceMonitored
public class PerformanceInterceptor {
    
    @AroundInvoke
    public Object monitor(InvocationContext ctx) throws Exception {
        String methodName = ctx.getTarget().getClass().getSimpleName() + 
                           "." + ctx.getMethod().getName();
        
        long startTime = System.nanoTime();
        
        try {
            Object result = ctx.proceed();
            
            long duration = System.nanoTime() - startTime;
            if (duration > 1_000_000_000) { // 1 secondo
                System.out.println("SLOW METHOD: " + methodName + 
                                 " took " + (duration / 1_000_000) + "ms");
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            System.out.println("FAILED METHOD: " + methodName + 
                             " failed after " + (duration / 1_000_000) + "ms");
            throw e;
        }
    }
}

@Stateless
@PerformanceMonitored
public class ComplexCalculationService {
    
    public BigDecimal calculatePi(int precision) {
        // Calcolo complesso che può richiedere tempo
        try {
            Thread.sleep(1500); // Simula calcolo lungo
            return new BigDecimal("3.14159");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

Cosa stamperà l'interceptor quando viene chiamato `calculatePi(100)`?

- a) Solo il tempo di esecuzione normale
- b) "SLOW METHOD: ComplexCalculationService.calculatePi took 1500ms"
- c) Nessun output perché il metodo non ha lanciato eccezioni
- d) L'interceptor non viene eseguito sui metodi che usano Thread.sleep()

---

### 🔵 Domanda 10

Quale annotazione permette di creare un interceptor che viene eseguito quando scade un timer EJB?

- a) `@AroundInvoke`
- b) `@AroundTimeout`
- c) `@AroundTimer`
- d) `@TimeoutInterceptor`

---

## 5. Timer Service Avanzato

### 💻 Domanda 11

Analizza questo sistema di scheduling complesso:

```java
@Singleton
@Startup
public class AdvancedSchedulerService {
    
    @Resource
    private TimerService timerService;
    
    private Timer backupTimer;
    
    @PostConstruct
    public void initTimers() {
        // Timer per backup quotidiano alle 02:00
        ScheduleExpression dailyBackup = new ScheduleExpression();
        dailyBackup.hour("2").minute("0").second("0");
        
        TimerConfig config = new TimerConfig("daily-backup", false);
        timerService.createCalendarTimer(dailyBackup, config);
        
        // Timer programmatico per pulizia cache ogni 5 minuti
        createCacheCleanupTimer();
    }
    
    public void createCacheCleanupTimer() {
        if (backupTimer != null) {
            backupTimer.cancel();
        }
        
        TimerConfig config = new TimerConfig("cache-cleanup", false);
        backupTimer = timerService.createIntervalTimer(
            300000, 300000, config); // 5 minuti
    }
    
    @Timeout
    public void handleTimeout(Timer timer) {
        String info = (String) timer.getInfo();
        
        switch (info) {
            case "daily-backup":
                performBackup();
                break;
            case "cache-cleanup":
                cleanupCache();
                break;
            default:
                System.out.println("Unknown timer: " + info);
        }
    }
    
    private void performBackup() {
        System.out.println("Performing daily backup...");
    }
    
    private void cleanupCache() {
        System.out.println("Cleaning up cache...");
    }
}
```

Cosa succede se il servizio viene riavviato?

- a) Tutti i timer vengono persi e devono essere ricreati
- b) Solo i timer persistenti vengono ripristinati automaticamente
- c) I timer calendario vengono ripristinati, quelli programmatici no
- d) Dipende dalla configurazione del container

---

### 🟢 Domanda 12

Quali tipi di timer sono disponibili in EJB Timer Service? (Seleziona tutti)

- a) Single Action Timer
- b) Interval Timer
- c) Calendar Timer
- d) Cron Timer
- e) Delay Timer

---

## 6. Metodi Asincroni Avanzati

### 💻 Domanda 13

Osserva questo pattern di elaborazione asincrona con callback:

```java
@Stateless
public class FileProcessingService {
    
    @Asynchronous
    public Future<ProcessingResult> processFileAsync(String filename) {
        try {
            // Simulazione elaborazione file lunga
            Thread.sleep(5000);
            
            ProcessingResult result = new ProcessingResult();
            result.setFilename(filename);
            result.setStatus("COMPLETED");
            result.setProcessedLines(1000);
            
            return new AsyncResult<>(result);
            
        } catch (InterruptedException e) {
            ProcessingResult errorResult = new ProcessingResult();
            errorResult.setStatus("FAILED");
            errorResult.setError(e.getMessage());
            return new AsyncResult<>(errorResult);
        }
    }
    
    @Asynchronous
    public void processMultipleFiles(List<String> filenames, 
                                   ProcessingCallback callback) {
        
        List<Future<ProcessingResult>> futures = new ArrayList<>();
        
        for (String filename : filenames) {
            Future<ProcessingResult> future = processFileAsync(filename);
            futures.add(future);
        }
        
        // Attesa completamento di tutti i file
        List<ProcessingResult> results = new ArrayList<>();
        for (Future<ProcessingResult> future : futures) {
            try {
                results.add(future.get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                ProcessingResult error = new ProcessingResult();
                error.setStatus("TIMEOUT");
                results.add(error);
            }
        }
        
        callback.onAllFilesProcessed(results);
    }
}
```

Cosa succede se uno dei file richiede più di 10 secondi per essere processato?

- a) Tutti i file vengono cancellati
- b) Viene creato un risultato con status "TIMEOUT" per quel file
- c) Il metodo lancia un'eccezione
- d) Il sistema attende indefinitamente

---

### 🔵 Domanda 14

Quale limitazione hanno i metodi `@Asynchronous` negli EJB?

- a) Non possono essere chiamati da altri EJB
- b) Non possono accedere al database
- c) Non possono essere chiamati dallo stesso bean (self-invocation)
- d) Non supportano le transazioni

---

## 7. Sicurezza Declarativa vs Programmatica

### 💻 Domanda 15

Analizza questo mix di sicurezza declarativa e programmatica:

```java
@Stateless
@RolesAllowed({"user", "admin"})
public class DocumentService {
    
    @Resource
    private SessionContext sessionContext;
    
    @PermitAll
    public List<Document> getPublicDocuments() {
        return findDocumentsByType("PUBLIC");
    }
    
    @RolesAllowed("admin")
    public void deleteDocument(Long documentId) {
        // Solo admin possono cancellare
        removeDocument(documentId);
    }
    
    public Document getDocument(Long documentId) {
        Document doc = findDocument(documentId);
        
        // Controllo programmatico per documenti privati
        if ("PRIVATE".equals(doc.getType()) && 
            !sessionContext.isCallerInRole("admin") &&
            !doc.getOwnerId().equals(getCurrentUserId())) {
            
            throw new SecurityException("Access denied to private document");
        }
        
        return doc;
    }
    
    private String getCurrentUserId() {
        return sessionContext.getCallerPrincipal().getName();
    }
}
```

Perché è stato usato un controllo programmatico in `getDocument()` invece di sicurezza declarativa?

- a) La sicurezza declarativa non funziona con documenti privati
- b) È necessaria logica condizionale basata sui dati (owner del documento)
- c) Le performance sono migliori con controlli programmatici
- d) È un errore, dovrebbe essere usata solo sicurezza declarativa

---

### 🟢 Domanda 16

Quali interfacce/classi forniscono accesso alle informazioni di sicurezza in un EJB? (Seleziona tutte)

- a) `SessionContext`
- b) `EJBContext`
- c) `Principal`
- d) `SecurityContext`
- e) `HttpServletRequest`

---

## 8. EJB e Patterns Architetturali

### 💻 Domanda 17

Osserva questo pattern Business Delegate:

```java
@Stateless
@Remote
public class RemoteInventoryService {
    
    public boolean checkAvailability(String productCode, int quantity) {
        // Simulazione chiamata lenta a sistema esterno
        try {
            Thread.sleep(2000);
            return quantity <= 100; // Mock logic
        } catch (InterruptedException e) {
            return false;
        }
    }
}

@Stateless
public class InventoryDelegate {
    
    @EJB
    private RemoteInventoryService remoteService;
    
    private Map<String, Boolean> cache = new ConcurrentHashMap<>();
    
    public boolean isProductAvailable(String productCode, int quantity) {
        String cacheKey = productCode + ":" + quantity;
        
        // Controlla cache locale prima
        Boolean cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Chiamata al servizio remoto
        boolean available = remoteService.checkAvailability(productCode, quantity);
        
        // Cache del risultato per 5 minuti
        cache.put(cacheKey, available);
        scheduleCache Expiration(cacheKey, 300000);
        
        return available;
    }
    
    private void scheduleCacheExpiration(String key, long delay) {
        // Implementazione timer per invalidazione cache
    }
}
```

Quale pattern architetturale implementa `InventoryDelegate`?

- a) Service Locator
- b) Business Delegate
- c) Data Access Object
- d) Facade Pattern

---

### 🔵 Domanda 18

Nel pattern **Service Locator**, quale problema risolve l'uso di un cache per le reference EJB?

- a) Riduce il consumo di memoria
- b) Evita costose operazioni di lookup JNDI ripetute
- c) Migliora la sicurezza
- d) Permette il load balancing automatico

---

## 9. Integrazione CDI Avanzata

### 💻 Domanda 19

Analizza questa integrazione EJB-CDI con eventi:

```java
@Stateless
public class OrderService {
    
    @Inject
    private Event<OrderProcessedEvent> orderEvent;
    
    @Inject
    @ConfigProperty(name = "order.processing.mode")
    private String processingMode;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processOrder(Order order) {
        // Processamento ordine
        order.setStatus("PROCESSED");
        order.setProcessedDate(new Date());
        
        // Evento per notificare il completamento
        OrderProcessedEvent event = new OrderProcessedEvent(order);
        orderEvent.fire(event);
    }
}

@ApplicationScoped
public class OrderEventHandler {
    
    @Inject
    private EmailService emailService;
    
    @Inject
    private InventoryService inventoryService;
    
    public void handleOrderProcessed(@Observes OrderProcessedEvent event) {
        Order order = event.getOrder();
        
        // Invia notifica email
        emailService.sendOrderConfirmation(order);
        
        // Aggiorna inventario
        inventoryService.updateStock(order.getItems());
    }
    
    public void handleOrderProcessedAsync(@ObservesAsync OrderProcessedEvent event) {
        // Elaborazione asincrona per analytics
        generateOrderAnalytics(event.getOrder());
    }
}
```

Quale vantaggio offre l'uso di eventi CDI in questo scenario?

- a) Migliori performance delle transazioni
- b) Disaccoppiamento tra processamento ordine e attività correlate
- c) Thread-safety automatica
- d) Gestione automatica degli errori

---

### 🟢 Domanda 20

Quali scope CDI sono compatibili con gli EJB? (Seleziona tutti)

- a) `@RequestScoped`
- b) `@SessionScoped`
- c) `@ApplicationScoped`
- d) `@ConversationScoped`
- e) `@Dependent`

---

## 10. Performance e Ottimizzazione

### 💻 Domanda 21

Osserva questo esempio di ottimizzazione pool EJB:

```java
@Stateless
@Pool(value = 50, timeout = 5000)
public class HighVolumeProcessingService {
    
    @PersistenceContext
    private EntityManager em;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processBatch(List<DataRecord> records) {
        
        int batchSize = 100;
        for (int i = 0; i < records.size(); i += batchSize) {
            List<DataRecord> batch = records.subList(i, 
                Math.min(i + batchSize, records.size()));
            
            processBatchChunk(batch);
            
            // Flush periodico per evitare OutOfMemoryError
            if (i % (batchSize * 10) == 0) {
                em.flush();
                em.clear();
            }
        }
    }
    
    private void processBatchChunk(List<DataRecord> chunk) {
        for (DataRecord record : chunk) {
            // Elaborazione record
            record.setProcessed(true);
            record.setProcessedDate(new Date());
            em.merge(record);
        }
    }
}
```

Perché viene usato `REQUIRES_NEW` invece di `REQUIRED`?

- a) Per migliorare le performance del batch
- b) Per evitare timeout su transazioni molto lunghe
- c) È obbligatorio per operazioni batch
- d) Per permettere elaborazione parallela

---

### 🟢 Domanda 22

Quali strategie sono efficaci per ottimizzare le performance degli EJB? (Seleziona tutte)

- a) Usare pool di dimensioni appropriate
- b) Minimizzare la durata delle transazioni
- c) Implementare caching applicativo
- d) Usare sempre Bean-Managed Transactions
- e) Batch processing per operazioni di massa

---

## 11. Troubleshooting e Debug

### 💻 Domanda 23

Analizza questo scenario di debug per deadlock:

```java
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ResourceManager {
    
    @Lock(LockType.WRITE)
    public void allocateResource(String resourceId, String userId) {
        System.out.println("Allocating " + resourceId + " to " + userId);
        
        // Simulazione operazione lunga
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Potenziale chiamata ad altro metodo locked
        logAllocation(resourceId, userId);
    }
    
    @Lock(LockType.WRITE)
    public void logAllocation(String resourceId, String userId) {
        System.out.println("Logging allocation: " + resourceId + " -> " + userId);
    }
    
    @Lock(LockType.READ)
    public List<String> getAllocatedResources() {
        // Lettura dello stato delle allocazioni
        return Arrays.asList("resource1", "resource2");
    }
}
```

Quale problema può verificarsi con questo codice?

- a) Race condition tra allocazione e logging
- b) Deadlock se due thread chiamano contemporaneamente allocateResource()
- c) Il metodo logAllocation() non può essere chiamato da allocateResource()
- d) Memory leak dovuto alle operazioni lunghe

---

### 🔵 Domanda 24

Quale strumento è più utile per diagnosticare problemi di performance negli EJB?

- a) Thread dump analysis
- b) Heap dump analysis
- c) JMX monitoring
- d) Tutti i precedenti

---

## 12. Migrazione e Compatibilità

### 🔵 Domanda 25

Quando si migra da EJB 2.x a EJB 3.x+, quale cambio principale semplifica il codice?

- a) Eliminazione delle interfacce Home
- b) Introduzione delle annotazioni
- c) Dependency injection automatica
- d) Tutte le precedenti

---

### 💻 Domanda 26

Osserva questo codice legacy e la sua versione moderna:

```java
// EJB 2.x Style (Legacy)
public class LegacyOrderService implements SessionBean {
    
    private SessionContext ctx;
    
    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
    }
    
    public void ejbCreate() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    
    public OrderResult createOrder(OrderData data) throws CreateException {
        // Business logic
        return new OrderResult();
    }
}

// EJB 3.x+ Style (Modern)
@Stateless
public class ModernOrderService {
    
    @Resource
    private SessionContext ctx;
    
    public OrderResult createOrder(OrderData data) throws BusinessException {
        // Same business logic
        return new OrderResult();
    }
}
```

Quale vantaggio principale offre la versione EJB 3.x+?

- a) Migliori performance di runtime
- b) Riduzione drastica del boilerplate code
- c) Maggiore sicurezza
- d) Supporto per più tipi di database

---

## 13. Best Practices Avanzate

### 🟢 Domanda 27

Quali sono considerate best practices per la progettazione di EJB? (Seleziona tutte)

- a) Preferire interfacce locali a quelle remote quando possibile
- b) Usare DTO (Data Transfer Objects) per interfacce remote
- c) Implementare pattern Facade per operazioni complesse
- d) Evitare l'uso di metodi statici nei bean
- e) Usare sempre @Asynchronous per operazioni che richiedono più di 1 secondo

---

### 💻 Domanda 28

Analizza questo pattern per gestire configurazioni dinamiche:

```java
@Singleton
@Startup
public class ConfigurationManager {
    
    private Properties config = new Properties();
    private volatile long lastModified = 0;
    
    @PostConstruct
    public void loadConfiguration() {
        reloadConfigurationIfNeeded();
    }
    
    @Lock(LockType.READ)
    public String getProperty(String key) {
        reloadConfigurationIfNeeded();
        return config.getProperty(key);
    }
    
    @Lock(LockType.WRITE)
    public void reloadConfiguration() {
        try {
            File configFile = new File("app.properties");
            if (configFile.exists() && configFile.lastModified() > lastModified) {
                config.load(new FileInputStream(configFile));
                lastModified = configFile.lastModified();
                System.out.println("Configuration reloaded");
            }
        } catch (IOException e) {
            System.err.println("Failed to reload configuration: " + e.getMessage());
        }
    }
    
    private void reloadConfigurationIfNeeded() {
        File configFile = new File("app.properties");
        if (configFile.exists() && configFile.lastModified() > lastModified) {
            reloadConfiguration();
        }
    }
}
```

Perché viene usato `volatile` per la variabile `lastModified`?

- a) Per garantire performance migliori
- b) Per assicurare visibilità delle modifiche tra thread
- c) È richiesto dalle specifiche EJB
- d) Per evitare NullPointerException

---

## 14. Scenari di Errore e Recovery

### 💻 Domanda 29

Osserva questo scenario di gestione errori con retry automatico:

```java
@Stateless
public class ResilientService {
    
    @EJB
    private ExternalApiService externalService;
    
    public ApiResponse callExternalApiWithRetry(String request) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                return externalService.makeCall(request);
                
            } catch (TemporaryException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new ServiceUnavailableException(
                        "External service unavailable after " + maxRetries + " retries", e);
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(1000 * (1 << retryCount));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ServiceUnavailableException("Interrupted during retry", ie);
                }
                
            } catch (PermanentException e) {
                // Non retry per errori permanenti
                throw new BusinessException("Permanent error: " + e.getMessage(), e);
            }
        }
        
        return null; // Never reached
    }
}
```

Quale pattern di resilience implementa questo codice?

- a) Circuit Breaker
- b) Retry with Exponential Backoff
- c) Bulkhead
- d) Timeout

---

### 🔵 Domanda 30

In caso di `OutOfMemoryError` in un EJB, quale è l'approccio migliore?

- a) Catturare l'errore e continuare l'esecuzione
- b) Lasciare che il container gestisca l'errore
- c) Fare retry dell'operazione
- d) Loggare l'errore e restituire null

---

---

## Risposte Corrette

### 1. **b)** Il StringBuilder non è thread-safe in un contesto multi-thread

Il StringBuilder è una variabile di istanza che può essere condivisa tra thread in un pool di Stateless Bean, causando corruption dei dati.

### 2. **b)** `@Lock(LockType.WRITE)`

`@Lock(LockType.WRITE)` garantisce accesso esclusivo a un metodo in un Singleton Bean.

### 3. **a, c, d, e)** Esiste una sola istanza, possono essere avviati con @Startup, supportano letture/scritture concorrenti, mantengono stato condiviso

I Singleton Bean non sono thread-safe per default, richiedono gestione esplicita della concorrenza.

### 4. **a)** La prima usa auto-discovery, la seconda usa lookup esplicito JNDI

La prima injection usa il nome dell'EJB automaticamente, la seconda specifica il JNDI name esplicitamente.

### 5. **b)** `DeploymentException`

Se una dipendenza EJB non può essere risolta, il deployment fallisce con DeploymentException.

### 6. **b)** Viene salvato perché è in una transazione separata

`REQUIRES_NEW` crea una transazione indipendente che viene committata anche se la transazione principale fallisce.

### 7. **b)** `REQUIRES_NEW`

Solo `REQUIRES_NEW` crea sempre una nuova transazione, indipendentemente dal contesto transazionale corrente.

### 8. **a)** Per forzare il rollback quando si vuole restituire un risultato controllato invece di lanciare un'eccezione

`setRollbackOnly()` permette di annullare la transazione continuando l'esecuzione del metodo per cleanup o logging.

### 9. **b)** "SLOW METHOD: ComplexCalculationService.calculatePi took 1500ms"

Il metodo impiega 1500ms (>1 secondo), quindi viene loggato come metodo lento.

### 10. **b)** `@AroundTimeout`

`@AroundTimeout` è l'interceptor specifico per i callback dei timer EJB.

### 11. **b)** Solo i timer persistenti vengono ripristinati automaticamente

I timer calendario sono persistenti per default e sopravvivono ai restart. I timer programmatici dipendono dalla configurazione di persistenza.

### 12. **a, b, c)** Single Action Timer, Interval Timer, Calendar Timer

Questi sono i tre tipi principali di timer supportati da EJB Timer Service.

### 13. **b)** Viene creato un risultato con status "TIMEOUT" per quel file

Il `future.get(10, TimeUnit.SECONDS)` lancia TimeoutException che viene catturata e gestita.

### 14. **c)** Non possono essere chiamati dallo stesso bean (self-invocation)

Le chiamate asincrone dallo stesso bean non passano attraverso il proxy EJB, quindi @Asynchronous non ha effetto.

### 15. **b)** È necessaria logica condizionale basata sui dati (owner del documento)

La sicurezza declarativa non può accedere ai dati per decisioni dinamiche. È necessaria logica programmatica.

### 16. **a, b, c)** `SessionContext`, `EJBContext`, `Principal`

Queste interfacce forniscono accesso alle informazioni di sicurezza negli EJB.

### 17. **b)** Business Delegate

Il pattern Business Delegate nasconde la complessità di accesso ai servizi remoti e fornisce caching/ottimizzazioni.

### 18. **b)** Evita costose operazioni di lookup JNDI ripetute

Il caching delle reference EJB evita lookup JNDI costosi ad ogni utilizzo.

### 19. **b)** Disaccoppiamento tra processamento ordine e attività correlate

Gli eventi CDI permettono di separare la logica principale dalle attività correlate (email, inventario).

### 20. **a, b, c, d, e)** Tutti gli scope CDI sono compatibili con EJB

Gli EJB possono iniettare bean CDI di qualsiasi scope valido.

### 21. **b)** Per evitare timeout su transazioni molto lunghe

`REQUIRES_NEW` crea transazioni separate più corte, evitando timeout su operazioni batch lunghe.

### 22. **a, b, c, e)** Usare pool appropriati, minimizzare transazioni, caching, batch processing

BMT non è automaticamente migliore per performance rispetto a CMT.

### 23. **c)** Il metodo logAllocation() non può essere chiamato da allocateResource()

Entrambi i metodi richiedono WRITE lock. Una chiamata da allocateResource() a logAllocation() causerebbe deadlock.

### 24. **d)** Tutti i precedenti

Thread dump, heap dump e JMX monitoring sono tutti strumenti complementari per diagnosticare problemi EJB.

### 25. **d)** Tutte le precedenti

EJB 3.x ha introdotto annotazioni, eliminato Home interfaces e abilitato dependency injection, semplificando drasticamente lo sviluppo.

### 26. **b)** Riduzione drastica del boilerplate code

La versione EJB 3.x+ elimina la necessità di implementare SessionBean e i metodi del ciclo di vita.

### 27. **a, b, c, d)** Preferire interfacce locali, usare DTO per remote, pattern Facade, evitare metodi statici

@Asynchronous non dovrebbe essere usato automaticamente per tutte le operazioni > 1 secondo.

### 28. **b)** Per assicurare visibilità delle modifiche tra thread

`volatile` garantisce che le modifiche a lastModified siano immediatamente visibili a tutti i thread.

### 29. **b)** Retry with Exponential Backoff

Il codice implementa retry con backoff esponenziale (1s, 2s, 4s) per gestire errori temporanei.

### 30. **b)** Lasciare che il container gestisca l'errore

Gli OutOfMemoryError sono errori di sistema gravi che devono essere gestiti dal container EJB, non dall'applicazione.