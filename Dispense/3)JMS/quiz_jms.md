# Quiz Avanzato su JMS (Java Message Service) - Domande Miste con Codice

Questo quiz avanzato copre i concetti di Java Message Service (JMS) con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Modelli di Messaggistica

### 🔵 Domanda 1

Osserva il seguente codice:

```java
@Resource(lookup = "java:/jms/queue/orderQueue")
private Queue orderQueue;

@Inject
private JMSContext context;

public void sendOrder(String orderData) {
    context.createProducer().send(orderQueue, orderData);
}
```

Se tre consumer sono in ascolto sulla stessa coda e viene inviato un messaggio, quanti consumer riceveranno il messaggio?

- a) Tutti e tre i consumer
- b) Solo un consumer
- c) Dipende dalla configurazione del broker
- d) Due consumer casuali

---

### 🟢 Domanda 2

Quali delle seguenti affermazioni sul modello **Point-to-Point** sono corrette? (Seleziona tutte quelle corrette)

- a) Un messaggio può essere consumato da più consumer contemporaneamente
- b) Offre load balancing naturale tra più consumer
- c) Il messaggio rimane nella coda finché non viene consumato con successo
- d) È basato sul concetto di Topic
- e) È ideale per task queue e elaborazione ordini

---

### 💻 Domanda 3

Analizza il seguente scenario Publish/Subscribe:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/topic/newsTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", 
                                propertyValue = "javax.jms.Topic")
    }
)
public class NewsReaderMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        try {
            String newsContent = message.getBody(String.class);
            System.out.println("Notizia ricevuta: " + newsContent);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
```

Se questo MDB è offline quando vengono pubblicati 5 messaggi sul topic, quanti messaggi riceverà quando tornerà online?

- a) Tutti e 5 i messaggi
- b) Nessun messaggio
- c) Solo i messaggi pubblicati dopo che è tornato online
- d) Dipende dalla configurazione del broker

---

### 🔵 Domanda 4

Quale modello di messaggistica è più appropriato per implementare un **sistema di notifiche** dove più applicazioni devono ricevere lo stesso aggiornamento?

- a) Point-to-Point con Queue
- b) Publish/Subscribe con Topic
- c) Entrambi sono equivalenti
- d) Dipende dal numero di consumer

---

## 2. Componenti JMS in Java EE

### 💻 Domanda 5

Osserva questa definizione di destinazione:

```java
@JMSDestinationDefinition(
    name = "java:global/jms/orderQueue",
    interfaceName = "javax.jms.Queue",
    destinationName = "PhysicalOrderQueue"
)
@JMSDestinationDefinition(
    name = "java:global/jms/alertTopic",
    interfaceName = "javax.jms.Topic",
    destinationName = "PhysicalAlertTopic"
)
@Singleton
public class JmsResources {
}
```

Quale vantaggio offre l'uso di `@JMSDestinationDefinition`?

- a) Migliori performance di runtime
- b) Portabilità dell'applicazione tra diversi application server
- c) Gestione automatica delle transazioni
- d) Thread-safety migliorata

---

### 🟢 Domanda 6

Quali delle seguenti affermazioni su **JMSContext** (JMS 2.0) sono corrette? (Seleziona tutte)

- a) Combina le funzionalità di Connection e Session
- b) Può essere iniettato direttamente con @Inject in ambiente CDI
- c) È Auto-Closeable per uso in try-with-resources
- d) Richiede sempre l'iniezione manuale di ConnectionFactory
- e) Semplifica significativamente l'API JMS

---

### 💻 Domanda 7

Analizza questo producer che usa l'API fluent:

```java
public class NotificationProducer {
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/alertQueue")
    private Queue alertQueue;
    
    public void sendAlert(String message, String severity) {
        context.createProducer()
               .setProperty("severity", severity)
               .setPriority(severity.equals("HIGH") ? 9 : 5)
               .setTimeToLive(3600000) // 1 ora
               .send(alertQueue, message);
    }
}
```

Cosa succede a un messaggio con severity "HIGH" dopo 2 ore se non è stato consumato?

- a) Il messaggio viene eliminato automaticamente
- b) Il messaggio rimane nella coda indefinitamente
- c) Il messaggio viene spostato in una dead letter queue
- d) Dipende dalla configurazione del broker

---

## 3. Message-Driven Beans (MDB)

### 🔵 Domanda 8

Quale è il comportamento **di default** per le transazioni in un MDB?

- a) Le transazioni sono disabilitate
- b) Ogni messaggio viene processato in una transazione JTA separata
- c) I messaggi sono processati in batch transazionali
- d) Le transazioni devono essere gestite manualmente

---

### 💻 Domanda 9

Analizza questo MDB transazionale:

```java
@MessageDriven(...)
public class OrderProcessorMDB implements MessageListener {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void onMessage(Message message) {
        try {
            String orderData = message.getBody(String.class);
            Order order = parseOrder(orderData);
            
            // Salva nel database
            em.persist(order);
            
            // Simula un errore casuale
            if (Math.random() < 0.3) {
                throw new RuntimeException("Errore simulato!");
            }
            
            System.out.println("Ordine processato: " + order.getId());
            
        } catch (JMSException e) {
            throw new RuntimeException("Errore JMS", e);
        }
    }
}
```

Se viene lanciata la RuntimeException "Errore simulato!", cosa succede?

- a) Solo il salvataggio su database viene annullato
- b) Il messaggio viene consumato ma l'ordine non viene salvato
- c) Sia il salvataggio che il consumo del messaggio vengono annullati (rollback)
- d) L'applicazione si blocca

---

### 🟢 Domanda 10

Quali delle seguenti sono caratteristiche degli **MDB**? (Seleziona tutte)

- a) Supporto transazionale automatico
- b) Gestione della concorrenza tramite pool di istanze
- c) Possibilità di sottoscrizioni durevoli per i Topic
- d) Accesso diretto ai metodi tramite interfacce business
- e) Gestione automatica del ciclo di vita da parte del container

---

### 💻 Domanda 11

Osserva questa configurazione per sottoscrizione durevole:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/topic/criticalAlerts"),
        @ActivationConfigProperty(propertyName = "destinationType", 
                                propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", 
                                propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "subscriptionName", 
                                propertyValue = "CriticalAlertsSubscription"),
        @ActivationConfigProperty(propertyName = "clientId", 
                                propertyValue = "AlertingApp")
    }
)
public class CriticalAlertMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processa alert critico
        System.out.println("Alert critico ricevuto!");
    }
}
```

Cosa succede ai messaggi pubblicati sul topic quando questo MDB è offline?

- a) I messaggi vengono persi definitivamente
- b) I messaggi vengono conservati e consegnati quando l'MDB torna online
- c) I messaggi vengono inviati ad altri subscriber
- d) I messaggi vengono spostati in una coda temporanea

---

## 4. Transazioni JMS

### 🔵 Domanda 12

In una transazione JTA che include operazioni JMS e database, quando viene effettivamente inviato il messaggio JMS?

- a) Immediatamente quando viene chiamato send()
- b) Al commit della transazione JTA
- c) Al rollback della transazione JTA
- d) Dipende dal tipo di messaggio

---

### 💻 Domanda 13

Analizza questo servizio transazionale:

```java
@Stateless
public class OrderService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/notificationQueue")
    private Queue notificationQueue;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processOrder(Order order) throws BusinessException {
        try {
            // 1. Validazione
            if (!order.isValid()) {
                throw new BusinessException("Ordine non valido");
            }
            
            // 2. Salvataggio database
            em.persist(order);
            
            // 3. Invio notifica
            context.createProducer().send(notificationQueue, 
                "Nuovo ordine: " + order.getId());
            
        } catch (DatabaseException e) {
            throw new RuntimeException("Errore database", e);
        }
    }
}
```

Se viene lanciata la `BusinessException`, cosa succede al messaggio JMS?

- a) Il messaggio viene inviato normalmente
- b) Il messaggio non viene inviato (rollback)
- c) Il messaggio viene spostato in una dead letter queue
- d) Dipende dalla configurazione del broker

---

### 🟢 Domanda 14

Quali tipi di eccezioni causano il **rollback automatico** di una transazione JTA in un MDB? (Seleziona tutte)

- a) RuntimeException
- b) EJBException
- c) JMSException
- d) Error
- e) Checked exceptions personalizzate

---

### 💻 Domanda 15

Osserva questo pattern di gestione errori in un MDB:

```java
@MessageDriven(...)
public class PaymentProcessorMDB implements MessageListener {
    
    @Resource
    private MessageDrivenContext mdcContext;
    
    @Override
    public void onMessage(Message message) {
        try {
            String paymentData = message.getBody(String.class);
            
            if (isTemporaryError(paymentData)) {
                // Errore temporaneo - vogliamo il retry
                throw new RuntimeException("Errore temporaneo, retry necessario");
            }
            
            if (isPermanentError(paymentData)) {
                // Errore permanente - non vogliamo il retry
                System.err.println("Errore permanente, scarto il messaggio");
                // Non lanciamo eccezioni - il messaggio viene consumato
                return;
            }
            
            processPayment(paymentData);
            
        } catch (JMSException e) {
            // Forza il rollback per problemi JMS
            mdcContext.setRollbackOnly();
        }
    }
}
```

Quale strategia implementa questo codice per gli errori permanenti?

- a) Rollback e retry automatico
- b) Consume del messaggio senza processing
- c) Invio del messaggio a una dead letter queue
- d) Interruzione del processing di tutti i messaggi

---

## 5. API JMS 2.0 e Funzionalità Avanzate

### 💻 Domanda 16

Analizza questo producer che invia oggetti serializzabili:

```java
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private String orderId;
    private double amount;
    
    // costruttori, getter, setter
}

@Stateless
public class OrderProducer {
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/orderQueue")
    private Queue orderQueue;
    
    public void sendOrder(Order order) {
        // Invio diretto dell'oggetto
        context.createProducer()
               .setProperty("orderType", "STANDARD")
               .send(orderQueue, order);
    }
}
```

Che tipo di messaggio JMS viene creato automaticamente quando si invia l'oggetto `Order`?

- a) TextMessage
- b) BytesMessage
- c) ObjectMessage
- d) MapMessage

---

### 🟢 Domanda 17

Quali delle seguenti sono **novità introdotte in JMS 2.0**? (Seleziona tutte)

- a) API fluent per JMSProducer
- b) JMSContext che unifica Connection e Session
- c) Invio diretto di oggetti serializzabili
- d) Message-Driven Beans
- e) Supporto per CDI e iniezione con @Inject

---

### 💻 Domanda 18

Osserva questo consumer che riceve oggetti:

```java
@MessageDriven(...)
public class OrderConsumerMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMsg = (ObjectMessage) message;
                Order order = (Order) objMsg.getObject();
                processOrder(order);
            } else {
                // Usa l'API JMS 2.0 per ricevere direttamente l'oggetto
                Order order = message.getBody(Order.class);
                processOrder(order);
            }
        } catch (JMSException e) {
            throw new RuntimeException("Errore nella ricezione", e);
        }
    }
}
```

Quale vantaggio offre l'uso di `message.getBody(Order.class)` rispetto al cast manuale?

- a) Migliori performance
- b) Type safety e codice più pulito
- c) Supporto per più formati di serializzazione
- d) Gestione automatica delle eccezioni

---

## 6. Proprietà dei Messaggi e Priorità

### 🔵 Domanda 19

Osserva questo codice che imposta proprietà dei messaggi:

```java
context.createProducer()
       .setProperty("source", "billingSystem")
       .setProperty("priority", "HIGH")
       .setPriority(8)
       .setTimeToLive(1800000) // 30 minuti
       .send(queue, messageData);
```

Qual è la priorità effettiva del messaggio?

- a) "HIGH" (stringa)
- b) 8 (numero)
- c) 0 (default)
- d) Causa un errore

---

### 💻 Domanda 20

Analizza questo message selector:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/orderQueue"),
        @ActivationConfigProperty(propertyName = "messageSelector", 
                                propertyValue = "priority = 'HIGH' AND amount > 1000")
    }
)
public class HighValueOrderMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processa solo ordini ad alta priorità e alto valore
    }
}
```

Quali messaggi riceverà questo MDB?

- a) Tutti i messaggi nella coda
- b) Solo messaggi con proprietà priority = 'HIGH'
- c) Solo messaggi con proprietà amount > 1000
- d) Solo messaggi che soddisfano entrambe le condizioni

---

## 7. Gestione delle Eccezioni e Affidabilità

### 🟢 Domanda 21

Quali strategie possono essere utilizzate per gestire **messaggi non processabili** (poison messages)? (Seleziona tutte)

- a) Dead Letter Queue (DLQ)
- b) Limitazione del numero di redelivery
- c) Message selector per filtrare messaggi problematici
- d) Logica di business per scartare messaggi non validi
- e) Rollback infinito per garantire elaborazione

---

### 💻 Domanda 22

Osserva questa configurazione di redelivery:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/orderQueue"),
        @ActivationConfigProperty(propertyName = "maxSession", 
                                propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "redeliveryAttempts", 
                                propertyValue = "3")
    }
)
public class OrderMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        try {
            processOrder(message.getBody(String.class));
        } catch (Exception e) {
            throw new RuntimeException("Processing failed", e);
        }
    }
}
```

Cosa succede a un messaggio che fallisce 4 volte consecutive?

- a) Viene ritentato indefinitamente
- b) Viene scartato definitivamente
- c) Viene spostato in una dead letter queue (se configurata)
- d) Causa l'arresto dell'MDB

---

## 8. Pattern di Messaggistica

### 💻 Domanda 23

Analizza questo pattern Request-Reply:

```java
// Sender
@Stateless
public class OrderQueryService {
    
    @Inject
    private JMSContext context;
    
    @Resource(lookup = "java:/jms/queue/queryQueue")
    private Queue queryQueue;
    
    @Resource(lookup = "java:/jms/queue/responseQueue")
    private Queue responseQueue;
    
    public void queryOrderStatus(String orderId) {
        String correlationId = UUID.randomUUID().toString();
        
        context.createProducer()
               .setJMSReplyTo(responseQueue)
               .setJMSCorrelationID(correlationId)
               .send(queryQueue, orderId);
    }
}

// Receiver
@MessageDriven(...)
public class QueryProcessorMDB implements MessageListener {
    
    @Inject
    private JMSContext context;
    
    @Override
    public void onMessage(Message message) {
        try {
            String orderId = message.getBody(String.class);
            String status = lookupOrderStatus(orderId);
            
            // Invia risposta
            context.createProducer()
                   .setJMSCorrelationID(message.getJMSCorrelationID())
                   .send(message.getJMSReplyTo(), status);
                   
        } catch (JMSException e) {
            throw new RuntimeException("Query processing failed", e);
        }
    }
}
```

A cosa serve il `JMSCorrelationID` in questo pattern?

- a) Identificare il tipo di messaggio
- b) Collegare la richiesta con la risposta corrispondente
- c) Impostare la priorità del messaggio
- d) Configurare il timeout della risposta

---

### 🔵 Domanda 24

Nel pattern **Message Aggregator**, qual è la funzione principale?

- a) Dividere un messaggio grande in più messaggi piccoli
- b) Raccogliere più messaggi correlati e combinarli in uno
- c) Filtrare messaggi in base a criteri specifici
- d) Instradare messaggi verso destinazioni diverse

---

## 9. Performance e Best Practices

### 🟢 Domanda 25

Quali delle seguenti sono **best practices** per ottimizzare le performance JMS? (Seleziona tutte)

- a) Usare connection pooling quando possibile
- b) Configurare appropriatamente la dimensione del pool MDB
- c) Usare messaggi persistenti solo quando necessario
- d) Implementare message batching per operazioni massive
- e) Evitare l'uso di message selector complessi

---

### 💻 Domanda 26

Osserva questa configurazione di performance:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/highVolumeQueue"),
        @ActivationConfigProperty(propertyName = "maxSession", 
                                propertyValue = "20"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", 
                                propertyValue = "Auto-acknowledge")
    }
)
public class HighVolumeMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processing veloce senza operazioni bloccanti
        processQuickly(message);
    }
}
```

Quale aspetto di questa configurazione migliora il throughput?

- a) L'uso di Auto-acknowledge mode
- b) Il valore alto di maxSession (20)
- c) Il nome della destinazione
- d) Il processing veloce nel metodo onMessage

---

## 10. Sicurezza e Configurazione

### 💻 Domanda 27

Analizza questa configurazione di sicurezza JMS:

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", 
                                propertyValue = "java:/jms/queue/secureQueue"),
        @ActivationConfigProperty(propertyName = "user", 
                                propertyValue = "jmsuser"),
        @ActivationConfigProperty(propertyName = "password", 
                                propertyValue = "jmspassword")
    }
)
@RolesAllowed("jms-processor")
public class SecureOrderMDB implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        // Processing sicuro
    }
}
```

Quali livelli di sicurezza sono implementati in questo MDB?

- a) Solo autenticazione JMS
- b) Solo autorizzazione EJB  
- c) Sia autenticazione JMS che autorizzazione EJB
- d) Nessuna sicurezza reale

---

### 🔵 Domanda 28

Per un'applicazione che deve processare milioni di messaggi al giorno, quale modalità di delivery è più appropriata per messaggi non critici?

- a) PERSISTENT - per garantire durabilità
- b) NON_PERSISTENT - per massimizzare performance
- c) AUTO - lasciare decidere al broker
- d) Non fa differenza

---

## 11. Troubleshooting e Monitoring

### 💻 Domanda 29

Osserva questo MDB di monitoring:

```java
@MessageDriven(...)
public class MonitoringMDB implements MessageListener {
    
    @Inject
    private Logger logger;
    
    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        
        try {
            String messageId = message.getJMSMessageID();
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            
            logger.info("Processing message: {} (delivery #{})", messageId, deliveryCount);
            
            // Process message
            processMessage(message);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Message {} processed in {}ms", messageId, duration);
            
        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw new RuntimeException("Processing failed", e);
        }
    }
}
```

Quale informazione fornisce `JMSXDeliveryCount`?

- a) Il numero totale di messaggi processati
- b) Il numero di volte che questo specifico messaggio è stato consegnato
- c) Il numero di consumer attivi sulla destinazione
- d) La priorità del messaggio

---

### 🟢 Domanda 30

Quali metriche sono importanti per il **monitoring di sistemi JMS**? (Seleziona tutte)

- a) Numero di messaggi in coda (queue depth)
- b) Tempo medio di processing per messaggio
- c) Numero di redelivery per messaggio
- d) Throughput di messaggi al secondo
- e) Numero di connessioni attive

---

---

## Risposte Corrette

### 1. **b)** Solo un consumer

Nel modello Point-to-Point, ogni messaggio viene consumato da un solo consumer, anche se ce ne sono diversi in ascolto.

### 2. **b, c, e)** Offre load balancing, il messaggio rimane finché non consumato, ideale per task queue

Point-to-Point non permette consumo multiplo ed è basato su Queue, non Topic.

### 3. **b)** Nessun messaggio

Senza sottoscrizione durevole, i messaggi pubblicati quando il subscriber è offline vengono persi.

### 4. **b)** Publish/Subscribe con Topic

Il modello Pub/Sub permette a più subscriber di ricevere lo stesso messaggio simultaneamente.

### 5. **b)** Portabilità dell'applicazione tra diversi application server

`@JMSDestinationDefinition` permette di definire destinazioni nel codice, rendendole portatili.

### 6. **a, b, c, e)** Combina Connection/Session, iniettabile con @Inject, Auto-Closeable, semplifica l'API

Non richiede sempre l'iniezione manuale di ConnectionFactory.

### 7. **a)** Il messaggio viene eliminato automaticamente

`setTimeToLive(3600000)` imposta una scadenza di 1 ora; dopo 2 ore il messaggio viene eliminato.

### 8. **b)** Ogni messaggio viene processato in una transazione JTA separata

I metodi `onMessage` degli MDB sono transazionali per default con CMT.

### 9. **c)** Sia il salvataggio che il consumo del messaggio vengono annullati (rollback)

La RuntimeException causa il rollback completo della transazione JTA.

### 10. **a, b, c, e)** Supporto transazionale, pool di istanze, sottoscrizioni durevoli, gestione ciclo di vita

Gli MDB non hanno interfacce business per accesso diretto.

### 11. **b)** I messaggi vengono conservati e consegnati quando l'MDB torna online

La sottoscrizione durevole garantisce la conservazione dei messaggi durante l'offline.

### 12. **b)** Al commit della transazione JTA

In una transazione JTA, l'invio JMS viene effettuato solo al commit della transazione.

### 13. **b)** Il messaggio non viene inviato (rollback)

Le checked exception non causano rollback automatico, ma qui viene rilanciata come RuntimeException.

### 14. **a, b, d)** RuntimeException, EJBException, Error

Solo le unchecked exception causano rollback automatico in CMT.

### 15. **b)** Consume del messaggio senza processing

Non lanciando eccezioni per errori permanenti, il messaggio viene consumato senza retry.

### 16. **c)** ObjectMessage

JMS 2.0 crea automaticamente un ObjectMessage quando si invia un oggetto Serializable.

### 17. **a, b, c, e)** API fluent, JMSContext, invio oggetti serializzabili, supporto CDI

Gli MDB esistevano già prima di JMS 2.0.

### 18. **b)** Type safety e codice più pulito

`getBody(Order.class)` fornisce type safety ed evita cast manuali.

### 19. **b)** 8 (numero)

`setPriority(8)` imposta la priorità numerica, che ha precedenza sulla proprietà stringa.

### 20. **d)** Solo messaggi che soddisfano entrambe le condizioni

Il message selector usa AND, quindi entrambe le condizioni devono essere vere.

### 21. **a, b, c, d)** DLQ, limitazione redelivery, message selector, logica business

Il rollback infinito non è una strategia valida.

### 22. **c)** Viene spostato in una dead letter queue (se configurata)

Dopo il numero massimo di redelivery, i messaggi vanno tipicamente in DLQ.

### 23. **b)** Collegare la richiesta con la risposta corrispondente

Il correlation ID permette di associare richieste e risposte nel pattern Request-Reply.

### 24. **b)** Raccogliere più messaggi correlati e combinarli in uno

Il Message Aggregator raccoglie e combina messaggi correlati.

### 25. **a, b, c, d, e)** Connection pooling, pool MDB configurato, persistenza selettiva, batching, selector semplici

Tutte sono best practices valide per le performance JMS.

### 26. **b)** Il valore alto di maxSession (20)

`maxSession` alto permette più sessioni concorrenti, aumentando il throughput.

### 27. **c)** Sia autenticazione JMS che autorizzazione EJB

Le proprietà user/password forniscono autenticazione JMS, `@RolesAllowed` autorizzazione EJB.

### 28. **b)** NON_PERSISTENT - per massimizzare performance

Per messaggi non critici, NON_PERSISTENT offre migliori performance.

### 29. **b)** Il numero di volte che questo specifico messaggio è stato consegnato

`JMSXDeliveryCount` indica quante volte il messaggio è stato tentato di consegnare.

### 30. **a, b, c, d, e)** Queue depth, tempo processing, redelivery, throughput, connessioni attive

Tutte sono metriche importanti per il monitoring JMS.
