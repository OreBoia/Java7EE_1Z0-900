# Caratteristiche Avanzate dei Message-Driven Beans (MDB)

I Message-Driven Beans (MDB) non sono semplici `MessageListener`; sono componenti EJB a tutti gli effetti, gestiti dal container Java EE, che offrono funzionalità enterprise robuste per la messaggistica asincrona. Vediamo le loro caratteristiche principali.

## 1. Supporto Transazionale Automatico

Per impostazione predefinita, l'invocazione del metodo `onMessage` di un MDB avviene all'interno di una transazione gestita dal container (Container-Managed Transaction, CMT).

### Come funziona

- **Inizio Transazione**: Quando un messaggio viene prelevato dalla coda/topic, il container avvia una transazione JTA.
- **Esecuzione Logica**: Il metodo `onMessage` viene eseguito. Qualsiasi operazione che supporta le transazioni (come accessi al database via JPA, invio di altri messaggi JMS) viene arruolata in questa transazione.
- **Commit**: Se `onMessage` termina con successo, la transazione viene commessa. L'operazione di "consumo" del messaggio viene confermata, e il messaggio viene rimosso definitivamente dalla destinazione.
- **Rollback e Redelivery**: Se durante l'esecuzione di `onMessage` viene lanciata una `RuntimeException` (o un'eccezione marcata per il rollback), il container esegue il rollback della transazione. Tutte le operazioni (es. scritture su DB) vengono annullate, e soprattutto, **il messaggio non viene considerato consumato**. Il broker JMS tenterà di riconsegnare il messaggio (redelivery) all'MDB (o a un altro MDB in ascolto) in un secondo momento.

### Esempio: MDB Transazionale

```java
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(...)
public class FinancialTransactionMDB implements MessageListener {

    @Inject
    private DatabaseService dbService;
    
    @Inject
    private AuditService auditService;

    // Di default, questo metodo è @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void onMessage(Message message) {
        try {
            String transactionData = message.getBody(String.class);
            
            // 1. Scrive sul database
            dbService.recordTransaction(transactionData);
            
            // 2. Aggiorna il sistema di audit
            auditService.log(transactionData);

            // Se una RuntimeException viene lanciata qui, sia recordTransaction
            // che log verranno annullate, e il messaggio tornerà in coda.

        } catch (Exception e) {
            // Se gestiamo l'eccezione, il container non farà il rollback.
            // Per forzare il rollback, possiamo usare MDBContext.setRollbackOnly()
            // o rilanciare una RuntimeException.
            throw new RuntimeException("Errore nell'elaborazione, forzo il rollback", e);
        }
    }
}
```

## 2. Gestione della Concorrenza

Un singolo MDB può processare un solo messaggio alla volta. Per gestire più messaggi parallelamente, l'application server crea un **pool di istanze** dell'MDB.

- **Scalabilità**: Il container può creare dinamicamente più istanze del bean per gestire picchi di messaggi, fino a un limite massimo configurabile.
- **Configurazione**: La dimensione del pool (min/max istanze) è tipicamente configurata a livello di application server (es. in `wildfly-ejb3.xml` per WildFly o tramite console di amministrazione), non tramite annotazioni standard.

Questo permette di processare messaggi in parallelo in modo efficiente e controllato, senza dover scrivere codice per la gestione dei thread.

## 3. Sottoscrizioni Durevoli (Durable Subscriptions) per i Topic

Quando un MDB è in ascolto su un `Topic`, per impostazione predefinita ha una sottoscrizione non durevole: se l'applicazione è offline, perde tutti i messaggi pubblicati in quel periodo.

È possibile configurare l'MDB per avere una **sottoscrizione durevole**, che garantisce la ricezione dei messaggi anche se il consumer era inattivo.

### Come si configura

Si usano specifiche `ActivationConfigProperty` nell'annotazione `@MessageDriven`.

```java
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/topic/newsTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        
        // Proprietà per la sottoscrizione durevole
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "CriticalNewsSubscription"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "NewsReaderApp") // ID client per l'applicazione
    }
)
public class DurableNewsReaderMDB implements MessageListener {
    // ... implementazione di onMessage ...
}
```

- `subscriptionDurability`: Deve essere impostato a `"Durable"`.
- `subscriptionName`: Un nome univoco che identifica la sottoscrizione.
- `clientId`: Un identificatore univoco per la connessione dell'applicazione al broker.

## 4. Contesto di Sicurezza

È importante notare che i messaggi JMS, di per sé, **non propagano un contesto di sicurezza utente**. Quando il metodo `onMessage` di un MDB viene eseguito, non c'è un'identità utente associata (il `EJBContext.getCallerPrincipal()` restituirà un principal anonimo).

La sicurezza in un sistema basato su MDB si concentra su:

- **Integrità Transazionale**: Garantita dal supporto JTA.
- **Autenticazione e Autorizzazione**: A livello di connessione al broker e di accesso alle destinazioni (code/topic), ma non a livello di singolo messaggio.
- **Sicurezza dei Dati**: Il contenuto del messaggio può essere crittografato, ma questa è una responsabilità dell'applicazione.

Se è necessario propagare l'identità dell'utente, bisogna implementare un meccanismo personalizzato, ad esempio inserendo un token o un ID utente nelle proprietà del messaggio JMS.

## Tabella Riepilogativa delle Proprietà di Configurazione

| PropertyName (`@ActivationConfigProperty`) | Scopo | Valori Comuni |
| --- | --- | --- |
| `destinationLookup` | Nome JNDI della coda o del topic. | `java:/jms/myQueue` |
| `destinationType` | Tipo di destinazione. | `javax.jms.Queue`, `javax.jms.Topic` |
| `subscriptionDurability` | Rende durevole una sottoscrizione a un topic. | `Durable`, `NonDurable` (default) |
| `subscriptionName` | Nome univoco per una sottoscrizione durevole o condivisa. | Qualsiasi stringa |
| `clientId` | ID client per la connessione, necessario per le sottoscrizioni durevoli. | Qualsiasi stringa |
| `subscriptionShared` | Abilita le sottoscrizioni condivise per i topic. | `true`, `false` (default) |
| `messageSelector` | Filtra i messaggi da ricevere basandosi sulle loro proprietà (header). | Es: `priority = 'HIGH' AND type = 'Order'` |
