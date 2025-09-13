# JPQL, Criteria API e Query Native in JPA

JPA offre diversi meccanismi per interrogare il database, ognuno con i propri punti di forza. I principali sono:

1. **JPQL (Java Persistence Query Language)**: Un linguaggio di query simile a SQL ma orientato agli oggetti.
2. **Criteria API**: Un'API Java per costruire query in modo programmatico e type-safe.
3. **Query SQL Native**: La possibilità di eseguire query SQL standard direttamente sul database.

---

## 1. JPQL (Java Persistence Query Language)

JPQL è il modo più comune e intuitivo per scrivere query in JPA. La sua sintassi è molto simile a quella di SQL, ma opera sul modello a oggetti (entità e le loro proprietà) invece che direttamente su tabelle e colonne.

- **Orientato agli oggetti**: Si scrive `SELECT u FROM Utente u` invece di `SELECT * FROM T_UTENTE`.
- **Portabile**: Poiché astrae i dettagli del database, una query JPQL funziona su qualsiasi database supportato da JPA.

### Esecuzione di Query JPQL

Le query vengono create e eseguite tramite l' `EntityManager`.

**Esempio: Query con `SELECT` e parametri**
Supponiamo di voler trovare tutti i prodotti che appartengono a una certa categoria e hanno un prezzo inferiore a un dato valore.

```java
@Entity
public class Prodotto {
    // ...
    private String nome;
    private double prezzo;
    @ManyToOne
    private Categoria categoria;
}
```

La query JPQL sarebbe:
`SELECT p FROM Prodotto p WHERE p.categoria = :cat AND p.prezzo < :maxPrezzo`

Esecuzione tramite `EntityManager`:

```java
public List<Prodotto> trovaProdotti(Categoria categoria, double prezzoMassimo) {
    String jpql = "SELECT p FROM Prodotto p WHERE p.categoria = :cat AND p.prezzo < :maxPrezzo";
    
    TypedQuery<Prodotto> query = em.createQuery(jpql, Prodotto.class);
    
    // Impostazione dei parametri nominati
    query.setParameter("cat", categoria);
    query.setParameter("maxPrezzo", prezzoMassimo);
    
    return query.getResultList();
}
```

### Parametri Posizionali vs. Nominati

- **Nominati (`:nomeParametro`)**: Più leggibili e meno soggetti a errori. **Sono la scelta raccomandata.**
- **Posizionali (`?1`, `?2`)**: L'ordine dei parametri è importante.

```java
// Esempio con parametri posizionali
String jpql = "SELECT p FROM Prodotto p WHERE p.categoria = ?1 AND p.prezzo < ?2";
TypedQuery<Prodotto> query = em.createQuery(jpql, Prodotto.class);
query.setParameter(1, categoria);
query.setParameter(2, prezzoMassimo);
```

### Query di Aggiornamento e Cancellazione (Bulk Operations)

JPQL non serve solo per leggere dati. Può anche eseguire operazioni di aggiornamento (`UPDATE`) e cancellazione (`DELETE`) di massa.

**Importante**: Queste operazioni agiscono direttamente sul database e bypassano il contesto di persistenza. Ciò significa che le entità già caricate in memoria (managed) non rifletteranno le modifiche.

**Esempio: `UPDATE` in blocco**
Aumentiamo del 10% il prezzo di tutti i prodotti di una categoria.

```java
@Transactional
public int aumentaPrezzi(Categoria categoria, double percentuale) {
    String jpql = "UPDATE Prodotto p SET p.prezzo = p.prezzo * (1 + :perc) WHERE p.categoria = :cat";
    
    Query query = em.createQuery(jpql);
    query.setParameter("perc", percentuale / 100.0);
    query.setParameter("cat", categoria);
    
    // executeUpdate() restituisce il numero di righe modificate
    int righeModificate = query.executeUpdate();
    return righeModificate;
}
```

*Nota: In contesti come Spring Data JPA, queste query vengono spesso annotate con `@Modifying` per indicare che modificano lo stato del database.*

---

## 2. Query SQL Native

Quando JPQL non è abbastanza potente o si ha bisogno di usare funzionalità specifiche di un database (es. query ricorsive, funzioni particolari), si può ricorrere a SQL nativo.

- **Come si usa**: `EntityManager.createNativeQuery()`
- **Svantaggi**: La query non è più portabile tra database diversi.

**Esempio: Eseguire una query SQL nativa mappando il risultato a un'entità**

```java
public List<Prodotto> trovaProdottiConQueryNativa() {
    String sql = "SELECT * FROM PRODOTTO WHERE IS_DISPONIBILE = 1";
    
    // Il secondo argomento indica a JPA di mappare il risultato all'entità Prodotto
    Query query = em.createNativeQuery(sql, Prodotto.class);
    
    return query.getResultList();
}
```

---

## 3. Criteria API

La Criteria API offre un modo per costruire query dinamicamente e in modo type-safe, usando oggetti Java invece di stringhe.

- **Pro**: Sicurezza a tempo di compilazione (type-safety). Se si rinomina un campo dell'entità, il codice che usa la Criteria API non compilerà, evidenziando subito l'errore. Con JPQL, l'errore si verificherebbe solo a runtime.
- **Contro**: Molto più verbosa e complessa da leggere e scrivere rispetto a JPQL.

**Esempio: Stessa query dei prodotti, ma con Criteria API**

```java
public List<Prodotto> trovaProdottiConCriteria(Categoria categoria, double prezzoMassimo) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Prodotto> cq = cb.createQuery(Prodotto.class);
    
    Root<Prodotto> prodottoRoot = cq.from(Prodotto.class);
    
    // Creazione delle condizioni (predicates)
    Predicate categoriaPredicate = cb.equal(prodottoRoot.get("categoria"), categoria);
    Predicate prezzoPredicate = cb.lessThan(prodottoRoot.get("prezzo"), prezzoMassimo);
    
    // Combinazione dei predicati con AND
    cq.where(cb.and(categoriaPredicate, prezzoPredicate));
    
    TypedQuery<Prodotto> query = em.createQuery(cq);
    return query.getResultList();
}
```

## Riepilogo e Comandi Principali

| Comando/API | Descrizione |
|---|---|
| **JPQL** | Linguaggio di query orientato agli oggetti, simile a SQL. Scelta standard per la maggior parte dei casi d'uso. |
| `em.createQuery(jpql, Class)` | Crea un oggetto `TypedQuery` per eseguire una query JPQL e mappare i risultati al tipo specificato. |
| `em.createQuery(jpql)` | Crea un oggetto `Query` generico, usato per query non-`SELECT` (es. `UPDATE`, `DELETE`). |
| `query.setParameter(name, value)` | Imposta un parametro (nominato o posizionale) per la query. |
| `query.getResultList()` | Esegue la query e restituisce una lista di risultati. |
| `query.getSingleResult()` | Esegue la query e si aspetta un unico risultato. Lancia `NoResultException` se non ci sono risultati, o `NonUniqueResultException` se ce n'è più di uno. |
| `query.executeUpdate()` | Esegue una query JPQL di tipo `UPDATE` o `DELETE`. |
| **Criteria API** | API Java per la costruzione di query dinamiche e type-safe. |
| `em.getCriteriaBuilder()` | Punto di ingresso per la Criteria API. |
| **SQL Nativo** | Permette di eseguire query SQL standard. |
| `em.createNativeQuery(sql, Class)` | Esegue una query SQL nativa e mappa i risultati a un'entità. |
