# Definizioni Programmatiche delle Risorse JMS

Java EE 7 ha introdotto annotazioni che permettono di definire risorse JMS (come `ConnectionFactory` e `Destination`) direttamente nel codice dell'applicazione. Questo approccio aumenta la portabilità, poiché l'applicazione non dipende più dalla configurazione specifica di un application server.

## 1. @JMSConnectionFactoryDefinition

Similmente a come `@JMSDestinationDefinition` definisce una coda o un topic, l'annotazione `@JMSConnectionFactoryDefinition` permette di definire una `ConnectionFactory` in modo programmatico.

Questo è utile per creare factory con configurazioni specifiche (es. credenziali, `clientId`, o altre proprietà del provider) senza doverle configurare manualmente sul server.

### Attributi Principali

- `name`: Il nome JNDI con cui la `ConnectionFactory` sarà registrata.
- `user`, `password`: Credenziali per connettersi al broker JMS.
- `clientId`: Un ID univoco per la connessione, fondamentale per i sottoscrittori durevoli su un topic.
- `properties`: Un array di `String` nel formato `"chiave=valore"` per impostare proprietà specifiche del provider JMS.

### Esempio di Definizione

```java
import javax.jms.JMSConnectionFactoryDefinition;
import javax.jms.JMSDestinationDefinition;
import javax.ejb.Singleton;

// Definisce una ConnectionFactory e una Queue a livello di applicazione
@JMSConnectionFactoryDefinition(
    name = "java:global/jms/myAppConnectionFactory",
    user = "admin",
    password = "password-segreta",
    clientId = "myAppClient",
    properties = {
        "reconnectAttempts=5",
        "reconnectInterval=1000"
    }
)
@JMSDestinationDefinition(
    name = "java:global/jms/myAppQueue",
    interfaceName = "javax.jms.Queue"
)
@Singleton
public class JmsConfiguration {
    // Questa classe serve solo come contenitore per le definizioni.
    // Può essere un EJB Singleton o qualsiasi altro bean gestito dal container.
}
```

Una volta definita, la `ConnectionFactory` può essere iniettata come qualsiasi altra risorsa:

```java
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;

public class MyService {

    // Iniezione della ConnectionFactory definita programmaticamente
    @Resource(lookup = "java:global/jms/myAppConnectionFactory")
    private ConnectionFactory myAppConnectionFactory;

    public void doWork() {
        // Si può creare un JMSContext da questa factory specifica
        try (JMSContext context = myAppConnectionFactory.createContext()) {
            // ... logica di invio/ricezione ...
        }
    }
}
```

## 2. @JMSDestinationDefinition

Come già visto, questa annotazione definisce una `Destination` (`Queue` o `Topic`). È possibile definire più destinazioni raggruppandole con `@JMSDestinationDefinitions`.

### Esempio con definizioni multiple

```java
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.ejb.Stateless;

// Contenitore per definizioni multiple
@JMSDestinationDefinitions({
    @JMSDestinationDefinition(
        name = "java:global/jms/QueueA",
        interfaceName = "javax.jms.Queue"
    ),
    @JMSDestinationDefinition(
        name = "java:global/jms/TopicB",
        interfaceName = "javax.jms.Topic"
    )
})
@Stateless
public class DestinationDefinitionsBean {
    // ...
}
```

L'uso di queste annotazioni rende l'applicazione self-contained e più facile da deployare su diversi application server compatibili con Java EE 7+.