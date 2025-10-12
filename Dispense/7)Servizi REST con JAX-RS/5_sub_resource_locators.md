# Sub-Resource Locators in JAX-RS

## Introduzione Teorica

I Sub-Resource Locators rappresentano uno dei pattern architetturali più potenti di JAX-RS per la costruzione di API REST complesse e scalabili. Dal punto di vista teorico, implementano il **Composite Pattern** e il **Strategy Pattern**, permettendo di decomporre una risorsa complessa in componenti più piccoli e gestibili.

### Problemi Architetturali Risolti

1. **Monolithic Resource Classes**: Senza sub-resource locators, tutte le operazioni di una risorsa e delle sue sotto-risorse dovrebbero essere concentrate in un'unica classe, violando il principio di Single Responsibility.

2. **URI Mapping Complexity**: La gestione di URI gerarchici complessi (/utenti/{id}/ordini/{ordineId}/items/{itemId}) richiederebbe metodi con parametri multipli e logica di validazione ripetitiva.

3. **Code Reusability**: Le sotto-risorse possono essere riutilizzate in contesti diversi, implementando il principio DRY (Don't Repeat Yourself).

4. **Separation of Concerns**: Ogni livello di risorsa può concentrarsi sulla propria logica specifica, migliorando la manutenibilità del codice.

### Vantaggi Architetturali

I Sub-Resource Locators sono una funzionalità avanzata di JAX-RS che permette di creare API gerarchiche e modulari. Dal punto di vista dell'architettura software, consentono di:

- **Decomporre la complessità**: Suddividere risorse complesse in componenti più piccoli e specializzati
- **Implementare il principio di responsabilità singola**: Ogni classe gestisce un aspetto specifico della risorsa
- **Creare strutture ad albero**: Organizzare le risorse seguendo la gerarchia naturale del dominio
- **Delegare la gestione**: Trasferire il controllo a classi specializzate per sotto-percorsi specifici

## Concetti Fondamentali

### Teoria dei Resource Methods vs Sub-Resource Locators

Dal punto di vista teorico, JAX-RS distingue tra due tipi di metodi per gestire le richieste HTTP:

1. **Resource Methods**: Sono i metodi "terminali" che effettivamente processano una richiesta HTTP e producono una risposta. Sono caratterizzati dalla presenza di annotazioni HTTP (@GET, @POST, @PUT, @DELETE) e rappresentano gli endpoint finali dell'API.

2. **Sub-Resource Locators**: Sono metodi "intermediari" che non processano direttamente le richieste HTTP, ma fungono da factory per creare istanze di altre classi che gestiranno la richiesta. Implementano il **Factory Method Pattern**.

### Differenza Pratica tra Resource Methods e Sub-Resource Locators

**Principio Teorico**: Un Resource Method termina la catena di matching degli URI e produce una risposta, mentre un Sub-Resource Locator continua il processo di routing delegando a un'altra risorsa.

```java
@Path("/utenti")  // Root resource: definisce il path base
public class UtenteResource {
    
    // RESOURCE METHOD - Ha annotazione HTTP (@GET, @POST, etc.)
    // Teoria: Questo è un endpoint TERMINALE che produce una risposta HTTP
    @GET
    @Path("/{id}")  // Template URI: {id} è un parametro variabile
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtente(@PathParam("id") int id) {
        // Logica di business diretta - non delega ad altre classi
        return utenteService.findById(id);
    }
    
    // SUB-RESOURCE LOCATOR - Solo @Path, nessuna annotazione HTTP
    // Teoria: Questo è un metodo FACTORY che crea istanze di sub-resource
    @Path("/{id}/ordini")  // URI Template più complesso
    public OrdineSubResource getOrdiniUtente(@PathParam("id") int userId) {
        
        /* PATTERN: Validation Before Delegation
         * Teoria: Il locator deve validare i parametri prima di creare 
         * la sub-resource, implementando il principio "Fail Fast"
         */
        Utente utente = utenteService.findById(userId);
        if (utente == null) {
            throw new NotFoundException("Utente non trovato");
        }
        
        /* PATTERN: Context Passing
         * Teoria: Il locator passa il contesto (userId) alla sub-resource,
         * permettendo l'incapsulamento dello stato necessario
         */
        return new OrdineSubResource(userId);
    }
}
```

### Sub-Resource Class - Teoria e Implementazione

**Principi Architetturali della Sub-Resource Class:**

1. **Stateful Context**: A differenza delle root resource, le sub-resource possono mantenere stato specifico del contesto (es. userId)
2. **Encapsulation**: Incapsula la logica specifica per un sottoinsieme di operazioni
3. **Composition over Inheritance**: Usa composizione per includere dipendenze invece di ereditarietà
4. **Single Responsibility**: Si concentra solo sulla gestione degli ordini per un utente specifico

```java
/* DESIGN PATTERN: Context Object Pattern
 * Teoria: Questa classe mantiene il contesto (userId) ricevuto dal locator
 * e lo usa per tutte le operazioni successive, garantendo coesione
 */
public class OrdineSubResource {
    
    /* IMMUTABLE STATE: Il contesto è immutabile dopo la costruzione
     * Teoria: L'userId è final per garantire thread-safety e immutabilità
     */
    private final int userId;
    private final OrdineService ordineService;
    
    /* DEPENDENCY INJECTION PATTERN: 
     * Teoria: Il costruttore riceve il contesto essenziale e risolve
     * le dipendenze usando CDI. Implementa il pattern "Constructor Injection"
     */
    public OrdineSubResource(int userId) {
        this.userId = userId; // Memorizza il contesto ricevuto dal locator
        
        // Risoluzione lazy delle dipendenze CDI nel momento della creazione
        this.ordineService = CDI.current().select(OrdineService.class).get();
    }
    
    /* RESOURCE METHOD: Endpoint terminale per GET su collezione
     * Teoria: Implementa il pattern "Collection Resource" limitato al contesto
     * dell'utente. I query parameters forniscono filtri opzionali.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getTuttiGliOrdini(@QueryParam("stato") String stato,
                                         @QueryParam("dal") String dataInizio) {
        
        // Il contesto (userId) viene automaticamente applicato a ogni operazione
        return ordineService.findByUtente(userId, stato, dataInizio);
    }
    
    /* RESOURCE METHOD: Creazione di nuova risorsa nel contesto
     * Teoria: Implementa il pattern POST-to-Collection per creare nuove risorse
     * all'interno del contesto specifico dell'utente
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creaOrdine(NuovoOrdineRequest request) {
        
        /* CONTEXT INJECTION PATTERN:
         * Teoria: Il contesto (userId) viene automaticamente iniettato nella request,
         * eliminando la necessità per il client di specificarlo
         */
        request.setUserId(userId);
        
        Ordine nuovoOrdine = ordineService.creaOrdine(request);
        
        /* REST BEST PRACTICE: Location Header
         * Teoria: Dopo una POST successful (201 Created), si deve fornire
         * l'URI della nuova risorsa creata nel header Location
         */
        return Response.status(Response.Status.CREATED)
                      .entity(nuovoOrdine)
                      .header("Location", "/api/utenti/" + userId + "/ordini/" + nuovoOrdine.getId())
                      .build();
    }
    
    /* RESOURCE METHOD: Accesso a risorsa specifica nel contesto
     * Teoria: Implementa il pattern "Item Resource" con validazione di ownership
     */
    @GET
    @Path("/{ordineId}")  // URI Template nidificato
    @Produces(MediaType.APPLICATION_JSON)
    public Ordine getOrdineSpecifico(@PathParam("ordineId") int ordineId) {
        
        Ordine ordine = ordineService.findById(ordineId);
        
        /* SECURITY PATTERN: Ownership Validation
         * Teoria: Verifica che la risorsa appartenga al contesto corrente (userId)
         * implementando il principio di "Principle of Least Privilege"
         */
        if (ordine == null || ordine.getUserId() != userId) {
            throw new NotFoundException("Ordine non trovato per questo utente");
        }
        
        return ordine;
    }
    
    /* RESOURCE METHOD: Aggiornamento con validazione contestuale
     * Teoria: PUT su risorsa specifica con doppia validazione (esistenza + ownership)
     */
    @PUT
    @Path("/{ordineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Ordine aggiornaOrdine(@PathParam("ordineId") int ordineId, 
                                AggiornamentoOrdineRequest request) {
        
        /* DEFENSIVE PROGRAMMING: Validazione prima di ogni operazione
         * Teoria: Ogni operazione su risorsa specifica deve validare ownership
         */
        verificaProprietaOrdine(ordineId);
        
        return ordineService.aggiorna(ordineId, request);
    }
    
    /* RESOURCE METHOD: Eliminazione sicura
     * Teoria: DELETE restituisce 204 No Content secondo le specifiche REST
     */
    @DELETE
    @Path("/{ordineId}")
    public Response eliminaOrdine(@PathParam("ordineId") int ordineId) {
        verificaProprietaOrdine(ordineId);
        
        ordineService.elimina(ordineId);
        
        /* REST CONVENTION: 204 No Content per DELETE successful
         * Teoria: Indica che l'operazione è riuscita ma non c'è contenuto da restituire
         */
        return Response.noContent().build();
    }
    
    /* NESTED SUB-RESOURCE LOCATOR: Locator di secondo livello
     * Teoria: Implementa una gerarchia a tre livelli: utenti -> ordini -> items
     * Permette di costruire API profondamente nidificate mantenendo la modularità
     */
    @Path("/{ordineId}/items")
    public ItemOrdineSubResource getItemsOrdine(@PathParam("ordineId") int ordineId) {
        
        verificaProprietaOrdine(ordineId); // Validazione del contesto superiore
        
        /* CONTEXT ACCUMULATION PATTERN:
         * Teoria: Il contesto si accumula attraverso i livelli (userId + ordineId)
         * permettendo validazioni granulari a ogni livello
         */
        return new ItemOrdineSubResource(userId, ordineId);
    }
    
    private void verificaProprietaOrdine(int ordineId) {
        Ordine ordine = ordineService.findById(ordineId);
        if (ordine == null || ordine.getUserId() != userId) {
            throw new NotFoundException("Ordine non trovato per questo utente");
        }
    }
}
```

### Sub-Resource Nidificati (Multi-livello) - Teoria della Composizione Gerarchica

**Principi Teorici dei Sub-Resource Nidificati:**

1. **Hierarchical Composition**: I sub-resource possono contenere altri sub-resource, creando alberi di risorse
2. **Context Inheritance**: Il contesto si propaga attraverso i livelli (userId → ordineId → itemId)
3. **Granular Security**: Ogni livello può implementare controlli di sicurezza specifici
4. **Modular Decomposition**: Ogni livello ha responsabilità ben definite e separate

```java
/* MULTI-LEVEL SUB-RESOURCE PATTERN:
 * Teoria: Questa classe rappresenta il terzo livello di una gerarchia:
 * Utenti -> Ordini -> Items
 * Mantiene il contesto completo di tutti i livelli superiori
 */
public class ItemOrdineSubResource {
    
    /* COMPOSITE CONTEXT PATTERN:
     * Teoria: Mantiene tutto il contesto necessario dai livelli superiori
     * per garantire validazioni complete e operazioni sicure
     */
    private final int userId;    // Contesto dal primo livello
    private final int ordineId;  // Contesto dal secondo livello
    private final ItemOrdineService itemService;
    
    /* CONSTRUCTOR PATTERN per Sub-Resource di Livello N:
     * Teoria: Riceve tutto il contesto accumulato dai livelli superiori
     */
    public ItemOrdineSubResource(int userId, int ordineId) {
        this.userId = userId;     // Per validazioni cross-level
        this.ordineId = ordineId; // Per operazioni specifiche del livello
        this.itemService = CDI.current().select(ItemOrdineService.class).get();
    }
    
    /* COLLECTION ENDPOINT nel contesto multi-livello
     * Teoria: Opera solo nel contesto dell'ordine specifico (ordineId)
     * nascondendo la complessità dei livelli superiori
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemOrdine> getItems() {
        // Il filtro per ordineId è implicito nel contesto
        return itemService.findByOrdine(ordineId);
    }
    
    /* CREATION ENDPOINT con contesto multi-livello
     * Teoria: La creazione avviene automaticamente nel contesto corretto
     * senza che il client debba specificare i riferimenti ai livelli superiori
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response aggiungiItem(NuovoItemRequest request) {
        
        /* AUTOMATIC CONTEXT INJECTION:
         * Teoria: Il contesto (ordineId) viene iniettato automaticamente,
         * garantendo consistenza e riducendo errori client
         */
        request.setOrdineId(ordineId);
        
        ItemOrdine item = itemService.aggiungi(request);
        
        /* HIERARCHICAL LOCATION HEADER:
         * Teoria: L'URI completo riflette tutta la gerarchia contestuale,
         * usando tutti i contesti accumulati (userId, ordineId, itemId)
         */
        String location = String.format("/api/utenti/%d/ordini/%d/items/%d", 
                                      userId, ordineId, item.getId());
        
        return Response.status(Response.Status.CREATED)
                      .entity(item)
                      .header("Location", location)
                      .build();
    }
    
    @GET
    @Path("/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemOrdine getItem(@PathParam("itemId") int itemId) {
        ItemOrdine item = itemService.findById(itemId);
        
        if (item == null || item.getOrdineId() != ordineId) {
            throw new NotFoundException("Item non trovato in questo ordine");
        }
        
        return item;
    }
    
    @PUT
    @Path("/{itemId}/quantita")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiornaQuantita(@PathParam("itemId") int itemId,
                                   QuantitaUpdateRequest request) {
        
        itemService.aggiornaQuantita(itemId, ordineId, request.getNuovaQuantita());
        
        return Response.noContent().build();
    }
}
```

## Struttura URI Risultante - Teoria del Mapping Gerarchico

**Principi Teorici della Struttura URI:**

I Sub-Resource Locators creano una mappatura gerarchica che riflette la struttura del dominio aziendale. Ogni livello dell'URI rappresenta una relazione "has-a" o "contains" nel modello di dominio.

### Mappatura URI-to-Handler

```text
/api/utenti                          # UtenteResource (Root Resource)
/api/utenti/123                      # UtenteResource.getUtente() (Resource Method)
/api/utenti/123/ordini               # OrdineSubResource.getTuttiGliOrdini() (Sub-Resource Method)
/api/utenti/123/ordini/456           # OrdineSubResource.getOrdineSpecifico() (Sub-Resource Method)
/api/utenti/123/ordini/456/items     # ItemOrdineSubResource.getItems() (Nested Sub-Resource Method)
/api/utenti/123/ordini/456/items/789 # ItemOrdineSubResource.getItem() (Nested Sub-Resource Method)
```

**Teoria del Routing Gerarchico:**

1. **Root Resource** (`/api/utenti`): Entry point gestito da `UtenteResource`
2. **Resource Method** (`/utenti/123`): Endpoint terminale per una risorsa specifica
3. **Sub-Resource Locator** (`/utenti/123/ordini`): Delega il controllo a `OrdineSubResource`
4. **Nested Locator** (`/ordini/456/items`): Delega ulteriormente a `ItemOrdineSubResource`

Ogni transizione rappresenta un cambio di **responsabilità** e **contesto** nella gestione della richiesta.

## Pattern Avanzati con Sub-Resource Locators - Teoria dei Design Patterns

I Sub-Resource Locators si prestano all'implementazione di pattern architetturali avanzati che risolvono problemi specifici di scalabilità, manutenibilità e flessibilità.

### 1. Sub-Resource con Iniezione di Dipendenze - CDI Integration Pattern

**Problema Teorico**: Come integrare il Dependency Injection in sub-resource create dinamicamente?

**Soluzione**: Utilizzo di CDI programmatico per risolvere dipendenze al momento della creazione della sub-resource.

**Vantaggi Teorici**:
- **Lazy Loading**: Le dipendenze vengono risolte solo quando necessario
- **Scope Management**: Possibilità di utilizzare scope CDI appropriati
- **Testing**: Facilita il testing con mock injection

```java
@Path("/aziende")
public class AziendaResource {
    
    @Inject
    private AziendaService aziendaService;
    
    @Path("/{aziendaId}/dipendenti")
    public DipendenteSubResource getDipendenti(@PathParam("aziendaId") int aziendaId) {
        
        Azienda azienda = aziendaService.findById(aziendaId);
        if (azienda == null) {
            throw new NotFoundException("Azienda non trovata");
        }
        
        /* PROGRAMMATIC CDI LOOKUP:
         * Teoria: Usa CDI programmatico per creare l'istanza con tutte
         * le dipendenze iniettate automaticamente
         */
        DipendenteSubResource subResource = CDI.current()
            .select(DipendenteSubResource.class)
            .get();
        
        /* CONTEXT INJECTION dopo CDI Resolution:
         * Teoria: Il contesto viene impostato dopo la creazione CDI
         */
        subResource.setAziendaId(aziendaId);
        return subResource;
    }
}

/* SUB-RESOURCE MANAGED BY CDI:
 * Teoria: @Dependent assicura una nuova istanza per ogni richiesta,
 * evitando problemi di thread-safety e stato condiviso
 */
@Dependent // Scope CDI appropriato per sub-resource
public class DipendenteSubResource {
    
    /* STANDARD CDI INJECTION:
     * Teoria: Le dipendenze sono risolte automaticamente da CDI
     * al momento della creazione dell'istanza
     */
    @Inject
    private DipendenteService dipendenteService;
    
    @Inject
    private SecurityService securityService;
    
    private int aziendaId;
    
    public void setAziendaId(int aziendaId) {
        this.aziendaId = aziendaId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dipendente> getDipendenti(@Context SecurityContext security) {
        
        // Usa servizi iniettati
        securityService.verificaAccessoAzienda(security, aziendaId);
        
        return dipendenteService.findByAzienda(aziendaId);
    }
}
```

### 2. Sub-Resource Condizionali - Strategy Pattern Dinamico

**Problema Teorico**: Come gestire risorse che richiedono comportamenti diversi basati su attributi runtime?

**Soluzione**: Implementazione del **Strategy Pattern** tramite sub-resource condizionali che vengono selezionati dinamicamente.

**Vantaggi Teorici**:

- **Polymorphic Behavior**: Stesso URI, comportamento diverso
- **Type Safety**: Ogni tipo ha il suo handler specializzato  
- **Open/Closed Principle**: Facile aggiungere nuovi tipi senza modificare codice esistente

```java
/* CONDITIONAL SUB-RESOURCE FACTORY:
 * Teoria: Implementa il Factory Pattern per creare sub-resource
 * basate su criteri runtime (tipo documento, header Accept, etc.)
 */
@Path("/documenti")
public class DocumentoResource {
    
    /* DYNAMIC STRATEGY SELECTION:
     * Teoria: Il locator decide quale strategia/sub-resource utilizzare
     * basandosi sui dati della risorsa o sui parametri della richiesta
     */
    @Path("/{docId}")
    public Object getDocumentHandler(@PathParam("docId") String docId,
                                   @HeaderParam("Accept") String acceptHeader) {
        
        Documento doc = documentoService.findById(docId);
        if (doc == null) {
            throw new NotFoundException("Documento non trovato");
        }
        
        /* POLYMORPHIC FACTORY PATTERN:
         * Teoria: Selezione dinamica dell'implementazione corretta
         * basata sui metadati dell'entità
         */
        switch (doc.getTipo()) {
            case "PDF":
                return new PdfDocumentSubResource(doc);
            case "IMAGE":
                return new ImageDocumentSubResource(doc);
            case "VIDEO":
                return new VideoDocumentSubResource(doc);
            default:
                return new GenericDocumentSubResource(doc);
        }
    }
}
```

### 3. Sub-Resource con Versioning

```java
@Path("/api")
public class ApiVersionResource {
    
    @Path("/v1/prodotti")
    public ProdottoV1SubResource getProdottiV1() {
        return new ProdottoV1SubResource();
    }
    
    @Path("/v2/prodotti")
    public ProdottoV2SubResource getProdottiV2() {
        return new ProdottoV2SubResource();
    }
    
    // Versioning tramite header
    @Path("/prodotti")
    public Object getProdotti(@HeaderParam("API-Version") String version) {
        
        if ("1.0".equals(version)) {
            return new ProdottoV1SubResource();
        } else if ("2.0".equals(version)) {
            return new ProdottoV2SubResource();
        } else {
            // Default alla versione più recente
            return new ProdottoV2SubResource();
        }
    }
}
```

## Vantaggi dei Sub-Resource Locators - Analisi Teorica dei Benefici Architetturali

Dal punto di vista dell'ingegneria del software, i Sub-Resource Locators risolvono fondamentali problemi architetturali e di design, applicando principi consolidati di progettazione orientata agli oggetti.

### 1. Organizzazione Modulare - Principio di Decomposizione

**Problema Teorico**: Il **God Object Anti-Pattern** nelle resource class che gestiscono troppe responsabilità.

**Soluzione Teorica**: Applicazione del **Single Responsibility Principle** attraverso la decomposizione modulare.

**Benefici Misurabili**:

- **Riduzione Complessità Ciclomatica**: Ogni classe ha meno path di esecuzione
- **Miglior Coesione**: Metodi correlati sono raggruppati logicamente
- **Minore Accoppiamento**: Dipendenze più chiare e limitate

```java
// Invece di una classe monolitica...
@Path("/ecommerce")
public class EcommerceResourceMonolitica {
    
    @GET @Path("/utenti/{id}") 
    public Utente getUtente(@PathParam("id") int id) { /*...*/ }
    
    @GET @Path("/utenti/{id}/ordini") 
    public List<Ordine> getOrdiniUtente(@PathParam("id") int id) { /*...*/ }
    
    @GET @Path("/utenti/{id}/ordini/{ordineId}") 
    public Ordine getOrdine(@PathParam("id") int id, @PathParam("ordineId") int ordineId) { /*...*/ }
    
    @GET @Path("/prodotti/{id}") 
    public Prodotto getProdotto(@PathParam("id") int id) { /*...*/ }
    
    @GET @Path("/prodotti/{id}/recensioni") 
    public List<Recensione> getRecensioni(@PathParam("id") int id) { /*...*/ }
    
    // ... centinaia di altri metodi
}

// ...possiamo avere struttura modulare
@Path("/")
public class EcommerceResource {
    
    @Path("/utenti")
    public UtenteResource getUtenti() {
        return new UtenteResource();
    }
    
    @Path("/prodotti")
    public ProdottoResource getProdotti() {
        return new ProdottoResource();
    }
    
    @Path("/categorie")
    public CategoriaResource getCategorie() {
        return new CategoriaResource();
    }
}
```

### 2. Riutilizzabilità

```java
// Sub-resource riutilizzabile per commenti
public class CommentoSubResource {
    
    private final String entityType;
    private final int entityId;
    
    public CommentoSubResource(String entityType, int entityId) {
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Commento> getCommenti() {
        return commentoService.findByEntity(entityType, entityId);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiungiCommento(NuovoCommentoRequest request) {
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        
        Commento commento = commentoService.crea(request);
        return Response.status(201).entity(commento).build();
    }
}

// Riutilizzabile da diverse risorse
@Path("/articoli")
public class ArticoloResource {
    
    @Path("/{id}/commenti")
    public CommentoSubResource getCommenti(@PathParam("id") int id) {
        return new CommentoSubResource("ARTICOLO", id);
    }
}

@Path("/prodotti")
public class ProdottoResource {
    
    @Path("/{id}/commenti")
    public CommentoSubResource getCommenti(@PathParam("id") int id) {
        return new CommentoSubResource("PRODOTTO", id);
    }
}
```

## Best Practices - Principi di Progettazione per Sub-Resource Locators

Le Best Practices per Sub-Resource Locators derivano da principi consolidati di ingegneria del software e pattern di sicurezza enterprise.

### 1. Validazione nel Locator - Fail-Fast Principle

**Teoria del Fail-Fast**: Validare tutti i prerequisiti il prima possibile nella catena di elaborazione per:

- **Ridurre costi computazionali**: Evitare elaborazioni inutili
- **Migliorare esperienza utente**: Errori immediati e chiari
- **Semplificare debugging**: Punto di fallimento ben definito
- **Garantire invarianti**: Assicurare che le sub-resource ricevano sempre dati validi

```java
@Path("/progetti")
public class ProgettoResource {
    
    @Path("/{progettoId}/tasks")
    public TaskSubResource getTasks(@PathParam("progettoId") int progettoId,
                                   @Context SecurityContext security) {
        
        // 1. Validazione parametri
        if (progettoId <= 0) {
            throw new BadRequestException("ID progetto non valido");
        }
        
        // 2. Verifica esistenza
        Progetto progetto = progettoService.findById(progettoId);
        if (progetto == null) {
            throw new NotFoundException("Progetto non trovato");
        }
        
        // 3. Controllo autorizzazioni
        if (!hasAccess(security, progetto)) {
            throw new ForbiddenException("Accesso negato al progetto");
        }
        
        // 4. Creazione sub-resource con contesto
        return new TaskSubResource(progettoId, progetto.getStato());
    }
    
    private boolean hasAccess(SecurityContext security, Progetto progetto) {
        // Logica di autorizzazione
        return true;
    }
}
```

### 2. Gestione del Contesto

```java
public class TaskSubResource {
    
    private final int progettoId;
    private final StatoProgetto statoProgetto;
    
    public TaskSubResource(int progettoId, StatoProgetto statoProgetto) {
        this.progettoId = progettoId;
        this.statoProgetto = statoProgetto;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response creaTask(NuovoTaskRequest request) {
        
        // Controlli basati sul contesto del progetto
        if (statoProgetto == StatoProgetto.CHIUSO) {
            throw new BadRequestException("Impossibile aggiungere task a progetto chiuso");
        }
        
        request.setProgettoId(progettoId);
        
        Task task = taskService.crea(request);
        return Response.status(201).entity(task).build();
    }
}
```

### 3. Evitare Sub-Resource Troppo Profondi

```java
// ❌ EVITARE: troppi livelli di nidificazione
/api/aziende/1/dipartimenti/2/progetti/3/tasks/4/commenti/5/allegati/6

// ✅ PREFERIRE: struttura più piatta con riferimenti
/api/tasks/4/commenti
/api/commenti/5/allegati
/api/allegati/6
```

## Glossario - Terminologia Tecnica e Teorica

| Termine | Definizione Teorica |
|---------|-------------|
| **Sub-Resource Locator** | Metodo factory annotato solo con @Path che implementa il Factory Method Pattern per creare istanze di sub-resource dinamicamente |
| **Sub-Resource Class** | Classe specializzata che implementa il Single Responsibility Principle gestendo un sottoinsieme di operazioni per una risorsa nel contesto specifico |
| **Resource Method** | Metodo endpoint annotato con @Path e verbo HTTP che implementa il Command Pattern per elaborare richieste HTTP specifiche |
| **URI Template** | Pattern URI con placeholder che implementa il Template Method Pattern per mappature dinamiche (es. /utenti/{id}/ordini) |
| **Nested Sub-Resource** | Sub-resource che contiene altri sub-resource, implementando il Composite Pattern per gerarchie complesse |
| **Locator Method** | Sinonimo di Sub-Resource Locator Method - metodo che "localizza" e crea la sub-resource appropriata |
| **Hierarchical API** | Architettura API basata su struttura ad albero che riflette le relazioni del dominio business tramite sub-resources |
| **Context Accumulation** | Pattern per cui il contesto si accumula attraverso i livelli gerarchici (userId → ordineId → itemId) |
| **Fail-Fast Validation** | Principio di validazione immediata nei locator per garantire che le sub-resource ricevano sempre dati validi |
| **Polymorphic Sub-Resource** | Sub-resource diverse per la stessa URI basate su criteri runtime, implementando lo Strategy Pattern |
