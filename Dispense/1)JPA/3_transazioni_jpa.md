# Gestione delle Transazioni in JPA e Java EE

L'interazione con un database tramite JPA avviene quasi sempre all'interno di un contesto transazionale. Una transazione garantisce che un gruppo di operazioni (letture, scritture, aggiornamenti, cancellazioni) venga eseguito come un'unica unità atomica: o tutte le operazioni hanno successo (commit), o nessuna di esse viene applicata (rollback).

In Java EE, JPA si integra perfettamente con il sistema transazionale del container, tipicamente JTA (Java Transaction API).

## 1. Transazioni Gestite dal Container (CMT - Container-Managed Transactions)

Questa è la modalità più comune e raccomandata in Java EE. Il container applicativo (es. WildFly, Payara) si occupa di avviare, committare o annullare le transazioni automaticamente. Lo sviluppatore deve solo definire i confini transazionali in modo dichiarativo.

### Come funziona?

Quando un metodo di un EJB (o un bean CDI annotato) viene invocato, il container controlla i suoi attributi transazionali. Se il metodo è transazionale, il container avvia una transazione JTA prima di eseguirlo. L'`EntityManager`, se iniettato con `@PersistenceContext`, rileva la transazione attiva e vi si unisce automaticamente.

Tutte le operazioni eseguite tramite l' `EntityManager` (`persist`, `merge`, `remove`) vengono accodate nel contesto di persistenza. Solo al termine del metodo, se non si sono verificate eccezioni di sistema, il container esegue il **commit** della transazione, e a quel punto il provider JPA sincronizza le modifiche con il database. In caso di eccezione, il container esegue il **rollback**, annullando tutte le modifiche.

### Esempio con EJB (CMT di default)

Per gli EJB di sessione, il comportamento transazionale di default è `REQUIRED`, il che significa che un metodo verrà sempre eseguito all'interno di una transazione.

```java
@Stateless
public class ServizioClienti {

    @PersistenceContext(unitName = "my-persistence-unit")
    private EntityManager em;

    // Questo metodo è implicitamente transazionale
    public void registraNuovoCliente(String nome, String email) {
        Cliente nuovoCliente = new Cliente();
        nuovoCliente.setNome(nome);
        nuovoCliente.setEmail(email);
        
        em.persist(nuovoCliente); // Aggiunto al contesto di persistenza

        Indirizzo indirizzoDefault = new Indirizzo("Via Standard");
        em.persist(indirizzoDefault); // Anche questo aggiunto

    } // Al termine del metodo, il container fa il commit. 
      // Se qualcosa va storto (es. violazione di un vincolo), fa il rollback.
}
```

### Esempio con CDI e `@Transactional`

Con i bean CDI, è necessario usare l'annotazione `@Transactional` per demarcare i confini della transazione.

```java
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class ServizioOrdini {

    @Inject
    private EntityManager em;

    @Transactional // Definisce esplicitamente i confini della transazione
    public void creaOrdine(Ordine ordine) {
        em.persist(ordine);
        
        // Aggiorna lo stock del prodotto
        Prodotto prodotto = em.find(Prodotto.class, ordine.getProdottoId());
        prodotto.setQuantitaDisponibile(prodotto.getQuantitaDisponibile() - 1);
        em.merge(prodotto);

        // Se si verifica un'eccezione qui, sia il salvataggio dell'ordine
        // sia l'aggiornamento dello stock verranno annullati (rollback).
    }
}
```

## 2. Transazioni Gestite dal Bean (BMT - Bean-Managed Transactions)

In questa modalità, lo sviluppatore ha il controllo completo e deve demarcare esplicitamente l'inizio e la fine di una transazione usando l'API `UserTransaction`. È una modalità più complessa e usata raramente, solo quando è necessario un controllo granulare sul ciclo di vita della transazione.

### Esempio con BMT

Per usare BMT in un EJB, è necessario specificarlo con `@TransactionManagement(TransactionManagementType.BEAN)`.

```java
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ServizioFatturazioneBMT {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx; // Iniezione dell'oggetto UserTransaction

    public void generaFattura(Fattura fattura) {
        try {
            utx.begin(); // Inizio esplicito della transazione

            em.persist(fattura);
            // ... altre operazioni complesse ...

            utx.commit(); // Commit esplicito
        } catch (Exception e) {
            try {
                utx.rollback(); // Rollback esplicito in caso di errore
            } catch (Exception ex) {
                // Gestisci l'errore nel rollback
            }
            // Gestisci l'eccezione originale
        }
    }
}
```

## 3. L'interfaccia `UserTransaction`

L'interfaccia `javax.transaction.UserTransaction` è il cuore della gestione delle transazioni BMT. Fornisce metodi semplici per controllare il ciclo di vita di una transazione direttamente dal codice applicativo.

Viene ottenuta tramite dependency injection con l'annotazione `@Resource`.

```java
@Resource
private UserTransaction utx;
```

### Metodi Principali

- **`void begin()`**: Avvia una nuova transazione. Se una transazione è già associata al thread corrente, viene sollevata un'eccezione.
- **`void commit()`**: Esegue il commit della transazione corrente. Tutte le modifiche vengono rese permanenti.
- **`void rollback()`**: Annulla la transazione corrente. Tutte le modifiche vengono scartate.
- **`void setRollbackOnly()`**: Contrassegna la transazione corrente in modo che possa solo essere annullata (rollback). Qualsiasi tentativo di `commit` fallirà. È utile quando si rileva un errore di logica di business ma si vuole lasciare che il chiamante gestisca il rollback.
- **`int getStatus()`**: Restituisce lo stato corrente della transazione (es. attiva, commessa, annullata), utilizzando le costanti definite in `javax.transaction.Status`.

### Struttura Tipica di Utilizzo

Il pattern più comune per usare `UserTransaction` è un blocco `try-catch` per garantire che il `rollback` venga sempre chiamato in caso di errore.

```java
public void operazioneComplessa() {
    try {
        utx.begin();

        // --- Logica di business e operazioni con l'EntityManager ---
        // em.persist(...);
        // em.merge(...);
        // ...

        utx.commit();
    } catch (Exception e) {
        // Log dell'errore
        try {
            // Controlla se la transazione è ancora attiva prima di tentare il rollback
            if (utx.getStatus() == Status.STATUS_ACTIVE) {
                utx.rollback();
            }
        } catch (SystemException se) {
            // Log dell'errore durante il rollback
        }
        // Rilancia l'eccezione o gestiscila
        throw new RuntimeException("Operazione fallita", e);
    }
}
```

Questo schema assicura una gestione robusta delle transazioni, anche in scenari di errore complessi.

## Lista dei Comandi e Concetti Chiave

| Elemento | Tipo | Descrizione |
|---|---|---|
| **CMT** | Concetto | **Container-Managed Transactions**. Il container gestisce il ciclo di vita delle transazioni. È l'approccio standard e preferito. |
| **BMT** | Concetto | **Bean-Managed Transactions**. Lo sviluppatore gestisce il ciclo di vita delle transazioni manualmente. |
| `@Transactional` | Annotazione | (JTA/CDI) Demarca un metodo di un bean CDI come transazionale. |
| `@TransactionManagement` | Annotazione | (EJB) Specifica se un EJB usa CMT (default) o BMT. |
| `@TransactionAttribute` | Annotazione | (EJB) Definisce il comportamento transazionale di un metodo EJB (es. `REQUIRED`, `REQUIRES_NEW`, `NOT_SUPPORTED`). |
| `UserTransaction` | Interfaccia API | (JTA) Fornisce i metodi `begin()`, `commit()`, `rollback()` per la gestione manuale delle transazioni (BMT). |
| `Commit` | Operazione | Finalizza la transazione, rendendo permanenti tutte le modifiche al database. |
| `@Stateless` | Annotazione | (EJB) Definisce un EJB di sessione senza stato, i cui metodi sono spesso transazionali di default. |
| `@Resource` | Annotazione | (Java EE) Usata per l'iniezione di dipendenze di risorse gestite dal container, come `UserTransaction`. |
| `@PersistenceContext` | Annotazione | (JPA) Usata per iniettare un `EntityManager` gestito dal container, che si unirà automaticamente a una transazione JTA. |
