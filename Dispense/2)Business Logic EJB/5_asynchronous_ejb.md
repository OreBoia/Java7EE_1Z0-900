# Metodi Asincroni in EJB (`@Asynchronous`)

A partire da EJB 3.1, è stata introdotta la possibilità di eseguire metodi di business in modo **asincrono**. Questa potente funzionalità permette di migliorare la reattività e le performance delle applicazioni, delegando operazioni a lunga esecuzione a un thread in background.

Quando un client invoca un metodo marcato come asincrono, il container EJB intercetta la chiamata, la affida a un thread separato e restituisce immediatamente il controllo al client, che può così continuare la sua esecuzione senza rimanere bloccato in attesa.

## L'annotazione `@Asynchronous`

Per rendere un metodo (o tutti i metodi di una classe) asincrono, si utilizza l'annotazione `@Asynchronous`.

- **A livello di metodo**: Rende asincrono solo quel metodo specifico.
- **A livello di classe**: Rende asincroni tutti i metodi di business di quell'EJB.

### Tipi di Ritorno e Comportamento

Il comportamento della chiamata asincrona dipende dal tipo di ritorno del metodo:

1. **`void` (Fire-and-Forget)**: Se il metodo ha un tipo di ritorno `void`, il container restituisce immediatamente il controllo al client. Il client non ha modo di sapere quando il task è completato né se ha avuto successo. È ideale per operazioni "fire-and-forget" come il logging o l'invio di notifiche non critiche.

2. **`Future<V>` (Risultato Futuro)**: Se il metodo restituisce un oggetto `java.util.concurrent.Future<V>`, il container restituisce immediatamente un'istanza di `Future`. Questo oggetto agisce come un "segnaposto" per il risultato, che verrà calcolato in futuro. Il client può usare l'oggetto `Future` per controllare lo stato del task, attenderne il completamento e recuperare il risultato.

---

### Esempio 1: L'EJB con metodi asincroni

Creiamo un EJB che espone due metodi asincroni: uno "fire-and-forget" e uno che restituisce un risultato.

```java
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.AsyncResult; // Helper class EJB per creare un Future
import java.util.concurrent.Future;

@Stateless
@Asynchronous // Rende tutti i metodi di questa classe asincroni di default
public class ServizioElaborazioneLunga {

    /**
     * Metodo asincrono "fire-and-forget".
     * Il client che lo chiama non attende la fine dell'esecuzione.
     */
    public void generaReportComplesso() {
        try {
            System.out.println("Inizio generazione report...");
            // Simula un'operazione lunga
            Thread.sleep(10000); // 10 secondi
            System.out.println("Report generato con successo.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Metodo asincrono che restituisce un risultato.
     * @return un Future che conterrà il risultato finale.
     */
    public Future<String> calcolaDatiImportanti(String input) {
        try {
            System.out.println("Inizio calcolo dati per: " + input);
            // Simula un calcolo complesso
            Thread.sleep(5000); // 5 secondi
            String risultato = "Risultato per " + input + " è 42";
            System.out.println("Calcolo completato.");
            
            // AsyncResult è una classe di utilità fornita da EJB
            // per wrappare facilmente un risultato in un oggetto Future.
            return new AsyncResult<>(risultato);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new AsyncResult<>(null); // O gestire l'errore
        }
    }
}
```

---

### Esempio 2: Il Client che invoca i metodi asincroni

Un altro componente, ad esempio un altro EJB o un bean CDI, può invocare i metodi asincroni.

```java
@Stateless
public class ServizioPrincipale {

    @EJB
    private ServizioElaborazioneLunga elaboratore;

    public void avviaOperazioni() throws Exception {
        System.out.println("Servizio Principale: Avvio le operazioni in background.");

        // 1. Chiamata Fire-and-Forget
        // Il controllo torna immediatamente a questa riga.
        elaboratore.generaReportComplesso(); 
        System.out.println("Servizio Principale: La generazione del report è stata avviata in background.");

        // 2. Chiamata con risultato futuro
        // Anche qui, il controllo torna subito, e otteniamo un Future.
        Future<String> risultatoFuturo = elaboratore.calcolaDatiImportanti("input-chiave");
        System.out.println("Servizio Principale: Il calcolo dei dati è stato avviato.");

        // ... possiamo fare altro lavoro qui mentre il calcolo è in esecuzione ...
        System.out.println("Servizio Principale: Faccio altro mentre attendo il risultato...");
        Thread.sleep(1000);

        // 3. Recupero del risultato
        System.out.println("Servizio Principale: Controllo se il calcolo è finito...");
        while (!risultatoFuturo.isDone()) {
            System.out.println("...non ancora, attendo 1 secondo...");
            Thread.sleep(1000);
        }

        // Il metodo get() è bloccante: attende la fine se non è già completato.
        String risultato = risultatoFuturo.get(); 
        System.out.println("Servizio Principale: Risultato finale ricevuto: '" + risultato + "'");
    }
}
```

---

## Casi d'Uso Tipici

- **Migliorare la reattività dell'interfaccia utente**: Un'azione dell'utente può avviare un task in background e dare subito un feedback, senza bloccare l'UI.
- **Operazioni a lunga esecuzione**: Generazione di report, elaborazione di file, backup di dati.
- **Integrazione con sistemi esterni lenti**: Invocare un servizio web esterno che impiega molto tempo a rispondere.
- **Parallelizzazione**: Eseguire più operazioni indipendenti in parallelo per ridurre il tempo totale di esecuzione.

## Riepilogo dei Comandi e API

| Elemento | Tipo | Descrizione |
|---|---|---|
| `@Asynchronous` | Annotazione | Marca un metodo o una classe EJB come asincrona. |
| `Future<V>` | Interfaccia Java | Rappresenta il risultato di una computazione asincrona. Fornisce metodi per controllare lo stato e ottenere il risultato. |
| `AsyncResult<V>` | Classe EJB | Implementazione di `Future<V>` fornita da EJB per restituire facilmente un risultato da un metodo asincrono. |
| `future.isDone()` | Metodo | Restituisce `true` se il task asincrono è completato (normalmente o con errore). |
| `future.get()` | Metodo | Attende il completamento del task (se necessario) e restituisce il risultato. È un'operazione bloccante. |
| `future.cancel(boolean)` | Metodo | Tenta di cancellare l'esecuzione del task. |
