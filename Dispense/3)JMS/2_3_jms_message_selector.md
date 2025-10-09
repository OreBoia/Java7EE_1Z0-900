# Message Selector

Un **Message Selector** è una stringa, simile a una clausola `WHERE` di SQL, che permette a un `JMSConsumer` di specificare quali messaggi è interessato a ricevere. Il filtering avviene a livello di broker (server), prima che i messaggi vengano consegnati al client.

Questo è un meccanismo potente per instradare messaggi a consumer specifici senza dover creare destinazioni separate per ogni tipo di messaggio.

## Come Funziona

1.  **Il Producer imposta le proprietà**: Il `JMSProducer` aggiunge metadati al messaggio sotto forma di proprietà (header custom). Queste proprietà sono coppie chiave-valore.
2.  **Il Consumer definisce un selettore**: Il `JMSConsumer` viene creato con una stringa di selezione che fa riferimento a queste proprietà.
3.  **Il Broker filtra i messaggi**: Il provider JMS valuta il selettore per ogni messaggio presente nella destinazione. Consegna al consumer solo i messaggi che soddisfano la condizione del selettore.

### Sintassi del Selettore

La sintassi è un sottoinsieme di SQL-92. Si possono usare:

-   Operatori aritmetici: `+`, `-`, `*`, `/`
-   Operatori di confronto: `=`, `>`, `<`, `>=`, `<=`, `<>` (diverso)
-   Operatori logici: `AND`, `OR`, `NOT`
-   `BETWEEN`, `IN`, `LIKE`, `IS NULL`

Le proprietà possono essere di tipo `boolean`, `byte`, `short`, `int`, `long`, `float`, `double`, e `String`.

### Esempio Pratico

Immaginiamo un sistema di elaborazione ordini dove i messaggi rappresentano ordini di diverso tipo e priorità.

**1. Il Producer imposta le proprietà**

Il producer invia un messaggio per un ordine, aggiungendo proprietà come `tipoOrdine` e `importo`.

```java
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.annotation.Resource;

public class OrderProducer {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:global/jms/ordiniQueue")
    private Queue ordiniQueue;

    public void sendOrder(String payload, String tipo, double importo) {
        context.createProducer()
               .setProperty("tipoOrdine", tipo) // Es. "ELETTRONICA", "LIBRI"
               .setProperty("importo", importo)
               .send(ordiniQueue, payload);
    }
}
```

**2. I Consumer usano i selettori**

Abbiamo due tipi di consumer: uno per ordini urgenti di importo elevato e un altro per tutti gli ordini di libri.

**Consumer per ordini urgenti e costosi (MDB)**

Questo MDB riceverà solo messaggi dove `tipoOrdine` è `'ELETTRONICA'` E `importo` è maggiore di 1000.

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", 
        propertyValue = "java:global/jms/ordiniQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", 
        propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "messageSelector", 
        propertyValue = "tipoOrdine = 'ELETTRONICA' AND importo > 1000")
})
public class UrgentOrderProcessor implements MessageListener {
    @Override
    public void onMessage(Message msg) {
        // Logica per elaborare ordini urgenti e di alto valore
        System.out.println("Processo un ordine elettronico costoso...");
    }
}
```

**Consumer per ordini di libri (sincrono)**

Questo consumer, creato manualmente, riceverà solo messaggi relativi a libri.

```java
public class BookOrderProcessor {

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:global/jms/ordiniQueue")
    private Queue ordiniQueue;

    public void processBookOrders() {
        // Crea un consumer con un selettore
        String selector = "tipoOrdine = 'LIBRI'";
        try (JMSConsumer consumer = context.createConsumer(ordiniQueue, selector)) {
            while (true) {
                Message msg = consumer.receive(1000); // Attende 1 secondo
                if (msg != null) {
                    System.out.println("Processo un ordine di libri...");
                } else {
                    break; // Esce se non ci sono più messaggi
                }
            }
        }
    }
}
```

## Vantaggi dei Message Selector

-   **Flessibilità**: Permette di gestire diversi tipi di messaggi su una singola destinazione, semplificando l'architettura.
-   **Efficienza**: Il filtraggio avviene sul server, riducendo il traffico di rete e il carico di lavoro del client, che non deve scartare messaggi indesiderati.
-   **Disaccoppiamento**: I producer non devono conoscere quali consumer sono interessati a quali messaggi. Aggiungono semplicemente metadati.

## Limitazioni

-   **Performance**: L'uso di selettori complessi può avere un impatto sulle performance del broker JMS, poiché deve ispezionare e valutare le proprietà di ogni messaggio.
-   **Solo Proprietà**: I selettori possono operare solo sulle proprietà (header) del messaggio, non sul suo corpo (`payload`).

I Message Selector sono uno strumento fondamentale per costruire logiche di routing complesse in applicazioni basate su messaggistica.
````