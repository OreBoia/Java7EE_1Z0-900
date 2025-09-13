# Strategie di Generazione delle Chiavi Primarie in JPA

In JPA, la gestione delle chiavi primarie (Primary Keys) è un aspetto fondamentale del mapping O/R. La specifica fornisce un meccanismo flessibile e potente per generare automaticamente i valori delle chiavi primarie, astraendo le differenze tra i vari dialetti SQL dei database.

L'annotazione principale per questa funzionalità è `@GeneratedValue`, che viene usata in combinazione con `@Id`.

## L'annotazione `@GeneratedValue`

Questa annotazione indica che il valore della chiave primaria per l'entità verrà generato automaticamente. Il suo attributo più importante è `strategy`, che definisce quale meccanismo utilizzare per la generazione.

```java
@Entity
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Specifica la strategia
    private Long id;
    // ...
}
```

Esistono quattro strategie principali definite nell'enum `GenerationType`.

---

### 1. `GenerationType.IDENTITY`

Questa strategia delega la generazione della chiave primaria direttamente al database, utilizzando una colonna a incremento automatico (come `AUTO_INCREMENT` in MySQL o `IDENTITY` in SQL Server).

- **Come funziona**: JPA non sa quale sarà il valore della chiave prima di eseguire l'istruzione `INSERT`. L'entità viene salvata e il valore della chiave generato dal database viene quindi recuperato e assegnato all'oggetto entità.
- **Pro**: Semplice da usare e molto efficiente su database che la supportano nativamente.
- **Contro**: Rende il batching degli `INSERT` più difficile per il provider JPA, poiché deve eseguire ogni insert singolarmente per ottenere l'ID generato.

**Esempio:**

```java
@Entity
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messaggio;
    // ...
}
```

---

### 2. `GenerationType.SEQUENCE`

Questa strategia utilizza una sequenza del database per generare i valori delle chiavi. Le sequenze sono oggetti di database speciali che generano valori numerici univoci.

- **Come funziona**: Prima di eseguire l' `INSERT`, il provider JPA chiede alla sequenza del database il prossimo valore disponibile. Questo valore viene assegnato all'entità e poi l' `INSERT` viene eseguito.
- **Pro**: Molto efficiente, specialmente con il batching. JPA può recuperare un blocco di ID dalla sequenza in una sola chiamata al database e usarli per un batch di `INSERT`. È la strategia più performante per la maggior parte dei database moderni (Oracle, PostgreSQL, etc.).
- **Contro**: Richiede una configurazione leggermente più verbosa se si vogliono personalizzare i dettagli della sequenza.

**Esempio con `@SequenceGenerator`:**
Per un controllo fine, si usa l'annotazione `@SequenceGenerator` per definire il nome, la dimensione dell'allocazione e altri parametri della sequenza.

```java
@Entity
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ordine_seq")
    @SequenceGenerator(
        name = "ordine_seq",
        sequenceName = "DB_ORDINE_SEQUENCE", // Nome della sequenza nel DB
        allocationSize = 10 // Ottimizzazione: pre-alloca 10 ID alla volta
    )
    private Long id;
    // ...
}
```

---

### 3. `GenerationType.TABLE`

Questa strategia utilizza una tabella separata nel database per tenere traccia dell'ultimo valore di chiave generato.

- **Come funziona**: Il provider JPA legge il valore corrente dalla tabella, lo incrementa, lo salva di nuovo nella tabella e lo usa come chiave per la nuova entità. Questa operazione richiede un lock sulla riga della tabella per garantire l'univocità, il che può creare un collo di bottiglia.
- **Pro**: Funziona su tutti i database, anche quelli che non supportano le sequenze.
- **Contro**: È la strategia meno performante a causa della necessità di leggere, bloccare e scrivere su una tabella per ogni generazione di ID. Generalmente sconsigliata se sono disponibili alternative migliori.

**Esempio:**

```java
@Entity
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "prodotto_gen")
    @TableGenerator(
        name = "prodotto_gen",
        table = "ID_GENERATOR", // Nome della tabella che memorizza gli ID
        pkColumnName = "GEN_NAME",
        valueColumnName = "GEN_VAL",
        pkColumnValue = "PRODOTTO_ID",
        allocationSize = 1
    )
    private Long id;
    // ...
}
```

---

### 4. `GenerationType.AUTO`

Questa è la strategia di default. Lascia che sia il provider JPA a scegliere la strategia più appropriata in base al dialetto del database sottostante.

- **Come funziona**:
  - Se il database supporta le sequenze (es. Oracle, PostgreSQL), di solito viene scelta `SEQUENCE`.
  - Se il database supporta le colonne di identità (es. MySQL), potrebbe scegliere `IDENTITY`.
  - In altri casi, potrebbe ripiegare su `TABLE`.
- **Pro**: Portabilità. Il codice non deve cambiare se si cambia il database.
- **Contro**: La strategia effettiva potrebbe non essere quella più ottimale. Ad esempio, su un database che supporta le sequenze, potrebbe comunque usare una sequenza globale condivisa tra più entità, che potrebbe non essere l'ideale.

**Esempio:**

```java
@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Lascia decidere al provider JPA
    private Long id;
    // ...
}
```

## Riepilogo e Raccomandazioni

| Strategia | Performance | Portabilità | Note |
|---|---|---|---|
| `IDENTITY` | Buona | Media | La migliore per DB come MySQL. Rende difficile il batching. |
| `SEQUENCE` | **Eccellente** | Alta | **La strategia raccomandata** per la maggior parte dei DB (Oracle, PostgreSQL, H2). Ottima per il batching. |
| `TABLE` | Scarsa | Massima | Da evitare se possibile a causa dei colli di bottiglia. Utile solo come ultima risorsa. |
| `AUTO` | Variabile | Massima | Comoda per iniziare, ma per applicazioni in produzione è meglio scegliere una strategia esplicita (`SEQUENCE` o `IDENTITY`). |

## Lista dei Comandi e API

| Elemento | Tipo | Descrizione |
|---|---|---|
| `@GeneratedValue` | Annotazione | Indica che il valore dell'ID è generato automaticamente. Richiede l'attributo `strategy`. |
| `GenerationType` | Enum | Contiene le quattro strategie di generazione: `AUTO`, `IDENTITY`, `SEQUENCE`, `TABLE`. |
| `@SequenceGenerator` | Annotazione | Usata con `GenerationType.SEQUENCE` per configurare i dettagli della sequenza del database. |
| `Sequenza` | Oggetto del Database | Oggetto del database che genera una serie di numeri interi univoci, tipicamente usato per le chiavi primarie. |
