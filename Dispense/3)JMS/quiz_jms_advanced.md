# Quiz Avanzato su JMS (Java Message Service) - Parte 2

Questo quiz avanzato copre concetti avanzati di Java Message Service (JMS) con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Configurazione Avanzata di JMS

### 💻 Domanda 1

Analizza la seguente configurazione di Connection Factory:

```java
@JMSConnectionFactoryDefinition(
    name = "java:global/jms/CustomConnectionFactory",
    interfaceName = "javax.jms.ConnectionFactory",
    resourceAdapter = "activemq-ra",
    properties = {
        "brokerURL=tcp://localhost:61616",
        "userName=admin",
        "password=admin"
    }
)
@ApplicationScoped
public class JMSConfiguration {
}
```

Qual è il principale vantaggio di questa configurazione?

- a) Migliori performance rispetto al JNDI lookup
- b) Configurazione embedded dell'application senza dipendenze esterne
- c) Supporto automatico per clustering
- d) Gestione automatica delle transazioni distribuite

---

### 🔵 Domanda 2

In quale contesto è obbligatorio utilizzare `@JMSConnectionFactoryDefinition`?

- a) Solo in applicazioni standalone
- b) Solo quando si usano resource adapter esterni
- c) Quando si vuole evitare la configurazione JNDI dell'application server
- d) Solo per applicazioni web

---

### 🟢 Domanda 3

Quali delle seguenti proprietà possono essere configurate in una `@JMSConnectionFactoryDefinition`? (Seleziona tutte)

- a) clientID per identificare univocamente il client
- b) maxPoolSize per il connection pooling
- c) transacted per abilitare le transazioni locali
- d) acknowledgeMode per il tipo di acknowledgment
- e) brokerURL per specificare l'indirizzo del message broker

---

## 2. Message Selectors Avanzati

### 💻 Domanda 4

Osserva questo MDB con message selector complesso:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destinationLookup", 
            propertyValue = "java:/jms/topic/tradingTopic"
        ),
        @ActivationConfigProperty(
            propertyName = "messageSelector",
            propertyValue = "symbol IN ('AAPL', 'GOOGL', 'MSFT') AND " +
                          "price > 100.0 AND " +
                          "JMSTimestamp > " + (System.currentTimeMillis() - 300000)
        )
    }
)
public class HighValueStockMDB implements MessageListener {
    @Override
    public void onMessage(Message message) {
        // Processa solo stock ad alto valore degli ultimi 5 minuti
    }
}
```

Qual è il problema principale con questo message selector?

- a) La sintassi IN non è supportata nei message selector
- b) JMSTimestamp non può essere usato nei selector
- c) System.currentTimeMillis() viene valutato al deployment, non runtime
- d) Non si possono combinare più condizioni con AND

---

### 🔵 Domanda 5

Quale delle seguenti espressioni di message selector è **NON valida**?

- a) `category = 'electronics' AND price BETWEEN 50 AND 500`
- b) `priority IS NOT NULL AND status LIKE 'PEND%'`
- c) `quantity > 0 AND description CONTAINS 'premium'`
- d) `orderDate IS NULL OR customerType IN ('VIP', 'PREMIUM')`

---

### 🟢 Domanda 6

Nei message selector JMS, quali operatori sono supportati? (Seleziona tutti)

- a) LIKE per pattern matching
- b) BETWEEN per range numerici
- c) IN per elenchi di valori
- d) IS NULL/IS NOT NULL per valori nulli
- e) REGEX per espressioni regolari

---

## 3. Gestione delle Eccezioni e Recovery

### 💻 Domanda 7

Analizza questo pattern di gestione errori con Dead Letter Queue:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/orderQueue"),
        @ActivationConfigProperty(propertyName = "maxSession", 
                                propertyValue = "5"),
        @ActivationConfigProperty(propertyName = "redeliveryAttempts", 
                                propertyValue = "3")
    }
)
public class OrderProcessorMDB implements MessageListener {
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/dlq")
    private Queue deadLetterQueue;
    
    @Override
    public void onMessage(Message message) {
        try {
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            
            if (deliveryCount > 3) {
                // Sposta manualmente in DLQ
                context.createProducer()
                       .setProperty("originalQueue", "orderQueue")
                       .setProperty("failureReason", "Max delivery exceeded")
                       .send(deadLetterQueue, message.getBody(String.class));
                return; // Non rilancia eccezione
            }
            
            processOrder(message.getBody(String.class));
            
        } catch (BusinessException e) {
            // Errore business - non tentare retry
            logError("Business error", e);
        } catch (Exception e) {
            // Errore tecnico - forza retry
            throw new RuntimeException("Technical error", e);
        }
    }
}
```

Qual è il comportamento di questo MDB quando `deliveryCount > 3`?

- a) Il messaggio viene processato normalmente
- b) Il messaggio viene spostato manualmente in DLQ e consumato dalla coda originale
- c) Il messaggio rimane nella coda originale per ulteriori retry
- d) Viene generata un'eccezione che blocca l'MDB

---

### 🔵 Domanda 8

Quale strategia di recovery è più appropriata per **errori temporanei di rete**?

- a) Immediate retry senza delay
- b) Exponential backoff con retry limitati
- c) Spostamento immediato in Dead Letter Queue
- d) Interruzione del processing di tutti i messaggi

---

### 🟢 Domanda 9

Quali informazioni dovrebbero essere incluse quando si sposta un messaggio in una Dead Letter Queue? (Seleziona tutte)

- a) Timestamp dell'errore
- b) Motivo del fallimento
- c) Numero di tentativi effettuati
- d) Queue di origine
- e) Stack trace completo dell'eccezione

---

## 4. Configurazione di Performance

### 💻 Domanda 10

Osserva questa configurazione ottimizzata per alto throughput:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/highThroughputQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", 
                                propertyValue = "Dups-ok-acknowledge"),
        @ActivationConfigProperty(propertyName = "maxSession", 
                                propertyValue = "50"),
        @ActivationConfigProperty(propertyName = "prefetchSize", 
                                propertyValue = "100"),
        @ActivationConfigProperty(propertyName = "useBatch", 
                                propertyValue = "true"),
        @ActivationConfigProperty(propertyName = "batchSize", 
                                propertyValue = "10")
    }
)
public class HighThroughputMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processing ottimizzato
        processMessageQuickly(message);
    }
}
```

Quale configurazione ha l'impatto maggiore sul throughput?

- a) `acknowledgeMode = "Dups-ok-acknowledge"`
- b) `maxSession = "50"`
- c) `prefetchSize = "100"`
- d) `batchSize = "10"`

---

### 🔵 Domanda 11

Cosa comporta l'uso di `Dups-ok-acknowledge` mode?

- a) Maggiore affidabilità con acknowledgment duplicato
- b) Migliori performance con possibili messaggi duplicati
- c) Acknowledgment automatico senza intervento del developer
- d) Disabilitazione completa dell'acknowledgment

---

### 🟢 Domanda 12

Per ottimizzare le performance di un sistema JMS ad alto volume, quali strategie sono consigliate? (Seleziona tutte)

- a) Usare messaggi NON_PERSISTENT quando possibile
- b) Implementare connection pooling efficace
- c) Configurare appropriatamente la dimensione del prefetch
- d) Usare acknowledge mode meno rigorosi quando appropriato
- e) Implementare processing asincrono all'interno degli MDB

---

## 5. Transazioni Distribuite e XA

### 💻 Domanda 13

Analizza questo scenario di transazione distribuita:

```java
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class OrderService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private JMSContext jmsContext;
    
    @Resource(lookup = "java:/jms/queue/inventoryQueue")
    private Queue inventoryQueue;
    
    @Resource(lookup = "java:/jms/queue/paymentQueue")
    private Queue paymentQueue;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processComplexOrder(Order order) {
        try {
            // 1. Salva ordine nel database
            em.persist(order);
            
            // 2. Invia messaggio per controllo inventario
            jmsContext.createProducer()
                     .setProperty("orderId", order.getId())
                     .send(inventoryQueue, "CHECK_INVENTORY");
            
            // 3. Invia messaggio per elaborazione pagamento
            jmsContext.createProducer()
                     .setProperty("orderId", order.getId())
                     .setProperty("amount", order.getTotal().toString())
                     .send(paymentQueue, "PROCESS_PAYMENT");
            
            // 4. Simula errore condizionale
            if (order.getTotal().compareTo(new BigDecimal("10000")) > 0) {
                throw new RuntimeException("Ordine troppo grande");
            }
            
        } catch (Exception e) {
            throw new EJBException("Errore nel processing dell'ordine", e);
        }
    }
}
```

Se viene lanciata l'eccezione per "Ordine troppo grande", cosa succede?

- a) Solo il salvataggio nel database viene annullato
- b) Solo i messaggi JMS non vengono inviati
- c) Tutte le operazioni (database + JMS) vengono annullate
- d) L'ordine viene salvato ma i messaggi non vengono inviati

---

### 🔵 Domanda 14

In una transazione XA, quando vengono effettivamente commitati i messaggi JMS?

- a) Immediatamente quando viene chiamato send()
- b) Durante la fase di prepare del protocollo 2PC
- c) Durante la fase di commit del protocollo 2PC
- d) Dopo che tutte le altre risorse hanno fatto commit

---

### 🟢 Domanda 15

Quali sono i vantaggi delle transazioni XA in ambiente JMS? (Seleziona tutti)

- a) Consistenza ACID attraverso multiple risorse
- b) Atomicità tra operazioni database e messaging
- c) Migliori performance rispetto alle transazioni locali
- d) Gestione automatica del recovery in caso di fallimento
- e) Coordinamento automatico tra diversi message broker

---

## 6. Monitoring e Diagnostics

### 💻 Domanda 16

Osserva questo MDB con logging avanzato e metriche:

```java
@MessageDriven(...)
public class MonitoredOrderMDB implements MessageListener {
    
    @Inject
    private Logger logger;
    
    @Inject
    private MetricRegistry metrics;
    
    private Timer processingTimer;
    private Counter successCounter;
    private Counter errorCounter;
    
    @PostConstruct
    public void init() {
        processingTimer = metrics.timer("order.processing.time");
        successCounter = metrics.counter("order.processing.success");
        errorCounter = metrics.counter("order.processing.errors");
    }
    
    @Override
    public void onMessage(Message message) {
        Timer.Context timerContext = processingTimer.time();
        
        try {
            String orderId = message.getStringProperty("orderId");
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            long messageAge = System.currentTimeMillis() - message.getJMSTimestamp();
            
            logger.info("Processing order {} (delivery #{}, age: {}ms)", 
                       orderId, deliveryCount, messageAge);
            
            if (deliveryCount > 1) {
                logger.warn("Redelivery detected for order {}, attempt #{}", 
                           orderId, deliveryCount);
            }
            
            processOrder(message.getBody(String.class));
            successCounter.inc();
            
        } catch (Exception e) {
            errorCounter.inc();
            logger.error("Error processing message", e);
            throw new RuntimeException(e);
        } finally {
            timerContext.stop();
        }
    }
}
```

Quale metrica è più utile per identificare problemi di performance?

- a) `successCounter` - numero di successi
- b) `errorCounter` - numero di errori
- c) `processingTimer` - tempo di elaborazione
- d) `deliveryCount` - numero di tentativi

---

### 🔵 Domanda 17

Cosa indica un alto valore di `messageAge` (età del messaggio)?

- a) Il messaggio è stato processato molto velocemente
- b) Il broker JMS ha problemi di performance
- c) Possibili colli di bottiglia nel processing o backlog nella coda
- d) Il messaggio ha una priorità bassa

---

### 🟢 Domanda 18

Quali metriche JMS dovrebbero essere monitorate in produzione? (Seleziona tutte)

- a) Queue depth (numero di messaggi in coda)
- b) Message throughput (messaggi al secondo)
- c) Average processing time per messaggio
- d) Redelivery rate (percentuale di redelivery)
- e) Connection pool utilization

---

## 7. Patterns di Integrazione

### 💻 Domanda 19

Analizza questo pattern Content-Based Router:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/incomingQueue")
    }
)
public class ContentBasedRouterMDB implements MessageListener {
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/domesticQueue")
    private Queue domesticQueue;
    
    @Resource(lookup = "java:/jms/queue/internationalQueue")
    private Queue internationalQueue;
    
    @Resource(lookup = "java:/jms/queue/priorityQueue")
    private Queue priorityQueue;
    
    @Override
    public void onMessage(Message message) {
        try {
            String orderType = message.getStringProperty("orderType");
            String country = message.getStringProperty("country");
            boolean isPriority = message.getBooleanProperty("isPriority");
            
            Queue targetQueue;
            
            if (isPriority) {
                targetQueue = priorityQueue;
            } else if ("IT".equals(country) || "US".equals(country)) {
                targetQueue = domesticQueue;
            } else {
                targetQueue = internationalQueue;
            }
            
            // Forward del messaggio preservando proprietà originali
            context.createProducer()
                   .setProperty("routedFrom", "ContentBasedRouter")
                   .setProperty("routedAt", System.currentTimeMillis())
                   .send(targetQueue, message.getBody(String.class));
                   
        } catch (JMSException e) {
            throw new RuntimeException("Routing failed", e);
        }
    }
}
```

Qual è il principale vantaggio di questo pattern?

- a) Migliori performance di processing
- b) Disaccoppiamento e routing automatico basato sul contenuto
- c) Gestione automatica degli errori
- d) Load balancing tra multiple destinazioni

---

### 🔵 Domanda 20

Nel pattern **Message Translator**, qual è la responsabilità principale?

- a) Instradare messaggi verso destinazioni diverse
- b) Trasformare il formato dei messaggi tra sistemi diversi
- c) Aggregare più messaggi in uno singolo
- d) Filtrare messaggi in base a criteri specifici

---

### 🟢 Domanda 21

Quali pattern di Enterprise Integration sono comunemente implementati con JMS? (Seleziona tutti)

- a) Content-Based Router per instradamento intelligente
- b) Message Translator per trasformazione formati
- c) Scatter-Gather per elaborazione parallela
- d) Dead Letter Channel per messaggi non processabili
- e) Message Sequencer per ordinamento messaggi

---

## 8. Sicurezza Avanzata

### 💻 Domanda 22

Analizza questa configurazione di sicurezza con SSL/TLS:

```java
@JMSConnectionFactoryDefinition(
    name = "java:global/jms/SecureConnectionFactory",
    interfaceName = "javax.jms.ConnectionFactory",
    properties = {
        "brokerURL=ssl://secure-broker:61617",
        "trustStore=/path/to/truststore.jks",
        "trustStorePassword=trustpass",
        "keyStore=/path/to/keystore.jks",
        "keyStorePassword=keypass",
        "verifyHostName=true"
    }
)
public class SecureJMSConfig {
}

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/secureQueue"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup",
                                propertyValue = "java:global/jms/SecureConnectionFactory")
    }
)
@RolesAllowed({"secure-processor", "admin"})
public class SecureProcessorMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processing sicuro
    }
}
```

Quali livelli di sicurezza sono implementati in questa configurazione?

- a) Solo crittografia SSL/TLS per il trasporto
- b) Solo autenticazione tramite certificati client
- c) Solo autorizzazione EJB con ruoli
- d) Tutti i precedenti: SSL/TLS, autenticazione certificati, autorizzazione ruoli

---

### 🔵 Domanda 23

Cosa garantisce l'impostazione `verifyHostName=true` nella configurazione SSL?

- a) Verifica che il certificato del server corrisponda al hostname
- b) Abilita l'autenticazione mutua SSL
- c) Forza l'uso di protocolli TLS più recenti
- d) Abilita la verifica dell'identità del client

---

### 🟢 Domanda 24

In un ambiente JMS sicuro, quali meccanismi di autenticazione sono supportati? (Seleziona tutti)

- a) Username/password tradizionale
- b) Certificati client SSL/TLS
- c) Token JWT per autenticazione
- d) Integrazione con LDAP/Active Directory
- e) Kerberos authentication

---

## 9. Clustering e Alta Disponibilità

### 💻 Domanda 25

Osserva questa configurazione per clustering JMS:

```java
@JMSConnectionFactoryDefinition(
    name = "java:global/jms/ClusteredConnectionFactory",
    interfaceName = "javax.jms.ConnectionFactory",
    properties = {
        "brokerURL=failover:(tcp://broker1:61616,tcp://broker2:61616,tcp://broker3:61616)",
        "randomize=false",
        "maxReconnectAttempts=10",
        "initialReconnectDelay=1000",
        "maxReconnectDelay=30000",
        "useExponentialBackOff=true",
        "backOffMultiplier=2.0"
    }
)
public class ClusteredJMSConfig {
}

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/clusteredQueue"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup",
                                propertyValue = "java:global/jms/ClusteredConnectionFactory"),
        @ActivationConfigProperty(propertyName = "maxSession", 
                                propertyValue = "20")
    }
)
public class ClusteredMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        String nodeId = System.getProperty("jboss.node.name", "unknown");
        logger.info("Processing on node: {}", nodeId);
        processMessage(message);
    }
}
```

Cosa succede se il broker principale (broker1) diventa non disponibile?

- a) Tutti i messaggi vengono persi
- b) L'applicazione si connette automaticamente a broker2 o broker3
- c) L'MDB smette di funzionare fino a riavvio manuale
- d) I messaggi vengono messi in coda locale temporanea

---

### 🔵 Domanda 26

Qual è il vantaggio di impostare `randomize=false` nella configurazione failover?

- a) Migliori performance di connessione
- b) Comportamento deterministico nell'ordine di connessione ai broker
- c) Disabilitazione del load balancing
- d) Connessione sempre al primo broker disponibile

---

### 🟢 Domanda 27

In un cluster JMS, quali strategie garantiscono alta disponibilità? (Seleziona tutte)

- a) Replicazione dei messaggi tra broker multipli
- b) Configurazione di failover automatico nei client
- c) Load balancing intelligente tra broker del cluster
- d) Persistent storage condiviso per le code
- e) Health check automatici dei nodi del cluster

---

## 10. Troubleshooting Avanzato

### 💻 Domanda 28

Analizza questo scenario di debugging per problemi di performance:

```java
@MessageDriven(...)
public class PerformanceDebugMDB implements MessageListener {
    
    private static final AtomicLong messageCounter = new AtomicLong();
    private static final AtomicLong totalProcessingTime = new AtomicLong();
    
    @Override
    public void onMessage(Message message) {
        long startTime = System.nanoTime();
        long msgCount = messageCounter.incrementAndGet();
        
        try {
            // Log ogni 1000 messaggi
            if (msgCount % 1000 == 0) {
                long avgTime = totalProcessingTime.get() / msgCount;
                System.out.printf("Processed %d messages, avg time: %d ns%n", 
                                msgCount, avgTime);
            }
            
            // Verifica memoria
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            
            if (usedMemory > runtime.maxMemory() * 0.8) {
                System.err.println("WARNING: High memory usage detected!");
            }
            
            // Processing del messaggio
            processMessage(message);
            
        } finally {
            long processingTime = System.nanoTime() - startTime;
            totalProcessingTime.addAndGet(processingTime);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        long finalCount = messageCounter.get();
        long finalAvgTime = totalProcessingTime.get() / finalCount;
        System.out.printf("Final stats: %d messages, avg time: %d ns%n", 
                        finalCount, finalAvgTime);
    }
}
```

Quale problema di performance può essere identificato con questo approccio?

- a) Memory leak nel processing dei messaggi
- b) Degradazione progressiva delle performance
- c) Colli di bottiglia nella rete JMS
- d) Tutti i precedenti

---

### 🔵 Domanda 29

Se un MDB sembra "bloccarsi" e non processa più messaggi, quale è la prima cosa da verificare?

- a) La configurazione del message selector
- b) Lo stato delle connessioni JMS e del connection pool
- c) La disponibilità del database
- d) La configurazione delle transazioni

---

### 🟢 Domanda 30

Quali strumenti e tecniche sono utili per il troubleshooting di sistemi JMS? (Seleziona tutti)

- a) JConsole/VisualVM per monitoraggio JVM
- b) Log analysis per pattern di errori
- c) Message broker management console
- d) Thread dump analysis per deadlock detection
- e) Network monitoring per latenza connessioni

---

---

## Risposte Corrette

### 1. **b)** Configurazione embedded dell'application senza dipendenze esterne

`@JMSConnectionFactoryDefinition` permette di definire connection factory nel codice, riducendo le dipendenze di configurazione esterna.

### 2. **c)** Quando si vuole evitare la configurazione JNDI dell'application server

È particolarmente utile per portabilità e per evitare configurazioni specifiche dell'AS.

### 3. **a, b, c, d, e)** clientID, maxPoolSize, transacted, acknowledgeMode, brokerURL

Tutte queste proprietà possono essere configurate in una JMSConnectionFactoryDefinition.

### 4. **c)** System.currentTimeMillis() viene valutato al deployment, non runtime

Il valore viene calcolato una sola volta al deployment, non ad ogni valutazione del selector.

### 5. **c)** `quantity > 0 AND description CONTAINS 'premium'`

CONTAINS non è un operatore supportato nei message selector JMS. Si deve usare LIKE.

### 6. **a, b, c, d)** LIKE, BETWEEN, IN, IS NULL/IS NOT NULL

REGEX non è supportato nei message selector standard JMS.

### 7. **b)** Il messaggio viene spostato manualmente in DLQ e consumato dalla coda originale

Il codice sposta manualmente il messaggio in DLQ e non rilancia eccezioni, quindi viene consumato.

### 8. **b)** Exponential backoff con retry limitati

Per errori temporanei, l'exponential backoff evita di sovraccaricare il sistema che ha problemi.

### 9. **a, b, c, d, e)** Timestamp, motivo, tentativi, queue origine, stack trace

Tutte queste informazioni sono utili per il debugging e la risoluzione dei problemi.

### 10. **c)** `prefetchSize = "100"`

Il prefetch size determina quanti messaggi vengono precaricati, avendo l'impatto maggiore sul throughput.

### 11. **b)** Migliori performance con possibili messaggi duplicati

Dups-ok-acknowledge offre performance migliori ma può permettere duplicati in caso di failure.

### 12. **a, b, c, d, e)** NON_PERSISTENT, connection pooling, prefetch, acknowledge mode, processing asincrono

Tutte sono strategie valide per ottimizzare le performance JMS.

### 13. **c)** Tutte le operazioni (database + JMS) vengono annullate

La RuntimeException in una transazione JTA causa il rollback completo di tutte le risorse.

### 14. **c)** Durante la fase di commit del protocollo 2PC

In XA, i messaggi vengono effettivamente committati durante la fase finale di commit del 2PC.

### 15. **a, b, d, e)** Consistenza ACID, atomicità, recovery automatico, coordinamento broker

Le transazioni XA non migliorano le performance, anzi le peggiorano.

### 16. **c)** `processingTimer` - tempo di elaborazione

Il tempo di elaborazione è la metrica più importante per identificare problemi di performance.

### 17. **c)** Possibili colli di bottiglia nel processing o backlog nella coda

Un'alta età del messaggio indica che i messaggi restano in coda troppo a lungo.

### 18. **a, b, c, d, e)** Queue depth, throughput, processing time, redelivery rate, connection pool

Tutte sono metriche critiche per il monitoring JMS in produzione.

### 19. **b)** Disaccoppiamento e routing automatico basato sul contenuto

Il Content-Based Router permette routing intelligente basato sul contenuto dei messaggi.

### 20. **b)** Trasformare il formato dei messaggi tra sistemi diversi

Il Message Translator si occupa della trasformazione dei formati tra sistemi eterogenei.

### 21. **a, b, c, d, e)** Content-Based Router, Message Translator, Scatter-Gather, Dead Letter Channel, Message Sequencer

Tutti questi pattern sono comunemente implementati con JMS.

### 22. **d)** Tutti i precedenti: SSL/TLS, autenticazione certificati, autorizzazione ruoli

La configurazione implementa crittografia SSL, autenticazione tramite certificati e autorizzazione EJB.

### 23. **a)** Verifica che il certificato del server corrisponda al hostname

`verifyHostName=true` abilita la verifica che il certificato SSL corrisponda al hostname del server.

### 24. **a, b, c, d, e)** Username/password, certificati SSL, JWT, LDAP, Kerberos

Tutti questi meccanismi di autenticazione sono supportati in ambienti JMS enterprise.

### 25. **b)** L'applicazione si connette automaticamente a broker2 o broker3

Il failover URL permette connessione automatica ai broker alternativi quando quello principale non è disponibile.

### 26. **b)** Comportamento deterministico nell'ordine di connessione ai broker

`randomize=false` garantisce che i broker vengano tentati sempre nello stesso ordine.

### 27. **a, b, c, d, e)** Replicazione, failover automatico, load balancing, storage condiviso, health check

Tutte queste strategie contribuiscono all'alta disponibilità in un cluster JMS.

### 28. **d)** Tutti i precedenti

Il codice può identificare memory leak, degradazione performance e colli di bottiglia attraverso monitoring continuo.

### 29. **b)** Lo stato delle connessioni JMS e del connection pool

Problemi di connessione sono spesso la causa principale quando un MDB smette di processare messaggi.

### 30. **a, b, c, d, e)** JConsole, log analysis, broker console, thread dump, network monitoring

Tutti questi strumenti sono essenziali per il troubleshooting completo di sistemi JMS.
