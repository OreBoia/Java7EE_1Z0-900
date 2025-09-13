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
