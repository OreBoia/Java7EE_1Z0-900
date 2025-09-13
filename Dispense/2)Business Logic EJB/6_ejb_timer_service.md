# EJB Timer Service: Operazioni Pianificate in Java EE

Il container EJB fornisce un robusto servizio di scheduling, noto come **EJB Timer Service**, per eseguire operazioni a intervalli di tempo regolari o in momenti specifici. Questo servizio è la soluzione standard e transazionale per implementare "cron job" in un ambiente Java EE.

I timer possono essere creati in due modi: **automaticamente** tramite annotazioni o **programmaticamente** tramite un'API.

---

## 1. Timer Automatici (Dichiarativi) con `@Schedule`

Questo è il modo più semplice e comune per creare un task pianificato. Si annota un metodo di un EJB (`@Stateless` o `@Singleton`) con `@Schedule`, specificando quando deve essere eseguito.

### Perché non su un EJB Stateful?

Un EJB `@Stateful` è progettato per mantenere uno stato conversazionale con un **client specifico**. La sua esistenza e il suo stato sono legati alla sessione di quel client. Un timer `@Schedule`, invece, esegue un'operazione a livello di applicazione, indipendentemente da qualsiasi client.

Associare un timer a un bean stateful sarebbe concettualmente sbagliato:

- **A quale istanza apparterrebbe il timer?** Ogni client ha la sua istanza di un bean stateful.
- **Cosa succederebbe quando la sessione del client termina?** Il bean verrebbe distrutto, e con esso il timer, il che va contro l'idea di un task pianificato e affidabile.

Per questi motivi, la specifica EJB **proibisce** l'uso dell'annotazione `@Schedule` sui metodi di un EJB Stateful. I timer automatici sono pensati per operazioni a livello di applicazione (`@Singleton`) o per task stateless (`@Stateless`).

- **Caratteristiche**:
  - **Semplice e Dichiarativo**: La logica di scheduling è definita direttamente nel codice.
  - **Cron-like**: Supporta espressioni simili a quelle di cron per definizioni complesse.
  - **Persistenti di Default**: I timer automatici sono persistenti per impostazione predefinita. Se il server viene riavviato, il container si assicurerà che le esecuzioni mancate vengano recuperate (se configurato) e che lo scheduling riprenda.

### Attributi dell'annotazione `@Schedule`

L'annotazione `@Schedule` ha diversi attributi per definire la pianificazione:

- `second`: Secondi (0-59)
- `minute`: Minuti (0-59)
- `hour`: Ore (0-23)
- `dayOfMonth`: Giorno del mese (1-31)
- `month`: Mese (1-12)
- `dayOfWeek`: Giorno della settimana (0-7, dove 0 e 7 sono Domenica)
- `year`: Anno
- `persistent`: `true` (default) o `false`.
- `timezone`: Per specificare un fuso orario (es. "Europe/Rome").

### Esempio di Codice - Timer Automatico

Un EJB Singleton che esegue un task di pulizia ogni notte alle 2:00 AM.

```java
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ServizioManutenzioneNotturna {

    /**
     * Questo metodo verrà eseguito automaticamente dal container
     * ogni giorno alle 2 del mattino.
     */
    @Schedule(hour = "2", minute = "0", second = "0", persistent = true)
    public void eseguiPuliziaDati() {
        System.out.println("Esecuzione pulizia notturna dei dati temporanei...");
        // ... logica per pulire il database o il filesystem ...
        System.out.println("Pulizia completata.");
    }

    /**
     * Esempio con espressione cron-like: ogni 5 minuti.
     */
    @Schedule(minute = "*/5", hour = "*")
    public void controllaStatoSistema() {
        System.out.println("Controllo stato del sistema...");
    }
}
```

---

## 2. Timer Programmatici con `TimerService`

Per scenari più dinamici, dove i timer devono essere creati, modificati o cancellati a runtime, si usa il `TimerService`.

- **Come funziona**:
    1. Si inietta il `TimerService` nell'EJB.
    2. Si usa il metodo `timerService.createTimer(...)` per creare un nuovo timer. Questo metodo restituisce un oggetto `Timer` che può essere usato per cancellare il timer o ottenere informazioni.
    3. Si definisce un metodo di callback, annotato con `@Timeout`, che il container invocherà quando il timer scade.

### Esempio di Codice - Timer Programmatico

Un servizio che permette di impostare un promemoria per un utente.

```java
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.io.Serializable;

@Stateless
public class ServizioPromemoria {

    @Resource // Iniezione del TimerService
    private TimerService timerService;

    /**
     * Crea un timer che scadrà dopo un certo numero di millisecondi.
     * @param durataMs Durata prima della scadenza.
     * @param info Dati da associare al timer (devono essere serializzabili).
     */
    public void impostaPromemoria(long durataMs, Serializable info) {
        System.out.println("Impostato promemoria tra " + durataMs + " ms per: " + info);
        timerService.createTimer(durataMs, info);
    }

    /**
     * Questo è il metodo di callback che il container invoca
     * quando un timer creato da questo EJB scade.
     * @param timer L'oggetto Timer che è scaduto.
     */
    @Timeout
    public void gestisciScadenza(Timer timer) {
        Serializable info = timer.getInfo();
        System.out.println("PROMEMORIA SCADUTO! Info: " + info);
        
        // Qui si potrebbe inviare un'email, una notifica, etc.
    }
}
```

---

## Persistenza dei Timer

- **Timer Persistenti (`persistent = true`)**: Sopravvivono ai riavvii del server. Il container li memorizza (tipicamente in un database) e li riattiva dopo un riavvio. Sono transazionali e robusti, ideali per operazioni critiche.
- **Timer Non Persistenti (`persistent = false`)**: Esistono solo in memoria. Se il server viene spento, vengono persi. Sono più leggeri e adatti per operazioni non critiche o per notifiche legate allo stato corrente dell'applicazione.

## Riepilogo dei Comandi e API

| Elemento | Tipo | Descrizione |
|---|---|---|
| `@Schedule` | Annotazione | Definisce un timer automatico e dichiarativo con una pianificazione cron-like. |
| `TimerService` | Interfaccia API | Servizio iniettabile (`@Resource`) per la gestione programmatica dei timer. |
| `timerService.createTimer(...)` | Metodo | Crea un nuovo timer programmatico. Può essere a intervallo fisso o a singola esecuzione. |
| `@Timeout` | Annotazione | Identifica il metodo di callback che viene eseguito alla scadenza di un timer programmatico. |
| `Timer` | Interfaccia API | Rappresenta un singolo timer. Permette di cancellarlo (`timer.cancel()`) o di recuperare le informazioni associate (`timer.getInfo()`). |
| `persistent` (in `@Schedule`) | Attributo | Controlla se un timer automatico deve sopravvivere ai riavvii del server. |
