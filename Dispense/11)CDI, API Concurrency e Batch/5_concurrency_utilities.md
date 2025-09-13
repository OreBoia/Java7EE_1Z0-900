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

## Tabella dei Termini e Concetti Chiave

| Termine | Descrizione |
| --- | --- |
| **`@Resource`** | Annotazione usata per iniettare una risorsa gestita dal container, come le Concurrency Utilities. |
| **`ManagedExecutorService`** | Servizio per eseguire task asincroni su thread gestiti, propagando il contesto del container. |
| **`ManagedScheduledExecutorService`** | Servizio per schedulare task (con ritardo o periodici) su thread gestiti, propagando il contesto. |
| **`ManagedThreadFactory`** | Factory per creare istanze di `Thread` che operano all'interno del contesto del container. |
| **`ContextService`** | Utility per catturare il contesto del container e applicarlo a task eseguiti in thread non gestiti. |
| **Propagazione del Contesto** | Il processo automatico con cui le informazioni contestuali (sicurezza, transazioni, CDI, JNDI) vengono rese disponibili ai thread figli. |
| **JSR 236** | La specifica Java (Java Specification Request) che definisce le Concurrency Utilities for Java EE. |
