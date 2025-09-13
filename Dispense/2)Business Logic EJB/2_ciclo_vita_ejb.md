# Ciclo di Vita e Callback degli EJB

Il container EJB non solo ospita i bean, ma ne gestisce attivamente il ciclo di vita, dalla creazione alla distruzione. Per permettere agli sviluppatori di interagire con questi eventi, la specifica EJB fornisce una serie di **annotazioni di callback**. Queste annotazioni marcano metodi che il container invocherà automaticamente in momenti specifici del ciclo di vita di un bean.

---

## 1. Ciclo di Vita di uno Stateless Session Bean (`@Stateless`)

Lo scopo di un bean stateless è essere un "operaio" generico e riutilizzabile. Il suo ciclo di vita è ottimizzato per l'efficienza e la scalabilità attraverso un **pool di istanze**.

1. **Creazione e Inizializzazione**: All'avvio dell'applicazione (o al primo accesso), il container crea un pool di istanze del bean. Per ogni istanza creata, invoca il metodo annotato con `@PostConstruct`, se presente. Questo è il posto ideale per inizializzare risorse che l'istanza userà per tutta la sua vita (es. cache di configurazione, connessioni leggere).
2. **Servizio (nel pool)**: Le istanze attendono nel pool. Quando arriva una richiesta da un client, il container ne preleva una, esegue il metodo richiesto e la rimette immediatamente nel pool, pronta per servire un altro client.
3. **Distruzione**: Quando il container decide di ridurre le dimensioni del pool o durante lo shutdown dell'applicazione, distrugge le istanze. Prima di distruggere un'istanza, invoca il metodo annotato con `@PreDestroy`. Qui si dovrebbero rilasciare le risorse aperte in `@PostConstruct`.

### Esempio di Codice - Stateless Session Bean

```java
@Stateless
public class ServizioAudit {

    private Logger logger;

    @PostConstruct
    public void inizializza() {
        // Chiamato una volta per istanza, quando viene creata e aggiunta al pool.
        System.out.println("Istanza di ServizioAudit creata e inizializzata.");
        // Inizializzazione di risorse (es. un logger specifico)
        this.logger = Logger.getLogger(ServizioAudit.class.getName());
    }

    public void registraEvento(String evento) {
        logger.info("Evento registrato: " + evento);
    }

    @PreDestroy
    public void pulizia() {
        // Chiamato prima che l'istanza venga rimossa definitivamente dal pool.
        System.out.println("Istanza di ServizioAudit in fase di distruzione.");
        // Rilascio risorse
        this.logger = null;
    }
}
```

---

## 2. Ciclo di Vita di uno Stateful Session Bean (`@Stateful`)

Il ciclo di vita di un bean stateful è legato alla "conversazione" con un singolo client.

1. **Creazione e Inizializzazione**: L'istanza viene creata **su richiesta**, quando un client la inietta o la cerca per la prima volta. Subito dopo, viene invocato il metodo `@PostConstruct`.
2. **In Conversazione**: L'istanza è ora associata al client e mantiene il suo stato tra le chiamate.
3. **Passivazione/Attivazione (Opzionale)**: Se il client rimane inattivo per un certo periodo, il container può decidere di **passivare** l'istanza per liberare memoria.
    - Invoca `@PrePassivate`: Il bean viene notificato che sta per essere serializzato (es. su disco). È un'opportunità per rilasciare risorse non serializzabili (es. connessioni di rete).
    - Quando il client torna attivo, l'istanza viene **attivata** (deserializzata).
    - Invoca `@PostActivate`: Il bean viene notificato che è tornato in memoria e può riacquisire le risorse rilasciate in `@PrePassivate`.
4. **Fine della Conversazione e Distruzione**: La conversazione termina quando il client invoca un metodo annotato con `@Remove` o quando scade il timeout della sessione.
    - Il container invoca il metodo `@PreDestroy`.
    - L'istanza viene distrutta.

### Esempio di Codice - Stateful Session Bean

```java
@Stateful
public class WizardRegistrazione {

    private Utente datiUtente;

    @PostConstruct
    public void inizioConversazione() {
        this.datiUtente = new Utente();
        System.out.println("Nuova conversazione di registrazione avviata.");
    }

    // ... metodi per raccogliere dati ...

    @PrePassivate
    void primaDellaPassivazione() { System.out.println("Wizard in passivazione..."); }

    @PostActivate
    void dopoLattivazione() { System.out.println("Wizard riattivato."); }

    @Remove // Annota il metodo che termina la conversazione
    public void completaRegistrazione() {
        // Salva l'utente nel database...
        System.out.println("Registrazione completata. Conversazione terminata.");
    }

    @PreDestroy
    public void fineConversazione() {
        // Pulizia finale prima della distruzione
        System.out.println("Istanza Wizard distrutta.");
    }
}
```

---

## 3. Ciclo di Vita di un Singleton Session Bean (`@Singleton`)

Il ciclo di vita di un singleton è il più semplice: è legato a quello dell'applicazione.

1. **Creazione e Inizializzazione**: Di default, il singleton è "lazy": viene creato al primo accesso. Se si desidera che venga creato all'avvio dell'applicazione (eager), si aggiunge l'annotazione `@Startup`. Subito dopo la creazione, viene invocato `@PostConstruct`.
2. **In Servizio**: L'unica istanza rimane in memoria per tutta la durata dell'applicazione, servendo tutti i client.
3. **Distruzione**: L'istanza viene distrutta solo allo shutdown dell'applicazione. Prima della distruzione, viene invocato `@PreDestroy`.

### Esempio di Codice - Singleton Session Bean

```java
@Singleton
@Startup // Crea il bean all'avvio dell'applicazione
public class GestoreCacheApplicazione {

    private Map<String, Object> cache;

    @PostConstruct
    public void avvio() {
        this.cache = new ConcurrentHashMap<>();
        System.out.println("Cache di applicazione inizializzata.");
    }

    // ... metodi per usare la cache ...

    @PreDestroy
    public void spegnimento() {
        this.cache.clear();
        System.out.println("Cache di applicazione svuotata e distrutta.");
    }
}
```

## Riepilogo delle Callback del Ciclo di Vita

| Annotazione | Invocata quando... | Tipi di Bean Applicabili |
|---|---|---|
| `@PostConstruct` | Subito dopo la creazione dell'istanza e l'iniezione delle dipendenze. | Tutti |
| `@PreDestroy` | Appena prima che il container distrugga l'istanza. | Tutti |
| `@PrePassivate` | Prima che un'istanza stateful venga passivata (serializzata). | Stateful |
| `@PostActivate` | Dopo che un'istanza stateful è stata attivata (deserializzata). | Stateful |
| `@Remove` | Metodo invocato dal client per terminare la conversazione con un bean stateful. | Stateful |
| `@Startup` | Forza la creazione di un bean singleton all'avvio dell'applicazione. | Singleton |
