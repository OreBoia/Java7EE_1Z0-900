# Persistence Unit e EntityManager in JPA

## Persistence Unit (`persistence.xml`)

Le entità JPA sono gestite all'interno di un'unità di persistenza (Persistence Unit). Questa unità è definita nel file di configurazione `persistence.xml`, che si trova tipicamente nella directory `META-INF` del progetto.

In un'applicazione Java EE, il container applicativo legge questo file per configurare il provider di persistenza (come Hibernate, EclipseLink, etc.), creare un `EntityManagerFactory` e gestire il ciclo di vita degli `EntityManager`.

**Esempio di `persistence.xml`:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.1"
    xmlns="http://xmlns.jcp.org/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">

    <persistence-unit name="my-persistence-unit" transaction-type="JTA">
        <jta-data-source>java:jboss/datasources/MySqlDS</jta-data-source>
        <properties>
            <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>

</persistence>
```

- **`name`**: Identifica univocamente la Persistence Unit.
- **`transaction-type="JTA"`**: Specifica che le transazioni sono gestite dal container (Java Transaction API).
- **`<jta-data-source>`**: Indica il JNDI name della sorgente dati gestita dal container.

## EntityManager

L'`EntityManager` è l'interfaccia principale per interagire con il contesto di persistenza e il database. In un'applicazione Java EE, viene tipicamente iniettato in componenti come EJB o bean CDI utilizzando l'annotazione `@PersistenceContext`.

**Esempio di iniezione:**

```java
@Stateless
public class ProdottoService {

    @PersistenceContext(unitName = "my-persistence-unit")
    private EntityManager em;

    public void creaProdotto(Prodotto p) {
        em.persist(p);
    }
    // ... altri metodi
}
```

### Contesto di Persistenza (Persistence Context)

L'`EntityManager` opera all'interno di un "contesto di persistenza", che è una cache di primo livello dove vengono memorizzate e gestite le entità. Le entità all'interno del contesto sono dette "managed" o "attaccate".

Qualsiasi modifica a un'entità *managed* viene tracciata e sincronizzata automaticamente con il database. Questa sincronizzazione avviene tipicamente al commit della transazione o quando viene chiamato esplicitamente il metodo `flush()`.

## Operazioni Principali dell'EntityManager

L'`EntityManager` fornisce metodi per eseguire le operazioni CRUD (Create, Read, Update, Delete) e query.

### 1. `persist(Object entity)`

Rende un'istanza di un'entità *managed* e persistente. L'entità viene inserita nel database al momento del commit della transazione (o con `flush`).

```java
public void creaProdotto(String nome, double prezzo) {
    Prodotto nuovoProdotto = new Prodotto();
    nuovoProdotto.setNome(nome);
    nuovoProdotto.setPrezzo(prezzo);
    em.persist(nuovoProdotto); // L'entità è ora managed
}
```

### 2. `find(Class<T> entityClass, Object primaryKey)`

Trova un'entità tramite la sua chiave primaria. Se l'entità è già nel contesto di persistenza, viene restituita da lì senza interrogare il database.

```java
public Prodotto trovaProdottoPerId(Long id) {
    return em.find(Prodotto.class, id);
}
```

### 3. `remove(Object entity)`

Rimuove un'entità dal database. L'entità deve essere *managed* per poter essere rimossa.

```java
public void eliminaProdotto(Long id) {
    Prodotto prodottoDaEliminare = em.find(Prodotto.class, id);
    if (prodottoDaEliminare != null) {
        em.remove(prodottoDaEliminare);
    }
}
```

### 4. `merge(T entity)`

Unisce lo stato di un'entità *detached* (non più gestita dal contesto) nel contesto di persistenza corrente. Restituisce una nuova istanza *managed* con lo stato aggiornato.

```java
public Prodotto aggiornaProdotto(Prodotto prodottoDetached) {
    // prodottoDetached arriva dall'esterno (es. da una form web)
    Prodotto prodottoGestito = em.merge(prodottoDetached);
    return prodottoGestito;
}
```

### 5. `createQuery(String jpqlString)` e `createNativeQuery(String sqlString)`

Esegue query. `createQuery` usa JPQL (Java Persistence Query Language), mentre `createNativeQuery` usa SQL nativo.

```java
// JPQL
public List<Prodotto> trovaProdottiSottoPrezzo(double prezzoMax) {
    String jpql = "SELECT p FROM Prodotto p WHERE p.prezzo < :prezzoMax";
    return em.createQuery(jpql, Prodotto.class)
             .setParameter("prezzoMax", prezzoMax)
             .getResultList();
}

// SQL Nativo
public List<Prodotto> trovaTuttiIProdottiConSQLNativo() {
    String sql = "SELECT * FROM PRODOTTI";
    return em.createNativeQuery(sql, Prodotto.class).getResultList();
}
```

### 6. `flush()`

Forza la sincronizzazione del contesto di persistenza con il database. Tutte le modifiche in sospeso (insert, update, delete) vengono eseguite.

```java
public void aggiornaEControllaSubito() {
    Prodotto p = em.find(Prodotto.class, 1L);
    p.setPrezzo(99.99);
    em.flush(); // Scrive subito la modifica al DB, prima del commit
    // ... altre operazioni
}
```

## Stati delle Entità (Ciclo di Vita)

Un'entità JPA può trovarsi in uno dei seguenti quattro stati durante il suo ciclo di vita:

### 1. New (o Transient)

Un'istanza di un'entità è nello stato *New* o *Transient* quando è appena stata creata con l'operatore `new` e non è ancora associata a un contesto di persistenza. Non ha una rappresentazione nel database e non ha un identificatore persistente.

```java
Prodotto p = new Prodotto(); // p è nello stato New/Transient
```

### 2. Managed (o Persistent)

Un'entità è nello stato **Managed** quando è associata a un contesto di persistenza. L'EntityManager gestisce il suo stato. Qualsiasi modifica apportata a un'entità *managed* verrà rilevata e sincronizzata con il database al momento del commit della transazione. Le entità diventano *managed* quando:

- Vengono caricate dal database tramite `find()` o una query.
- Vengono salvate con `persist()`.
- Sono il risultato di un'operazione di `merge()`.

```java
em.persist(p); // p è ora Managed
Prodotto p2 = em.find(Prodotto.class, 1L); // p2 è Managed
```

### 3. Detached

Un'entità è nello stato **Detached** quando era *managed* ma il contesto di persistenza a cui era associata è stato chiuso o l'entità è stata esplicitamente "staccata" dal contesto (usando `em.detach()`). L'entità ha ancora un ID persistente, ma non è più gestita dall'EntityManager. Le modifiche apportate non vengono tracciate. Può essere ri-associata al contesto usando `em.merge()`.

```java
em.detach(p); // p è ora Detached
// oppure se l'EntityManager viene chiuso
entityManager.close(); // tutte le entità gestite diventano Detached
```

### 4. Removed

Un'entità è nello stato *Removed* dopo che il metodo `em.remove()` è stato invocato su di essa. È ancora associata al contesto di persistenza ma è stata pianificata per la rimozione dal database. La rimozione effettiva avviene al commit della transazione.

```java
Prodotto p = em.find(Prodotto.class, 1L);
em.remove(p); // p è ora nello stato Removed
```

## Lista dei Comandi (Metodi dell'EntityManager)

| Metodo | Descrizione |
|---|---|
| `persist(entity)` | Salva una nuova entità nel database. |
| `find(class, pk)` | Cerca un'entità per chiave primaria. |
| `remove(entity)` | Rimuove un'entità dal database. |
| `merge(entity)` | Aggiorna un'entità *detached* unendola al contesto di persistenza. |
| `createQuery(jpql)` | Crea un oggetto `Query` per eseguire una query JPQL. |
| `createNativeQuery(sql)` | Crea un oggetto `Query` per eseguire una query SQL nativa. |
| `flush()` | Sincronizza il contesto di persistenza con il database. |
| `getReference(class, pk)` | Ottiene un'istanza proxy dell'entità (caricamento lazy). |
| `contains(entity)` | Verifica se un'entità è gestita dal contesto di persistenza. |
| `detach(entity)` | Rimuove un'entità dal contesto di persistenza, rendendola *detached*. |
