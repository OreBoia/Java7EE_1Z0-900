# Componenti Fondamentali di JMS in Java EE

Per utilizzare JMS in un'applicazione Java EE, è necessario interagire con alcuni componenti chiave che fungono da ponte tra il codice applicativo e il broker di messaggi (il provider JMS). Con JMS 2.0, l'API è stata notevolmente semplificata.

## 1. ConnectionFactory

La `ConnectionFactory` è un oggetto amministrato, ovvero una risorsa configurata a livello di application server. Il suo scopo è creare connessioni verso il provider JMS. Invece di istanziarla manualmente, viene recuperata tramite JNDI.

### Caratteristiche

- **Punto d'ingresso**: È il primo oggetto necessario per iniziare a comunicare con il broker JMS.
- **Configurazione centralizzata**: Le sue proprietà (come l'URL del broker, le credenziali, etc.) sono gestite dall'amministratore del server, disaccoppiando il codice dalla configurazione dell'infrastruttura.
- **Iniezione**: In Java EE, si ottiene tipicamente tramite iniezione di risorse.

### Esempio di Iniezione

```java
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;

public class MyJmsClient {

    // Il container inietta la ConnectionFactory predefinita o una specifica
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    public void doSomething() {
        // ... ora si può usare la connectionFactory per creare un JMSContext
    }
}
```

**Nota**: Con JMS 2.0 e CDI, spesso non è necessario iniettare la `ConnectionFactory` direttamente, poiché si può iniettare direttamente il `JMSContext`.

## 2. Destination (Queue e Topic)

La `Destination` rappresenta la destinazione dei messaggi: una `Queue` per il modello Point-to-Point o un `Topic` per il modello Publish/Subscribe. Anche queste sono risorse amministrate.

### Caratteristiche

- **Indirizzo del messaggio**: Specifica dove un producer invia i messaggi e da dove un consumer li riceve.
- **Definizione**: Possono essere pre-configurate sul server e recuperate via JNDI, oppure definite programmaticamente all'interno dell'applicazione.

### Esempi di Definizione e Iniezione

**a) Iniezione di una risorsa pre-configurata**

```java
import javax.annotation.Resource;
import javax.jms.Queue;

public class MyProducer {
    @Resource(lookup = "java:/jms/myApp/myQueue")
    private Queue destinationQueue;
    // ...
}
```

**b) Definizione programmatica con `@JMSDestinationDefinition`**
Questa annotazione, introdotta in Java EE 7, permette di definire una destinazione direttamente nel codice, rendendo l'applicazione più portabile.

```java
import javax.jms.JMSDestinationDefinition;
import javax.ejb.Singleton;

// Definisce una coda e un topic a livello di applicazione
@JMSDestinationDefinition(
    name = "java:global/jms/myQueue",
    interfaceName = "javax.jms.Queue",
    destinationName = "PhysicalQueueName"
)
@JMSDestinationDefinition(
    name = "java:global/jms/myTopic",
    interfaceName = "javax.jms.Topic",
    destinationName = "PhysicalTopicName"
)
@Singleton
public class JmsResources {
    // Questa classe serve solo come contenitore per le definizioni
}
```

## 3. JMSContext (Introdotto in JMS 2.0)

`JMSContext` è la vera rivoluzione di JMS 2.0. È un'interfaccia che combina le vecchie `Connection` e `Session` in un unico oggetto, semplificando enormemente il codice.

### Caratteristiche

- **API Semplificata**: Unifica la gestione della connessione e della sessione.
- **Auto-Closeable**: Può essere usato in un blocco `try-with-resources` per garantire che le risorse vengano chiuse correttamente.
- **Iniezione Diretta**: In un ambiente Java EE con CDI, si può iniettare direttamente, rendendo il codice ancora più pulito. Il container si occupa di creare e gestire il contesto usando la `ConnectionFactory` di default.

### Esempi di Utilizzo

**a) Iniezione diretta con `@Inject` (metodo preferito)**

```java
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

public class SimpleProducer {

    @Inject
    private JMSContext context; // Il container fornisce un contesto pronto all'uso

    @Resource(lookup = "java:global/jms/myQueue")
    private Queue myQueue;

    public void sendMessage(String message) {
        context.createProducer().send(myQueue, message);
    }
}
```

**b) Creazione manuale dalla ConnectionFactory**
Utile se si necessita di un controllo più fine (es. transazioni gestite dal client).

```java
// ... connectionFactory iniettata come prima ...

public void sendMessage(String message) {
    try (JMSContext context = connectionFactory.createContext()) {
        context.createProducer().send(myQueue, message);
    } // Il contesto viene chiuso automaticamente
}
```

## 4. Invio e Ricezione di Messaggi (Producer e Consumer)

Una volta ottenuto il `JMSContext`, si possono creare `JMSProducer` per inviare messaggi e `JMSConsumer` per riceverli.

### Invio di Messaggi (Producer)

Un `JMSProducer` è un oggetto leggero, creato al volo, utilizzato per inviare messaggi a una destinazione.

**Esempio di invio**

```java
//... contesto e coda iniettati ...
public void sendMessage(String payload, String correlationId) {
    context.createProducer()
           .setJMSCorrelationID(correlationId) // Imposta proprietà del messaggio
           .setProperty("myCustomHeader", "someValue")
           .send(myQueue, payload); // Invia il messaggio
}
```

### Ricezione di Messaggi (Consumer)

Esistono due modalità principali per consumare messaggi:

#### 1. Consumo Sincrono (Pull)

In questa modalità, il consumer richiede attivamente un messaggio e attende (blocca il thread) finché non ne arriva uno o scade un timeout. Si utilizza un `JMSConsumer` e si invoca il suo metodo `receive()`.

Questo approccio è raramente usato in un container Java EE perché bloccare un thread in attesa di un messaggio è inefficiente e non scala bene.

**Esempio di ricezione sincrona**

```java
//... contesto e coda iniettati ...
public String receiveMessage() {
    try (JMSConsumer consumer = context.createConsumer(myQueue)) {
        // Attende indefinitamente un messaggio
        String messagePayload = consumer.receiveBody(String.class); 
        
        // Oppure attende per un tempo limitato (es. 1 secondo)
        // String messagePayload = consumer.receiveBody(String.class, 1000);
        
        return messagePayload;
    }
}
```

#### 2. Consumo Asincrono (Push)

Questa è la modalità preferita e più efficiente in Java EE. Invece di chiedere attivamente un messaggio, si registra un "ascoltatore" (`MessageListener`) che viene notificato automaticamente dal container non appena un messaggio arriva sulla destinazione.

##### Message-Driven Bean (MDB)

In Java EE, il modo standard per implementare un consumer asincrono è usare un **Message-Driven Bean (MDB)**. Un MDB è un tipo speciale di EJB che agisce come un `MessageListener`. Il container Java EE gestisce il suo ciclo di vita, l'invocazione e la concorrenza, astraendo tutta la complessità.

Si configura l'MDB tramite l'annotazione `@MessageDriven` per specificare da quale coda o topic ascoltare.

**Esempio di MDB per l'elaborazione di ordini**

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(
   activationConfig = {     
      @ActivationConfigProperty(propertyName = "destinationLookup", 
          propertyValue = "jms/OrdiniQueue"), 
      @ActivationConfigProperty(propertyName = "destinationType", 
          propertyValue = "javax.jms.Queue") 
    }
) 
public class ElaboraOrdineBean implements MessageListener { 
   
   @Inject 
   private OrderService orderService; // Inietta un altro bean per la logica di business
   
   @Override
   public void onMessage(Message msg) {
      try { 
         // Estrae il corpo del messaggio, che si presume sia una stringa JSON
         String ordineJson = msg.getBody(String.class);   
         
         // Delega l'elaborazione a un servizio specializzato
         orderService.processaOrdine(ordineJson); 
         
      } catch (JMSException e) { 
         // Gestisce eventuali errori di JMS
         e.printStackTrace(); 
      } 
   } 
}
```

Questo approccio è scalabile, efficiente e disaccoppia completamente la logica di business dalla gestione dell'infrastruttura di messaggistica.

## Tabella Riepilogativa dei Componenti

| Componente | Scopo | Come si ottiene (in Java EE) |
| --- | --- | --- |
| `ConnectionFactory` | Creare connessioni al broker JMS. | `@Resource(lookup="...")` |
| `Destination` (`Queue`/`Topic`) | Rappresenta la destinazione dei messaggi. | `@Resource(lookup="...")` o definito con `@JMSDestinationDefinition` |
| `JMSContext` | API unificata (JMS 2.0) per connessione e sessione. | `@Inject` (preferito) o `connectionFactory.createContext()` |
| `JMSProducer` | Inviare messaggi a una `Destination`. | `context.createProducer()` |
| `JMSConsumer` | Ricevere messaggi da una `Destination` in modo sincrono. | `context.createConsumer(destination)` |
| `MessageListener` (MDB) | Ricevere messaggi da una `Destination` in modo asincrono. | Implementato da un EJB `@MessageDriven`. |
