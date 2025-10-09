# Modelli di Messaggistica JMS

Java Message Service (JMS) è un'API standard di Java per l'invio e la ricezione di messaggi tra componenti software in modo asincrono e disaccoppiato. JMS definisce due modelli di messaggistica principali che determinano come i messaggi vengono scambiati.

## 1. Modello Point-to-Point (P2P)

Questo modello è basato sul concetto di **Queue** (coda).

### Caratteristiche Principali

- **Comunicazione Uno-a-Uno**: Un messaggio inviato a una coda viene consegnato a un solo consumer, anche se ce ne sono molti in ascolto.
- **Load Balancing Naturale**: Se più consumer sono in ascolto sulla stessa coda, il broker JMS distribuisce i messaggi tra di loro. Questo permette di scalare l'elaborazione dei messaggi in modo semplice
- **Affidabilità**: Il messaggio rimane nella coda finché non viene consumato con successo.
- **Utilizzo Tipico**: Task queue, elaborazione di ordini, comunicazioni che richiedono che un solo destinatario processi il messaggio.

### Componenti

- **Producer**: Invia messaggi alla coda.
- **Queue**: La destinazione dove i messaggi vengono memorizzati temporaneamente.
- **Consumer**: Riceve e processa messaggi dalla coda.

### Esempio di Codice Applicato (JMS 2.0)

**Producer che invia a una Queue**

```java
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

public class P2PProducer {

    @Inject
    private JMSContext context; // Contesto JMS iniettato da CDI

    @Resource(lookup = "java:/jms/queue/myQueue") // Risorsa JNDI della coda
    private Queue myQueue;

    public void sendMessage(String messageText) {
        // Il producer viene creato dal contesto e invia il messaggio
        context.createProducer().send(myQueue, messageText);
        System.out.println("Messaggio inviato alla coda: " + messageText);
    }
}
```

**Consumer che riceve da una Queue (Message-Driven Bean)**
Un MDB è il modo più semplice e standard in Java EE per consumare messaggi.

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/myQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    }
)
public class P2PConsumer implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("Messaggio ricevuto dalla coda: " + textMessage.getText());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
```

## 2. Modello Publish/Subscribe (Pub/Sub)

Questo modello è basato sul concetto di **Topic**.

### Caratteristiche Principali

- **Comunicazione Uno-a-Molti (Broadcast)**: Un messaggio pubblicato su un topic viene consegnato a *tutti* i consumer che hanno una sottoscrizione attiva per quel topic.
- **Disaccoppiamento Totale**: Il publisher non sa nulla dei subscriber (e viceversa).
- **Utilizzo Tipico**: Notifiche di eventi, aggiornamenti in tempo reale, feed di notizie, sistemi in cui più componenti devono reagire allo stesso evento.

### Tipi di Sottoscrizione

I consumer (subscriber) possono avere diversi tipi di sottoscrizione:

- **Non-Durable (Non Duratura)**: Il consumer riceve i messaggi solo se è attivo e connesso al momento della pubblicazione. Se è offline, perde tutti i messaggi inviati in sua assenza. Questa è la modalità predefinita.
- **Durable (Duratura)**: Il broker JMS memorizza i messaggi per il subscriber anche se è offline. Quando il subscriber si riconnette, riceve tutti i messaggi accumulati. Per creare una sottoscrizione duratura, il consumer deve fornire un **Client ID univoco** e un **nome per la sottoscrizione**.
- **Shared (Condivisa - da JMS 2.0)**: Permette a più consumer di condividere una singola sottoscrizione (sia durable che non-durable). I messaggi inviati al topic vengono distribuiti tra i consumer condivisi, combinando il modello pub/sub con il load balancing del P2P.

### Esempio di Codice (JMS 2.0)

**Publisher che pubblica su un Topic**

```java
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;

public class PubSubPublisher {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/topic/myTopic")
    private Topic myTopic;

    public void publishMessage(String messageText) {
        context.createProducer().send(myTopic, messageText);
        System.out.println("Messaggio pubblicato sul topic: " + messageText);
    }
}
```

**Subscriber (MDB) per un Topic**

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/myTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    }
)
public class PubSubSubscriber implements MessageListener {
    // ... implementazione di onMessage come nell'esempio P2P ...
}
```

**Subscriber Duraturo (MDB)**
Per creare una sottoscrizione duratura, si specificano `subscriptionDurability`, `clientId` e `subscriptionName`.

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/myTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "myDurableClient"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "myDurableSubscription")
    }
)
public class DurableSubscriber implements MessageListener {
    // ... implementazione di onMessage ...
}
```

**Subscriber Condiviso (Shared Subscriber - MDB)**
Per creare una sottoscrizione condivisa, si imposta shareSubscriptions a true e si fornisce un subscriptionName. Tutti i consumer che usano lo stesso nome condivideranno la sottoscrizione, e il broker distribuirà i messaggi tra di loro.

```java
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/myTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"), // Abilita la condivisione
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "mySharedSubscription") // Nome della sottoscrizione condivisa
    }
)
public class SharedSubscriber implements MessageListener {
    // ... implementazione di onMessage ...
    // Più istanze di questo MDB possono essere deployate, e il broker
}
```

## Esempio Applicativo: Elaborazione Asincrona di Ordini (P2P)

Vediamo un caso d'uso pratico per il modello Point-to-Point: un sistema di e-commerce che processa gli ordini in modo asincrono. Quando un utente invia un ordine, invece di processarlo immediatamente (bloccando l'utente), inviamo un messaggio a una coda. Un componente specializzato (MDB) elaborerà l'ordine in background.

### 1. Il Producer (Endpoint JAX-RS)

Questo endpoint REST riceve la richiesta di creazione dell'ordine e invia un messaggio a una coda JMS.

```java
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/orders")
@ApplicationScoped
public class OrderEndpoint {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/queue/orderQueue")
    private Queue orderQueue;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(OrderData order) {
        try {
            // Invia l'ID dell'ordine come messaggio alla coda
            context.createProducer().send(orderQueue, String.valueOf(order.getOrderId()));
            
            // Rispondi immediatamente al client
            return Response.accepted("Ordine " + order.getOrderId() + " ricevuto e in elaborazione.").build();
            
        } catch (Exception e) {
            return Response.serverError().entity("Errore nell'invio dell'ordine: " + e.getMessage()).build();
        }
    }
}

// Classe di supporto per i dati dell'ordine
class OrderData {
    private long orderId;
    private String customer;
    // ... altri campi, getter e setter
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}
```

### 2. Il Consumer (Message-Driven Bean)

Questo MDB è in ascolto sulla `orderQueue`. Quando arriva un messaggio, lo elabora, ad esempio aggiornando il database e notificando il magazzino.

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/orderQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    }
)
public class OrderProcessorMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String orderId = ((TextMessage) message).getText();
                System.out.println("Inizio elaborazione ordine: " + orderId);
                
                // Logica di business:
                // 1. Recupera i dettagli dell'ordine dal database usando l'ID
                // 2. Verifica la disponibilità dei prodotti
                // 3. Processa il pagamento
                // 4. Invia notifica al magazzino
                
                Thread.sleep(5000); // Simula un'elaborazione lunga
                
                System.out.println("Ordine " + orderId + " elaborato con successo.");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione dell'ordine: " + e.getMessage());
            // Qui si potrebbe gestire una ri-consegna del messaggio
        }
    }
}
```

Questo approccio migliora la responsività e la resilienza del sistema. L'endpoint REST risponde immediatamente, e se il sistema di elaborazione ordini è temporaneamente sovraccarico o non disponibile, i messaggi si accumulano nella coda e verranno processati non appena il consumer sarà di nuovo disponibile.

## Esempio Applicativo: Notifiche Scalabili con Sottoscrizioni Condivise (Pub/Sub)

Il modello Publish/Subscribe è ideale per le notifiche, ma cosa succede se il sistema che riceve le notifiche deve scalare? Usando le sottoscrizioni condivise (Shared Subscriptions), più istanze di un'applicazione possono condividere il carico di lavoro di una singola sottoscrizione a un topic.

Immaginiamo un sistema che notifica gli aggiornamenti di stato di un social network. Vogliamo che più istanze di un servizio di "Activity Feed" processino questi aggiornamenti per essere altamente disponibili e scalabili.

### 1. Il Publisher (un EJB che pubblica l'evento)

Questo componente pubblica un messaggio ogni volta che si verifica un'attività rilevante.

```java
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ejb.Stateless;

@Stateless
public class UserActivityPublisher {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/topic/activityTopic")
    private Topic activityTopic;

    public void publishUpdate(String updateMessage) {
        System.out.println("Pubblicazione aggiornamento: " + updateMessage);
        context.createProducer().send(activityTopic, updateMessage);
    }
}
```

### 2. I Consumer Condivisi (Message-Driven Bean)

Questo MDB è configurato per usare una sottoscrizione condivisa. Se si deployano tre istanze di questa applicazione su un application server clusterizzato, le tre istanze dell'MDB condivideranno la sottoscrizione `activityFeedSubscription`. Quando un messaggio arriva sull' `activityTopic`, il broker lo consegnerà a solo una delle tre istanze, distribuendo il carico.

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/activityTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        // Abilita la sottoscrizione condivisa
        @ActivationConfigProperty(propertyName = "subscriptionShared", propertyValue = "true"),
        // Nome univoco per la sottoscrizione condivisa
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "activityFeedSubscription")
    }
)
public class ActivityFeedMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String update = ((TextMessage) message).getText();
                // Identifica l'istanza specifica che ha ricevuto il messaggio
                String instanceId = java.util.UUID.randomUUID().toString().substring(0, 8);
                System.out.println("Istanza MDB [" + instanceId + "] ha ricevuto l'aggiornamento: " + update);
                
                // Logica per aggiornare il feed di attività per gli utenti
            }
        } catch (Exception e) {
            System.err.println("Errore nell'elaborazione dell'aggiornamento: " + e.getMessage());
        }
    }
}
```

Questo pattern è estremamente potente per scalare orizzontalmente i consumer in un'architettura basata su eventi, garantendo al contempo che ogni messaggio venga processato una sola volta dal gruppo di consumer.

## Riepilogo e Comandi Principali

| Caratteristica | Point-to-Point (Queue) | Publish/Subscribe (Topic) |
| --- | --- | --- |
| **Modello** | Uno-a-Uno | Uno-a-Molti (Broadcast) |
| **Destinazione** | `javax.jms.Queue` | `javax.jms.Topic` |
| **Destinatari** | Un solo consumer per messaggio | Tutti i subscriber |
| **Load Balancing** | Supportato nativamente tra più consumer | Supportato tramite *Shared Subscriptions* |
| **Messaggi Offline**| I messaggi persistono nella coda | I messaggi persistono solo per *Durable Subscriptions* |
| **Caso d'uso** | Elaborazione di task, ordini | Notifiche, eventi, aggiornamenti |

### Annotazioni e API Chiave

| Elemento | Descrizione |
| --- | --- |
| `@MessageDriven` | Annotazione EJB per creare un consumer asincrono (MDB). |
| `activationConfig` | Proprietà di `@MessageDriven` per configurare la destinazione e il tipo. |
| `destinationLookup` | Specifica il nome JNDI della Queue o del Topic. |
| `destinationType` | Specifica il tipo di destinazione (`javax.jms.Queue` o `javax.jms.Topic`). |
| `JMSContext` | (JMS 2.0) Interfaccia unificata per creare producer e consumer. Semplifica notevolmente l'API. |
| `JMSProducer` | (JMS 2.0) Interfaccia per inviare messaggi. |
| `JMSConsumer` | (JMS 2.0) Interfaccia per ricevere messaggi in modo sincrono. |
| `subscriptionDurability` | Proprietà per MDB per specificare se una sottoscrizione a un topic è `Durable` o `NonDurable`. |
