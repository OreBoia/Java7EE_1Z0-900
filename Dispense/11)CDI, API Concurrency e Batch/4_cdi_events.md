# Eventi in CDI

Il modello di eventi di CDI (Contexts and Dependency Injection) è una delle funzionalità più potenti per creare applicazioni modulari e a basso accoppiamento (loose coupling). Permette a diverse componenti del sistema di comunicare tra loro senza avere una conoscenza diretta le une delle altre. Questo disaccoppiamento favorisce l'estensibilità e la manutenibilità del codice.

Il sistema si basa su un modello *publish-subscribe*:

* **Producer (o Publisher)**: Un bean che "pubblica" (spara, *fires*) un evento.
* **Consumer (o Observer)**: Uno o più bean che "ascoltano" e reagiscono a un determinato tipo di evento.

Il producer non ha bisogno di sapere chi sono gli observer, né quanti ce ne sono. Allo stesso modo, gli observer non conoscono il producer. L'unica cosa che li lega è il **tipo dell'evento** (il *payload*).

## Componenti Chiave

1. **Event Payload**: È un semplice oggetto Java (POJO) che rappresenta l'evento e trasporta i dati relativi.
2. **`Event<T>`**: Un'interfaccia CDI che viene iniettata nel bean che deve pubblicare l'evento. `T` è il tipo del payload.
3. **`@Observes`**: Un'annotazione che si applica a un metodo per registrarlo come "osservatore" di un certo tipo di evento.

## Come Funziona: Esempio Pratico

Immaginiamo un'applicazione di e-commerce. Quando un pagamento viene completato, vogliamo che accadano due cose:

1. Il sistema di fatturazione deve generare una fattura.
2. Il servizio di notifica deve inviare un'email di conferma all'utente.

Senza eventi, il servizio di pagamento dovrebbe avere dipendenze dirette sia verso il servizio di fatturazione sia verso quello di notifica, creando un forte accoppiamento. Con gli eventi CDI, il servizio di pagamento si limita a notificare che un pagamento è avvenuto.

**1. Definizione dell'Event Payload**

Creiamo una classe `PaymentEvent` che contiene i dati del pagamento.

```java
public class PaymentEvent {
    private final String orderId;
    private final double amount;

    public PaymentEvent(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
}
```

**2. Pubblicazione dell'Evento (Producer)**

Il `PaymentService` inietta `Event<PaymentEvent>` e usa il metodo `fire()` per pubblicare l'evento dopo aver processato un pagamento.

```java
import javax.enterprise.event.Event;
import javax.inject.Inject;

public class PaymentService {

    @Inject
    private Event<PaymentEvent> paymentEvent;

    public void processPayment(String orderId, double amount) {
        System.out.println("Processando pagamento per l'ordine: " + orderId);
        // Logica di elaborazione del pagamento...
        
        // Pubblica l'evento per notificare le altre parti del sistema
        paymentEvent.fire(new PaymentEvent(orderId, amount));
        
        System.out.println("Pagamento completato.");
    }
}
```

**3. Osservazione dell'Evento (Consumers)**

Due bean diversi, `BillingService` e `NotificationService`, osservano lo stesso evento e reagiscono in modo indipendente.

```java
import javax.enterprise.event.Observes;

public class BillingService {

    public void createInvoice(@Observes PaymentEvent event) {
        System.out.println("Fatturazione: Creata fattura per l'ordine " + event.getOrderId() + 
                           " di importo " + event.getAmount());
        // Logica di creazione fattura...
    }
}
```

```java
import javax.enterprise.event.Observes;

public class NotificationService {

    public void sendConfirmationEmail(@Observes PaymentEvent event) {
        System.out.println("Notifica: Inviata email di conferma per l'ordine " + event.getOrderId());
        // Logica di invio email...
    }
}
```

Quando `paymentEvent.fire()` viene chiamato, il container CDI invocherà automaticamente i metodi `createInvoice()` e `sendConfirmationEmail()`, passando loro lo stesso oggetto `PaymentEvent`.

## Tabella dei Termini e Concetti Chiave

| Termine/Comando | Descrizione |
| --- | --- |
| **`Event<T>`** | Interfaccia iniettabile usata per pubblicare eventi di tipo `T`. |
| **`event.fire(payload)`** | Metodo per pubblicare un evento. Il `payload` è l'oggetto che rappresenta l'evento. |
| **`@Observes`** | Annotazione che marca un metodo come "osservatore". Il tipo del primo parametro del metodo determina quale evento ascolta. |
| **Event Payload** | L'oggetto (POJO) che viene passato dal producer ai consumer e che contiene i dati dell'evento. |
| **Loose Coupling** | Principio di progettazione per cui i componenti di un sistema hanno poca o nessuna conoscenza gli uni degli altri. Gli eventi CDI sono un mezzo per ottenerlo. |
| **Estensibilità** | Grazie al disaccoppiamento, è possibile aggiungere nuovi observer per un evento senza modificare il codice esistente (né il producer né gli altri observer). |
| **`@ObservesAsync`** | Annotazione per definire un osservatore asincrono. L'evento viene gestito in un thread separato, senza bloccare il producer. |
| **Qualificatori di Evento** | È possibile usare i qualificatori (es. `@CreditCard`, `@PayPal`) insieme a `@Observes` e `Event<T>` per filtrare ulteriormente gli eventi. |
