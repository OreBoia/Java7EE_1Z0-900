# Funzionalità Avanzate di JMS

JMS, in particolare con le migliorie introdotte in JMS 2.0, offre una serie di funzionalità avanzate che semplificano lo sviluppo, migliorano la leggibilità del codice e forniscono pattern di messaggistica più flessibili.

## 1. API Fluent per i Producer

JMS 2.0 ha introdotto un'API "fluente" (fluent) per i `JMSProducer`, che permette di concatenare le chiamate per configurare e inviare un messaggio in una singola istruzione chiara e leggibile.

Questo approccio elimina la necessità di creare un oggetto `Message`, impostare le sue proprietà separatamente e poi inviarlo.

### Esempio: Invio di un messaggio con API fluente

```java
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Queue;

public class FluentProducer {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/queue/myQueue")
    private Queue myQueue;

    public void sendUrgentMessage(String payload, String correlationId) {
        
        context.createProducer()
               // Imposta una proprietà custom
               .setProperty("source", "billingSystem")
               
               // Imposta l'ID di correlazione per legare richiesta/risposta
               .setJMSCorrelationID(correlationId)
               
               // Imposta la priorità del messaggio (0-9, 9 è la più alta)
               .setPriority(8)
               
               // Imposta la modalità di consegna (PERSISTENT o NON_PERSISTENT)
               .setDeliveryMode(DeliveryMode.PERSISTENT)
               
               // Imposta un tempo di vita per il messaggio (in millisecondi)
               .setTimeToLive(3600000) // 1 ora
               
               // Infine, invia il messaggio
               .send(myQueue, payload);
               
        System.out.println("Messaggio urgente inviato con API fluente.");
    }
}
```

## 2. Invio Diretto di Oggetti Serializzabili

Con JMS 2.0, non è più necessario creare manualmente un `ObjectMessage` per inviare un oggetto Java. Se l'oggetto implementa l'interfaccia `java.io.Serializable`, può essere passato direttamente al metodo `send()`. JMS si occuperà di creare l' `ObjectMessage` dietro le quinte.

### Esempio: Invio di un oggetto `Order`

**La classe serializzabile:**

```java
import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String orderId;
    private final double amount;
    
    public Order(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }
    
    // ... getter e toString ...
}
```

**Il producer che invia l'oggetto:**

```java
public class OrderProducer {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/queue/orderQueue")
    private Queue orderQueue;

    public void sendOrder(Order order) {
        // Invia direttamente l'oggetto Order. JMS lo serializzerà.
        context.createProducer().send(orderQueue, order);
        System.out.println("Oggetto Order inviato: " + order);
    }
}
```

**Il consumer che riceve l'oggetto:**

```java
// In un MDB o un consumer sincrono
public void onMessage(Message message) {
    try {
        // Estrae l'oggetto direttamente specificando la classe
        Order receivedOrder = message.getBody(Order.class);
        System.out.println("Oggetto Order ricevuto: " + receivedOrder);
        // ... processa l'ordine ...
    } catch (JMSException e) {
        e.printStackTrace();
    }
}
```

## 3. Shared Consumer (Consumer Condivisi)

JMS 2.0 ha introdotto le sottoscrizioni condivise per i topic, che permettono a più consumer di condividere il carico di lavoro di una singola sottoscrizione. Questo pattern è utile per scalare orizzontalmente i consumer di un topic, garantendo al contempo che ogni messaggio venga processato una sola volta dal gruppo di consumer.

Esistono due tipi di consumer condivisi:

- **Shared Durable Consumer**: Una sottoscrizione durevole condivisa da più consumer.
- **Shared Non-Durable Consumer**: Una sottoscrizione non durevole condivisa. Se tutti i consumer si disconnettono, la sottoscrizione cessa di esistere e i messaggi andranno persi.

### Esempio: Creazione di un Shared Consumer (non MDB)

Questo esempio mostra come creare un consumer condiviso programmaticamente. In un ambiente Java EE, si utilizzerebbe più comunemente un MDB con la proprietà `subscriptionShared = true`.

```java
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Topic;

public class SharedTopicReader implements Runnable {

    private final JMSContext context;
    private final Topic topic;
    private final String subscriptionName = "live-data-feed";

    public SharedTopicReader(JMSContext context, Topic topic) {
        this.context = context;
        this.topic = topic;
    }

    @Override
    public void run() {
        // Crea un consumer condiviso non durevole
        // Tutti i consumer che usano lo stesso nome ("live-data-feed")
        // condivideranno i messaggi provenienti dal topic.
        try (JMSConsumer sharedConsumer = context.createSharedConsumer(topic, subscriptionName)) {
            
            System.out.println("Consumer " + Thread.currentThread().getName() + " in ascolto sul topic condiviso.");
            
            while (true) {
                String message = sharedConsumer.receiveBody(String.class);
                if (message != null) {
                    System.out.println("Thread " + Thread.currentThread().getName() + " ha ricevuto: " + message);
                }
            }
        }
    }
}
```

## JMS nel Contesto Moderno

Sebbene esistano sistemi di messaggistica più recenti e specializzati come **Apache Kafka** (orientato allo streaming di eventi e log) e protocolli come **AMQP** (es. RabbitMQ), JMS rimane uno standard estremamente rilevante e diffuso in ambito enterprise Java.

- **Integrazione Standard**: È lo standard Java EE, quindi è perfettamente integrato con gli application server (es. WildFly/JBoss EAP con Artemis, WebLogic JMS, IBM MQ).
- **Semplicità e Robustezza**: Offre un modello di programmazione maturo e ben compreso per la messaggistica asincrona tradizionale (code e topic).
- **Transazionalità JTA**: La sua integrazione con le transazioni JTA è un punto di forza per garantire la coerenza dei dati in architetture complesse.

La scelta tra JMS e altri sistemi dipende dai requisiti specifici del caso d'uso (es. messaggistica tradizionale vs. event streaming, throughput, persistenza dei log).

## Tabella Riepilogativa delle Funzionalità

| Funzionalità | Descrizione | Vantaggio Principale |
| --- | --- | --- |
| **API Fluent** | Permette di concatenare metodi per configurare e inviare messaggi. | Codice più pulito, leggibile e compatto. |
| **Invio di Oggetti** | Possibilità di inviare oggetti `Serializable` direttamente, senza creare `ObjectMessage`. | Semplificazione del codice del producer e del consumer. |
| **Shared Consumer** | Permette a più consumer di condividere una sottoscrizione a un topic, distribuendo il carico. | Scalabilità orizzontale per i consumer di topic. |
| **`JMSContext`** | API unificata che astrae `Connection` e `Session`. | Riduzione drastica del codice boilerplate. |
