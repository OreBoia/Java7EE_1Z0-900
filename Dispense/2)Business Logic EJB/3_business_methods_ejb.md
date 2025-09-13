# Metodi di Business e Viste Client in EJB

In un Enterprise JavaBean (EJB), i **metodi di business** sono i metodi pubblici, non `static` e non `final`, che espongono le funzionalità del bean ai client. Sono il cuore dell'EJB, l'interfaccia attraverso cui il mondo esterno interagisce con la logica di business incapsulata.

Il container EJB intercetta le chiamate a questi metodi per fornire servizi come transazioni, sicurezza e gestione della concorrenza.

## Viste Client: Come i Client "Vedono" l'EJB

Un client non accede mai direttamente a un'istanza di un EJB. Interagisce invece con un proxy gestito dal container. La **vista client** definisce il contratto (l'insieme di metodi di business) che il client può invocare. Esistono tre tipi principali di viste.

---

### 1. Vista Remota (`@Remote`)

Una vista remota espone i metodi di business a client che si trovano in JVM (Java Virtual Machine) diverse, potenzialmente su macchine differenti.

- **Caratteristiche**:
  - **Distribuzione**: Progettata per applicazioni distribuite.
  - **Passaggio per Valore (Pass-by-Value)**: I parametri e i valori di ritorno vengono serializzati e inviati sulla rete. Questo significa che il client e il server lavorano su copie separate degli oggetti.
  - **Interfaccia Obbligatoria**: Si definisce un'interfaccia Java annotata con `@Remote` che dichiara i metodi di business da esporre.

- **Quando usarla**:
  - Quando un client "puro" (es. un'applicazione desktop Java) deve invocare un EJB su un application server.
  - In architetture distribuite dove diversi application server devono comunicare tra loro.

#### Esempio di Codice - Vista Remota

**1. Definire l'interfaccia remota:**

```java
import javax.ejb.Remote;

@Remote
public interface TraduttoreRemoto {
    String traduci(String testo);
}
```

**2. Implementare l'EJB:**

```java
import javax.ejb.Stateless;

@Stateless
public class ServizioTraduzioneBean implements TraduttoreRemoto {

    @Override
    public String traduci(String testo) {
        // Logica di traduzione...
        return "Testo tradotto: " + testo;
    }
    
    // Questo metodo non è nell'interfaccia remota, quindi non è visibile ai client remoti.
    public void logInterno() {
        // ...
    }
}
```

---

### 2. Vista Locale (`@Local`)

Una vista locale espone i metodi di business a client che si trovano **nella stessa applicazione e JVM**.

- **Caratteristiche**:
  - **Co-locazione**: Client e bean risiedono nello stesso processo.
  - **Passaggio per Riferimento (Pass-by-Reference)**: Le chiamate ai metodi sono molto più performanti perché gli oggetti vengono passati per riferimento, evitando l'overhead della serializzazione.
  - **Interfaccia Opzionale**: Si può definire un'interfaccia annotata con `@Local`, ma non è strettamente necessario (vedi No-Interface View).

- **Quando usarla**: È la scelta standard e raccomandata per la comunicazione tra componenti all'interno della stessa applicazione (es. un Servlet che invoca un EJB, o un EJB che ne invoca un altro).

#### Esempio di Codice - Vista Locale

**1. Definire l'interfaccia locale:**

```java
import javax.ejb.Local;

@Local
public interface GestoreOrdiniLocale {
    void processaOrdine(Ordine ordine);
}
```

**2. Implementare l'EJB:**

```java
import javax.ejb.Stateless;

@Stateless
public class GestoreOrdiniBean implements GestoreOrdiniLocale {

    @Override
    public void processaOrdine(Ordine ordine) {
        // Logica di processamento...
        System.out.println("Processo l'ordine: " + ordine.getId());
    }
}
```

---

### 3. Vista Senza Interfaccia (No-Interface View)

A partire da EJB 3.1, questa è la modalità di default per l'accesso locale. Non è necessario creare un'interfaccia separata; la classe stessa dell'EJB, con i suoi metodi pubblici, agisce da vista.

- **Caratteristiche**:
  - **Semplicità**: Riduce il numero di artefatti da creare e mantenere.
  - **Accesso Locale**: È una vista locale per definizione.
  - **Default**: Se un EJB non implementa esplicitamente un'interfaccia remota o locale, espone automaticamente una vista no-interface.

- **Quando usarla**: Praticamente sempre per l'accesso locale, a meno che non si abbia una ragione specifica per voler disaccoppiare l'implementazione dall'interfaccia.

#### Esempio di Codice - No-Interface View

```java
import javax.ejb.Stateless;

@Stateless
// Nessuna interfaccia implementata, espone una No-Interface View
public class ServizioNotifiche {

    public void inviaEmail(String destinatario, String messaggio) {
        // Logica di invio...
        System.out.println("Email inviata a " + destinatario);
    }
}

// Un altro componente può iniettare e usare il bean direttamente
@Stateless
public class AltroServizio {
    @EJB
    private ServizioNotifiche servizioNotifiche; // Iniezione diretta della classe

    public void faiQualcosa() {
        servizioNotifiche.inviaEmail("test@example.com", "Ciao!");
    }
}
```

## Comportamento di Default e Controllo dell'Accesso

- **Default**: Per default, **tutti i metodi pubblici** di una classe EJB sono considerati metodi di business e vengono esposti nella vista (remota, locale o no-interface).
- **Controllo**: Usare un'interfaccia (locale o remota) è il modo migliore per controllare esattamente quali metodi esporre, poiché solo i metodi definiti nell'interfaccia saranno visibili al client.
- **Sicurezza**: L'accesso ai metodi di business può essere ulteriormente limitato utilizzando la sicurezza basata sui ruoli di Java EE (es. con annotazioni come `@RolesAllowed`), che permette di specificare quali ruoli utente possono invocare determinati metodi.

## Approfondimento sulla Sicurezza Basata sui Ruoli

La sicurezza dichiarativa in EJB permette di definire le policy di accesso ai metodi di business senza scrivere codice di sicurezza complesso. Questo si ottiene tramite annotazioni che specificano quali ruoli sono autorizzati a invocare i metodi.

I ruoli sono definiti a livello di application server o nel descrittore di deployment (`ejb-jar.xml`). Le principali annotazioni sono:

- **`@RolesAllowed({"ruolo1", "ruolo2"})`**: Specifica che solo gli utenti con i ruoli elencati (`ruolo1` o `ruolo2` in questo caso) possono eseguire il metodo. Può essere applicata a livello di classe (per tutti i metodi) o a livello di singolo metodo (per sovrascrivere l'impostazione della classe).

- **`@PermitAll`**: Permette l'accesso al metodo a qualsiasi utente, anche non autenticato. È utile per metodi pubblici come pagine di login o di registrazione.

- **`@DenyAll`**: Nega l'accesso al metodo a tutti gli utenti. È l'opposto di `@PermitAll` e viene usato per disabilitare temporaneamente un metodo senza rimuoverlo dal codice.

### Esempio di Codice - Sicurezza con Annotazioni

```java
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

@Stateless
@RolesAllowed({"ADMIN"}) // Di default, solo gli ADMIN possono accedere ai metodi
public class GestioneBancaBean {

    // Solo gli ADMIN possono chiamare questo metodo (eredita dalla classe)
    public void eseguiOperazioneAmministrativa() {
        // ... logica per amministratori
    }

    @RolesAllowed({"USER", "ADMIN"}) // Sovrascrive l'annotazione della classe
    public void visualizzaSaldo(String contoId) {
        // ... logica per utenti e amministratori
    }

    @PermitAll // Accessibile a tutti, anche utenti non loggati
    public String getInfoBanca() {
        return "Benvenuto nella nostra banca!";
    }

    @DenyAll // Metodo temporaneamente disabilitato per tutti
    public void manutenzioneStraordinaria() {
        // ...
    }
}
```

Se un client non autorizzato tenta di invocare un metodo protetto, il container EJB lancerà un'eccezione `javax.ejb.EJBAccessException`, impedendo l'esecuzione del metodo.

## Riepilogo delle Viste

| Vista | Annotazione | Posizione Client | Passaggio Parametri | Performance | Uso Tipico |
|---|---|---|---|---|---|
| **Remota** | `@Remote` | Altra JVM | Per Valore (lento) | Bassa | Client esterni, comunicazione tra server |
| **Locale** | `@Local` | Stessa JVM | Per Riferimento (veloce) | Alta | Comunicazione interna all'applicazione |
| **No-Interface**| (nessuna) | Stessa JVM | Per Riferimento (veloce) | Alta | **Default per l'accesso locale** |
