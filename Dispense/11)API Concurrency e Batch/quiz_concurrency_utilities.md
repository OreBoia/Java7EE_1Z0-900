# Quiz: Concurrency Utilities for Java EE (JSR 236)

## Domanda 1

Quale delle seguenti affermazioni sulle Concurrency Utilities è CORRETTA?

A) I thread creati con `new Thread()` in Java EE propagano automaticamente il contesto del container  
B) `ManagedExecutorService` permette di eseguire task asincroni mantenendo il contesto di sicurezza, transazioni e CDI  
C) Le Concurrency Utilities sono disponibili solo dalla versione Java EE 8  
D) È necessario chiamare sempre `shutdown()` su un `ManagedExecutorService` iniettato con `@Resource`

**Risposta:** B

**Spiegazione:** Il `ManagedExecutorService` è una delle risorse gestite introdotte da JSR 236 in Java EE 7 che garantisce la propagazione automatica del contesto del container (sicurezza, transazioni, CDI, JNDI) ai thread che eseguono i task asincroni.

---

## Domanda 2

Quale annotazione si usa per iniettare un `ManagedScheduledExecutorService`?

A) `@Inject`  
B) `@EJB`  
C) `@Resource`  
D) `@ManagedExecutor`

**Risposta:** C

**Spiegazione:** Le Concurrency Utilities vengono iniettate usando l'annotazione `@Resource`, che è lo standard per l'iniezione di risorse gestite dal container Java EE.

---

## Domanda 3

Quale interfaccia permette di schedulare task che si ripetono periodicamente?

A) `ManagedExecutorService`  
B) `ManagedScheduledExecutorService`  
C) `ManagedThreadFactory`  
D) `ContextService`

**Risposta:** B

**Spiegazione:** `ManagedScheduledExecutorService` è l'equivalente gestito di `ScheduledExecutorService` e fornisce metodi come `scheduleAtFixedRate()` e `scheduleWithFixedDelay()` per l'esecuzione periodica di task.

---

## Domanda 4

Dato il seguente codice:

```java
@Resource
private ManagedScheduledExecutorService scheduler;

public void init() {
    scheduler.scheduleAtFixedRate(() -> {
        System.out.println("Task periodico");
    }, 10, 60, TimeUnit.SECONDS);
}
```

Cosa succede?

A) Il task viene eseguito ogni 60 secondi, partendo immediatamente  
B) Il task viene eseguito ogni 60 secondi, con un ritardo iniziale di 10 secondi  
C) Il task viene eseguito dopo 10 secondi e poi si ferma  
D) Il codice non compila

**Risposta:** B

**Spiegazione:** Il metodo `scheduleAtFixedRate(task, initialDelay, period, timeUnit)` schedula un task con un ritardo iniziale di 10 secondi, dopodiché lo esegue ogni 60 secondi.

---

## Domanda 5

Quale delle seguenti affermazioni sui task asincroni e le transazioni è VERA?

A) I task eseguiti su `ManagedExecutorService` ereditano automaticamente la transazione del thread chiamante  
B) Le transazioni JTA si propagano automaticamente ai thread gestiti  
C) Per operazioni transazionali nei task asincroni, è consigliabile delegare a un EJB  
D) Non è possibile usare transazioni in task asincroni

**Risposta:** C

**Spiegazione:** I task asincroni NON ereditano automaticamente le transazioni. La soluzione migliore è delegare le operazioni transazionali a un EJB che gestisce esplicitamente le transazioni.

---

## Domanda 6

Qual è lo scopo dell'interfaccia `ManagedTaskListener`?

A) Creare nuovi thread gestiti  
B) Catturare il contesto del container  
C) Intercettare eventi del ciclo di vita di un task (submit, start, done, abort)  
D) Gestire le eccezioni nei task

**Risposta:** C

**Spiegazione:** `ManagedTaskListener` permette di registrare callback che vengono invocati in momenti specifici del ciclo di vita di un task: quando viene sottomesso (`taskSubmitted`), quando inizia (`taskStarting`), quando termina (`taskDone`) o viene abortito (`taskAborted`).

---

## Domanda 7

Come si recupera un'eccezione lanciata da un task asincrono?

A) Tramite un blocco try-catch attorno al metodo `submit()`  
B) Chiamando `get()` sul `Future` e gestendo `ExecutionException`  
C) L'eccezione viene automaticamente propagata al thread chiamante  
D) Non è possibile recuperare eccezioni da task asincroni

**Risposta:** B

**Spiegazione:** Quando un task lancia un'eccezione, questa viene incapsulata in un `ExecutionException`. Chiamando `future.get()`, si può catturare questa eccezione e recuperare la causa originale con `getCause()`.

---

## Domanda 8

A cosa serve `ContextService`?

A) A gestire le transazioni nei task  
B) A creare thread gestiti  
C) A catturare il contesto del container per applicarlo a task eseguiti su thread non gestiti  
D) A schedulare task periodici

**Risposta:** C

**Spiegazione:** `ContextService` è una utility avanzata che permette di "catturare" il contesto corrente del container e applicarlo successivamente a proxy di task (es. `Runnable`, `Callable`) eseguiti anche su executor non gestiti.

---

## Domanda 9

Quale metodo si deve usare per schedulare un task che deve attendere il completamento dell'esecuzione precedente prima di avviare la successiva?

A) `schedule()`  
B) `scheduleAtFixedRate()`  
C) `scheduleWithFixedDelay()`  
D) `submit()`

**Risposta:** C

**Spiegazione:** `scheduleWithFixedDelay()` garantisce che ci sia un intervallo di tempo fisso tra la fine di un'esecuzione e l'inizio della successiva, mentre `scheduleAtFixedRate()` cerca di mantenere un rate fisso indipendentemente dalla durata dell'esecuzione.

---

## Domanda 10

Cosa rappresenta il parametro `hungTaskThreshold` nella configurazione di un `ManagedExecutorService`?

A) Il numero massimo di thread nel pool  
B) Il tempo massimo di attesa per l'avvio di un task  
C) Il tempo in millisecondi dopo il quale un task è considerato "bloccato"  
D) Il numero massimo di task che possono essere sottomessi

**Risposta:** C

**Spiegazione:** `hungTaskThreshold` definisce il tempo (in millisecondi) dopo il quale il container considera un task come "hung" (bloccato/sospeso), utile per il monitoraggio e il debugging di task long-running.

---

## Domanda 11

Dato il seguente codice:

```java
@Resource
private ManagedExecutorService executor;

public void executeTask() {
    Future<String> future = executor.submit(() -> {
        Thread.sleep(5000);
        return "Completato";
    });
    
    // Cosa restituisce questa riga?
    boolean done = future.isDone();
}
```

Immediatamente dopo la chiamata a `submit()`, cosa restituisce `future.isDone()`?

A) `true`  
B) `false`  
C) Lancia un'eccezione  
D) Dipende dalla velocità del sistema

**Risposta:** B

**Spiegazione:** Subito dopo `submit()`, il task è stato solo sottomesso ma non ancora completato. `isDone()` restituirà `false` fino a quando il task non termina (con successo o con errore).

---

## Domanda 12

Quale interfaccia deve implementare un task se vuole fornire un `ManagedTaskListener` personalizzato?

A) `Runnable`  
B) `Callable`  
C) `ManagedTask`  
D) `ManagedExecutor`

**Risposta:** C

**Spiegazione:** L'interfaccia `ManagedTask` estende `Runnable` e aggiunge metodi per fornire un listener (`getManagedTaskListener()`) e proprietà di esecuzione (`getExecutionProperties()`).

---

## Domanda 13

Quale delle seguenti è una best practice per le Concurrency Utilities?

A) Usare sempre `Executors.newFixedThreadPool()` per creare pool di thread  
B) Non chiamare mai `shutdown()` sui `ManagedExecutorService` iniettati  
C) Creare thread con `new Thread()` per operazioni critiche  
D) Gestire sempre le transazioni manualmente nei task asincroni

**Risposta:** B

**Spiegazione:** I `ManagedExecutorService` iniettati con `@Resource` sono gestiti dal container, che si occupa del loro ciclo di vita. Non si deve mai chiamare `shutdown()` su di essi, altrimenti si rischia di interferire con il container.

---

## Domanda 14

Quale tipo di contesto viene propagato automaticamente dai `ManagedExecutorService`?

A) Solo il contesto di sicurezza  
B) Solo il contesto transazionale  
C) Contesto di sicurezza, CDI, JNDI (non le transazioni attive)  
D) Nessun contesto viene propagato

**Risposta:** C

**Spiegazione:** Le risorse gestite propagano automaticamente il contesto di sicurezza, CDI e JNDI. Le transazioni JTA attive NON vengono propagate automaticamente ai task asincroni.

---

## Domanda 15

A cosa serve `ManagedThreadFactory`?

A) A eseguire task asincroni  
B) A schedulare task periodici  
C) A creare nuovi thread che operano all'interno del contesto del container  
D) A gestire le eccezioni nei thread

**Risposta:** C

**Spiegazione:** `ManagedThreadFactory` è una factory per creare istanze di `Thread` che, a differenza dei thread creati con `new Thread()`, operano all'interno del contesto del container e propagano le informazioni contestuali.
