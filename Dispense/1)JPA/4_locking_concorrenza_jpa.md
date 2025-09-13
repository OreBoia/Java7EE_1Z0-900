# Locking e Gestione della Concorrenza in JPA

Quando più utenti o processi tentano di accedere e modificare gli stessi dati contemporaneamente, possono sorgere problemi di concorrenza, portando a dati inconsistenti. JPA fornisce meccanismi di locking per gestire queste situazioni in modo controllato.

Esistono due strategie principali: **Locking Ottimistico** e **Locking Pessimistico**.

## 1. Locking Ottimistico

Questa è la strategia di default e la più comune in JPA. Si basa sull'idea "ottimistica" che i conflitti di concorrenza siano rari. Invece di bloccare i dati in anticipo, il sistema si limita a verificare se i dati sono stati modificati da un'altra transazione prima di salvarli.

### Come funziona: L'annotazione `@Version`

Per implementare il locking ottimistico, si aggiunge un campo speciale all'entità, annotato con `@Version`. Questo campo può essere di tipo numerico (come `int`, `long`) o un timestamp (`java.sql.Timestamp`).

1. Quando un'entità viene letta, JPA memorizza il valore del suo campo versione.
2. Quando l'entità viene aggiornata, JPA incrementa automaticamente il numero di versione.
3. Al momento del commit, JPA controlla se il valore della versione nel database è ancora uguale a quello che aveva letto inizialmente.
    * **Se è uguale**: L'aggiornamento procede e il numero di versione viene incrementato nel database.
    * **Se è diverso**: Significa che un'altra transazione ha modificato e committato i dati nel frattempo. JPA lancia una `OptimisticLockException`, e la transazione corrente viene annullata (rollback).

### Esempio con `@Version`

```java
@Entity
public class Prodotto {

    @Id
    @GeneratedValue
    private Long id;

    private String nome;
    private int quantitaInStock;

    @Version
    private long versione; // Campo per il locking ottimistico

    // Getters e Setters...
}
```

### Scenario di Conflitto

1. **Transazione A** legge il Prodotto con `id=1`. La `versione` è `1`.
2. **Transazione B** legge lo stesso Prodotto con `id=1`. La `versione` è `1`.
3. **Transazione A** modifica la quantità, esegue il commit. Il database ora ha il Prodotto `id=1` con `versione = 2`.
4. **Transazione B** prova a modificare lo stesso prodotto e a fare il commit. JPA controlla la versione: si aspetta `1` ma trova `2`.
5. **La Transazione B fallisce** con una `OptimisticLockException`. Sta allo sviluppatore gestire l'eccezione, ad esempio informando l'utente che i dati sono cambiati e che deve ricaricarli e riprovare.

## 2. Locking Pessimistico

Questa strategia è "pessimistica": assume che i conflitti siano probabili e quindi blocca le righe del database non appena vengono lette per un aggiornamento. Questo impedisce ad altre transazioni di leggere (in alcuni casi) o modificare quelle righe fino a quando la transazione corrente non viene completata (commit o rollback).

### Come funziona: `LockModeType`

Il locking pessimistico viene richiesto esplicitamente quando si legge un'entità, usando i metodi `find()` o `lock()` dell'EntityManager e specificando un `LockModeType`.

A livello di database, questo si traduce tipicamente in un comando SQL come `SELECT ... FOR UPDATE`, che blocca la riga a livello di database.

### Esempio con `LockModeType.PESSIMISTIC_WRITE`

Questo lock impedisce ad altre transazioni di leggere, aggiornare o cancellare i dati bloccati.

```java
@Stateless
public class MagazzinoService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void scaricaProdotto(long prodottoId, int quantitaDaScaricare) {
        // Trova il prodotto e bloccalo subito per l'aggiornamento
        Prodotto prodotto = em.find(Prodotto.class, prodottoId, LockModeType.PESSIMISTIC_WRITE);

        if (prodotto == null) {
            throw new ProdottoNonTrovatoException("Prodotto non trovato");
        }

        if (prodotto.getQuantitaInStock() < quantitaDaScaricare) {
            throw new QuantitaNonDisponibileException("Quantità non sufficiente");
        }

        // Se un'altra transazione prova a fare lo stesso find con lock,
        // rimarrà in attesa finché questa transazione non viene completata.
        prodotto.setQuantitaInStock(prodotto.getQuantitaInStock() - quantitaDaScaricare);
        em.merge(prodotto);
    } // Il lock viene rilasciato al commit/rollback
}
```

## Riepilogo e Buone Pratiche

* **Locking Ottimistico**:
* **Pro**: Alta concorrenza, nessuna attesa a livello di database, buona scalabilità.
* **Contro**: I conflitti vengono rilevati solo alla fine (commit), richiedendo una logica di gestione delle eccezioni e tentativi ripetuti.
* **Quando usarlo**: Nella maggior parte delle applicazioni web e aziendali, dove i conflitti sono relativamente infrequenti. **È la pratica consigliata di default.**

* **Locking Pessimistico**:
* **Pro**: Garantisce che i dati non vengano modificati da altri una volta letti, previene i conflitti a monte.
* **Contro**: Riduce la concorrenza (le altre transazioni devono attendere), può causare deadlock se non usato con attenzione.
* **Quando usarlo**: In scenari ad alta contesa dove è critico evitare conflitti e il costo di un rollback è molto alto (es. operazioni finanziarie complesse).

## Lista dei Comandi e API

| Elemento | Tipo | Descrizione |
|---|---|---|
| `@Version` | Annotazione | Abilita il locking ottimistico su un'entità. Il campo annotato viene usato per il controllo di versione. |
| `OptimisticLockException` | Eccezione | Lanciata quando viene rilevato un conflitto di aggiornamento durante il commit in una transazione con locking ottimistico. |
| `LockModeType` | Enum | Specifica il tipo di lock da acquisire. I valori principali sono: |
| `LockModeType.OPTIMISTIC` | Valore Enum | Forza un incremento della versione dell'entità (locking ottimistico esplicito). |
| `LockModeType.PESSIMISTIC_READ` | Valore Enum | Lock pessimistico in lettura. Impedisce ad altri di ottenere un lock in scrittura, ma permette letture non bloccanti. |
| `LockModeType.PESSIMISTIC_WRITE` | Valore Enum | Lock pessimistico in scrittura. Impedisce ad altri di leggere (con lock) o scrivere. |
| `em.find(class, pk, lockMode)` | Metodo | Trova un'entità per chiave primaria e acquisisce immediatamente il lock specificato. |
| `em.lock(entity, lockMode)` | Metodo | Acquisisce esplicitamente un lock su un'entità già gestita. |
