# Entity Graphs in JPA 2.1

Una delle sfide più comuni in JPA è la gestione del caricamento delle associazioni tra entità (le relazioni `@OneToMany`, `@ManyToOne`, etc.). Un caricamento "pigro" (`FetchType.LAZY`) può causare `LazyInitializationException`, mentre un caricamento "avido" (`FetchType.EAGER`) può portare a caricare troppi dati (il problema N+1 select).

JPA 2.1 ha introdotto gli **Entity Graphs** come soluzione elegante e potente a questo problema. Essi permettono di definire, a runtime, un "piano di caricamento" per una specifica query, specificando esattamente quali relazioni e attributi devono essere caricati insieme all'entità radice.

## Il Problema: `LazyInitializationException` e `JOIN FETCH`

Consideriamo queste entità:

```java
@Entity
public class Ordine {
    @Id private Long id;
    private String codice;

    @OneToMany(mappedBy = "ordine", fetch = FetchType.LAZY) // Caricamento pigro di default
    private List<RigaOrdine> righeOrdine;
}

@Entity
public class RigaOrdine {
    @Id private Long id;
    private String prodotto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Ordine ordine;
}
```

Se carichiamo un `Ordine` e poi, fuori dalla transazione (es. nel layer di presentazione), proviamo ad accedere a `ordine.getRigheOrdine()`, otterremo una `LazyInitializationException`.

La soluzione tradizionale è usare `JOIN FETCH` in una query JPQL:
`SELECT o FROM Ordine o JOIN FETCH o.righeOrdine WHERE o.id = :id`

Questo funziona, ma ha due svantaggi:

1. La logica di fetching è "hardcoded" nella stringa della query.
2. È meno flessibile se si vogliono piani di caricamento diversi per la stessa entità in contesti diversi.

## La Soluzione: Entity Graphs

Un Entity Graph permette di definire quali attributi e relazioni caricare, separatamente dalla query stessa.

### 1. Definizione di un Entity Graph (Modo Dichiarativo)

Il modo più comune per definire un Entity Graph è usare l'annotazione `@NamedEntityGraph` a livello di entità.

**Esempio:** Definiamo un grafo per l'entità `Ordine` che carica anche le sue `righeOrdine`.

```java
@Entity
@NamedEntityGraph(
    name = "Ordine.conRighe", // Nome univoco per il grafo
    attributeNodes = {
        @NamedAttributeNode("righeOrdine") // Specifica l'attributo da caricare
    }
)
public class Ordine {
    // ... come prima
}
```

Possiamo anche definire grafi nidificati. Ad esempio, per caricare un `Autore`, i suoi `Libri` e per ogni libro anche l' `Editore`.

```java
@NamedEntityGraph(
    name = "Autore.conLibriEdEditore",
    attributeNodes = {
        @NamedAttributeNode(value = "libri", subgraph = "libri.conEditore")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "libri.conEditore",
            attributeNodes = {
                @NamedAttributeNode("editore")
            }
        )
    }
)
```

### 2. Utilizzo di un Entity Graph

Una volta definito, un Entity Graph può essere "attivato" per una query `find` o JPQL usando le *hints* di persistenza.

**Uso con `EntityManager.find()`**

```java
public Ordine trovaOrdineConRighe(Long id) {
    // 1. Ottieni l'EntityGraph definito sull'entità
    EntityGraph<?> entityGraph = em.getEntityGraph("Ordine.conRighe");
    
    // 2. Crea una mappa di hints
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", entityGraph);
    
    // 3. Esegui la find con gli hints
    return em.find(Ordine.class, id, hints);
}
```

Quando questa `find` viene eseguita, JPA genererà una query SQL con un `LEFT JOIN` per caricare l'ordine e le sue righe in un'unica istruzione, anche se la relazione è definita come `LAZY`.

**Uso con una query JPQL**

```java
public List<Ordine> trovaTuttiGliOrdiniConRighe() {
    EntityGraph<?> entityGraph = em.getEntityGraph("Ordine.conRighe");
    
    TypedQuery<Ordine> query = em.createQuery("SELECT o FROM Ordine o", Ordine.class);
    query.setHint("javax.persistence.fetchgraph", entityGraph);
    
    return query.getResultList();
}
```

### 3. Definizione Programmatica di un Entity Graph

Se non si vuole (o non si può) usare le annotazioni, è possibile creare un Entity Graph dinamicamente.

```java
public Ordine trovaOrdineProgrammatico(Long id) {
    // 1. Crea un EntityGraph programmaticamente
    EntityGraph<Ordine> entityGraph = em.createEntityGraph(Ordine.class);
    entityGraph.addAttributeNodes("righeOrdine"); // Aggiungi gli attributi da caricare
    
    // 2. Usa l'hint come prima
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", entityGraph);
    
    return em.find(Ordine.class, id, hints);
}
```

## Fetch Graph vs. Load Graph

Esistono due tipi di hint per applicare un Entity Graph:

- `javax.persistence.fetchgraph`: Carica **solo** gli attributi specificati nel grafo. Gli altri attributi, anche se marcati come `EAGER` nell'entità, verranno trattati come `LAZY`.
- `javax.persistence.loadgraph`: Carica gli attributi specificati nel grafo **in aggiunta** a quelli già marcati come `EAGER` nell'entità.

In generale, `fetchgraph` offre un controllo più preciso ed è spesso la scelta preferita.

## Riepilogo e Comandi Chiave

| Elemento | Tipo | Descrizione |
|---|---|---|
| **Entity Graph** | Concetto | Un template che definisce quali attributi e relazioni di un'entità caricare in una specifica operazione. |
| `@NamedEntityGraph` | Annotazione | Definisce un Entity Graph in modo dichiarativo a livello di entità. |
| `@NamedAttributeNode` | Annotazione | Specifica un attributo da includere nel grafo. |
| `@NamedSubgraph` | Annotazione | Definisce un grafo nidificato per le relazioni complesse. |
| `em.getEntityGraph(name)` | Metodo | Recupera un `@NamedEntityGraph` definito in modo dichiarativo. |
| `em.createEntityGraph(Class)` | Metodo | Crea un Entity Graph in modo programmatico. |
| `"javax.persistence.fetchgraph"` | Hint | Applica un Entity Graph. Carica *solo* gli attributi del grafo. |
| `"javax.persistence.loadgraph"` | Hint | Applica un Entity Graph. Carica gli attributi del grafo *in aggiunta* a quelli EAGER. |
