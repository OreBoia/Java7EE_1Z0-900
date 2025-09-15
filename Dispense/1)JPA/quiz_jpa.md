
# Quiz su JPA - Domande a Scelta Multipla

Questo quiz copre i concetti fondamentali di JPA (Java Persistence API) trattati nelle dispense.

---

## 1. Entity e Mapping ORM

**Domanda 1:** Quale annotazione è obbligatoria per definire una classe come un'entità JPA?

- a) `@Table`
- b) `@Entity`
- c) `@Id`
- d) `@Column`

---

**Domanda 2:** In una relazione `@OneToMany`, quale `FetchType` è generalmente raccomandato per evitare di caricare grandi quantità di dati non necessari?

- a) `FetchType.EAGER`
- b) `FetchType.LAZY`
- c) `FetchType.AUTO`
- d) `FetchType.DEFAULT`

---

**Domanda 3:** Cosa succede quando si usa `CascadeType.PERSIST` su una relazione `@OneToOne`?

- a) Quando l'entità principale viene eliminata, viene eliminata anche quella correlata.
- b) Quando l'entità principale viene salvata (`persist`), viene salvata anche quella correlata.
- c) Quando l'entità principale viene aggiornata, viene aggiornata anche quella correlata.
- d) Tutte le operazioni vengono propagate.

---

## 2. Persistence Unit e EntityManager

**Domanda 4:** Qual è lo scopo del file `persistence.xml`?

- a) Definire le query JPQL.
- b) Configurare l'unità di persistenza, il data source e le proprietà del provider JPA.
- c) Mappare le classi Java alle tabelle del database.
- d) Gestire il ciclo di vita delle transazioni.

---

**Domanda 5:** Quale metodo dell'`EntityManager` viene utilizzato per rendere un'entità *detached* nuovamente *managed*?

- a) `persist()`
- b) `find()`
- c) `merge()`
- d) `refresh()`

---

**Domanda 6:** Un'entità appena creata con `new Prodotto()` si trova in quale stato del ciclo di vita?

- a) Managed
- b) Detached
- c) Removed
- d) New (o Transient)

---

## 3. Gestione delle Transazioni

**Domanda 7:** In un ambiente Java EE, qual è la modalità di gestione delle transazioni più comune e raccomandata per gli EJB?

- a) Bean-Managed Transactions (BMT)
- b) Container-Managed Transactions (CMT)
- c) Transazioni manuali con JDBC
- d) Nessuna transazione

---

**Domanda 8:** Quale annotazione si usa in un bean CDI per definire i confini di una transazione in modo dichiarativo?

- a) `@Transaction`
- b) `@Transactional`
- c) `@TransactionAttribute`
- d) `@EnableTransactionManagement`

---

## 4. Locking e Concorrenza

**Domanda 9:** Come si implementa il locking ottimistico in JPA?

- a) Usando `LockModeType.OPTIMISTIC`.
- b) Aggiungendo un campo all'entità annotato con `@Version`.
- c) Bloccando manualmente la tabella con SQL.
- d) Usando `em.lock()`.

---

**Domanda 10:** Cosa succede se due transazioni tentano di aggiornare la stessa entità con locking ottimistico?

- a) La prima transazione che esegue il commit ha successo, la seconda riceve una `OptimisticLockException`.
- b) Entrambe le transazioni hanno successo.
- c) La seconda transazione attende che la prima finisca.
- d) Si verifica un deadlock.

---

## 5. JPQL e Query

**Domanda 11:** Data la seguente query JPQL, cosa rappresenta `:cat`?
`SELECT p FROM Prodotto p WHERE p.categoria = :cat`

- a) Un parametro posizionale
- b) Un parametro nominato
- c) Una colonna della tabella
- d) Una subquery

---

**Domanda 12:** Quale metodo si usa per eseguire una query JPQL di tipo `UPDATE` o `DELETE`?

- a) `getResultList()`
- b) `getSingleResult()`
- c) `executeUpdate()`
- d) `execute()`

---

## 6. Strategie di Generazione delle Chiavi Primarie

**Domanda 13:** Quale strategia di `@GeneratedValue` è generalmente considerata la più performante per database come Oracle e PostgreSQL, specialmente con il batching?

- a) `GenerationType.IDENTITY`
- b) `GenerationType.TABLE`
- c) `GenerationType.AUTO`
- d) `GenerationType.SEQUENCE`

---

## 7. Entity Graphs

**Domanda 14:** Qual è lo scopo principale di un Entity Graph in JPA?

- a) Validare i dati delle entità.
- b) Definire un piano di caricamento dinamico per le associazioni, per risolvere problemi di `LazyInitializationException`.
- c) Creare grafici e visualizzazioni dei dati.
- d) Gestire le transazioni tra più entità.

---

**Domanda 15:** Qual è la differenza tra l'hint `javax.persistence.fetchgraph` e `javax.persistence.loadgraph`?

- a) `fetchgraph` carica solo gli attributi specificati nel grafo, `loadgraph` li carica in aggiunta a quelli EAGER.
- b) `loadgraph` carica solo gli attributi specificati nel grafo, `fetchgraph` li carica in aggiunta a quelli EAGER.
- c) Non c'è nessuna differenza.
- d) `fetchgraph` è per le letture, `loadgraph` per le scritture.

---

## 8. JPA Converters

**Domanda 16:** A cosa serve un `AttributeConverter` in JPA?

- a) A convertire un'entità in un DTO.
- b) A mappare un tipo di dato non standard del modello di dominio a un tipo di dato supportato dal database.
- c) A validare i dati prima di persisterli.
- d) A convertire una query JPQL in SQL nativo.

---

**Domanda 17:** Quali metodi deve implementare una classe che implementa `AttributeConverter<X, Y>`?

- a) `convertToDatabaseColumn(X attribute)` e `convertToEntityAttribute(Y dbData)`
- b) `toDatabase(X attribute)` e `toEntity(Y dbData)`
- c) `serialize(X object)` e `deserialize(Y data)`
- d) `mapToDb(X attribute)` e `mapToEntity(Y dbData)`

---

## 9. Bean Validation

**Domanda 18:** Quale annotazione si usa per attivare la validazione automatica di un'entità da parte del provider JPA prima di un'operazione di `persist` o `update`?

- a) `@Validate`
- b) `@Valid`
- c) La validazione è sempre attiva di default.
- d) Bisogna configurare la proprietà `javax.persistence.validation.mode` a `auto` in `persistence.xml`.

---

**Domanda 19:** Se la validazione di un'entità fallisce durante il commit di una transazione, cosa succede?

- a) Viene lanciata una `ValidationException`.
- b) I dati vengono salvati lo stesso, ma viene loggato un warning.
- c) La transazione esegue il rollback e viene lanciata una `ConstraintViolationException`.
- d) L'entità viene messa in stato "invalid".

---

## 10. JPQL - Funzioni e Operatori

**Domanda 20:** Quale funzione JPQL viene utilizzata per eseguire una "join fetch" esplicita per caricare una collezione LAZY?

- a) `FETCH JOIN`
- b) `LEFT JOIN FETCH`
- c) `LOAD`
- d) `EAGER`

---

**Domanda 21:** Come si esegue una proiezione in JPQL per selezionare solo alcuni campi di un'entità?

- a) `SELECT p.nome, p.prezzo FROM Prodotto p`
- b) `SELECT new com.example.ProdottoDTO(p.nome, p.prezzo) FROM Prodotto p`
- c) Entrambe le risposte a) e b) sono corrette.
- d) Non è possibile fare proiezioni in JPQL.

---

**Domanda 22:** Qual è lo scopo della clausola `GROUP BY` in JPQL?

- a) Ordinare i risultati.
- b) Raggruppare le righe che hanno gli stessi valori in colonne specificate in un'unica riga di riepilogo.
- c) Filtrare i risultati.
- d) Unire più tabelle.

---

**Domanda 23:** Quale operatore si usa in JPQL per verificare se un valore è nullo?

- a) `== NULL`
- b) `IS NULL`
- c) `= NULL`
- d) `EQUALS NULL`

---

## 11. Ciclo di Vita e Callback

**Domanda 24:** Quale annotazione si usa per definire un metodo che deve essere eseguito dopo che un'entità è stata caricata dal database?

- a) `@PostLoad`
- b) `@PostPersist`
- c) `@PostUpdate`
- d) `@PostRemove`

---

**Domanda 25:** Un metodo annotato con `@PrePersist` in un'entità viene invocato:

- a) Prima che l'entità venga salvata per la prima volta.
- b) Prima di ogni aggiornamento.
- c) Dopo che l'entità è stata eliminata.
- d) Immediatamente dopo il caricamento dal database.

---

**Domanda 26:** Cos'è un `EntityListener`?

- a) Una classe che ascolta eventi del ciclo di vita di un'entità, esterna all'entità stessa.
- b) Un'interfaccia per definire query native.
- c) Un componente per la gestione della cache di secondo livello.
- d) Un meccanismo per intercettare le chiamate ai metodi dell'EntityManager.

---

## 12. Cache di Secondo Livello

**Domanda 27:** Qual è lo scopo della cache di secondo livello (L2 Cache) in JPA?

- a) Memorizzare i risultati delle query per un utente specifico.
- b) Condividere dati di entità tra diverse transazioni e `EntityManager` per migliorare le performance.
- c) Mettere in cache solo le entità in stato "new".
- d) È una cache specifica per il locking pessimistico.

---

**Domanda 28:** Come si abilita la cache di secondo livello per un'entità?

- a) Aggiungendo l'annotazione `@Cacheable` all'entità e configurando `<shared-cache-mode>ALL</shared-cache-mode>` in `persistence.xml`.
- b) Usando `em.setCacheable(true)`.
- c) È abilitata di default per tutte le entità.
- d) Aggiungendo l'annotazione `@SecondLevelCache`.

---

## 13. Relazioni Avanzate

**Domanda 29:** In una relazione `@ManyToMany`, quale annotazione è necessaria per configurare la tabella di join?

- a) `@JoinTable`
- b) `@CollectionTable`
- c) `@SecondaryTable`
- d) `@JoinColumn`

---

**Domanda 30:** Cosa specifica l'attributo `mappedBy` in una relazione bidirezionale?

- a) La colonna della chiave esterna.
- b) Il nome della tabella di join.
- c) Che l'entità corrente non è la proprietaria della relazione e la gestione è delegata al campo specificato nell'altra entità.
- d) Il tipo di fetch da utilizzare.

---

## Risposte Corrette

1. **b)** `@Entity`
2. **b)** `FetchType.LAZY`
3. **b)** Quando l'entità principale viene salvata (`persist`), viene salvata anche quella correlata.
4. **b)** Configurare l'unità di persistenza, il data source e le proprietà del provider JPA.
5. **c)** `merge()`
6. **d)** New (o Transient)
7. **b)** Container-Managed Transactions (CMT)
8. **b)** `@Transactional`
9. **b)** Aggiungendo un campo all'entità annotato con `@Version`.
10. **a)** La prima transazione che esegue il commit ha successo, la seconda riceve una `OptimisticLockException`.
11. **b)** Un parametro nominato
12. **c)** `executeUpdate()`
13. **d)** `GenerationType.SEQUENCE`
14. **b)** Definire un piano di caricamento dinamico per le associazioni, per risolvere problemi di `LazyInitializationException`.
15. **a)** `fetchgraph` carica solo gli attributi specificati nel grafo, `loadgraph` li carica in aggiunta a quelli EAGER.
16. **b)** A mappare un tipo di dato non standard del modello di dominio a un tipo di dato supportato dal database.
17. **a)** `convertToDatabaseColumn(X attribute)` e `convertToEntityAttribute(Y dbData)`
18. **d)** Bisogna configurare la proprietà `javax.persistence.validation.mode` a `auto` in `persistence.xml`.
19. **c)** La transazione esegue il rollback e viene lanciata una `ConstraintViolationException`.
20. **b)** `LEFT JOIN FETCH`
21. **c)** Entrambe le risposte a) e b) sono corrette.
22. **b)** Raggruppare le righe che hanno gli stessi valori in colonne specificate in un'unica riga di riepilogo.
23. **b)** `IS NULL`
24. **a)** `@PostLoad`
25. **a)** Prima che l'entità venga salvata per la prima volta.
26. **a)** Una classe che ascolta eventi del ciclo di vita di un'entità, esterna all'entità stessa.
27. **b)** Condividere dati di entità tra diverse transazioni e `EntityManager` per migliorare le performance.
28. **a)** Aggiungendo l'annotazione `@Cacheable` all'entità e configurando `<shared-cache-mode>ALL</shared-cache-mode>` in `persistence.xml`.
29. **a)** `@JoinTable`
30. **c)** Che l'entità corrente non è la proprietaria della relazione e la gestione è delegata al campo specificato nell'altra entità.
