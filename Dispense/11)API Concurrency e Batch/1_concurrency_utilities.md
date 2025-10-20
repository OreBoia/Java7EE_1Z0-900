# API di Concorrenza per Java EE (JSR 236)

Prima di Java EE 7, la gestione manuale dei thread in un ambiente enterprise era fortemente sconsigliata. Creare thread con `new Thread()` o usare le API `java.util.concurrent` standard portava alla perdita del contesto del container (sicurezza, transazioni, CDI, JNDI), rendendo i task in background isolati e difficili da integrare.

Java EE 7 ha risolto questo problema introducendo le **Concurrency Utilities for Java EE (JSR 236)**. Questa specifica fornisce versioni "gestite" delle familiari API di concorrenza, garantendo che i task eseguiti in thread separati operino all'interno del contesto del container.

## Risorse Principali

Le Concurrency Utilities offrono quattro tipi principali di risorse gestite, che possono essere ottenute tramite iniezione con `@Resource`.

### 1. `ManagedExecutorService`

È l'equivalente gestito di `java.util.concurrent.ExecutorService`. Permette di sottomettere task asincroni (implementando `Runnable` o `Callable`) che verranno eseguiti su thread gestiti dal container. Il grande vantaggio è che il contesto del thread che sottomette il task (es. un EJB o un Servlet) viene automaticamente propagato al thread che esegue il task.

**Contesti propagati:**

* Contesto di sicurezza (Security context)
* Contesto transazionale (JTA)
* Contesto CDI (Contexts and Dependency Injection)
* Contesto JNDI (Java Naming and Directory Interface)

#### Esempio di Codice

Un Servlet che avvia un'operazione di lunga durata in background.

```java
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Future;

@WebServlet("/long-operation")
public class LongOperationServlet extends HttpServlet {

    @Resource
    private ManagedExecutorService executor;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("Avvio operazione lunga in background...");

        // Sottomette un task asincrono
        Future<?> future = executor.submit(() -> {
            try {
                // Simula un'operazione che richiede tempo
                System.out.println("Task in background avviato...");
                Thread.sleep(5000); 
                // Qui si potrebbero usare EJB, JPA, ecc.
                // grazie alla propagazione del contesto.
                System.out.println("Task in background completato.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Il servlet risponde immediatamente, senza attendere la fine del task.
        response.getWriter().println("La richiesta è stata processata, il task è in esecuzione.");
    }
}
```

### 2. `ManagedScheduledExecutorService`

Simile al `ManagedExecutorService`, ma corrisponde a `java.util.concurrent.ScheduledExecutorService`. Permette di schedulare l'esecuzione di task:

* Dopo un certo ritardo (`schedule`).
* A intervalli periodici (`scheduleAtFixedRate` o `scheduleWithFixedDelay`).

Anche in questo caso, il contesto del container viene propagato.

#### Esempio di Codice

Un EJB che schedula un task di pulizia ogni ora.

```java
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class CleanupScheduler {

    @Resource
    private ManagedScheduledExecutorService scheduledExecutor;

    @PostConstruct
    public void init() {
        // Schedula un task che parte dopo 10 secondi e si ripete ogni ora
        scheduledExecutor.scheduleAtFixedRate(() -> {
            System.out.println("Esecuzione del task di pulizia periodico...");
            // Logica di pulizia...
        }, 10, 60 * 60, TimeUnit.SECONDS);
    }
}
```

### 3. `ManagedThreadFactory`

Corrisponde a `java.util.concurrent.ThreadFactory` e serve per creare nuovi thread "gestiti". A differenza dei thread creati con `new Thread()`, quelli prodotti da una `ManagedThreadFactory` propagano il contesto del container. È utile quando si ha bisogno di un controllo più fine sulla creazione dei thread.

#### Esempio di Codice

```java
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;

public class ThreadCreator {

    @Resource
    private ManagedThreadFactory threadFactory;

    public void startManagedThread() {
        Thread managedThread = threadFactory.newThread(() -> {
            System.out.println("Questo è un thread gestito!");
            // Il contesto del container è disponibile qui.
        });
        managedThread.start();
    }
}
```

### 4. `ContextService`

È una utility più avanzata che permette di "catturare" il contesto corrente del container per applicarlo in un secondo momento a un thread non gestito. Si usa per creare proxy di interfacce funzionali (come `Runnable` o `Callable`) che, quando eseguiti, avranno il contesto catturato.

#### Esempio di Codice

```java
import javax.annotation.Resource;
import javax.enterprise.concurrent.ContextService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContextPropagator {

    @Resource
    private ContextService contextService;

    public void runWithContext() {
        // Cattura il contesto corrente
        Runnable contextualRunnable = contextService.createContextualProxy(() -> {
            System.out.println("Task eseguito con il contesto del container.");
        }, Runnable.class);

        // Esegue il task su un ExecutorService non gestito
        ExecutorService nonManagedExecutor = Executors.newSingleThreadExecutor();
        nonManagedExecutor.submit(contextualRunnable);
        nonManagedExecutor.shutdown();
    }
}
```

## Gestione delle Eccezioni nei Task Asincroni

Quando un task asincrono lancia un'eccezione non gestita, questa viene catturata dal container e può essere recuperata tramite l'oggetto `Future` restituito dal metodo `submit()`.

### Esempio di Gestione delle Eccezioni

```java
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@WebServlet("/exception-handling")
public class ExceptionHandlingServlet extends HttpServlet {

    @Resource
    private ManagedExecutorService executor;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Future<String> future = executor.submit(() -> {
            // Simula un errore
            if (Math.random() > 0.5) {
                throw new RuntimeException("Errore durante l'elaborazione!");
            }
            return "Elaborazione completata con successo";
        });

        try {
            // Attende il completamento e recupera il risultato
            String result = future.get();
            response.getWriter().println(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.getWriter().println("Task interrotto");
        } catch (ExecutionException e) {
            // Recupera l'eccezione originale lanciata dal task
            Throwable cause = e.getCause();
            response.getWriter().println("Errore nel task: " + cause.getMessage());
        }
    }
}
```

## Listener per Task Gestiti (`ManagedTaskListener`)

L'interfaccia `ManagedTaskListener` permette di registrare callback che vengono invocati in momenti specifici del ciclo di vita di un task: quando viene sottomesso, quando inizia l'esecuzione, quando viene completato o quando viene abortito.

### Esempio con `ManagedTaskListener`

```java
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import java.util.Map;
import java.util.concurrent.Future;

public class TaskWithListener implements Runnable, ManagedTask {

    @Override
    public void run() {
        System.out.println("Esecuzione del task...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Task completato.");
    }

    @Override
    public ManagedTaskListener getManagedTaskListener() {
        return new ManagedTaskListener() {
            @Override
            public void taskSubmitted(Future<?> future, ManagedExecutorService executor, Object task) {
                System.out.println("Task sottomesso all'executor");
            }

            @Override
            public void taskStarting(Future<?> future, ManagedExecutorService executor, Object task) {
                System.out.println("Task in fase di avvio");
            }

            @Override
            public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
                if (exception != null) {
                    System.err.println("Task completato con errore: " + exception.getMessage());
                } else {
                    System.out.println("Task completato con successo");
                }
            }

            @Override
            public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
                System.err.println("Task abortito: " + exception.getMessage());
            }
        };
    }

    @Override
    public Map<String, String> getExecutionProperties() {
        // Può restituire proprietà personalizzate per il task
        return null;
    }
}

// Utilizzo
@Resource
private ManagedExecutorService executor;

public void executeTaskWithListener() {
    executor.submit(new TaskWithListener());
}
```

## Transazioni nei Task Asincroni

I task eseguiti su `ManagedExecutorService` **non ereditano automaticamente la transazione** del thread chiamante. Se un task necessita di operazioni transazionali, deve gestirle esplicitamente. La soluzione migliore è delegare il lavoro a un EJB con gestione transazionale.

### Esempio: Task Asincrono con Transazioni

```java
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/transactional-task")
public class TransactionalTaskServlet extends HttpServlet {

    @Resource
    private ManagedExecutorService executor;

    @EJB
    private TransactionalService transactionalService;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        executor.submit(() -> {
            // Delega a un EJB che gestisce la transazione
            transactionalService.performDatabaseOperation();
        });
        
        response.getWriter().println("Task transazionale avviato in background");
    }
}
```

```java
// EJB con transazioni
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class TransactionalService {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void performDatabaseOperation() {
        // Operazioni sul database all'interno di una transazione
        System.out.println("Esecuzione operazione transazionale...");
        // em.persist(...);
    }
}
```

## Configurazione Avanzata delle Risorse Gestite

Le risorse di concorrenza possono essere configurate in modo dichiarativo usando annotazioni o descrittori. È possibile definire pool di thread personalizzati con parametri specifici.

### Definizione con Annotazioni (Java EE 7+)

```java
import javax.enterprise.concurrent.ManagedExecutorDefinition;
import javax.enterprise.concurrent.ManagedScheduledExecutorDefinition;

@ManagedExecutorDefinition(
    name = "java:comp/concurrent/CustomExecutor",
    maxAsync = 10,  // Numero massimo di thread
    hungTaskThreshold = 300000  // 5 minuti in millisecondi
)
@ManagedScheduledExecutorDefinition(
    name = "java:comp/concurrent/CustomScheduledExecutor",
    maxAsync = 5,
    hungTaskThreshold = 600000  // 10 minuti
)
public class ConcurrencyConfig {
    // Configurazione delle risorse
}
```

### Utilizzo delle Risorse Personalizzate

```java
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;

public class CustomExecutorUser {

    @Resource(lookup = "java:comp/concurrent/CustomExecutor")
    private ManagedExecutorService customExecutor;

    public void executeTask() {
        customExecutor.submit(() -> {
            System.out.println("Task eseguito sul custom executor");
        });
    }
}
```

## Best Practices

1. **Preferire sempre le risorse gestite**: Non usare mai `new Thread()` o `Executors.newXXX()` direttamente in un ambiente Java EE, altrimenti si perde la propagazione del contesto.

2. **Gestire sempre le eccezioni**: I task asincroni devono gestire le proprie eccezioni o propagarle tramite `Future`.

3. **Chiudere gli ExecutorService quando non sono gestiti**: Se per qualche motivo si usano executor non gestiti, chiamare sempre `shutdown()` o `shutdownNow()`.

4. **Usare EJB per operazioni transazionali**: I task asincroni non ereditano le transazioni, quindi delegare a EJB quando necessario.

5. **Monitorare i task long-running**: Configurare `hungTaskThreshold` per identificare task che impiegano troppo tempo.

6. **Schedulare con criterio**: Usare `scheduleAtFixedRate` quando l'esecuzione deve avvenire a intervalli precisi, `scheduleWithFixedDelay` quando si vuole attendere la fine di un'esecuzione prima di avviare la successiva.

## Tabella dei Termini e Concetti Chiave

| Termine | Descrizione |
| --- | --- |
| **`@Resource`** | Annotazione usata per iniettare una risorsa gestita dal container, come le Concurrency Utilities. |
| **`ManagedExecutorService`** | Servizio per eseguire task asincroni su thread gestiti, propagando il contesto del container. |
| **`ManagedScheduledExecutorService`** | Servizio per schedulare task (con ritardo o periodici) su thread gestiti, propagando il contesto. |
| **`ManagedThreadFactory`** | Factory per creare istanze di `Thread` che operano all'interno del contesto del container. |
| **`ContextService`** | Utility per catturare il contesto del container e applicarlo a task eseguiti in thread non gestiti. |
| **`ManagedTaskListener`** | Interfaccia per registrare callback sul ciclo di vita di un task (submit, start, done, abort). |
| **`ManagedTask`** | Interfaccia che un task può implementare per fornire listener e proprietà personalizzate. |
| **Propagazione del Contesto** | Il processo automatico con cui le informazioni contestuali (sicurezza, transazioni, CDI, JNDI) vengono rese disponibili ai thread figli. |
| **`hungTaskThreshold`** | Tempo in millisecondi dopo il quale un task è considerato "bloccato" dal container. |
| **JSR 236** | La specifica Java (Java Specification Request) che definisce le Concurrency Utilities for Java EE. |
