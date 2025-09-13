# Gestione delle Transazioni in EJB

Una delle funzionalità più potenti e importanti fornite dal container EJB è la **gestione dichiarativa delle transazioni**. Questo servizio astrae la complessità della demarcazione delle transazioni, permettendo allo sviluppatore di concentrarsi sulla logica di business.

Esistono due modelli principali: **Container-Managed Transactions (CMT)**, che è il default e il più utilizzato, e **Bean-Managed Transactions (BMT)**, per un controllo manuale.

---

## 1. Container-Managed Transactions (CMT)

Con CMT, il container EJB è responsabile di avviare, committare e annullare (rollback) le transazioni JTA (Java Transaction API) per conto dell'applicazione. Lo sviluppatore si limita a specificare *come* i metodi del bean debbano partecipare alle transazioni, usando delle annotazioni.

Per default, ogni metodo pubblico di un EJB `@Stateless` o `@Stateful` è transazionale.

### L'annotazione `@TransactionAttribute`

Questa annotazione permette di personalizzare il comportamento transazionale di un metodo (o di un'intera classe EJB). L'attributo principale è `value`, che accetta un `TransactionAttributeType`.

#### `TransactionAttributeType.REQUIRED` (Default)

- **Comportamento**: Se il client che chiama il metodo si trova già all'interno di una transazione, il metodo si unisce a quella transazione. Se non c'è una transazione attiva, il container ne avvia una nuova prima di eseguire il metodo e la committa (o fa rollback) al termine.
- **Uso**: È l'attributo di default e va bene per il 90% dei casi. Garantisce che il metodo venga sempre eseguito in un contesto transazionale.

```java
@Stateless
public class ServizioFatturazione {
    
    @EJB
    private ServizioLog logService;

    // Essendo il default, @TransactionAttribute(TransactionAttributeType.REQUIRED) è implicito.
    public void creaFattura(Fattura f) {
        // ... logica per salvare la fattura ...
        
        // Questo metodo si unirà alla transazione di creaFattura
        logService.log("Fattura creata"); 
    }
}
```

#### `TransactionAttributeType.REQUIRES_NEW`

- **Comportamento**: Il container sospende sempre la transazione del chiamante (se ne esiste una) e avvia una transazione completamente nuova e indipendente per questo metodo. La nuova transazione viene committata o annullata al termine del metodo.
- **Uso**: Utile per operazioni che devono avere successo indipendentemente dall'esito della transazione principale, come l'auditing o il logging.

```java
@Stateless
public class ServizioLog {
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void log(String messaggio) {
        // Questa operazione di logging avverrà in una sua transazione separata.
        // Se la transazione principale fallisce e fa rollback, questo log rimarrà salvato.
        // ... salva il messaggio su DB ...
    }
}
```

#### `TransactionAttributeType.MANDATORY`

- **Comportamento**: Il metodo *deve* essere chiamato dall'interno di una transazione esistente. Se non c'è una transazione attiva, il container lancia un'eccezione (`EJBTransactionRequiredException`).
- **Uso**: Per metodi di supporto che non hanno senso al di fuori di un contesto transazionale più ampio.

#### `TransactionAttributeType.SUPPORTS`

- **Comportamento**: Se esiste una transazione, il metodo vi si unisce. Se non esiste, il metodo viene eseguito comunque, ma senza un contesto transazionale.
- **Uso**: Per operazioni di sola lettura che possono beneficiare di una transazione (es. per la coerenza dei dati letti) ma non la richiedono strettamente.

#### `TransactionAttributeType.NOT_SUPPORTED`

- **Comportamento**: Il metodo viene sempre eseguito al di fuori di un contesto transazionale. Se il chiamante ha una transazione attiva, questa viene sospesa dal container prima di eseguire il metodo.
- **Uso**: Per operazioni non transazionali, come l'invio di notifiche o l'interazione con sistemi esterni che non supportano JTA.

#### `TransactionAttributeType.NEVER`

- **Comportamento**: Il metodo non deve mai essere chiamato dall'interno di una transazione. Se una transazione è attiva, il container lancia un'eccezione (`EJBException`).
- **Uso**: Per metodi che potrebbero essere corrotti da un contesto transazionale. È usato raramente.

---

## 2. Bean-Managed Transactions (BMT)

In questo modello, lo sviluppatore rinuncia alla gestione dichiarativa e si assume la piena responsabilità di avviare, committare e annullare le transazioni manualmente.

- **Come si attiva**: Si annota l'EJB con `@TransactionManagement(TransactionManagementType.BEAN)`.
- **Come si usa**: Si inietta l'oggetto `UserTransaction` e si usano i suoi metodi `begin()`, `commit()` e `rollback()`.

### Esempio di BMT

```java
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN) // Attiva BMT
public class ServizioBatchBMT {

    @Resource // Inietta l'interfaccia per il controllo manuale
    private UserTransaction utx;

    public void eseguiLavoroComplesso() {
        try {
            utx.begin(); // 1. Avvio manuale

            // ... esegui molte operazioni sul database ...
            
            utx.commit(); // 2. Commit manuale
        } catch (Exception e) {
            try {
                utx.rollback(); // 3. Rollback manuale in caso di errore
            } catch (Exception ex) {
                // Logga l'errore di rollback
            }
            // Gestisci l'eccezione principale
        }
    }
}
```

---

## Riepilogo e Comandi Chiave

| Elemento | Tipo | Descrizione |
|---|---|---|
| **CMT** | Concetto | **Container-Managed Transactions**. Il container gestisce le transazioni. È il default. |
| **BMT** | Concetto | **Bean-Managed Transactions**. Lo sviluppatore gestisce le transazioni manualmente. |
| `@TransactionAttribute` | Annotazione | (CMT) Specifica il comportamento transazionale di un metodo o di una classe. |
| `TransactionAttributeType` | Enum | Contiene i sei tipi di attributi transazionali (`REQUIRED`, `REQUIRES_NEW`, etc.). |
| `@TransactionManagement` | Annotazione | Specifica se l'EJB usa CMT (default) o BMT. |
| `UserTransaction` | Interfaccia API | (BMT) Fornisce i metodi `begin()`, `commit()`, `rollback()` per il controllo programmatico. |
