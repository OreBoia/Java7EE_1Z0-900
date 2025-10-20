# API Batch per Java EE (JSR 352)

L'API Batch (JSR 352), introdotta in Java EE 7, fornisce un framework standardizzato per la creazione, l'esecuzione e la gestione di job batch. I job batch sono processi che operano su grandi volumi di dati in background, senza richiedere interazione da parte dell'utente. Sono ideali per compiti come la generazione di report, l'elaborazione di transazioni finanziarie, la migrazione di dati e altre operazioni di lunga durata.

Il framework separa nettamente la **logica di business** (implementata in classi Java) dalla **struttura del job** (definita in un file XML), promuovendo riusabilità e manutenibilità.

## Architettura di un Job Batch

Un job batch è definito tramite **Job Specification Language (JSL)**, un linguaggio basato su XML. I file JSL, tipicamente situati in `META-INF/batch-jobs/`, descrivono la sequenza di operazioni che compongono il job.

I componenti principali di un job sono:

* **Job**: L'unità di esecuzione principale, identificata da un ID.
* **Step**: Un'unità di lavoro autonoma all'interno di un job. Un job è composto da uno o più step.
* **Flow, Split, Decision**: Elementi che controllano l'ordine di esecuzione degli step (sequenziale, parallelo, condizionale).

## Tipi di Step

Esistono due tipi fondamentali di step.

### 1. Step di tipo `chunk`

Questo è il modello più comune per l'elaborazione di dati in batch. Un `chunk` step processa i dati in blocchi (chunks) di una dimensione definita (`item-count`). Ogni chunk viene elaborato all'interno di una singola transazione. Questo approccio garantisce performance e resilienza, grazie al meccanismo di **checkpointing**: al termine di ogni chunk, lo stato di avanzamento viene salvato, permettendo di riavviare il job dal punto esatto in cui si è interrotto in caso di fallimento.

Un `chunk` step è composto da tre componenti:

* **`ItemReader`**: Legge i dati da una sorgente (es. file CSV, database, coda JMS) un elemento alla volta.
* **`ItemProcessor`**: Esegue la logica di business su ogni elemento letto. Può trasformare, filtrare o arricchire i dati. Questo componente è opzionale.
* **`ItemWriter`**: Scrive i dati elaborati (il "chunk") in una destinazione (es. database, file, API esterna).

#### Esempio di Flusso `chunk`

Un job per il calcolo delle buste paga:

1. **`ItemReader`**: Legge le ore lavorate di un dipendente da un file CSV.
2. **`ItemProcessor`**: Calcola lo stipendio sulla base delle ore e della tariffa oraria.
3. **`ItemWriter`**: Dopo aver processato un "chunk" di 10 dipendenti, scrive i 10 record risultanti nella tabella del database delle buste paga. La transazione viene committata.

### 2. Step di tipo `batchlet` (o task)

Un `batchlet` è uno step più semplice che esegue un'operazione singola e coesa, senza il modello reader-processor-writer. È utile per compiti come l'invio di una email di notifica, la pulizia di file temporanei o l'esecuzione di un comando SQL di DDL.

#### Esempio di Codice `batchlet`

```java
import javax.batch.api.AbstractBatchlet;
import javax.inject.Named;

@Named
public class CleanupBatchlet extends AbstractBatchlet {
    @Override
    public String process() throws Exception {
        System.out.println("Esecuzione pulizia file temporanei...");
        // Logica per eliminare i file...
        return "COMPLETED"; // Ritorna l'exit status
    }
}
```

## Definizione di un Job (JSL)

La struttura del job, i riferimenti alle classi Java e il flusso di esecuzione sono definiti nell'XML.

```xml
<!-- META-INF/batch-jobs/payroll-job.xml -->
<job id="payrollJob" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <step id="calculatePayrollStep">
        <chunk item-count="10"> <!-- Processa 10 item per transazione -->
            <reader ref="employeeDataReader"/>    <!-- Bean CDI o nome classe -->
            <processor ref="payrollProcessor"/>
            <writer ref="databaseWriter"/>
        </chunk>
    </step>
    <step id="notificationStep">
        <batchlet ref="notificationBatchlet"/>
    </step>
</job>
```

In questo esempio, `employeeDataReader`, `payrollProcessor`, ecc., sono i nomi dei bean CDI (o i fully qualified name delle classi) che implementano le interfacce `ItemReader`, `ItemProcessor`, ecc.

## Controllo del Flusso di Esecuzione

* **Flow**: Raggruppa una serie di step da eseguire in sequenza.
* **Split**: Permette di eseguire più `flow` in parallelo, su thread separati. È utile per parallelizzare task indipendenti.
* **Decision**: Elemento condizionale che instrada il flusso del job verso step diversi in base all'**exit status** dello step precedente.

## Esecuzione e Gestione dei Job

I job batch vengono avviati e controllati programmaticamente tramite l'interfaccia `JobOperator`.

```java
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.Stateless;
import java.util.Properties;

@Stateless
public class BatchStarter {

    public void runPayrollJob() {
        // Ottiene un'handle al JobOperator
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        
        // Avvia il job batch specificando il nome del file XML (senza .xml)
        long executionId = jobOperator.start("payrollJob", new Properties());
        
        System.out.println("Job avviato con ID di esecuzione: " + executionId);
    }
}
```

Il container (Batch Runtime) si occupa di gestire il ciclo di vita di ogni esecuzione, tracciandone lo stato (`STARTING`, `STARTED`, `COMPLETED`, `FAILED`, `STOPPED`) e garantendo la ripartenza dai checkpoint.

## Parametri e Proprietà dei Job

### Job Parameters

I parametri possono essere passati al job al momento dell'avvio e sono accessibili durante l'esecuzione tramite `JobContext` o `StepContext`.

```java
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import java.util.Properties;

public class JobLauncher {
    public long startJobWithParameters() {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        
        // Definisce i parametri del job
        Properties jobParams = new Properties();
        jobParams.setProperty("inputFile", "/data/employees.csv");
        jobParams.setProperty("outputDir", "/reports/");
        jobParams.setProperty("reportDate", "2025-10-20");
        
        // Avvia il job con i parametri
        return jobOperator.start("payrollJob", jobParams);
    }
}
```

### Accesso ai Parametri negli Step

I parametri possono essere letti tramite `JobContext` o direttamente iniettati con `@BatchProperty`.

```java
import javax.batch.api.chunk.ItemReader;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
public class FileItemReader implements ItemReader {

    @Inject
    private JobContext jobContext;
    
    // Iniezione diretta di una proprietà
    @Inject
    @BatchProperty(name = "inputFile")
    private String inputFile;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        // Accesso ai parametri tramite JobContext
        Properties jobParams = jobContext.getProperties();
        String reportDate = jobParams.getProperty("reportDate");
        
        System.out.println("Apertura file: " + inputFile);
        System.out.println("Data report: " + reportDate);
        
        // Inizializzazione del reader...
    }

    @Override
    public Object readItem() throws Exception {
        // Legge un elemento dal file
        return null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        // Salva lo stato corrente per il checkpoint
        return null;
    }

    @Override
    public void close() throws Exception {
        // Chiude le risorse
    }
}
```

### Proprietà negli Step (JSL)

Le proprietà possono anche essere definite direttamente nel file XML e passate ai componenti.

```xml
<job id="dataProcessingJob" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <step id="processStep">
        <chunk item-count="100">
            <reader ref="fileReader">
                <properties>
                    <property name="fileName" value="#{jobParameters['inputFile']}"/>
                    <property name="encoding" value="UTF-8"/>
                </properties>
            </reader>
            <processor ref="dataProcessor"/>
            <writer ref="databaseWriter"/>
        </chunk>
    </step>
</job>
```

## Listeners nei Batch Job

I listener permettono di intercettare eventi durante l'esecuzione del job, degli step e dei chunk. Sono utili per logging, metriche, notifiche e gestione di operazioni trasversali.

### Tipi di Listener

| Listener | Interfaccia | Eventi |
|----------|-------------|--------|
| **Job Listener** | `JobListener` | `beforeJob()`, `afterJob()` |
| **Step Listener** | `StepListener` | `beforeStep()`, `afterStep()` |
| **Chunk Listener** | `ChunkListener` | `beforeChunk()`, `afterChunk()` |
| **Item Read Listener** | `ItemReadListener` | `beforeRead()`, `afterRead()`, `onReadError()` |
| **Item Process Listener** | `ItemProcessListener` | `beforeProcess()`, `afterProcess()`, `onProcessError()` |
| **Item Write Listener** | `ItemWriteListener` | `beforeWrite()`, `afterWrite()`, `onWriteError()` |

### Esempio di Job Listener

```java
import javax.batch.api.listener.JobListener;
import javax.inject.Named;

@Named
public class PayrollJobListener implements JobListener {

    @Override
    public void beforeJob() throws Exception {
        System.out.println("Job iniziato: preparazione delle risorse...");
        // Invia notifica, inizializza log, ecc.
    }

    @Override
    public void afterJob() throws Exception {
        System.out.println("Job completato: pulizia delle risorse...");
        // Invia email di notifica, genera report finale, ecc.
    }
}
```

### Esempio di Chunk Listener

```java
import javax.batch.api.chunk.listener.ChunkListener;
import javax.inject.Named;

@Named
public class TransactionChunkListener implements ChunkListener {

    @Override
    public void beforeChunk() throws Exception {
        System.out.println("Inizio elaborazione chunk...");
    }

    @Override
    public void afterChunk() throws Exception {
        System.out.println("Chunk completato e committato.");
    }

    @Override
    public void onError(Exception ex) throws Exception {
        System.err.println("Errore durante l'elaborazione del chunk: " + ex.getMessage());
    }
}
```

### Configurazione dei Listener nel JSL

```xml
<job id="payrollJob" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <listeners>
        <listener ref="payrollJobListener"/>
    </listeners>
    
    <step id="calculateStep">
        <listeners>
            <listener ref="stepListener"/>
        </listeners>
        <chunk item-count="50">
            <reader ref="employeeReader">
                <listeners>
                    <listener ref="itemReadListener"/>
                </listeners>
            </reader>
            <processor ref="payrollProcessor">
                <listeners>
                    <listener ref="itemProcessListener"/>
                </listeners>
            </processor>
            <writer ref="databaseWriter">
                <listeners>
                    <listener ref="itemWriteListener"/>
                </listeners>
            </writer>
            <chunk-listener ref="transactionChunkListener"/>
        </chunk>
    </step>
</job>
```

## Gestione degli Errori: Skip, Retry e Rollback

Nei chunk step, è possibile configurare politiche sofisticate per la gestione degli errori.

### Skip - Saltare elementi problematici

Permette di saltare elementi che causano errori, continuando l'elaborazione degli altri. Utile quando pochi record corrotti non devono bloccare l'intero job.

```xml
<chunk item-count="100">
    <reader ref="fileReader"/>
    <processor ref="dataProcessor"/>
    <writer ref="databaseWriter"/>
    
    <!-- Configurazione dello skip -->
    <skippable-exception-classes>
        <include class="java.io.IOException"/>
        <include class="com.example.InvalidDataException"/>
    </skippable-exception-classes>
    <skip-limit>10</skip-limit> <!-- Massimo 10 errori skippabili -->
</chunk>
```

### Retry - Ritentare operazioni fallite

Permette di riprovare automaticamente operazioni che potrebbero essere temporaneamente fallite (es. timeout di rete, lock sul database).

```xml
<chunk item-count="100">
    <reader ref="fileReader"/>
    <processor ref="dataProcessor"/>
    <writer ref="databaseWriter"/>
    
    <!-- Configurazione del retry -->
    <retryable-exception-classes>
        <include class="java.sql.SQLException"/>
        <include class="javax.persistence.OptimisticLockException"/>
    </retryable-exception-classes>
    <retry-limit>3</retry-limit> <!-- Massimo 3 tentativi -->
</chunk>
```

### Rollback Personalizzato

Di default, qualsiasi eccezione causa il rollback della transazione del chunk. È possibile configurare quali eccezioni **non** devono causare rollback.

```xml
<chunk item-count="100">
    <reader ref="fileReader"/>
    <processor ref="dataProcessor"/>
    <writer ref="databaseWriter"/>
    
    <!-- Eccezioni che NON causano rollback -->
    <no-rollback-exception-classes>
        <include class="com.example.WarningException"/>
    </no-rollback-exception-classes>
</chunk>
```

## Partitioning - Elaborazione Parallela

Il **partitioning** permette di suddividere un grande set di dati in parti più piccole (partizioni) ed elaborarle in parallelo su thread diversi. È ideale per migliorare le performance su dataset molto grandi.

### Architettura del Partitioning

* **Partition Plan**: Definisce quante partizioni creare e come distribuire i dati.
* **Partition Mapper**: Interfaccia `PartitionMapper` che genera le partizioni.
* **Partition Analyzer**: Interfaccia `PartitionAnalyzer` che aggrega i risultati delle partizioni.
* **Partition Collector**: Interfaccia `PartitionCollector` che raccoglie dati da ogni partizione.

### Esempio di Partition Plan nel JSL

```xml
<job id="massiveDataJob" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <step id="partitionedStep">
        <chunk item-count="1000">
            <reader ref="dataReader"/>
            <processor ref="dataProcessor"/>
            <writer ref="dataWriter"/>
        </chunk>
        
        <!-- Definizione del partitioning -->
        <partition>
            <plan partitions="4" threads="4">
                <!-- Ogni partizione riceve proprietà diverse -->
                <properties partition="0">
                    <property name="startRecord" value="0"/>
                    <property name="endRecord" value="25000"/>
                </properties>
                <properties partition="1">
                    <property name="startRecord" value="25000"/>
                    <property name="endRecord" value="50000"/>
                </properties>
                <properties partition="2">
                    <property name="startRecord" value="50000"/>
                    <property name="endRecord" value="75000"/>
                </properties>
                <properties partition="3">
                    <property name="startRecord" value="75000"/>
                    <property name="endRecord" value="100000"/>
                </properties>
            </plan>
        </partition>
    </step>
</job>
```

### PartitionMapper - Creazione Dinamica di Partizioni

```java
import javax.batch.api.partition.PartitionMapper;
import javax.batch.api.partition.PartitionPlan;
import javax.batch.api.partition.PartitionPlanImpl;
import javax.inject.Named;
import java.util.Properties;

@Named
public class DynamicPartitionMapper implements PartitionMapper {

    @Override
    public PartitionPlan mapPartitions() throws Exception {
        // Calcola dinamicamente il numero di partizioni
        int totalRecords = 100000;
        int recordsPerPartition = 25000;
        int numPartitions = totalRecords / recordsPerPartition;
        
        PartitionPlanImpl plan = new PartitionPlanImpl();
        plan.setPartitions(numPartitions);
        plan.setThreads(numPartitions);
        
        // Crea le proprietà per ogni partizione
        Properties[] partitionProps = new Properties[numPartitions];
        for (int i = 0; i < numPartitions; i++) {
            Properties props = new Properties();
            props.setProperty("startRecord", String.valueOf(i * recordsPerPartition));
            props.setProperty("endRecord", String.valueOf((i + 1) * recordsPerPartition));
            partitionProps[i] = props;
        }
        
        plan.setPartitionProperties(partitionProps);
        return plan;
    }
}
```

```xml
<!-- Uso del mapper dinamico -->
<partition>
    <mapper ref="dynamicPartitionMapper"/>
</partition>
```

## Restart e Stop dei Job

### Fermare un Job

```java
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;

public class JobController {
    public void stopJob(long executionId) {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        jobOperator.stop(executionId);
        System.out.println("Richiesta di stop inviata per l'esecuzione: " + executionId);
    }
}
```

### Riavviare un Job

Grazie al checkpointing, un job può essere riavviato dal punto in cui si è interrotto.

```java
public void restartJob(long executionId) {
    JobOperator jobOperator = BatchRuntime.getJobOperator();
    
    // Riavvia il job dalla sua ultima esecuzione
    Properties restartParams = new Properties();
    restartParams.setProperty("retryAttempt", "2");
    
    long newExecutionId = jobOperator.restart(executionId, restartParams);
    System.out.println("Job riavviato con nuovo ID: " + newExecutionId);
}
```

### Restartable Attribute

Nel JSL, è possibile specificare se un job può essere riavviato:

```xml
<job id="criticalJob" restartable="true" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <step id="step1">
        <!-- ... -->
    </step>
</job>
```

## JobContext e StepContext

Questi oggetti forniscono accesso al contesto runtime del job e degli step.

### Esempio con JobContext

```java
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ContextAwareProcessor implements ItemProcessor {

    @Inject
    private JobContext jobContext;
    
    @Inject
    private StepContext stepContext;

    @Override
    public Object processItem(Object item) throws Exception {
        // Accesso alle informazioni del job
        String jobName = jobContext.getJobName();
        long executionId = jobContext.getExecutionId();
        
        // Accesso alle informazioni dello step
        String stepName = stepContext.getStepName();
        
        // Lettura di parametri
        Properties jobParams = jobContext.getProperties();
        String reportDate = jobParams.getProperty("reportDate");
        
        System.out.println("Job: " + jobName + ", Step: " + stepName);
        System.out.println("Elaborazione per data: " + reportDate);
        
        // Impostazione di dati transitori (accessibili dai listener)
        stepContext.setTransientUserData("lastProcessedItem", item);
        
        return item;
    }
}
```

## Metriche e Monitoraggio

Il Batch Runtime fornisce API per monitorare lo stato e le metriche dei job.

```java
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.StepExecution;
import javax.batch.runtime.Metric;
import java.util.List;

public class JobMonitor {

    public void monitorJob(long executionId) {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        
        // Recupera le informazioni sull'esecuzione
        JobExecution jobExecution = jobOperator.getJobExecution(executionId);
        
        System.out.println("Job Name: " + jobExecution.getJobName());
        System.out.println("Batch Status: " + jobExecution.getBatchStatus());
        System.out.println("Start Time: " + jobExecution.getStartTime());
        System.out.println("End Time: " + jobExecution.getEndTime());
        
        // Verifica lo stato
        if (jobExecution.getBatchStatus() == BatchStatus.COMPLETED) {
            System.out.println("Job completato con successo!");
        } else if (jobExecution.getBatchStatus() == BatchStatus.FAILED) {
            System.out.println("Job fallito!");
        }
        
        // Recupera le metriche degli step
        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
        for (StepExecution stepExecution : stepExecutions) {
            System.out.println("\nStep: " + stepExecution.getStepName());
            
            Metric[] metrics = stepExecution.getMetrics();
            for (Metric metric : metrics) {
                System.out.println(metric.getType() + ": " + metric.getValue());
            }
        }
    }
}
```

### Metriche Disponibili

* **READ_COUNT**: Numero di elementi letti
* **WRITE_COUNT**: Numero di elementi scritti
* **COMMIT_COUNT**: Numero di transazioni committate
* **ROLLBACK_COUNT**: Numero di rollback
* **READ_SKIP_COUNT**: Elementi skippati in lettura
* **PROCESS_SKIP_COUNT**: Elementi skippati in elaborazione
* **WRITE_SKIP_COUNT**: Elementi skippati in scrittura
* **FILTER_COUNT**: Elementi filtrati dal processor

## Tabella dei Termini e Concetti Chiave

| Termine | Descrizione |
| --- | --- |
| **JSR 352** | La specifica Java che definisce le API Batch. |
| **JSL (Job Specification Language)** | Il linguaggio XML usato per definire la struttura e il flusso di un job batch. |
| **`JobOperator`** | Interfaccia principale per avviare, fermare, riavviare e ispezionare i job. |
| **Step `chunk`** | Un tipo di step che processa dati in blocchi, composto da `ItemReader`, `ItemProcessor`, `ItemWriter`. |
| **Step `batchlet`** | Un tipo di step che esegue un singolo task. |
| **`ItemReader`** | Componente che legge i dati di input. |
| **`ItemProcessor`** | Componente che elabora i dati (logica di business). |
| **`ItemWriter`** | Componente che scrive i dati elaborati. |
| **Checkpointing** | Il processo di salvataggio periodico dello stato di un job, che ne permette la ripresa in caso di fallimento. Avviene alla fine di ogni transazione di un `chunk`. |
| **Exit Status** | Una stringa restituita da uno step al suo completamento, usata dagli elementi `Decision` per controllare il flusso. |
| **Batch Runtime** | L'ambiente fornito dal container Java EE che gestisce l'esecuzione, il ciclo di vita e lo stato dei job. |
| **`@BatchProperty`** | Annotazione per iniettare proprietà definite nel JSL o passate come parametri. |
| **`JobContext`** | Fornisce accesso al contesto runtime del job corrente (nome, ID esecuzione, parametri). |
| **`StepContext`** | Fornisce accesso al contesto runtime dello step corrente. |
| **Skip** | Politica che permette di saltare elementi che causano eccezioni specifiche. |
| **Retry** | Politica che permette di ritentare automaticamente operazioni fallite. |
| **Partitioning** | Tecnica per dividere un set di dati in partizioni ed elaborarle in parallelo. |
| **`PartitionMapper`** | Componente che crea dinamicamente le partizioni per un step. |
| **Listener** | Componente che intercetta eventi durante l'esecuzione (job, step, chunk, item). |
| **`BatchStatus`** | Enum che rappresenta lo stato di un job o step (`STARTING`, `STARTED`, `COMPLETED`, `FAILED`, `STOPPED`, `ABANDONED`). |
| **Metrics** | Statistiche sull'esecuzione raccolte automaticamente dal runtime (count di read, write, skip, ecc.). |
