# JMS e Transazioni

Le transazioni sono un meccanismo fondamentale per garantire l'affidabilità e la coerenza dei dati nei sistemi distribuiti, e JMS offre un solido supporto per esse. In un ambiente Java EE, le transazioni JMS sono quasi sempre integrate con JTA (Java Transaction API) per coordinare operazioni su più risorse.

## Tipi di Transazioni

JMS supporta due modelli transazionali:

1. **Transazioni Locali JMS**: Gestite direttamente dalla sessione JMS (`Session.SESSION_TRANSACTED`). Una transazione locale può raggruppare solo operazioni JMS (invio e ricezione di messaggi) e non può essere coordinata con altre risorse come un database. Sono raramente usate in contesti Java EE complessi.

2. **Transazioni Globali JTA**: Gestite dal container Java EE. Una transazione JTA può orchestrare operazioni su più sistemi transazionali (es. un broker JMS, un database relazionale, un altro EJB). Questo è il modello standard e più potente in Java EE.

## Come funzionano le Transazioni JTA con JMS

Quando un'operazione JMS viene eseguita all'interno di una transazione JTA attiva, essa viene "arruolata" in quella transazione. Il comportamento effettivo dipende dal fatto che si stia inviando o ricevendo un messaggio.

### Invio di Messaggi in una Transazione JTA

Se si invia un messaggio da un componente transazionale (come un EJB), l'operazione di invio viene sincronizzata con l'esito della transazione JTA.

- **Commit**: Se la transazione JTA viene completata con successo (commit), il messaggio viene effettivamente inviato al broker JMS e reso disponibile per i consumer.
- **Rollback**: Se la transazione JTA fallisce (rollback), l'operazione di invio viene annullata. Il messaggio viene scartato e non raggiungerà mai la destinazione.

Questo garantisce che un messaggio venga inviato solo se anche tutte le altre operazioni nella transazione (es. un salvataggio su database) hanno successo.

#### Esempio: Producer Transazionale (EJB)

```java
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OrderService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/jms/queue/NotificationQueue")
    private Queue notificationQueue;

    // La transazione è REQUIRED di default, quindi questo metodo è transazionale
    public void createOrder(Order order) {
        // 1. Operazione su database
        em.persist(order);

        // 2. Operazione JMS
        String notification = "Nuovo ordine creato: " + order.getId();
        context.createProducer().send(notificationQueue, notification);

        // Se si verifica una RuntimeException qui, sia la persistenza dell'ordine
        // sia l'invio della notifica verranno annullati (rollback).
        // Altrimenti, al termine del metodo, entrambi verranno confermati (commit).
    }
}
```

### Ricezione di Messaggi in una Transazione JTA (MDB)

Per i consumer, in particolare per i Message-Driven Beans (MDB), il container avvia automaticamente una transazione JTA prima di invocare il metodo `onMessage`.

- **Commit**: Se il metodo `onMessage` termina normalmente, la transazione JTA viene commessa. L'acknowledgment del messaggio viene inviato al broker, che lo rimuove definitivamente dalla destinazione.
- **Rollback e Redelivery**: Se `onMessage` lancia una `RuntimeException`, la transazione JTA fallisce (rollback). Il messaggio **non** viene confermato (acknowledged) e viene restituito alla destinazione. Il broker JMS tenterà poi di riconsegnarlo (redelivery) secondo le sue politiche di configurazione (es. dopo un certo intervallo di tempo, per un numero massimo di tentativi).

#### Esempio: MDB Transazionale

```java
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(...)
public class OrderProcessorMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            // ... logica per processare il messaggio ...
            
            if (isInvalidData(message)) {
                // Lanciare una RuntimeException causa il rollback della transazione
                // e la successiva riconsegna del messaggio.
                throw new RuntimeException("Dati non validi, rollback in corso.");
            }
            
            // Se il metodo termina qui, la transazione fa commit e il messaggio
            // viene consumato con successo.

        } catch (Exception e) {
            // Gestire l'eccezione qui potrebbe impedire il rollback.
            // Per forzare il rollback, è meglio lanciare una RuntimeException.
            throw new RuntimeException("Errore imprevisto", e);
        }
    }
    
    private boolean isInvalidData(Message msg) { /* ... */ return false; }
}
```

## Il Ruolo delle Transazioni XA

Spesso, quando si parla di transazioni JTA, si sente il termine **XA**. Ma qual è la relazione?

- **XA** è uno standard (una specifica) che definisce un'interfaccia di comunicazione tra un **Transaction Manager** globale (come quello fornito dal container Java EE) e **Resource Manager** locali (come un driver di database JDBC o un broker JMS).
- **JTA** è l'API Java che implementa il ruolo del Transaction Manager e permette al codice applicativo di interagire con le transazioni.

In pratica, JTA utilizza il protocollo XA "sotto il cofano" per coordinare una transazione distribuita tra diverse risorse. Affinché una risorsa (es. un database) possa partecipare a una transazione JTA, deve esporre un driver o un connettore compatibile con XA.

Dal punto di vista dello sviluppatore Java EE, questa è per lo più una **questione di configurazione**:

1. **Configurazione del Datasource**: Quando si configura un `DataSource` nel server applicativo, si deve scegliere la versione XA (es. `OracleXADataSource` invece di un `OracleDataSource` semplice).
2. **Configurazione del Connettore JMS**: Il connettore JMS (Resource Adapter) deve essere configurato per supportare transazioni XA.

Una volta configurate correttamente le risorse, il codice applicativo che usa JTA (come l'EJB `OrderService` visto prima) non ha bisogno di conoscere i dettagli di XA. Il container si occupa di arruolare le risorse XA nella transazione JTA e di orchestrare il protocollo di commit a due fasi (two-phase commit) per garantire che la transazione venga confermata o annullata atomicamente su tutte le risorse.

## Semplificazioni con JMS 2.0 (`JMSContext`)

JMS 2.0 ha introdotto `JMSContext`, che semplifica ulteriormente la gestione delle transazioni.

Quando si inietta un `JMSContext` (`@Inject`) in un ambiente gestito dal container (come un EJB), questo parteciperà automaticamente a qualsiasi transazione JTA attiva.

La modalità di acknowledgment `AUTO_ACKNOWLEDGE` (la default per `JMSContext`) ha un comportamento "intelligente":

- **Fuori da una transazione JTA**: Conferma il messaggio non appena viene ricevuto.
- **Dentro una transazione JTA**: La conferma viene posticipata e sincronizzata con l'esito della transazione. L'acknowledgment viene inviato solo se la transazione JTA esegue il commit.

Questo elimina la necessità di configurare manualmente sessioni transazionali, rendendo il codice più pulito e meno propenso a errori.

## Tabella Riepilogativa dei Concetti Chiave

| Concetto | Descrizione |
| --- | --- |
| **JTA (Java Transaction API)** | API standard per la gestione di transazioni distribuite su più risorse (es. JMS + DB). |
| **Commit** | Esito positivo di una transazione. Le modifiche (invio/consumo di messaggi, scritture su DB) diventano permanenti. |
| **Rollback** | Esito negativo di una transazione. Le modifiche vengono annullate. |
| **Redelivery (Riconsegna)** | Meccanismo con cui un broker JMS riprova a consegnare un messaggio a un consumer dopo che un tentativo precedente è fallito a causa di un rollback. |
| **`@TransactionAttribute`** | Annotazione EJB per definire il comportamento transazionale di un metodo (es. `REQUIRED`, `REQUIRES_NEW`). |
| **`JMSContext` in JTA** | Se iniettato in un EJB, partecipa automaticamente alla transazione JTA, sincronizzando le operazioni JMS con l'esito della transazione. |
