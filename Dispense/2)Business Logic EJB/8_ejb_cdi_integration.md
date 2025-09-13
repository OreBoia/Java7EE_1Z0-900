# Integrazione tra EJB e CDI

In Java EE 7 e versioni successive, EJB (Enterprise JavaBeans) e CDI (Contexts and Dependency Injection) sono profondamente integrati. Questa integrazione unisce la potenza dei servizi transazionali e di sicurezza di EJB con il flessibile modello di dependency injection e gestione del ciclo di vita di CDI.

## Concetti Fondamentali

1. **Un EJB è (quasi sempre) un Bean CDI**: Qualsiasi EJB session bean (`@Stateless`, `@Stateful`, `@Singleton`) è automaticamente un bean CDI. Ciò significa che:
   * Può essere iniettato in altre classi (altri EJB, servlet, bean CDI, etc.) usando l'annotazione `@Inject`.
   * Può utilizzare le funzionalità di CDI come eventi (`@Observes`, `@Produces`) e interceptor CDI.
   * Segue le regole di scoping e injection di CDI.

2. **Un Bean CDI non è un EJB (di default)**: Una classe annotata solo con annotazioni CDI (es. `@ApplicationScoped`, `@Named`) non è un EJB. Di conseguenza, non beneficia automaticamente dei servizi EJB come:
   * Transazioni gestite dal container (Container-Managed Transactions - CMT).
   * Sicurezza a livello di metodo (`@RolesAllowed`).
   * Invocazione asincrona (`@Asynchronous`).
   * Accesso remoto.

Tuttavia, un bean CDI può utilizzare alcuni di questi servizi in modo esplicito, ad esempio usando l'annotazione `@Transactional` per ottenere il supporto transazionale.

## Scegliere tra EJB e CDI Bean

La scelta dipende dai requisiti specifici del componente che si sta sviluppando.

* **Usa un EJB quando hai bisogno di:**
  * **Servizi "Heavy-Duty"**: Logica di business complessa che richiede transazioni, sicurezza dichiarativa, e gestione della concorrenza.
  * **Transazioni Automatiche (CMT)**: Il container gestisce automaticamente l'inizio, il commit e il rollback delle transazioni.
  * **Sicurezza a livello di Metodo**: Restringere l'accesso ai metodi basandosi sui ruoli utente (`@RolesAllowed`).
  * **Metodi Asincroni**: Eseguire operazioni in background con `@Asynchronous`.
  * **Timer Service**: Eseguire compiti schedulati con `@Schedule`.
  * **Accesso Remoto**: Esporre la logica di business a client remoti.

* **Usa un CDI Bean "puro" quando hai bisogno di:**
  * **Componenti Leggeri**: Classi con un ciclo di vita ben definito (es. legato alla richiesta, alla sessione o all'applicazione) ma senza la necessità dei servizi avanzati di EJB.
  * **Backing Bean per UI**: I bean che gestiscono la logica delle pagine JSF sono quasi sempre bean CDI (`@Named`).
  * **"Glue Code"**: Componenti che orchestrano le chiamate tra diversi layer dell'applicazione.
  * **Flessibilità degli Scope**: CDI offre un set di scope più ricco (`@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`, `@ConversationScoped`).

## Esempi di Codice

### Esempio 1: Iniettare un EJB in un Bean CDI

Questo è il pattern più comune. Un backing bean JSF (che è un bean CDI) inietta un EJB per eseguire la logica di business transazionale.

**L'EJB (Service Layer)**

```java
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless // Questo rende la classe sia un EJB che un bean CDI
public class ProductService {

    @PersistenceContext
    private EntityManager em;

    // Questo metodo sarà automaticamente transazionale
    public void createProduct(Product product) {
        em.persist(product);
    }
}
```

**Il Bean CDI (Presentation Layer)**

```java
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named // Rende la classe un bean CDI accessibile da JSF
@RequestScoped
public class ProductController {

    @Inject // Usa l'iniezione CDI per ottenere un'istanza dell'EJB
    private ProductService productService;

    private Product newProduct = new Product();

    public String saveProduct() {
        productService.createProduct(newProduct);
        return "products.xhtml?faces-redirect=true"; // Naviga a una pagina di successo
    }

    // Getter e setter per newProduct
}
```

### Esempio 2: Bean CDI con Supporto Transazionale

Se non hai bisogno di tutte le funzionalità di un EJB ma solo delle transazioni, puoi usare un bean CDI con l'annotazione `@Transactional`.

```java
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped // Un bean CDI standard
public class SimpleAuditService {

    @Inject
    private EntityManager em;

    @Transactional // Aggiunge comportamento transazionale a questo metodo
    public void recordEvent(String event) {
        AuditRecord record = new AuditRecord(event);
        em.persist(record);
    }
}
```

**Nota:** Per usare `@Transactional` su un bean CDI, è necessario che nel progetto sia presente un file `beans.xml` e che l'annotazione sia intercettata.

## Tabella delle Annotazioni Principali

| Annotazione | Fornita da | Scopo |
| --- | --- | --- |
| `@Stateless`, `@Stateful`, `@Singleton` | EJB | Definiscono un EJB Session Bean. Rende la classe anche un bean CDI. |
| `@Inject` | CDI | Annota un punto di iniezione. È il modo standard per iniettare dipendenze, inclusi gli EJB. |
| `@EJB` | EJB | Vecchio modo per iniettare EJB. `@Inject` è ora preferito per coerenza. |
| `@Named` | CDI | Rende un bean CDI accessibile tramite Expression Language (EL), tipicamente in pagine JSF. |
| `@RequestScoped`, `@SessionScoped`, etc. | CDI | Definiscono il ciclo di vita (scope) di un bean CDI. |
| `@Transactional` | JTA (usato da CDI) | Fornisce controllo transazionale dichiarativo a metodi di bean CDI o EJB. |
| `@Observes` | CDI | Annota un metodo che agisce come osservatore di eventi CDI. |
| `@Produces` | CDI | Annota un metodo che funge da "fabbrica" per istanze di bean. |

In sintesi, la coesistenza di EJB e CDI permette di usare lo strumento giusto per il lavoro giusto: EJB per la logica di business robusta e transazionale, e CDI per l'accoppiamento flessibile e la gestione dei componenti più leggeri.
