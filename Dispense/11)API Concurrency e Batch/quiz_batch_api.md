# Quiz: Batch API for Java EE (JSR 352)

## Domanda 1

Quale delle seguenti affermazioni sulla Batch API (JSR 352) è CORRETTA?

A) La Batch API è stata introdotta in Java EE 6  
B) I job batch sono definiti usando file JSON  
C) Il Job Specification Language (JSL) usa XML per definire la struttura dei job  
D) I job batch richiedono sempre l'interazione dell'utente

**Risposta:** C

**Spiegazione:** JSR 352 introduce la Batch API in Java EE 7, usando JSL (Job Specification Language) basato su XML per definire la struttura e il flusso dei job. I job batch sono processi che operano in background senza richiedere interazione utente.

---

## Domanda 2

Quali sono i tre componenti principali di un chunk step?

A) Reader, Writer, Processor  
B) ItemReader, ItemProcessor, ItemWriter  
C) DataReader, DataProcessor, DataWriter  
D) InputReader, BusinessLogic, OutputWriter

**Risposta:** B

**Spiegazione:** Un chunk step è composto da tre componenti: `ItemReader` (legge i dati), `ItemProcessor` (elabora i dati) e `ItemWriter` (scrive i dati). Il processor è opzionale.

---

## Domanda 3

Cosa rappresenta il parametro `item-count` in un chunk step?

A) Il numero totale di elementi da processare  
B) Il numero di elementi elaborati in una singola transazione  
C) Il numero massimo di errori tollerati  
D) Il numero di thread da usare

**Risposta:** B

**Spiegazione:** `item-count` definisce la dimensione del chunk, ovvero quanti elementi vengono letti, processati e scritti in una singola transazione. Ad esempio, `item-count="10"` significa che ogni 10 elementi viene fatto un commit.

---

## Domanda 4

Quale interfaccia si usa per uno step che esegue un singolo task senza il pattern reader-processor-writer?

A) `ItemReader`  
B) `Chunk`  
C) `Batchlet`  
D) `SingleTask`

**Risposta:** C

**Spiegazione:** `Batchlet` (o task step) è un tipo di step più semplice che esegue un'operazione singola e coesa, utile per task come invio email, pulizia file, esecuzione comandi SQL.

---

## Domanda 5

Come si avvia un job batch programmaticamente?

A) Usando `BatchRuntime.getJobOperator().start("jobName", parameters)`  
B) Chiamando `new JobExecutor().run("jobName")`  
C) Con l'annotazione `@StartJob`  
D) Attraverso un file di configurazione

**Risposta:** A

**Spiegazione:** Per avviare un job batch si usa l'interfaccia `JobOperator` ottenuta da `BatchRuntime.getJobOperator()`, quindi si chiama il metodo `start()` passando il nome del job e i parametri.

---

## Domanda 6

Cosa si intende per "checkpointing" nella Batch API?

A) La validazione dei dati in input  
B) Il salvataggio periodico dello stato di avanzamento del job  
C) Il controllo degli errori durante l'elaborazione  
D) La verifica delle transazioni

**Risposta:** B

**Spiegazione:** Il checkpointing è il processo di salvataggio periodico dello stato di un job, che avviene alla fine di ogni transazione di un chunk. Permette di riavviare il job dal punto esatto in cui si è interrotto in caso di fallimento.

---

## Domanda 7

Dato il seguente XML:

```xml
<job id="myJob">
    <step id="step1">
        <chunk item-count="50">
            <reader ref="fileReader"/>
            <processor ref="dataProcessor"/>
            <writer ref="databaseWriter"/>
        </chunk>
    </step>
</job>
```

Quanti elementi vengono elaborati prima di fare un commit?

A) 1  
B) 10  
C) 50  
D) Dipende dalla configurazione del database

**Risposta:** C

**Spiegazione:** Il parametro `item-count="50"` specifica che 50 elementi vengono letti, processati e scritti prima di committare la transazione.

---

## Domanda 8

Come si passano parametri a un job batch?

A) Attraverso variabili d'ambiente  
B) Usando un oggetto `Properties` nel metodo `start()`  
C) Con l'annotazione `@JobParameter`  
D) Modificando il file XML JSL

**Risposta:** B

**Spiegazione:** I parametri vengono passati al job tramite un oggetto `Properties` nel metodo `start()` del `JobOperator`. Questi parametri sono poi accessibili tramite `JobContext` o con `@BatchProperty`.

---

## Domanda 9

Quale annotazione si usa per iniettare una proprietà batch in un componente?

A) `@Inject`  
B) `@Resource`  
C) `@BatchProperty`  
D) `@JobProperty`

**Risposta:** C

**Spiegazione:** L'annotazione `@BatchProperty` permette di iniettare proprietà definite nel JSL o passate come parametri al job direttamente nei componenti batch.

---

## Domanda 10

Quale listener intercetta eventi a livello di chunk?

A) `JobListener`  
B) `StepListener`  
C) `ChunkListener`  
D) `ItemListener`

**Risposta:** C

**Spiegazione:** `ChunkListener` fornisce callback per intercettare eventi che avvengono prima (`beforeChunk()`), dopo (`afterChunk()`) e in caso di errore (`onError()`) durante l'elaborazione di un chunk.

---

## Domanda 11

Cosa permette di fare la configurazione `<skip-limit>` in un chunk step?

A) Limitare il numero di elementi da processare  
B) Definire il numero massimo di elementi che possono essere saltati in caso di errore  
C) Impostare il timeout del job  
D) Configurare il numero di thread

**Risposta:** B

**Spiegazione:** `<skip-limit>` definisce il numero massimo di eccezioni skippable che possono verificarsi prima che il job fallisca. È usato insieme a `<skippable-exception-classes>`.

---

## Domanda 12

Dato il seguente codice:

```java
@Inject
private JobContext jobContext;

public void process() {
    String jobName = jobContext.getJobName();
    long executionId = jobContext.getExecutionId();
}
```

Cosa fornisce `JobContext`?

A) Accesso al database del job  
B) Accesso alle informazioni runtime del job corrente  
C) Configurazione del server  
D) Lista di tutti i job in esecuzione

**Risposta:** B

**Spiegazione:** `JobContext` fornisce accesso al contesto runtime del job, includendo informazioni come il nome del job, l'ID di esecuzione, i parametri e dati transitori.

---

## Domanda 13

Qual è la differenza tra `retry` e `skip` nella gestione degli errori?

A) Retry elimina gli elementi con errori, skip li riprova  
B) Retry ritenta l'operazione fallita, skip salta l'elemento e continua  
C) Retry è per lettura, skip per scrittura  
D) Non c'è differenza

**Risposta:** B

**Spiegazione:** **Retry** permette di ritentare automaticamente un'operazione fallita (utile per errori temporanei), mentre **skip** permette di saltare elementi problematici e continuare con l'elaborazione degli altri.

---

## Domanda 14

Cosa si intende per "partitioning" nella Batch API?

A) La divisione del database in partizioni  
B) La suddivisione di un dataset in parti elaborate in parallelo  
C) La separazione dei log del job  
D) La distribuzione su più server

**Risposta:** B

**Spiegazione:** Il partitioning permette di suddividere un grande set di dati in partizioni più piccole ed elaborarle in parallelo su thread diversi, migliorando significativamente le performance.

---

## Domanda 15

Come si riavvia un job batch fallito dal punto di interruzione?

A) Chiamando `start()` con lo stesso nome  
B) Usando `restart(executionId, parameters)`  
C) Modificando il file XML e riavviando l'applicazione  
D) Non è possibile riavviare un job fallito

**Risposta:** B

**Spiegazione:** Grazie al checkpointing, un job può essere riavviato usando `JobOperator.restart(executionId, parameters)`, che riprende l'esecuzione dall'ultimo checkpoint salvato.

---

## Domanda 16

Quali sono i possibili valori di `BatchStatus`?

A) RUNNING, STOPPED, COMPLETED  
B) STARTING, STARTED, STOPPING, STOPPED, COMPLETED, FAILED, ABANDONED  
C) SUCCESS, ERROR, WARNING  
D) PENDING, ACTIVE, DONE

**Risposta:** B

**Spiegazione:** `BatchStatus` è un enum che rappresenta lo stato di un job o step e può avere i valori: `STARTING`, `STARTED`, `STOPPING`, `STOPPED`, `COMPLETED`, `FAILED`, `ABANDONED`.

---

## Domanda 17

Cosa restituisce un `ItemProcessor` se vuole filtrare un elemento (non passarlo al writer)?

A) `null`  
B) Un'eccezione  
C) L'oggetto vuoto  
D) `false`

**Risposta:** A

**Spiegazione:** Se un `ItemProcessor` restituisce `null`, l'elemento viene filtrato e non viene passato all'`ItemWriter`. Questo è il meccanismo standard per filtrare elementi durante l'elaborazione.

---

## Domanda 18

Dove devono essere posizionati i file JSL (Job Specification Language)?

A) Nella directory `WEB-INF/`  
B) In `META-INF/batch-jobs/`  
C) Nella root del progetto  
D) In `src/main/resources/`

**Risposta:** B

**Spiegazione:** I file JSL devono essere posizionati nella directory `META-INF/batch-jobs/` dell'applicazione. Il nome del file (senza estensione .xml) diventa l'identificatore del job.

---

## Domanda 19

Quale interfaccia genera dinamicamente le partizioni per un step?

A) `PartitionPlan`  
B) `PartitionMapper`  
C) `PartitionAnalyzer`  
D) `PartitionCollector`

**Risposta:** B

**Spiegazione:** `PartitionMapper` è l'interfaccia che permette di creare dinamicamente le partizioni per un step, restituendo un `PartitionPlan` con il numero di partizioni, thread e proprietà per ogni partizione.

---

## Domanda 20

Come si specifica che un job NON può essere riavviato?

A) Nell'annotazione `@Job(restartable=false)`  
B) Nel JSL con l'attributo `restartable="false"` sul tag `<job>`  
C) Chiamando `setRestartable(false)` su `JobOperator`  
D) I job non sono mai riavviabili per default

**Risposta:** B

**Spiegazione:** Nel file JSL, si può specificare `restartable="false"` nell'elemento `<job>` per indicare che il job non può essere riavviato. Di default, i job sono riavviabili (`restartable="true"`).

---

## Domanda 21

Quale metrica NON è fornita automaticamente dal Batch Runtime?

A) `READ_COUNT`  
B) `WRITE_COUNT`  
C) `EXECUTION_TIME`  
D) `COMMIT_COUNT`

**Risposta:** C

**Spiegazione:** Il Batch Runtime fornisce metriche come `READ_COUNT`, `WRITE_COUNT`, `COMMIT_COUNT`, `ROLLBACK_COUNT`, `SKIP_COUNT`, `FILTER_COUNT`, ma non calcola automaticamente il tempo di esecuzione (che può essere derivato da `getStartTime()` e `getEndTime()` su `JobExecution`).

---

## Domanda 22

Quale elemento JSL permette di eseguire più flow in parallelo?

A) `<parallel>`  
B) `<split>`  
C) `<concurrent>`  
D) `<fork>`

**Risposta:** B

**Spiegazione:** L'elemento `<split>` nel JSL permette di eseguire più `<flow>` in parallelo su thread separati, utile per parallelizzare task indipendenti.

---

## Domanda 23

In un chunk step, quando viene chiamato il metodo `checkpointInfo()` dell'ItemReader?

A) All'inizio dello step  
B) Alla fine di ogni chunk (prima del commit)  
C) Solo in caso di errore  
D) All'inizio di ogni chunk

**Risposta:** B

**Spiegazione:** Il metodo `checkpointInfo()` viene chiamato alla fine di ogni chunk, prima del commit della transazione, per salvare lo stato corrente del reader. Questo stato viene usato per riprendere dal punto corretto in caso di restart.

---

## Domanda 24

Quale annotazione rende un campo iniettabile con un valore di proprietà batch?

A) `@Inject` insieme a `@BatchProperty`  
B) Solo `@BatchProperty`  
C) `@Resource` insieme a `@Named`  
D) `@JobProperty`

**Risposta:** A

**Spiegazione:** Per iniettare una proprietà batch, si usano entrambe le annotazioni: `@Inject` per l'iniezione CDI e `@BatchProperty` per specificare quale proprietà batch iniettare.

---

## Domanda 25

Quale elemento JSL permette di instradare il flusso del job in base all'exit status di uno step?

A) `<switch>`  
B) `<decision>`  
C) `<router>`  
D) `<condition>`

**Risposta:** B

**Spiegazione:** L'elemento `<decision>` nel JSL permette di implementare logica condizionale, instradando il flusso del job verso step diversi in base all'exit status dello step precedente o ad altre condizioni runtime.
