# Quiz Avanzato su SOAP, JAX-WS e JAXB - Domande Miste con Codice

Questo quiz avanzato copre i concetti dei servizi SOAP con JAX-WS e JAXB con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Concetti Base di SOAP e JAX-WS

### 🔵 Domanda 1

Osserva il seguente codice di un web service SOAP:

```java
@WebService(serviceName = "CalculatorService")
public class Calculator {
    
    @WebMethod(operationName = "add")
    public int somma(int a, int b) {
        return a + b;
    }
    
    @WebMethod(exclude = true)
    public int metodoPrivato() {
        return 42;
    }
}
```

Cosa apparirà nel WSDL generato?

- a) Entrambi i metodi `somma` e `metodoPrivato`
- b) Solo il metodo `somma` con nome `add`
- c) Solo il metodo `metodoPrivato`
- d) Nessuno dei due metodi

---

### 🟢 Domanda 2

Quali delle seguenti annotazioni sono specifiche di JAX-WS? (Seleziona tutte quelle corrette)

- a) `@WebService`
- b) `@WebMethod`
- c) `@WebParam`
- d) `@XmlRootElement`
- e) `@WebServiceRef`

---

### 💻 Domanda 3

Analizza questo servizio web integrato con EJB:

```java
@Stateless
@WebService(serviceName = "UserService")
public class UserServiceEJB {
    
    @PersistenceContext
    private EntityManager em;
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void createUser(String username, String email) {
        User user = new User(username, email);
        em.persist(user);
    }
}
```

Quali vantaggi offre l'integrazione EJB + JAX-WS?

- a) Solo migliori performance
- b) Gestione automatica delle transazioni e pooling delle istanze
- c) Sicurezza automatica senza configurazione
- d) Compatibilità solo con client Java

---

### 🔵 Domanda 4

Quale tipo di EJB è **NON compatibile** per essere esposto come web service SOAP?

- a) Stateless Session Bean
- b) Singleton Session Bean
- c) Stateful Session Bean
- d) Message-Driven Bean

---

## 2. Mapping Java-to-WSDL e JAXB

### 💻 Domanda 5

Osserva questa classe annotata con JAXB:

```java
@XmlRootElement(name = "Person")
@XmlType(propOrder = {"id", "name", "age"})
public class Persona {
    
    private int id;
    private String name;
    private int age;
    private String password;
    
    @XmlAttribute
    public int getId() { return id; }
    
    @XmlElement(name = "fullName", required = true)
    public String getName() { return name; }
    
    public int getAge() { return age; }
    
    @XmlTransient
    public String getPassword() { return password; }
    
    // setter methods...
}
```

Come apparirà l'XML generato per un oggetto con id=1, name="Mario", age=30, password="secret"?

- a) `<Person id="1"><fullName>Mario</fullName><age>30</age><password>secret</password></Person>`
- b) `<Person id="1"><fullName>Mario</fullName><age>30</age></Person>`
- c) `<Persona><id>1</id><fullName>Mario</fullName><age>30</age></Persona>`
- d) `<Person><id>1</id><name>Mario</name><age>30</age></Person>`

---

### 🟢 Domanda 6

Quali tipi di dati Java hanno un mapping **automatico** verso XSD in JAX-WS? (Seleziona tutti)

- a) `String`
- b) `int` e `Integer`
- c) `boolean` e `Boolean`
- d) `java.util.Date`
- e) `BigDecimal`

---

### 💻 Domanda 7

Analizza questo adattatore JAXB personalizzato:

```java
public class DateAdapter extends XmlAdapter<String, Date> {
    
    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    
    @Override
    public String marshal(Date date) throws Exception {
        return format.format(date);
    }
    
    @Override
    public Date unmarshal(String dateString) throws Exception {
        return format.parse(dateString);
    }
}

// Utilizzato in una classe
public class Evento {
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date dataEvento;
    // getter/setter...
}
```

Quale formato di data apparirà nell'XML?

- a) `2023-12-25` (ISO format)
- b) `25/12/2023` (formato personalizzato)
- c) Timestamp numerico
- d) `Dec 25, 2023`

---

## 3. Gestione delle Eccezioni (SOAP Fault)

### 🔵 Domanda 8

Osserva questa eccezione personalizzata:

```java
@WebFault(name = "InvalidUserFault")
public class InvalidUserException extends Exception {
    
    public InvalidUserException(String message) {
        super(message);
    }
}

@WebService
public class UserService {
    
    @WebMethod
    public User findUser(String username) throws InvalidUserException {
        if (username == null || username.isEmpty()) {
            throw new InvalidUserException("Username non può essere vuoto");
        }
        // logica di ricerca...
        return user;
    }
}
```

Come viene gestita l'eccezione `InvalidUserException` in SOAP?

- a) Viene ignorata e non compare nel WSDL
- b) Causa un errore di compilazione
- c) Viene mappata automaticamente in un SOAP Fault nel WSDL
- d) Viene gestita solo come RuntimeException

---

### 🟢 Domanda 9

Quali tipi di eccezioni causano il rollback automatico in un web service EJB? (Seleziona tutte)

- a) Eccezioni checked dichiarate con `@WebFault`
- b) `RuntimeException` non gestite
- c) `EJBException`
- d) Eccezioni checked del tipo `java.lang.Exception`
- e) `SystemException`

---

## 4. Client JAX-WS

### 💻 Domanda 10

Analizza questo codice client generato con `wsimport`:

```java
public class CalculatorClient {
    
    public static void main(String[] args) {
        try {
            // Stub generati da wsimport
            CalculatorService serviceFactory = new CalculatorService();
            CalculatorPortType calculatorProxy = serviceFactory.getCalculatorPort();
            
            // Chiamata al servizio
            int result = calculatorProxy.add(5, 3);
            System.out.println("Risultato: " + result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

Quale comando `wsimport` potrebbe aver generato queste classi?

- a) `wsimport -keep CalculatorService.java`
- b) `wsimport -keep -p com.example.client http://localhost:8080/CalculatorService?wsdl`
- c) `wsimport http://localhost:8080/CalculatorService`
- d) `wsimport -generate -client CalculatorService.wsdl`

---

### 💻 Domanda 11

Osserva questo client che usa l'iniezione in ambiente Java EE:

```java
@Stateless
public class ClientService {
    
    @WebServiceRef(wsdlLocation = "http://example.com/UserService?wsdl")
    private UserService userServiceFactory;
    
    public String getUserInfo(String username) {
        UserServicePortType proxy = userServiceFactory.getUserServicePort();
        return proxy.getUserDetails(username);
    }
}
```

Quale vantaggio offre `@WebServiceRef` rispetto alla creazione manuale del client?

- a) Migliori performance di rete
- b) Il container gestisce automaticamente la creazione e l'iniezione del proxy
- c) Supporto per protocolli diversi da HTTP
- d) Gestione automatica delle credenziali di sicurezza

---

## 5. Sicurezza e Transazioni

### 🔵 Domanda 12

In un web service SOAP, quale specifica fornisce **sicurezza a livello di messaggio**?

- a) HTTPS/TLS
- b) WS-Security
- c) Basic Authentication
- d) OAuth 2.0

---

### 💻 Domanda 13

Analizza questo servizio con sicurezza:

```java
@Stateless
@WebService
@RolesAllowed({"admin", "user"})
public class SecureService {
    
    @WebMethod
    @PermitAll
    public String getPublicInfo() {
        return "Informazioni pubbliche";
    }
    
    @WebMethod
    @RolesAllowed("admin")
    public void deleteUser(String username) {
        // Solo admin può eliminare utenti
    }
}
```

Se un utente con ruolo "user" chiama `deleteUser()`, cosa succede?

- a) Il metodo viene eseguito con successo
- b) Viene restituito un SOAP Fault con errore di autorizzazione
- c) Il metodo viene eseguito ma genera un warning
- d) L'accesso viene negato silenziosamente

---

## 6. SOAP vs REST

### 🟢 Domanda 14

Quali delle seguenti caratteristiche sono specifiche di **SOAP** rispetto a REST? (Seleziona tutte)

- a) Uso obbligatorio del formato XML
- b) Contratto formale tramite WSDL
- c) Supporto per WS-Security
- d) Orientamento alle risorse invece che alle operazioni
- e) Supporto per transazioni distribuite (WS-Transaction)

---

### 🔵 Domanda 15

Quale approccio è più appropriato per un'integrazione B2B enterprise che richiede alta sicurezza e transazioni distribuite?

- a) REST con JSON
- b) SOAP con WS-Security
- c) GraphQL
- d) WebSocket

---

## 7. Funzionalità Avanzate

### 💻 Domanda 16

Osserva questo handler SOAP:

```java
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {
    
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        if (outbound) {
            System.out.println("Messaggio in uscita");
        } else {
            System.out.println("Messaggio in entrata");
        }
        
        return true; // Continua la catena di processing
    }
    
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        System.out.println("Gestione fault");
        return true;
    }
    
    @Override
    public void close(MessageContext context) {}
    
    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
```

Quando viene chiamato il metodo `handleFault()`?

- a) Ad ogni messaggio SOAP
- b) Solo per messaggi in entrata
- c) Quando si verifica un SOAP Fault
- d) Solo durante la fase di inizializzazione

---

### 🔵 Domanda 17

Quale tecnologia JAX-WS è utilizzata per l'**ottimizzazione della trasmissione di dati binari**?

- a) SOAP Attachment
- b) MTOM (Message Transmission Optimization Mechanism)
- c) WS-Addressing
- d) SOAP over JMS

---

### 💻 Domanda 18

Analizza questo uso di JAXB stand-alone:

```java
public class JaxbProcessor {
    
    public String objectToXml(Person person) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Person.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(person, writer);
        return writer.toString();
    }
}
```

Cosa fa la proprietà `JAXB_FORMATTED_OUTPUT`?

- a) Valida l'XML contro uno schema XSD
- b) Formatta l'XML con indentazione per renderlo leggibile
- c) Comprime l'XML per ridurre la dimensione
- d) Cripta l'XML per sicurezza

---

## 8. Performance e Best Practices

### 🟢 Domanda 19

Quali delle seguenti sono **best practices** per i servizi SOAP? (Seleziona tutte)

- a) Usare EJB Stateless per esporre web service
- b) Implementare handler per logging e monitoraggio
- c) Utilizzare `@WebMethod(exclude=true)` per metodi non pubblici
- d) Preferire sempre SOAP a REST per qualsiasi tipo di API
- e) Usare MTOM per allegati binari grandi

---

### 💻 Domanda 20

Osserva questo pattern di Service Facade per SOAP:

```java
@Stateless
@WebService(serviceName = "OrderManagementService")
public class OrderManagementFacade {
    
    @EJB
    private CustomerService customerService;
    
    @EJB
    private ProductService productService;
    
    @EJB
    private OrderService orderService;
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderResult processCompleteOrder(OrderRequest request) 
            throws InvalidOrderException {
        
        // Validazione customer
        Customer customer = customerService.validateCustomer(request.getCustomerId());
        
        // Validazione prodotto
        Product product = productService.validateProduct(request.getProductId());
        
        // Creazione ordine
        Order order = orderService.createOrder(customer, product, request.getQuantity());
        
        return new OrderResult(order.getId(), "Order processed successfully");
    }
}
```

Quale pattern architetturale implementa questo codice?

- a) Data Transfer Object (DTO)
- b) Service Facade
- c) Command Pattern
- d) Observer Pattern

---

## 9. Mapping e Collezioni

### 💻 Domanda 21

Analizza questa classe con collezioni:

```java
@XmlRootElement(name = "Department")
public class Department {
    
    private String name;
    private List<Employee> employees;
    
    @XmlElement(name = "departmentName")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @XmlElementWrapper(name = "employeeList")
    @XmlElement(name = "employee")
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}
```

Come apparirà la struttura XML per una lista di dipendenti?

- a) `<Department><employees><Employee>...</Employee></employees></Department>`
- b) `<Department><employeeList><employee>...</employee></employeeList></Department>`
- c) `<Department><Employee>...</Employee><Employee>...</Employee></Department>`
- d) `<Department><name>...</name><Employee>...</Employee></Department>`

---

### 🔵 Domanda 22

Quale annotazione JAXB viene utilizzata per **escludere un campo** dalla serializzazione XML?

- a) `@XmlIgnore`
- b) `@XmlTransient`
- c) `@XmlExclude`
- d) `@XmlHidden`

---

## 10. Scenari Avanzati

### 💻 Domanda 23

Osserva questo scenario di web service con gestione di allegati:

```java
@WebService
public class DocumentService {
    
    @WebMethod
    @MTOM
    public String uploadDocument(@XmlMimeType("application/octet-stream") DataHandler document, 
                                String filename) {
        try {
            InputStream input = document.getInputStream();
            // Processo di salvataggio del file
            saveFile(input, filename);
            return "File caricato con successo: " + filename;
        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento", e);
        }
    }
}
```

Quale vantaggio offre l'uso di `@MTOM` e `DataHandler`?

- a) Sicurezza migliorata per i file
- b) Trasmissione ottimizzata di dati binari senza codifica Base64
- c) Supporto per file compressi automaticamente
- d) Validazione automatica del tipo di file

---

### 💻 Domanda 24

Analizza questo client che gestisce timeout:

```java
@Stateless
public class RobustClient {
    
    @WebServiceRef
    private ExternalService serviceFactory;
    
    public String callExternalService(String data) {
        ExternalServicePortType port = serviceFactory.getExternalServicePort();
        
        // Configurazione timeout
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                                  "http://external-service.com/service");
        bp.getRequestContext().put("com.sun.xml.ws.connect.timeout", 5000);
        bp.getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        
        try {
            return port.processData(data);
        } catch (WebServiceException e) {
            return "Servizio non disponibile";
        }
    }
}
```

Cosa specificano i timeout configurati?

- a) `connect.timeout`: tempo per stabilire connessione, `request.timeout`: tempo totale per la richiesta
- b) Entrambi specificano lo stesso timeout di connessione
- c) `connect.timeout`: timeout del server, `request.timeout`: timeout del client
- d) I timeout sono ignorati in ambiente Java EE

---

### 🟢 Domanda 25

Quali delle seguenti sono considerazioni importanti per la **scalabilità** dei servizi SOAP? (Seleziona tutte)

- a) Usare EJB Stateless invece di Stateful
- b) Implementare connection pooling per database
- c) Minimizzare la dimensione dei messaggi SOAP
- d) Usare caching per operazioni frequenti
- e) Preferire sempre comunicazione sincrona

---

## 11. Integrazione e Deployment

### 💻 Domanda 26

Osserva questa configurazione di deployment:

```java
@WebService(
    serviceName = "BankingService",
    portName = "BankingPort",
    targetNamespace = "http://banking.example.com/",
    endpointInterface = "com.example.banking.BankingServiceSEI"
)
@Stateless
public class BankingServiceImpl implements BankingServiceSEI {
    
    @WebMethod
    public AccountBalance getBalance(String accountNumber) 
            throws AccountNotFoundException {
        // Implementazione
        return new AccountBalance(accountNumber, 1000.0);
    }
}
```

Quale vantaggio offre la specifica di `endpointInterface`?

- a) Migliori performance di runtime
- b) Separazione tra contratto (interfaccia) e implementazione
- c) Sicurezza automatica
- d) Compatibilità solo con client Java

---

### 🔵 Domanda 27

In un ambiente di application server Java EE, dove vengono automaticamente esposti i web service SOAP?

- a) Solo su porta 8080
- b) Su un endpoint generato automaticamente dal container
- c) Solo tramite configurazione manuale in web.xml
- d) Non vengono esposti automaticamente

---

## 12. Debugging e Troubleshooting

### 💻 Domanda 28

Osserva questo codice per il debugging di messaggi SOAP:

```java
@WebService
public class DebuggableService {
    
    @WebMethod
    public String processRequest(String input) {
        // Log del messaggio in entrata
        SOAPMessageContext context = // ottenuto da handler
        try {
            SOAPMessage message = context.getMessage();
            message.writeTo(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "Processed: " + input;
    }
}
```

Quale approccio è **più appropriato** per il logging di messaggi SOAP in produzione?

- a) Usare System.out.println direttamente nei metodi business
- b) Implementare un SOAPHandler dedicato per il logging
- c) Loggare solo in caso di errore
- d) Non loggare mai i messaggi SOAP per motivi di sicurezza

---

### 🔵 Domanda 29

Quale strumento della JDK può essere utilizzato per **testare manualmente** un servizio SOAP?

- a) `javac`
- b) `wsimport`
- c) Browser web visualizzando il WSDL
- d) Tool specifici come SoapUI o client generati con wsimport

---

### 🔵 Domanda 30

Se un client riceve l'errore "Service Temporarily Unavailable", quale potrebbe essere la causa più probabile?

- a) Errore nella sintassi del messaggio SOAP
- b) Il servizio web non è deployato o l'application server è down
- c) Errore nel mapping JAXB
- d) Versione incompatibile di JAX-WS

---

---

## Risposte Corrette

### 1. **b)** Solo il metodo `somma` con nome `add`

`@WebMethod(exclude=true)` esclude il metodo dal WSDL, mentre `operationName` personalizza il nome dell'operazione.

### 2. **a, b, c, e)** `@WebService`, `@WebMethod`, `@WebParam`, `@WebServiceRef`

`@XmlRootElement` è un'annotazione JAXB, non JAX-WS specifica.

### 3. **b)** Gestione automatica delle transazioni e pooling delle istanze

L'integrazione EJB+JAX-WS combina i vantaggi del container EJB con la semplicità dei web service.

### 4. **c)** Stateful Session Bean

I Stateful Session Bean mantengono stato conversazionale e non sono adatti per web service stateless.

### 5. **b)** `<Person id="1"><fullName>Mario</fullName><age>30</age></Person>`

`@XmlAttribute` rende id un attributo, `@XmlElement` rinomina name, `@XmlTransient` esclude password.

### 6. **a, b, c, e)** `String`, `int/Integer`, `boolean/Boolean`, `BigDecimal`

`java.util.Date` richiede un adattatore personalizzato per il mapping.

### 7. **b)** `25/12/2023` (formato personalizzato)

L'adattatore definisce il formato `dd/MM/yyyy` nel metodo `marshal()`.

### 8. **c)** Viene mappata automaticamente in un SOAP Fault nel WSDL

`@WebFault` permette di mappare eccezioni checked in SOAP Fault documentati nel WSDL.

### 9. **b, c)** `RuntimeException` non gestite, `EJBException`

Solo le RuntimeException causano rollback automatico in CMT.

### 10. **b)** `wsimport -keep -p com.example.client http://localhost:8080/CalculatorService?wsdl`

`wsimport` richiede l'URL del WSDL e `-p` specifica il package per le classi generate.

### 11. **b)** Il container gestisce automaticamente la creazione e l'iniezione del proxy

`@WebServiceRef` permette l'iniezione automatica gestita dal container Java EE.

### 12. **b)** WS-Security

WS-Security fornisce sicurezza end-to-end a livello di messaggio SOAP, non solo a livello trasporto.

### 13. **b)** Viene restituito un SOAP Fault con errore di autorizzazione

La violazione delle autorizzazioni EJB si traduce in un SOAP Fault per il client.

### 14. **a, b, c, e)** Uso XML obbligatorio, WSDL, WS-Security, transazioni distribuite

L'orientamento alle risorse è caratteristico di REST, non SOAP.

### 15. **b)** SOAP con WS-Security

SOAP con WS-Security è progettato per scenari enterprise con alta sicurezza e transazioni.

### 16. **c)** Quando si verifica un SOAP Fault

`handleFault()` viene chiamato quando si verificano errori durante il processing dei messaggi.

### 17. **b)** MTOM (Message Transmission Optimization Mechanism)

MTOM ottimizza la trasmissione di allegati binari evitando la codifica Base64.

### 18. **b)** Formatta l'XML con indentazione per renderlo leggibile

`JAXB_FORMATTED_OUTPUT` produce XML formattato con indentazione per la leggibilità.

### 19. **a, b, c, e)** Stateless EJB, handler per monitoring, exclude per metodi privati, MTOM per binari

Non si dovrebbe sempre preferire SOAP a REST.

### 20. **b)** Service Facade

Il pattern coordina multiple operazioni di servizi diversi in un'unica operazione transazionale.

### 21. **b)** `<Department><employeeList><employee>...</employee></employeeList></Department>`

`@XmlElementWrapper` crea un contenitore per la lista, `@XmlElement` specifica il nome degli elementi.

### 22. **b)** `@XmlTransient`

`@XmlTransient` esclude un campo/proprietà dalla serializzazione JAXB.

### 23. **b)** Trasmissione ottimizzata di dati binari senza codifica Base64

MTOM ottimizza il trasferimento di allegati binari evitando l'overhead della codifica Base64.

### 24. **a)** `connect.timeout`: tempo per stabilire connessione, `request.timeout`: tempo totale per la richiesta

I due timeout controllano aspetti diversi della comunicazione di rete.

### 25. **a, b, c, d)** Stateless EJB, connection pooling, messaggi piccoli, caching

La comunicazione asincrona non è sempre preferibile per la scalabilità.

### 26. **b)** Separazione tra contratto (interfaccia) e implementazione

`endpointInterface` permette di separare il contratto del servizio dalla sua implementazione.

### 27. **b)** Su un endpoint generato automaticamente dal container

I container Java EE espongono automaticamente i web service su endpoint derivati dalla configurazione.

### 28. **b)** Implementare un SOAPHandler dedicato per il logging

Gli handler sono il modo appropriato per intercettare e loggare messaggi SOAP.

### 29. **d)** Tool specifici come SoapUI o client generati con wsimport

Per testare servizi SOAP servono tool specifici o client generati.

### 30. **b)** Il servizio web non è deployato o l'application server è down

"Service Temporarily Unavailable" indica tipicamente problemi di disponibilità del servizio.
