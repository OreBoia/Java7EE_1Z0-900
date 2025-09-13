# Integrazione di JSF con CDI

In Java EE 6 e versioni successive, **Contexts and Dependency Injection (CDI)** è diventato il framework standard per la gestione dei bean e l'iniezione delle dipendenze. A partire da JSF 2.0, e in modo ancora più profondo in JSF 2.2 (parte di Java EE 7), l'integrazione tra JSF e CDI è stata notevolmente migliorata, tanto che l'uso dei bean CDI è diventato la pratica raccomandata per i backing bean di JSF, sostituendo i vecchi `@ManagedBean` specifici di JSF.

## Unificazione dell'Expression Language (EL)

La caratteristica chiave dell'integrazione è l'**unificazione dell'Expression Language (EL)**. Questo significa che il resolver EL di JSF è in grado di trovare e risolvere direttamente i bean gestiti da CDI.

In pratica, qualsiasi bean CDI annotato con `@Named` diventa immediatamente accessibile da una pagina Facelets tramite EL, senza alcuna configurazione aggiuntiva.

## Vantaggi dell'Uso di CDI in JSF

1. **Standard De Facto**: CDI è il modello a componenti standard di Java EE, utilizzato non solo da JSF ma anche da JAX-RS, EJB, e altri. Usarlo in JSF rende l'architettura dell'applicazione più coerente.
2. **Dependency Injection Potenziata**: CDI offre un meccanismo di iniezione delle dipendenze (`@Inject`) molto più potente e flessibile rispetto a quello base di JSF. Permette di iniettare facilmente altri bean, EJB, risorse, ecc.
3. **Scope Potenziati**: CDI fornisce un set di scope standard (`@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`) e introduce lo scope `@ConversationScoped`, che offre un controllo programmatico sulla durata di uno scope a cavallo di più richieste.
4. **Eventi e Interceptor**: CDI ha un potente modello a eventi (`@Observes`) e un sistema di interceptor (`@Interceptor`) che permettono di costruire applicazioni disaccoppiate e modulari.

## Sostituzione di `@ManagedBean` con `@Named`

Con l'integrazione, la prassi è quella di non usare più l'annotazione `@javax.faces.bean.ManagedBean` di JSF, ma di usare l'annotazione `@javax.inject.Named` di CDI.

### Esempio: Backing Bean come Bean CDI

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("prodottoBean") // 1. Rende il bean accessibile in EL con il nome "prodottoBean"
@RequestScoped      // 2. Usa lo scope di CDI
public class ProdottoBean {

    private String nomeProdotto;
    
    // 3. Iniezione di un altro servizio/bean tramite CDI
    @Inject
    private DatabaseService dbService;

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }

    public String salvaProdotto() {
        if (dbService.salva(nomeProdotto)) {
            return "success"; // Outcome di navigazione
        } else {
            return "failure";
        }
    }
}

// Un ipotetico servizio che viene iniettato
@ApplicationScoped
public class DatabaseService {
    public boolean salva(String data) {
        System.out.println("Salvataggio di: " + data);
        return true;
    }
}
```

Nella pagina Facelets, l'uso è identico:

```xhtml
<h:form>
    Nome Prodotto: <h:inputText value="#{prodottoBean.nomeProdotto}" />
    <h:commandButton value="Salva" action="#{prodottoBean.salvaProdotto}" />
</h:form>
```

## La Questione di `@ViewScoped`

Uno degli scope più utili in JSF è `@ViewScoped`, che mantiene un bean vivo finché l'utente rimane sulla stessa pagina, anche attraverso richieste AJAX.

- **JSF `@ViewScoped`**: `javax.faces.bean.ViewScoped`
- **CDI `@ViewScoped`**: `javax.faces.view.ViewScoped` (introdotto in JSF 2.2 per colmare il gap)

In **Java EE 7 (JSF 2.2)**, è stato introdotto uno scope `@ViewScoped` compatibile con CDI (`javax.faces.view.ViewScoped`). Questo permette di avere bean CDI che sono legati al ciclo di vita di una vista JSF.

Tuttavia, prima di questa aggiunta e in alcune implementazioni, l'uso di `@ViewScoped` con i bean CDI non era diretto e poteva richiedere estensioni di terze parti (come quelle fornite da Seam 3 o Apache CODI). Per la certificazione 1Z0-900, è importante sapere che **JSF 2.2 ha standardizzato uno scope `@ViewScoped` per CDI**, rendendo l'integrazione completa.

### Esempio con `@ViewScoped` CDI

```java
import javax.inject.Named;
import javax.faces.view.ViewScoped; // Importare la classe corretta!
import java.io.Serializable;

@Named("dettaglioOrdineBean")
@ViewScoped
public class DettaglioOrdineBean implements Serializable { // I bean passivanti devono essere Serializable

    private int ordineId;
    private Ordine ordine;

    // ... logica per caricare l'ordine in base all'ID ...

    public void ricaricaDettagli() {
        // Logica eseguita via AJAX senza distruggere il bean
    }
    
    // Getter e Setter
}
```

## Glossario dei Termini Importanti

| Termine                 | Definizione                                                                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **CDI (Contexts and Dependency Injection)** | Il framework standard di Java EE per l'iniezione delle dipendenze e la gestione del ciclo di vita dei bean.          |
| **`@Named`**            | Annotazione CDI che rende un bean gestito da CDI accessibile tramite Expression Language (EL) con il nome specificato.                   |
| **`@Inject`**           | Annotazione CDI usata per richiedere l'iniezione di un'istanza di un altro bean o risorsa.                                              |
| **Unificazione EL**     | Il meccanismo che permette all'EL di JSF di risolvere e accedere direttamente ai bean gestiti dal container CDI.                         |
| **`@ManagedBean`**      | L'annotazione "legacy" di JSF per definire un backing bean. Sostituita da `@Named` di CDI.                                               |
| **`@ViewScoped` (CDI)** | (`javax.faces.view.ViewScoped`) Lo scope, compatibile con CDI a partire da JSF 2.2, che lega un bean alla vita di una singola vista JSF. |
| **`@ConversationScoped`** | Uno scope CDI che permette di controllare programmaticamente l'inizio e la fine di un'interazione che può estendersi su più richieste. |
