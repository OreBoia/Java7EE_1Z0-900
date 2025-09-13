# Tipi di Session Bean EJB in Java EE 7

Gli Enterprise JavaBeans (EJB) sono componenti server-side gestiti dal container Java EE che incapsulano la logica di business di un'applicazione. I Session Bean sono il tipo più comune di EJB e si dividono in tre categorie principali, ognuna con uno scopo e un ciclo di vita specifici.

---

## 1. Stateless Session Bean (`@Stateless`)

Come suggerisce il nome, uno Stateless Session Bean **non mantiene uno stato conversazionale** legato a un client specifico tra una chiamata di metodo e l'altra.

- **Caratteristiche Principali**:
  - **Senza stato (Stateless)**: Ogni invocazione di un metodo è indipendente dalle precedenti. Non memorizza dati specifici del client.
  - **Pooling di istanze**: Il container EJB gestisce un pool di istanze identiche di questo bean. Quando un client effettua una chiamata, il container ne preleva una dal pool, la usa per l'esecuzione del metodo e poi la rende nuovamente disponibile per altri client. Questo li rende estremamente efficienti e scalabili.
  - **Thread-safe per natura**: Poiché non c'è uno stato condiviso per client, sono intrinsecamente sicuri in un ambiente concorrente.

- **Quando usarli**: Sono la scelta ideale per la maggior parte dei servizi di business che eseguono operazioni atomiche e riutilizzabili, come:
  - Servizi di accesso ai dati (DAO/Repository).
  - Operazioni di calcolo che non richiedono memoria di chiamate precedenti.
  - Servizi web (es. endpoint JAX-RS).

### Esempio di Codice - Stateless Session Bean

```java
import javax.ejb.Stateless;

@Stateless
public class CalcolatriceService {

    /**
     * Un metodo di un bean stateless. Non ricorda nulla
     * delle operazioni precedenti.
     */
    public int somma(int a, int b) {
        // Esegue l'operazione e restituisce il risultato.
        // Nessuno stato viene salvato nell'istanza del bean.
        return a + b;
    }
}
```

---

## 2. Stateful Session Bean (`@Stateful`)

Uno Stateful Session Bean **mantiene uno stato conversazionale** specifico per il client che lo sta utilizzando. Ogni client interagisce con la propria istanza dedicata del bean.

- **Caratteristiche Principali**:
  - **Con stato (Stateful)**: Mantiene i valori delle sue variabili d'istanza tra le chiamate dello stesso client.
  - **Istanza dedicata**: Il container crea un'istanza per ogni client e la associa a quella sessione.
  - **Ciclo di vita conversazionale**: Lo stato può evolvere nel tempo attraverso più chiamate a metodi.
  - **Passivazione/Attivazione**: Per ottimizzare l'uso della memoria, il container può "passivare" un'istanza inattiva (serializzandola su disco) e "attivarla" (ricaricandola in memoria) quando il client la richiede di nuovo.

- **Quando usarli**: Sono perfetti per processi che si sviluppano in più passaggi e richiedono di mantenere informazioni tra le interazioni dell'utente.
  - Carrelli della spesa in un sito di e-commerce.
  - Wizard di registrazione multi-pagina.
  - Processi di prenotazione complessi.

### Esempio di Codice - Stateful Session Bean

```java
import javax.ejb.Stateful;
import java.util.ArrayList;
import java.util.List;

@Stateful
public class CarrelloAcquisti {

    private List<String> articoli = new ArrayList<>();

    public void aggiungiArticolo(String articolo) {
        // Lo stato (la lista di articoli) viene mantenuto tra le chiamate.
        this.articoli.add(articolo);
    }

    public List<String> getArticoli() {
        return this.articoli;
    }

    public void checkout() {
        // Logica di acquisto...
        this.articoli.clear(); // Pulisce lo stato alla fine della conversazione.
    }
}
```

---

## 3. Singleton Session Bean (`@Singleton`)

Un Singleton Session Bean è un bean di cui esiste **una sola istanza per l'intera applicazione**. Questa singola istanza è condivisa tra tutti i client.

- **Caratteristiche Principali**:
  - **Istanza unica**: Il container EJB crea una sola istanza del bean all'avvio dell'applicazione e la distrugge quando l'applicazione viene fermata.
  - **Stato condiviso**: Lo stato di un singleton è condiviso globalmente. Questo lo rende potente ma richiede un'attenta gestione della concorrenza.
  - **Gestione della concorrenza**: Di default, il container serializza l'accesso ai metodi del singleton per prevenire problemi di concorrenza (usando un lock in scrittura). Questo comportamento può essere personalizzato con le annotazioni `@Lock(LockType.READ)` e `@Lock(LockType.WRITE)`.

- **Quando usarli**: Per gestire dati o logiche che devono essere centralizzati e condivisi a livello di applicazione.
  - Cache di dati di configurazione o dati raramente modificati.
  - Coordinamento di operazioni a livello di applicazione.
  - Contatori globali o registri.

### Esempio di Codice - Singleton Session Bean

```java
import javax.ejb.Singleton;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.annotation.PostConstruct;

@Singleton
public class ConfiguratoreApplicazione {

    private String urlServizioEsterno;

    @PostConstruct // Eseguito una sola volta, alla creazione del singleton
    public void inizializza() {
        // Carica la configurazione da un file o da un'altra sorgente
        this.urlServizioEsterno = "http://api.example.com/data";
        System.out.println("Singleton ConfiguratoreApplicazione creato.");
    }

    @Lock(LockType.READ) // Permette letture concorrenti
    public String getUrlServizioEsterno() {
        return this.urlServizioEsterno;
    }
    
    @Lock(LockType.WRITE) // Solo un thread alla volta può modificare
    public void setUrlServizioEsterno(String nuovoUrl) {
        this.urlServizioEsterno = nuovoUrl;
    }
}
```

---

### Nota sui Message-Driven Beans (MDB)

Oltre ai Session Bean, esiste un altro tipo importante di EJB: i **Message-Driven Bean (`@MessageDriven`)**. Questi bean non vengono invocati direttamente dai client, ma agiscono come ascoltatori asincroni di messaggi provenienti da una coda o un topic JMS (Java Message Service). Sono fondamentali per l'integrazione asincrona tra sistemi.

## Riepilogo Comparativo

| Caratteristica | `@Stateless` | `@Stateful` | `@Singleton` |
|---|---|---|---|
| **Stato** | Nessuno stato conversazionale | Mantiene lo stato per un client | Mantiene uno stato globale condiviso |
| **Istanze** | Pool di istanze identiche | Un'istanza per client | Una sola istanza per applicazione |
| **Performance** | Molto alta | Inferiore (overhead di gestione stato) | Alta, ma dipende dalla contesa |
| **Concorrenza** | Sicuro per natura | Non concorrente (un client per istanza) | Gestita dal container con `@Lock` |
| **Uso Tipico** | Servizi di business generici | Processi multi-step (carrello) | Cache, configurazioni globali |

## Lista dei Comandi e Annotazioni

| Annotazione | Tipo | Descrizione |
|---|---|---|
| `@Stateless` | EJB | Definisce un Session Bean senza stato. |
| `@Stateful` | EJB | Definisce un Session Bean con stato conversazionale. |
| `@Singleton` | EJB | Definisce un Session Bean con una sola istanza per applicazione. |
| `@Lock` | EJB (Singleton) | Controlla l'accesso concorrente ai metodi di un Singleton. Accetta `LockType.READ` o `LockType.WRITE`. |
| `@PostConstruct` | Lifecycle | Identifica un metodo da eseguire dopo la creazione del bean. |
| `@MessageDriven` | EJB | Definisce un bean che agisce come ascoltatore di messaggi JMS. |
