# Bean Validation in JPA (JSR 303/349)

Java EE integra nativamente la specifica **Bean Validation**, che fornisce un framework basato su annotazioni per validare gli oggetti Java (POJO). Questa integrazione è particolarmente potente quando usata con JPA, poiché permette di garantire l'integrità dei dati a livello di modello prima ancora che vengano inviati al database.

## Integrazione Automatica tra JPA e Bean Validation

La magia dell'integrazione in Java EE è che è quasi sempre a **configurazione zero**. Se un provider JPA (come Hibernate) rileva la presenza delle librerie di Bean Validation nel classpath dell'applicazione, abiliterà automaticamente la validazione.

**Come funziona?**
Il provider JPA si registra per ascoltare gli eventi del ciclo di vita delle entità. Prima di eseguire operazioni di persistenza chiave, esegue la validazione:

- **Pre-persist**: Prima di inserire una nuova entità (`em.persist()`).
- **Pre-update**: Prima di aggiornare un'entità esistente (`em.merge()`).
- **Pre-remove**: Prima di rimuovere un'entità (`em.remove()`).

Se un'entità non soddisfa i vincoli di validazione definiti dalle annotazioni, l'operazione viene interrotta e il provider JPA lancia una `ConstraintViolationException`. Questo impedisce che dati non validi raggiungano il database.

## Annotazioni di Validazione Comuni

Le regole di validazione vengono applicate direttamente sui campi delle entità tramite annotazioni. Ecco alcune delle più usate:

| Annotazione | Descrizione |
|---|---|
| `@NotNull` | Il valore non può essere nullo. |
| `@NotEmpty` | (Hibernate-specifica) Per stringhe o collezioni, non può essere nullo o vuoto. |
| `@NotBlank` | (Hibernate-specifica) Per stringhe, non può essere nullo e deve contenere almeno un carattere non-spazio. |
| `@Size(min=, max=)` | La dimensione (di una stringa, collezione, mappa, array) deve essere compresa nell'intervallo specificato. |
| `@Min(value)` | Per numeri, il valore deve essere almeno quello specificato. |
| `@Max(value)` | Per numeri, il valore non deve superare quello specificato. |
| `@Pattern(regexp=)` | La stringa deve corrispondere all'espressione regolare specificata. |
| `@Email` | La stringa deve essere un indirizzo email ben formato. |
| `@Past` / `@Future` | La data deve essere nel passato o nel futuro. |
| `@Valid` | Abilita la validazione a cascata su un oggetto correlato (es. in una relazione `@OneToOne` o `@OneToMany`). |

### Esempio di Entità con Validazioni

```java
@Entity
public class Utente {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Size(min = 2, max = 50, message = "Il nome deve avere tra 2 e 50 caratteri")
    private String nome;

    @NotNull(message = "L'email non può essere nulla")
    @Email(message = "Formato email non valido")
    @Column(unique = true)
    private String email;

    @Min(value = 18, message = "L'utente deve essere maggiorenne")
    private int eta;

    @Pattern(regexp = "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$", message = "Codice fiscale non valido")
    private String codiceFiscale;
    
    @Valid // Se l'utente ha un indirizzo, valida anche i campi dell'indirizzo
    @OneToOne(cascade = CascadeType.ALL)
    private Indirizzo indirizzo;

    // Costruttori, Getters e Setters...
}
```

In questo esempio, se si tenta di persistere un'istanza di `Utente` con un `nome` nullo, una `email` mal formata o un'`eta` di 16 anni, JPA lancerà una `ConstraintViolationException` prima di eseguire l'INSERT sul database.

### Scenario di Fallimento

```java
@Stateless
public class ServizioUtenti {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void registraUtente(Utente utente) {
        try {
            // L'utente ha un'email nulla, che viola il vincolo @NotNull
            em.persist(utente); 
        } catch (ConstraintViolationException e) {
            // Il container JTA segnerà la transazione per il rollback.
            // L'eccezione viene catturata e può essere gestita (es. loggata).
            System.out.println("Validazione fallita: " + e.getConstraintViolations());
            // Rilanciare un'eccezione specifica dell'applicazione è una buona pratica.
            throw new DatiUtenteNonValidiException("I dati forniti per l'utente non sono validi.", e);
        }
    }
}
```

## Vantaggi dell'Integrazione

- **Data Integrity**: Assicura che solo dati validi vengano salvati.
- **Single Source of Truth**: Le regole di validazione sono definite in un unico posto (il modello), riducendo la duplicazione del codice.
- **Codice più Pulito**: Elimina la necessità di scrivere blocchi di `if/else` per la validazione manuale nel codice di business.
- **Framework Standard**: Le stesse annotazioni possono essere riutilizzate in altri layer dell'applicazione, come il frontend (es. JSF), per una validazione consistente end-to-end.

## Lista dei Comandi e Concetti Chiave

| Elemento | Tipo | Descrizione |
|---|---|---|
| **Bean Validation** | Specifica (JSR 349) | Framework standard per la validazione di oggetti Java tramite annotazioni. |
| `@NotNull`, `@Size`, etc. | Annotazioni | Dichiarano i vincoli di validazione sui campi di una classe. |
| `ConstraintViolationException` | Eccezione | Lanciata dal provider di persistenza quando un'operazione viola uno o più vincoli di validazione. |
| **Validazione Automatica** | Concetto | Meccanismo con cui JPA esegue automaticamente la validazione prima degli eventi di persistenza (insert, update). |
| `@Valid` | Annotazione | Utilizzata per abilitare la validazione ricorsiva su oggetti associati. |
