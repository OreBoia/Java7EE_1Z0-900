# Entity e Mapping ORM in JPA

Un'entità JPA è una classe POJO (Plain Old Java Object) che rappresenta una tabella in un database. Le annotazioni JPA vengono utilizzate per mappare la classe e i suoi campi alle tabelle e colonne del database.

## Concetti Chiave

### 1. `@Entity`

L'annotazione `@Entity` definisce una classe come un'entità JPA. Ogni istanza di questa classe corrisponde a una riga nella tabella del database.

**Esempio:**

```java
@Entity
public class Prodotto {
    // ...
}
```

### 2. `@Table`

L'annotazione `@Table` (opzionale) specifica il nome della tabella nel database a cui l'entità è mappata. Se non viene specificata, il nome della tabella viene dedotto dal nome della classe.

**Esempio:**

```java
@Entity
@Table(name = "PRODOTTI")
public class Prodotto {
    // ...
}
```

### 3. `@Id` e `@GeneratedValue`

L'annotazione `@Id` definisce la chiave primaria dell'entità. L'annotazione `@GeneratedValue` specifica come viene generato il valore della chiave primaria.

**Strategie di Generazione:**

- `GenerationType.AUTO`: Il provider di persistenza sceglie la strategia più appropriata.
- `GenerationType.IDENTITY`: Il valore viene generato dalla colonna di identità del database (es. auto-increment).
- `GenerationType.SEQUENCE`: Il valore viene generato da una sequenza del database.
- `GenerationType.TABLE`: Viene utilizzata una tabella di supporto per generare i valori.

**Esempio:**

```java
@Entity
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

### 4. `@Column`

L'annotazione `@Column` (opzionale) mappa un campo a una colonna del database. Se non viene specificata, il nome della colonna viene dedotto dal nome del campo.

**Esempio:**

```java
@Entity
public class Prodotto {
    // ...
    @Column(name = "NOME_PRODOTTO", nullable = false, length = 100)
    private String nome;
    // ...
}
```

## Relazioni tra Entità

Le relazioni tra entità vengono definite utilizzando le seguenti annotazioni:

### 1. `@OneToOne`

Rappresenta una relazione uno-a-uno.

**Esempio:**

```java
@Entity
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "indirizzo_id", referencedColumnName = "id")
    private Indirizzo indirizzo;
}

@Entity
public class Indirizzo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "indirizzo")
    private Utente utente;
}
```

### 2. `@OneToMany` e `@ManyToOne`

Rappresentano una relazione uno-a-molti e molti-a-uno.

**Esempio:**

```java
@Entity
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RigaOrdine> righeOrdine = new ArrayList<>();
}

@Entity
public class RigaOrdine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordine_id")
    private Ordine ordine;
}
```

### 3. `@ManyToMany`

Rappresenta una relazione molti-a-molti.

**Esempio:**

```java
@Entity
public class Studente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "studente_corso",
               joinColumns = @JoinColumn(name = "studente_id"),
               inverseJoinColumns = @JoinColumn(name = "corso_id"))
    private Set<Corso> corsi = new HashSet<>();
}

@Entity
public class Corso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "corsi")
    private Set<Studente> studenti = new HashSet<>();
}
```

## Fetch Types

Il `fetch type` definisce quando i dati correlati vengono caricati dal database.

- `FetchType.EAGER`: I dati correlati vengono caricati immediatamente insieme all'entità principale.
- `FetchType.LAZY`: I dati correlati vengono caricati solo quando vi si accede per la prima volta.

## Cascade Types

Il `cascade type` definisce quali operazioni di persistenza vengono propagate alle entità correlate.

- `CascadeType.PERSIST`: Quando l'entità principale viene salvata, anche le entità correlate vengono salvate.
- `CascadeType.MERGE`: Quando lo stato dell'entità principale viene aggiornato, anche le entità correlate vengono aggiornate.
- `CascadeType.REMOVE`: Quando l'entità principale viene eliminata, anche le entità correlate vengono eliminate.
- `CascadeType.ALL`: Applica tutte le operazioni di cascade.

## Lista dei Comandi (Annotazioni)

| Annotazione | Descrizione |
|---|---|
| `@Entity` | Definisce una classe come entità JPA. |
| `@Table` | Specifica la tabella del database. |
| `@Id` | Definisce la chiave primaria. |
| `@GeneratedValue` | Specifica la strategia di generazione della chiave primaria. |
| `@Column` | Mappa un campo a una colonna del database. |
| `@OneToOne` | Definisce una relazione uno-a-uno. |
| `@OneToMany` | Definisce una relazione uno-a-molti. |
| `@ManyToOne` | Definisce una relazione molti-a-uno. |
| `@ManyToMany` | Definisce una relazione molti-a-molti. |
| `@JoinColumn` | Specifica la colonna di join per le relazioni. |
| `@JoinTable` | Specifica la tabella di join per le relazioni `@ManyToMany`. |
| `fetch` | Attributo per specificare il tipo di fetch (`EAGER` o `LAZY`). |
| `cascade` | Attributo per specificare il tipo di cascade (`PERSIST`, `MERGE`, `REMOVE`, `ALL`, ecc.). |
