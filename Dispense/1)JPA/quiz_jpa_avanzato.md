# Quiz Avanzato su JPA - Domande Miste con Codice

Questo quiz avanzato copre i concetti di JPA con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Entity e Mapping ORM

### 🔵 Domanda 1

Osserva il seguente codice:

```java
@Entity
@Table(name = "PRODOTTI")
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "NOME_PRODOTTO", nullable = false, length = 100)
    private String nome;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal prezzo;
}
```

Quale delle seguenti affermazioni è **corretta**?

- a) Il campo `prezzo` può contenere massimo 10 cifre decimali
- b) Il campo `nome` può essere nullo nel database
- c) Il campo `prezzo` può contenere massimo 10 cifre totali, di cui 2 decimali
- d) La tabella nel database si chiamerà "Prodotto"

---

### 🟢 Domanda 2

Quali delle seguenti annotazioni sono **obbligatorie** per definire un'entità JPA valida? (Seleziona tutte quelle corrette)

- a) `@Entity`
- b) `@Table`
- c) `@Id`
- d) `@Column`
- e) `@GeneratedValue`

---

### 💻 Domanda 3

Analizza il seguente mapping di relazioni:

```java
@Entity
public class Cliente {
    @Id
    private Long id;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ordine> ordini = new ArrayList<>();
}

@Entity
public class Ordine {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
```

Cosa succede quando esegui `em.remove(cliente)`?

- a) Solo l'entità Cliente viene eliminata, gli Ordini rimangono
- b) Viene lanciata un'eccezione perché ci sono ordini associati
- c) Vengono eliminati sia il Cliente che tutti i suoi Ordini
- d) Gli Ordini vengono mantenuti ma il campo cliente_id viene impostato a NULL

---

## 2. EntityManager e Ciclo di Vita

### 🔵 Domanda 4

Osserva il seguente codice:

```java
@Stateless
public class ProdottoService {
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public void aggiornaProdotto(Long id, String nuovoNome) {
        Prodotto p = em.find(Prodotto.class, id);
        p.setNome(nuovoNome);
        // Nessuna chiamata esplicita a em.persist() o em.merge()
    }
}
```

Cosa succede al termine del metodo?

- a) Le modifiche non vengono salvate perché manca em.merge()
- b) Le modifiche vengono salvate automaticamente durante il commit della transazione
- c) Viene lanciata un'eccezione perché l'entità non è stata esplicitamente aggiornata
- d) L'entità rimane in stato detached

---

### 🟢 Domanda 5

Quali dei seguenti metodi dell'EntityManager **richiedono** una transazione attiva? (Seleziona tutti quelli corretti)

- a) `find()`
- b) `persist()`
- c) `merge()`
- d) `remove()`
- e) `createQuery()`
- f) `flush()`

---

### 💻 Domanda 6

Analizza questo codice e identifica il problema:

```java
public class OrderService {
    @PersistenceContext
    private EntityManager em;
    
    public Prodotto getProdottoConOrdini(Long prodottoId) {
        Prodotto prodotto = em.find(Prodotto.class, prodottoId);
        return prodotto;
    }
    
    // Questo metodo viene chiamato da un altro bean
    public void stampaOrdini(Long prodottoId) {
        Prodotto p = getProdottoConOrdini(prodottoId);
        for (Ordine ordine : p.getOrdini()) { // ERRORE!
            System.out.println(ordine.getNumero());
        }
    }
}
```

Assumendo che `ordini` sia mappato con `FetchType.LAZY`, quale errore si verificherà?

- a) `LazyInitializationException`
- b) `EntityNotFoundException`
- c) `TransactionRequiredException`
- d) `IllegalStateException`

---

## 3. JPQL e Query

### 🔵 Domanda 7

Quale delle seguenti query JPQL è **sintatticamente corretta**?

- a) `SELECT p FROM Prodotto p WHERE p.categoria.nome LIKE '%elettronica%'`
- b) `SELECT p FROM PRODOTTI p WHERE p.nome = :nome`
- c) `SELECT * FROM Prodotto WHERE prezzo > 100`
- d) `SELECT p.id, p.nome FROM Prodotto AS p ORDER BY prezzo DESC`

---

### 💻 Domanda 8

Osserva questo codice e identifica cosa restituisce:

```java
@Repository
public class ProdottoRepository {
    @PersistenceContext
    private EntityManager em;
    
    public List<Object[]> getStatisticheProdotti() {
        String jpql = "SELECT p.categoria.nome, COUNT(p), AVG(p.prezzo) " +
                     "FROM Prodotto p " +
                     "GROUP BY p.categoria.nome " +
                     "HAVING COUNT(p) > 5";
        
        return em.createQuery(jpql).getResultList();
    }
}
```

Il metodo restituisce:

- a) Una lista di entità Prodotto raggruppate per categoria
- b) Una lista di array Object[] contenenti: nome categoria, numero prodotti, prezzo medio
- c) Una lista di stringhe con i nomi delle categorie
- d) Un errore di compilazione

---

### 🟢 Domanda 9

Quali delle seguenti affermazioni sulle **query native** sono corrette? (Seleziona tutte)

- a) Utilizzano SQL standard invece di JPQL
- b) Sono portabili tra database diversi
- c) Possono utilizzare funzioni specifiche del database
- d) Si creano con `em.createNativeQuery()`
- e) Supportano automaticamente il mapping verso entità JPA

---

## 4. Transazioni

### 💻 Domanda 10

Analizza questo codice e prevedi il comportamento:

```java
@Stateless
public class BankService {
    @PersistenceContext
    private EntityManager em;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void trasferisci(Long daId, Long aId, BigDecimal importo) {
        ContoCorrente da = em.find(ContoCorrente.class, daId);
        ContoCorrente a = em.find(ContoCorrente.class, aId);
        
        da.setSaldo(da.getSaldo().subtract(importo));
        a.setSaldo(a.getSaldo().add(importo));
        
        if (da.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Saldo insufficiente");
        }
    }
}
```

Se viene lanciata l'eccezione "Saldo insufficiente", cosa succede?

- a) Solo il conto "da" viene aggiornato
- b) Solo il conto "a" viene aggiornato  
- c) Entrambi i conti vengono aggiornati
- d) Nessun conto viene aggiornato (rollback completo)

---

### 🔵 Domanda 11

In un ambiente Java EE, qual è la differenza principale tra `@Transactional` (CDI) e `@TransactionAttribute` (EJB)?

- a) `@Transactional` è più performante
- b) `@TransactionAttribute` è disponibile solo negli EJB, `@Transactional` nei bean CDI
- c) `@Transactional` supporta solo transazioni locali
- d) Non c'è nessuna differenza funzionale

---

## 5. Locking e Concorrenza

### 💻 Domanda 12

Osserva questa implementazione di locking ottimistico:

```java
@Entity
public class Prodotto {
    @Id
    private Long id;
    
    @Version
    private Long version;
    
    private String nome;
    private BigDecimal prezzo;
    
    // getters e setters
}

@Stateless
public class ProdottoService {
    @PersistenceContext
    private EntityManager em;
    
    public void aggiornaProdotto(Long id, String nuovoNome) {
        Prodotto p = em.find(Prodotto.class, id);
        p.setNome(nuovoNome);
        // Il commit avviene automaticamente
    }
}
```

Se due utenti eseguono contemporaneamente `aggiornaProdotto()` sullo stesso prodotto:

- a) Il secondo utente riceverà un `OptimisticLockException`
- b) Entrambe le modifiche verranno applicate
- c) Il primo utente riceverà un `OptimisticLockException`
- d) Si verificherà un deadlock

---

### 🟢 Domanda 13

Quali delle seguenti strategie di locking sono disponibili in JPA? (Seleziona tutte)

- a) `LockModeType.OPTIMISTIC`
- b) `LockModeType.PESSIMISTIC_READ`
- c) `LockModeType.PESSIMISTIC_WRITE`
- d) `LockModeType.SHARED`
- e) `LockModeType.NONE`

---

## 6. Converters e Bean Validation

### 💻 Domanda 14

Analizza questo converter personalizzato:

```java
@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {
    
    @Override
    public String convertToDatabaseColumn(Status status) {
        return status != null ? status.getCode() : null;
    }
    
    @Override
    public Status convertToEntityAttribute(String code) {
        return code != null ? Status.fromCode(code) : null;
    }
}

@Entity
public class Ordine {
    @Id
    private Long id;
    
    private Status status; // Verrà convertito automaticamente
}
```

Cosa significa `autoApply = true`?

- a) Il converter viene applicato solo se specificato esplicitamente con `@Convert`
- b) Il converter viene applicato automaticamente a tutti i campi di tipo `Status`
- c) Il converter viene applicato solo durante la lettura dal database
- d) Il converter viene applicato solo durante la scrittura nel database

---

### 🔵 Domanda 15

Osserva questa validazione:

```java
@Entity
public class Cliente {
    @Id
    private Long id;
    
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 50)
    private String nome;
    
    @Email
    @Column(unique = true)
    private String email;
    
    @Min(value = 18, message = "L'età deve essere almeno 18 anni")
    private Integer eta;
}
```

Se si tenta di persistere un Cliente con età = 16, quando viene lanciata la `ConstraintViolationException`?

- a) Immediatamente quando si chiama `em.persist()`
- b) Durante il `flush()` del contesto di persistenza
- c) Al commit della transazione
- d) Solo se si chiama esplicitamente `Validator.validate()`

---

## 7. Entity Graphs e Performance

### 💻 Domanda 16

Analizza questo Entity Graph:

```java
@Entity
@NamedEntityGraph(
    name = "Ordine.conDettagli",
    attributeNodes = {
        @NamedAttributeNode("cliente"),
        @NamedAttributeNode(value = "righeOrdine", subgraph = "righe"),
    },
    subgraphs = {
        @NamedSubgraph(name = "righe", attributeNodes = @NamedAttributeNode("prodotto"))
    }
)
public class Ordine {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;
    
    @OneToMany(mappedBy = "ordine", fetch = FetchType.LAZY)
    private List<RigaOrdine> righeOrdine;
}

// Utilizzo
Map<String, Object> hints = new HashMap<>();
hints.put("javax.persistence.fetchgraph", em.getEntityGraph("Ordine.conDettagli"));
Ordine ordine = em.find(Ordine.class, ordineId, hints);
```

Cosa viene caricato con una singola query?

- a) Solo l'Ordine
- b) Ordine + Cliente
- c) Ordine + Cliente + RigheOrdine + Prodotti delle righe
- d) Tutto il grafo dell'oggetto in modo ricorsivo

---

## 8. Bulk Operations e Performance

### 💻 Domanda 17

Osserva questo codice di aggiornamento bulk:

```java
@Stateless
public class ProdottoService {
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public int aumentaPrezzi(String categoria, double percentuale) {
        // Step 1: Bulk update
        String jpql = "UPDATE Prodotto p SET p.prezzo = p.prezzo * :moltiplicatore " +
                     "WHERE p.categoria.nome = :cat";
        
        int rowsUpdated = em.createQuery(jpql)
            .setParameter("moltiplicatore", 1 + percentuale/100)
            .setParameter("cat", categoria)
            .executeUpdate();
        
        // Step 2: Cerca prodotti aggiornati
        List<Prodotto> prodotti = em.createQuery(
            "SELECT p FROM Prodotto p WHERE p.categoria.nome = :cat", Prodotto.class)
            .setParameter("cat", categoria)
            .getResultList();
        
        return rowsUpdated;
    }
}
```

Quale problema potrebbe verificarsi nello Step 2?

- a) I prodotti restituiti potrebbero non riflettere gli aggiornamenti fatti nello Step 1
- b) La query dello Step 2 fallirà con un errore
- c) I prezzi verranno aumentati due volte
- d) Nessun problema, il codice è corretto

---

---

## Risposte Corrette

### 1. **c)** Il campo `prezzo` può contenere massimo 10 cifre totali, di cui 2 decimali

`precision = 10, scale = 2` significa 10 cifre totali, di cui 2 decimali (es: 12345678.99)

### 2. **a, c)** `@Entity` e `@Id`

Sono le uniche due annotazioni strettamente obbligatorie per un'entità JPA valida.

### 3. **c)** Vengono eliminati sia il Cliente che tutti i suoi Ordini

`CascadeType.ALL` include `CascadeType.REMOVE`, quindi l'eliminazione si propaga.

### 4. **b)** Le modifiche vengono salvate automaticamente durante il commit della transazione

L'entità è in stato "managed", quindi i cambiamenti vengono tracciati e sincronizzati automaticamente.

### 5. **b, c, d, f)** `persist()`, `merge()`, `remove()`, `flush()`

Questi metodi modificano lo stato del database e richiedono una transazione attiva.

### 6. **a)** `LazyInitializationException`

La collezione lazy non può essere inizializzata fuori dal contesto transazionale.

### 7. **a)** `SELECT p FROM Prodotto p WHERE p.categoria.nome LIKE '%elettronica%'`

È l'unica sintassi JPQL corretta (orientata agli oggetti, non alle tabelle).

### 8. **b)** Una lista di array Object[] contenenti: nome categoria, numero prodotti, prezzo medio

Le query con proiezioni multiple restituiscono Object[].

### 9. **a, c, d)** Utilizzano SQL standard, possono usare funzioni specifiche del DB, si creano con createNativeQuery()

Non sono portabili e il mapping automatico richiede configurazione aggiuntiva.

### 10. **d)** Nessun conto viene aggiornato (rollback completo)

L'eccezione RuntimeException causa il rollback dell'intera transazione.

### 11. **b)** `@TransactionAttribute` è disponibile solo negli EJB, `@Transactional` nei bean CDI

Sono le annotazioni specifiche per ciascun modello di programmazione.

### 12. **a)** Il secondo utente riceverà un `OptimisticLockException`

Il locking ottimistico permette al primo che fa commit di avere successo.

### 13. **a, b, c, e)** OPTIMISTIC, PESSIMISTIC_READ, PESSIMISTIC_WRITE, NONE

SHARED non esiste in JPA.

### 14. **b)** Il converter viene applicato automaticamente a tutti i campi di tipo `Status`

`autoApply = true` applica il converter a tutti i campi del tipo specificato.

### 15. **c)** Al commit della transazione

Bean Validation viene eseguita dal provider JPA durante il flush/commit.

### 16. **c)** Ordine + Cliente + RigheOrdine + Prodotti delle righe

L'Entity Graph definisce esattamente cosa caricare in una singola query.

### 17. **a)** I prodotti restituiti potrebbero non riflettere gli aggiornamenti fatti nello Step 1

Le bulk operations bypassano il contesto di persistenza, serve un `em.clear()` o `em.refresh()`.
