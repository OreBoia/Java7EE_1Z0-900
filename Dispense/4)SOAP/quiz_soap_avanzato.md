# Quiz Pratico su SOAP, JAX-WS e JAXB - Scenari Reali e Implementazioni

Questo quiz pratico si concentra su scenari reali di implementazione di servizi SOAP con JAX-WS e JAXB, con domande a **risposta multipla**, **scelta multipla**, e **analisi di snippet di codice**.

---

## Legenda Tipi di Domanda

- 🔵 **Risposta Multipla**: Una sola risposta corretta
- 🟢 **Scelta Multipla**: Più risposte corrette possibili
- 💻 **Analisi Codice**: Domande basate su snippet di codice

---

## 1. Configurazione e Setup di Web Service

### 💻 Domanda 1

Analizza questo servizio web per la gestione di prodotti:

```java
@WebService(
    name = "ProductManagement",
    serviceName = "ProductService",
    targetNamespace = "http://ecommerce.example.com/products",
    portName = "ProductPort"
)
@Stateless
public class ProductWebService {
    
    @PersistenceContext
    private EntityManager em;
    
    @WebMethod(action = "urn:getProduct")
    public Product getProductById(@WebParam(name = "productId") Long id) {
        return em.find(Product.class, id);
    }
    
    @WebMethod(exclude = true)
    public void internalMethod() {
        // Metodo per uso interno
    }
}
```

Quale sarà l'endpoint URL generato automaticamente dal container?

- a) `http://localhost:8080/ProductManagement`
- b) `http://localhost:8080/[app-context]/ProductService`
- c) `http://localhost:8080/[app-context]/ProductWebService`
- d) `http://localhost:8080/products/ProductPort`

---

### 🔵 Domanda 2

In quale file di configurazione puoi personalizzare il mapping degli endpoint per i web service SOAP in Java EE?

- a) `web.xml`
- b) `ejb-jar.xml`
- c) `webservices.xml`
- d) `soap-config.xml`

---

### 🟢 Domanda 3

Quali elementi sono **obbligatori** per pubblicare un EJB come web service SOAP? (Seleziona tutti)

- a) Annotazione `@WebService`
- b) Metodi annotati con `@WebMethod`
- c) Classe EJB Stateless o Singleton
- d) Implementazione di un'interfaccia specifica
- e) Configurazione in `web.xml`

---

## 2. Gestione di Dati Complessi con JAXB

### 💻 Domanda 4

Osserva questa classe per la gestione di ordini:

```java
@XmlRootElement(name = "Order")
@XmlAccessorType(XmlAccessType.FIELD)
public class Order {
    
    @XmlAttribute
    private String orderId;
    
    @XmlElement(name = "customerInfo", required = true)
    private Customer customer;
    
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<OrderItem> orderItems;
    
    @XmlElement
    @XmlSchemaType(name = "dateTime")
    private Date orderDate;
    
    @XmlTransient
    private String internalNotes;
    
    // constructors, getters, setters...
}
```

Come apparirà la struttura XML per un ordine con 2 items?

- a) `<Order orderId="123"><customerInfo>...</customerInfo><orderItems><item>...</item><item>...</item></orderItems><orderDate>...</orderDate></Order>`
- b) `<Order orderId="123"><customerInfo>...</customerInfo><items><item>...</item><item>...</item></items><orderDate>...</orderDate></Order>`
- c) `<Order><orderId>123</orderId><customer>...</customer><items><item>...</item><item>...</item></items><orderDate>...</orderDate></Order>`
- d) `<Order orderId="123"><customer>...</customer><item>...</item><item>...</item><orderDate>...</orderDate></Order>`

---

### 💻 Domanda 5

Analizza questo adattatore per gestire enum personalizzati:

```java
public class StatusAdapter extends XmlAdapter<String, OrderStatus> {
    
    @Override
    public String marshal(OrderStatus status) throws Exception {
        if (status == null) return null;
        
        switch (status) {
            case PENDING: return "PENDING_APPROVAL";
            case APPROVED: return "READY_TO_SHIP";
            case SHIPPED: return "IN_TRANSIT";
            case DELIVERED: return "COMPLETED";
            default: return status.name();
        }
    }
    
    @Override
    public OrderStatus unmarshal(String value) throws Exception {
        if (value == null) return null;
        
        switch (value) {
            case "PENDING_APPROVAL": return OrderStatus.PENDING;
            case "READY_TO_SHIP": return OrderStatus.APPROVED;
            case "IN_TRANSIT": return OrderStatus.SHIPPED;
            case "COMPLETED": return OrderStatus.DELIVERED;
            default: return OrderStatus.valueOf(value);
        }
    }
}

public enum OrderStatus {
    PENDING, APPROVED, SHIPPED, DELIVERED
}
```

Se un oggetto ha `OrderStatus.SHIPPED`, quale valore apparirà nell'XML?

- a) `SHIPPED`
- b) `IN_TRANSIT`
- c) `READY_TO_SHIP`
- d) `3` (posizione ordinale)

---

### 🔵 Domanda 6

Quale annotazione JAXB permette di controllare l'**ordine degli elementi** nell'XML generato?

- a) `@XmlElementOrder`
- b) `@XmlType(propOrder = {...})`
- c) `@XmlSequence`
- d) `@XmlOrder`

---

## 3. Implementazione di Client SOAP

### 💻 Domanda 7

Analizza questo client che consume un servizio esterno:

```java
@Stateless
public class PaymentServiceClient {
    
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/PaymentService.wsdl")
    private PaymentService paymentServiceFactory;
    
    public PaymentResult processPayment(String cardNumber, BigDecimal amount) {
        try {
            PaymentServicePortType port = paymentServiceFactory.getPaymentServicePort();
            
            // Configurazione endpoint e timeout
            BindingProvider bp = (BindingProvider) port;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                                      "https://payment-gateway.example.com/service");
            bp.getRequestContext().put("com.sun.xml.ws.request.timeout", 30000);
            
            return port.authorizePayment(cardNumber, amount);
            
        } catch (PaymentException e) {
            throw new EJBException("Errore nel processing del pagamento", e);
        }
    }
}
```

Quale vantaggio offre l'uso del file WSDL locale in `META-INF/wsdl/`?

- a) Migliori performance di runtime
- b) Il client può funzionare anche se il servizio remoto è temporaneamente non disponibile per il download del WSDL
- c) Sicurezza maggiore nelle comunicazioni
- d) Compatibilità con versioni precedenti di JAX-WS

---

### 🟢 Domanda 8

Quali tecniche possono essere utilizzate per gestire **timeout e retry** in client SOAP? (Seleziona tutte)

- a) Configurazione di `connect.timeout` e `request.timeout`
- b) Uso di pattern Circuit Breaker
- c) Implementazione di retry automatico con backoff esponenziale
- d) Uso di `@Asynchronous` per chiamate non bloccanti
- e) Configurazione di pool di connessioni HTTP

---

### 💻 Domanda 9

Osserva questo handler per aggiungere autenticazione:

```java
public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {
    
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        if (outbound) {
            try {
                SOAPMessage message = context.getMessage();
                SOAPHeader header = message.getSOAPHeader();
                
                if (header == null) {
                    header = message.getSOAPPart().getEnvelope().addHeader();
                }
                
                // Aggiunge token di autenticazione
                QName qname = new QName("http://security.example.com/", "AuthToken");
                SOAPHeaderElement authElement = header.addHeaderElement(qname);
                authElement.addTextNode("Bearer xyz123token");
                
            } catch (SOAPException e) {
                return false;
            }
        }
        return true;
    }
    
    // Altri metodi dell'interfaccia...
}
```

Come deve essere registrato questo handler per essere utilizzato?

- a) Automaticamente dal container
- b) Tramite annotazione `@HandlerChain` sul client
- c) Nella configurazione `web.xml`
- d) Solo programmaticamente nel codice del client

---

## 4. Gestione delle Eccezioni e SOAP Fault

### 💻 Domanda 10

Analizza questa gerarchia di eccezioni personalizzate:

```java
@WebFault(name = "BusinessRuleFault", targetNamespace = "http://example.com/faults")
public class BusinessRuleException extends Exception {
    
    private BusinessRuleFaultInfo faultInfo;
    
    public BusinessRuleException(String message, BusinessRuleFaultInfo faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }
    
    public BusinessRuleFaultInfo getFaultInfo() {
        return faultInfo;
    }
}

public class BusinessRuleFaultInfo {
    private String errorCode;
    private String fieldName;
    private String suggestion;
    
    // constructors, getters, setters...
}

@WebService
public class ValidationService {
    
    @WebMethod
    public void validateCustomer(Customer customer) throws BusinessRuleException {
        if (customer.getAge() < 18) {
            BusinessRuleFaultInfo fault = new BusinessRuleFaultInfo();
            fault.setErrorCode("AGE_VALIDATION");
            fault.setFieldName("age");
            fault.setSuggestion("Customer must be at least 18 years old");
            
            throw new BusinessRuleException("Validation failed", fault);
        }
    }
}
```

Quale informazione NON apparirà nel SOAP Fault generato?

- a) Il messaggio "Validation failed"
- b) I dettagli dell'oggetto `BusinessRuleFaultInfo`
- c) Il nome del fault "BusinessRuleFault"
- d) Lo stack trace dell'eccezione Java

---

### 🔵 Domanda 11

Quale tipo di eccezione causa il **rollback automatico** di una transazione in un web service EJB?

- a) Tutte le eccezioni checked
- b) Solo le eccezioni annotate con `@WebFault`
- c) Le `RuntimeException` e le `EJBException`
- d) Solo le eccezioni di tipo `SOAPFaultException`

---

### 💻 Domanda 12

Osserva questo codice per la gestione centralizzata degli errori:

```java
@WebService
@Interceptors(ErrorHandlingInterceptor.class)
public class CriticalService {
    
    @WebMethod
    public String processImportantData(String data) throws ServiceException {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        
        // Business logic che può generare errori
        return processData(data);
    }
}

@Interceptor
public class ErrorHandlingInterceptor {
    
    @AroundInvoke
    public Object handleErrors(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (RuntimeException e) {
            // Log dell'errore
            System.err.println("Critical error: " + e.getMessage());
            
            // Trasforma in eccezione business
            throw new ServiceException("Internal processing error", e);
        }
    }
}
```

Cosa succede quando viene lanciata `IllegalArgumentException` nel metodo `processImportantData`?

- a) Viene restituita direttamente come SOAP Fault
- b) Viene intercettata dall'interceptor e trasformata in `ServiceException`
- c) Causa il rollback automatico della transazione
- d) Viene ignorata dal web service

---

## 5. Sicurezza nei Web Service SOAP

### 💻 Domanda 13

Analizza questa configurazione di sicurezza dichiarativa:

```java
@WebService(serviceName = "SecureBankingService")
@Stateless
@DeclareRoles({"customer", "employee", "manager"})
@RolesAllowed({"employee", "manager"})
public class BankingService {
    
    @WebMethod
    @PermitAll
    public BankInfo getBankInfo() {
        return new BankInfo("Example Bank", "123-456-789");
    }
    
    @WebMethod
    @RolesAllowed("customer")
    public Account getMyAccount() {
        String username = sessionContext.getCallerPrincipal().getName();
        return accountService.findByUsername(username);
    }
    
    @WebMethod
    @RolesAllowed("manager")
    public List<Account> getAllAccounts() {
        return accountService.findAll();
    }
    
    @EJBContext
    private SessionContext sessionContext;
}
```

Un utente con ruolo "customer" può chiamare il metodo `getBankInfo()`?

- a) No, perché la classe richiede ruolo "employee" o "manager"
- b) Sì, perché il metodo ha `@PermitAll` che sovrascrive le restrizioni della classe
- c) No, perché non è specificato `@RolesAllowed("customer")` sul metodo
- d) Dipende dalla configurazione in `web.xml`

---

### 🔵 Domanda 14

Quale protocollo è raccomandato per la **sicurezza a livello di trasporto** nei web service SOAP in produzione?

- a) HTTP Basic Authentication
- b) HTTPS/TLS con certificati SSL
- c) Digest Authentication
- d) Token-based authentication custom

---

### 🟢 Domanda 15

Quali sono i livelli di sicurezza disponibili per i web service SOAP? (Seleziona tutti)

- a) Sicurezza a livello di trasporto (Transport-level security)
- b) Sicurezza a livello di messaggio (Message-level security)
- c) Sicurezza a livello di applicazione (Application-level security)
- d) Sicurezza a livello di database (Database-level security)
- e) Sicurezza a livello di container (Container-managed security)

---

## 6. Performance e Ottimizzazione

### 💻 Domanda 16

Osserva questo servizio ottimizzato per grandi volumi di dati:

```java
@WebService(serviceName = "DocumentService")
@Stateless
public class DocumentManagementService {
    
    @WebMethod
    @MTOM
    public String uploadDocument(
        @XmlMimeType("application/octet-stream") DataHandler fileData,
        @WebParam(name = "filename") String filename,
        @WebParam(name = "metadata") DocumentMetadata metadata) {
        
        try {
            // Salvataggio streaming del file per grandi dimensioni
            try (InputStream inputStream = fileData.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream("/docs/" + filename)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return "Document uploaded successfully: " + filename;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }
}
```

Quale vantaggio principale offre l'uso di `@MTOM` in questo scenario?

- a) Maggiore sicurezza durante il trasferimento
- b) Compressione automatica dei file
- c) Trasmissione binaria ottimizzata senza encoding Base64
- d) Validazione automatica del tipo di file

---

### 🟢 Domanda 17

Quali tecniche possono migliorare le **performance** dei web service SOAP? (Seleziona tutte)

- a) Uso di EJB Stateless con pooling
- b) Implementazione di caching a livello di servizio
- c) Riduzione della dimensione dei messaggi SOAP
- d) Uso di connection pooling HTTP
- e) Compressione GZIP per i messaggi

---

### 💻 Domanda 18

Analizza questo pattern di paginazione per grandi dataset:

```java
@WebService
public class ProductCatalogService {
    
    @WebMethod
    public ProductPage getProducts(
        @WebParam(name = "pageNumber") int page,
        @WebParam(name = "pageSize") int size,
        @WebParam(name = "sortBy") String sortField) {
        
        // Validazione parametri
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        
        int offset = page * size;
        List<Product> products = productDAO.findProducts(offset, size, sortField);
        long totalCount = productDAO.getTotalProductCount();
        
        return new ProductPage(products, page, size, totalCount);
    }
}

@XmlRootElement
public class ProductPage {
    private List<Product> products;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    
    // constructors, getters, setters...
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}
```

Perché è importante limitare la `pageSize` a un massimo di 100?

- a) Per rispettare le limitazioni del protocollo SOAP
- b) Per evitare timeout e messaggi SOAP troppo grandi
- c) Per motivi di sicurezza
- d) Per compatibilità con client più vecchi

---

## 7. Testing e Debugging

### 💻 Domanda 19

Osserva questo test di integrazione per un web service:

```java
@RunWith(Arquillian.class)
public class OrderServiceIT {
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(OrderService.class, Order.class, Customer.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Test
    public void testCreateOrder() throws Exception {
        // Creazione client programmatico
        URL wsdlURL = new URL("http://localhost:8080/test/OrderService?wsdl");
        QName serviceName = new QName("http://service.example.com/", "OrderService");
        
        Service service = Service.create(wsdlURL, serviceName);
        OrderServicePortType port = service.getPort(OrderServicePortType.class);
        
        // Test del servizio
        Customer customer = new Customer("John Doe", "john@example.com");
        Order order = port.createOrder(customer, "PRODUCT123", 2);
        
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals(customer.getName(), order.getCustomer().getName());
    }
}
```

Quale tecnologia di testing è utilizzata in questo esempio?

- a) JUnit standard con mock objects
- b) Arquillian per test di integrazione in-container
- c) TestNG con Spring Test
- d) Mockito per unit testing

---

### 🔵 Domanda 20

Quale strumento è più appropriato per **testare manualmente** un web service SOAP durante lo sviluppo?

- a) Un browser web standard
- b) cURL con parametri specifici
- c) SoapUI o Postman
- d) Un debugger Java

---

### 💻 Domanda 21

Analizza questo handler di logging per debugging:

```java
@Component
public class DebugLoggingHandler implements SOAPHandler<SOAPMessageContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugLoggingHandler.class);
    
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        try {
            SOAPMessage message = context.getMessage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            
            String direction = outbound ? "OUTBOUND" : "INBOUND";
            logger.debug("{} SOAP Message: {}", direction, baos.toString());
            
        } catch (Exception e) {
            logger.error("Error logging SOAP message", e);
        }
        
        return true;
    }
    
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logger.error("SOAP Fault detected");
        return handleMessage(context);
    }
    
    // Altri metodi...
}
```

Quale configurazione di logging è necessaria per vedere i messaggi SOAP completi in produzione?

- a) Impostare il livello DEBUG per questo handler
- b) Abilitare sempre il logging, indipendentemente dal livello
- c) Configurare il livello INFO per tutti i logger
- d) Non loggare mai i messaggi SOAP completi in produzione per sicurezza

---

## 8. Integrazione con Altri Componenti Java EE

### 💻 Domanda 22

Analizza questa integrazione con JMS per elaborazione asincrona:

```java
@WebService(serviceName = "AsyncOrderService")
@Stateless
public class AsyncOrderService {
    
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Resource(mappedName = "java:/queue/OrderProcessing")
    private Queue orderQueue;
    
    @WebMethod
    public String submitOrderAsync(Order order) {
        try {
            // Validazione sincrona
            validateOrder(order);
            
            // Generazione ID ordine
            String orderId = generateOrderId();
            order.setOrderId(orderId);
            
            // Invio asincrono per elaborazione
            try (JMSContext context = connectionFactory.createContext()) {
                ObjectMessage message = context.createObjectMessage(order);
                message.setStringProperty("orderId", orderId);
                context.createProducer().send(orderQueue, message);
            }
            
            return orderId;
            
        } catch (JMSException e) {
            throw new EJBException("Failed to submit order for processing", e);
        }
    }
}
```

Quale vantaggio offre questo pattern di elaborazione asincrona?

- a) Migliore sicurezza delle transazioni
- b) Il client riceve una risposta immediata mentre l'elaborazione continua in background
- c) Maggiore compatibilità con client SOAP
- d) Riduzione dell'uso di memoria

---

### 🟢 Domanda 23

Quali componenti Java EE possono essere **integrati** efficacemente con web service SOAP? (Seleziona tutti)

- a) Enterprise JavaBeans (EJB)
- b) Java Persistence API (JPA)
- c) Java Message Service (JMS)
- d) Contexts and Dependency Injection (CDI)
- e) Java Transaction API (JTA)

---

### 💻 Domanda 24

Osserva questo servizio che utilizza CDI per iniezione:

```java
@WebService(serviceName = "InventoryService")
@Stateless
public class InventoryWebService {
    
    @Inject
    @AuditLog
    private InventoryManager inventoryManager;
    
    @Inject
    private NotificationService notificationService;
    
    @WebMethod
    @Transactional
    public void updateInventory(@WebParam(name = "productId") String productId,
                               @WebParam(name = "newQuantity") int quantity) {
        
        Product product = inventoryManager.updateQuantity(productId, quantity);
        
        // Notifica automatica se stock basso
        if (product.getQuantity() < product.getMinimumStock()) {
            notificationService.sendLowStockAlert(product);
        }
    }
}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface AuditLog {
}
```

Quale vantaggio offre l'uso di CDI con qualifiers in questo contesto?

- a) Migliori performance del web service
- b) Iniezione di dipendenze tipizzata e configurabile con interceptors personalizzati
- c) Compatibilità automatica con tutti i client SOAP
- d) Gestione automatica delle transazioni

---

## 9. Versioning e Evoluzione dei Servizi

### 💻 Domanda 25

Analizza questa strategia di versioning per un servizio esistente:

```java
// Versione 1 - Servizio originale
@WebService(
    serviceName = "CustomerService",
    targetNamespace = "http://example.com/customer/v1"
)
public class CustomerServiceV1 {
    
    @WebMethod
    public Customer getCustomer(@WebParam(name = "customerId") String id) {
        return customerDAO.findById(id);
    }
}

// Versione 2 - Servizio evoluto
@WebService(
    serviceName = "CustomerService", 
    targetNamespace = "http://example.com/customer/v2"
)
public class CustomerServiceV2 {
    
    @WebMethod
    public CustomerDetails getCustomer(@WebParam(name = "customerId") String id) {
        Customer customer = customerDAO.findById(id);
        return enhanceCustomerDetails(customer);
    }
    
    @WebMethod
    public Customer getBasicCustomer(@WebParam(name = "customerId") String id) {
        return customerDAO.findById(id);
    }
}
```

Quale vantaggio offre l'uso di **namespace diversi** per le versioni?

- a) Migliori performance
- b) I client possono scegliere quale versione utilizzare e i servizi possono coesistere
- c) Sicurezza maggiore
- d) Compatibilità automatica tra versioni

---

### 🔵 Domanda 26

Quale approccio è **meno raccomandato** per l'evoluzione di web service SOAP in produzione?

- a) Aggiungere nuovi metodi opzionali
- b) Modificare la signature di metodi esistenti
- c) Creare nuove versioni con namespace diversi
- d) Aggiungere parametri opzionali con valori di default

---

### 🟢 Domanda 27

Quali tecniche supportano la **backward compatibility** nei web service SOAP? (Seleziona tutte)

- a) Aggiunta di nuovi elementi opzionali negli XSD
- b) Uso di valori di default per nuovi parametri
- c) Mantenimento di metodi deprecated insieme ai nuovi
- d) Uso di adapter pattern per trasformazioni
- e) Cambio del tipo di ritorno dei metodi esistenti

---

## 10. Monitoring e Osservabilità

### 💻 Domanda 28

Analizza questo interceptor per metriche:

```java
@Interceptor
@MetricsBinding
public class MetricsInterceptor {
    
    @Inject
    private MetricRegistry metricRegistry;
    
    @AroundInvoke
    public Object collectMetrics(InvocationContext ctx) throws Exception {
        String methodName = ctx.getMethod().getName();
        Timer.Context timerContext = metricRegistry.timer("soap.service." + methodName).time();
        
        try {
            Object result = ctx.proceed();
            metricRegistry.counter("soap.service." + methodName + ".success").inc();
            return result;
            
        } catch (Exception e) {
            metricRegistry.counter("soap.service." + methodName + ".error").inc();
            throw e;
            
        } finally {
            timerContext.stop();
        }
    }
}

@WebService
@Interceptors(MetricsInterceptor.class)
public class MonitoredService {
    
    @WebMethod
    public String processRequest(String data) {
        // Business logic
        return "Processed: " + data;
    }
}
```

Quali metriche vengono raccolte da questo interceptor?

- a) Solo il tempo di esecuzione dei metodi
- b) Tempo di esecuzione, contatori di successo e di errore per ogni metodo
- c) Solo il numero di chiamate totali
- d) Solo gli errori che si verificano

---

### 🔵 Domanda 29

Quale protocollo è comunemente utilizzato per **health check** dei web service SOAP?

- a) Un endpoint SOAP dedicato
- b) HTTP GET su un endpoint di status
- c) Ping ICMP
- d) Controllo della connessione TCP

---

### 💻 Domanda 30

Osserva questa implementazione di circuit breaker per servizi esterni:

```java
@Stateless
public class ResilientPaymentClient {
    
    private CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("paymentService");
    
    @WebServiceRef
    private PaymentService paymentServiceFactory;
    
    public PaymentResult processPayment(PaymentRequest request) {
        
        Supplier<PaymentResult> paymentCall = () -> {
            PaymentServicePortType port = paymentServiceFactory.getPaymentServicePort();
            return port.authorizePayment(request);
        };
        
        // Esecuzione con circuit breaker
        return circuitBreaker.executeSupplier(paymentCall);
    }
}
```

Quando si attiva il circuit breaker?

- a) Dopo un numero configurabile di fallimenti consecutivi
- b) Solo in caso di timeout di rete
- c) Quando il servizio restituisce SOAP Fault
- d) Automaticamente ogni 5 minuti

---

---

## Risposte Corrette

### 1. **b)** `http://localhost:8080/[app-context]/ProductService`

Il `serviceName` nell'annotazione `@WebService` determina l'endpoint URL generato dal container.

### 2. **c)** `webservices.xml`

Il file `webservices.xml` permette di configurare endpoint personalizzati e altre proprietà dei web service.

### 3. **a, c)** Annotazione `@WebService`, Classe EJB Stateless o Singleton

Gli altri elementi sono opzionali. I metodi senza `@WebMethod` sono inclusi automaticamente se la classe ha `@WebService`.

### 4. **b)** `<Order orderId="123"><customerInfo>...</customerInfo><items><item>...</item><item>...</item></items><orderDate>...</orderDate></Order>`

`@XmlAttribute` rende `orderId` un attributo, `@XmlElementWrapper` crea il contenitore `items` per la lista.

### 5. **b)** `IN_TRANSIT`

L'adattatore trasforma `OrderStatus.SHIPPED` nel valore stringa `IN_TRANSIT` durante il marshalling.

### 6. **b)** `@XmlType(propOrder = {...})`

`@XmlType` con `propOrder` controlla l'ordine degli elementi nell'XML Schema e nell'output XML.

### 7. **b)** Il client può funzionare anche se il servizio remoto è temporaneamente non disponibile per il download del WSDL

Il WSDL locale garantisce che il client possa essere inizializzato anche quando il servizio remoto è inaccessibile.

### 8. **a, b, c, d)** Timeout configuration, Circuit Breaker, retry con backoff, chiamate asincrone

Il pool di connessioni HTTP è gestito dal container, non configurabile direttamente nel codice del client.

### 9. **b)** Tramite annotazione `@HandlerChain` sul client

Gli handler vengono registrati tramite `@HandlerChain` che referenzia un file di configurazione XML.

### 10. **d)** Lo stack trace dell'eccezione Java

Il SOAP Fault contiene il messaggio, i fault details e il nome del fault, ma non lo stack trace Java per sicurezza.

### 11. **c)** Le `RuntimeException` e le `EJBException`

Solo le RuntimeException causano rollback automatico in Container Managed Transactions.

### 12. **b)** Viene intercettata dall'interceptor e trasformata in `ServiceException`

L'interceptor con `@AroundInvoke` cattura la `RuntimeException` e la trasforma.

### 13. **b)** Sì, perché il metodo ha `@PermitAll` che sovrascrive le restrizioni della classe

`@PermitAll` a livello di metodo ha precedenza sulle restrizioni della classe.

### 14. **b)** HTTPS/TLS con certificati SSL

HTTPS/TLS fornisce sicurezza a livello di trasporto, raccomandato per ambienti di produzione.

### 15. **a, b, c, e)** Transport-level, message-level, application-level, container-managed security

La sicurezza a livello di database non è specifica dei web service SOAP.

### 16. **c)** Trasmissione binaria ottimizzata senza encoding Base64

MTOM ottimizza il trasferimento di allegati binari evitando l'overhead della codifica Base64.

### 17. **a, b, c, d, e)** Tutte le opzioni elencate

Tutte queste tecniche contribuiscono a migliorare le performance dei web service SOAP.

### 18. **b)** Per evitare timeout e messaggi SOAP troppo grandi

Limitare la dimensione delle pagine previene timeout di rete e messaggi SOAP eccessivamente grandi.

### 19. **b)** Arquillian per test di integrazione in-container

Arquillian permette di testare i web service in un container reale durante i test di integrazione.

### 20. **c)** SoapUI o Postman

Questi tool sono specificamente progettati per testare web service SOAP con interfacce user-friendly.

### 21. **d)** Non loggare mai i messaggi SOAP completi in produzione per sicurezza

In produzione, il logging completo dei messaggi SOAP può esporre dati sensibili e impattare le performance.

### 22. **b)** Il client riceve una risposta immediata mentre l'elaborazione continua in background

Il pattern asincrono migliora la responsiveness del client separando accettazione ed elaborazione.

### 23. **a, b, c, d, e)** Tutti i componenti elencati

Tutti questi componenti Java EE possono essere integrati efficacemente con web service SOAP.

### 24. **b)** Iniezione di dipendenze tipizzata e configurabile con interceptors personalizzati

CDI con qualifiers permette iniezione tipizzata e interceptors personalizzati come l'audit logging.

### 25. **b)** I client possono scegliere quale versione utilizzare e i servizi possono coesistere

Namespace diversi permettono il deployment simultaneo di multiple versioni del servizio.

### 26. **b)** Modificare la signature di metodi esistenti

Cambiare la signature di metodi esistenti rompe la compatibilità con i client esistenti.

### 27. **a, b, c, d)** Elementi opzionali, valori default, metodi deprecated mantenuti, adapter pattern

Cambiare il tipo di ritorno rompe la backward compatibility.

### 28. **b)** Tempo di esecuzione, contatori di successo e di errore per ogni metodo

L'interceptor raccoglie metriche complete: timing, successi ed errori per ogni metodo.

### 29. **b)** HTTP GET su un endpoint di status

Gli health check tipicamente usano endpoint HTTP semplici, non chiamate SOAP complete.

### 30. **a)** Dopo un numero configurabile di fallimenti consecutivi

Il circuit breaker si attiva quando il numero di fallimenti consecutivi supera una soglia configurata.