# Oracle Certified Expert, Java EE 7 Application Developer (1Z0-900)

# Exam Simulation Quiz 2 of 3

**Instructions:**

- This quiz contains 70 multiple-choice questions covering all Java EE 7 topics
- Each question has 4 options (a, b, c, d)
- Some questions may have multiple correct answers
- Time limit: 150 minutes (simulating real exam conditions)
- Passing score: 66% (46 correct answers out of 70)

---

## CDI - Contexts and Dependency Injection (Questions 1-10)

### Question 1

```java
@Named
@RequestScoped
public class UserBean {
    private String username;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
```

What scope does this bean have?

- a) Request scope
- b) Application scope
- c) Session scope
- d) Conversation scope

---

### Question 2

Which annotation is used to inject a CDI bean into another bean?

- a) `@Resource`
- b) `@Bean`
- c) `@Autowired`
- d) `@Inject`

---

### Question 3

```java
public interface PaymentProcessor {
    void processPayment(double amount);
}

@Default
public class CreditCardProcessor implements PaymentProcessor {
    public void processPayment(double amount) {
        // Process credit card payment
    }
}

@Alternative
public class PayPalProcessor implements PaymentProcessor {
    public void processPayment(double amount) {
        // Process PayPal payment
    }
}
```

Which implementation will be injected by default without additional configuration?

- a) Both will be injected
- b) Neither, a qualifier is required
- c) PayPalProcessor
- d) CreditCardProcessor

---

### Question 4

What is the purpose of the `@Produces` annotation in CDI?

- a) To mark methods as HTTP endpoints
- b) To enable serialization
- c) To produce factory methods for bean creation
- d) To create database connections

---

### Question 5

```java
@ApplicationScoped
public class ConfigBean {
    @Produces
    @Named("appVersion")
    public String getVersion() {
        return "1.0.0";
    }
}
```

How can you access the produced value in an EL expression?

- a) `${ConfigBean.version}`
- b) `${config.version}`
- c) `${appVersion}`
- d) `${produces.appVersion}`

---

### Question 6

Which CDI scope maintains state for the duration of a conversation?

- a) `@RequestScoped`
- b) `@ConversationScoped`
- c) `@SessionScoped`
- d) `@DialogScoped`

---

### Question 7

```java
@Interceptor
@Logged
public class LoggingInterceptor {
    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        System.out.println("Method called: " + ctx.getMethod().getName());
        return ctx.proceed();
    }
}
```

What must be done to enable this interceptor?

- a) Annotate the target class with @EnableInterceptors
- b) Register it in web.xml
- c) Nothing, it's enabled by default
- d) Add it to beans.xml

---

### Question 8

What is the purpose of `@Qualifier` annotation in CDI?

- a) To disambiguate between multiple implementations of the same type
- b) To mark beans as qualified for production
- c) To enable bean validation
- d) To specify database qualifiers

---

### Question 9

```java
@PostConstruct
public void init() {
    // Initialization code
}

@PreDestroy
public void cleanup() {
    // Cleanup code
}
```

When is the `@PostConstruct` method called?

- a) Before each method invocation
- b) When the application shuts down
- c) Before the bean constructor
- d) After dependency injection is complete

---

### Question 10

Which CDI event mechanism allows beans to communicate without direct coupling?

- a) `@Event` and `@Observer`
- b) `@Publish` and `@Subscribe`
- c) `@Observes` (for observer) and `Event<T>` (for firing)
- d) `@Fire` and `@Listen`

---

## SOAP Web Services (Questions 11-20)

### Question 11

```java
@WebService(serviceName = "CalculatorService")
public class CalculatorWS {
    
    @WebMethod
    public int add(int a, int b) {
        return a + b;
    }
    
    @WebMethod(exclude = true)
    public int subtract(int a, int b) {
        return a - b;
    }
}
```

Which method will be exposed as a web service operation?

- a) Only add
- b) Only subtract
- c) Neither
- d) Both add and subtract

---

### Question 12

What protocol does SOAP use for message exchange?

- a) Only HTTP
- b) JSON over HTTP
- c) Binary over TCP
- d) XML over various protocols (HTTP, SMTP, JMS, etc.)

---

### Question 13

```java
@WebServiceClient(name = "CalculatorService",
                  wsdlLocation = "http://localhost:8080/calc?wsdl")
public class CalculatorClient extends Service {
    // Generated client code
}
```

What tool is typically used to generate JAX-WS client code from WSDL?

- a) `jaxws-gen`
- b) `wsimport`
- c) `javac`
- d) `wsdl2java`

---

### Question 14

What does WSDL stand for?

- a) Wireless Service Data Link
- b) Web Services Definition List
- c) Web Service Description Language
- d) Web Socket Description Language

---

### Question 15

```java
@Stateless
@WebService
public class OrderService {
    @PersistenceContext
    private EntityManager em;
    
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void createOrder(Order order) {
        em.persist(order);
    }
}
```

What advantage does combining `@Stateless` and `@WebService` provide?

- a) Required by specification
- b) Automatic transaction management and pooling
- c) Better performance only
- d) No advantage, they cannot be combined

---

### Question 16

Which annotation is used to customize the XML representation of a Java class in JAXB?

- a) `@XmlType`
- b) `@XmlElement`
- c) `@XmlRootElement`
- d) All of the above

---

### Question 17

What is MTOM in the context of JAX-WS?

- a) A transaction coordination protocol
- b) A security protocol
- c) Message Transmission Optimization Mechanism for efficient binary data transfer
- d) A message routing protocol

---

### Question 18

```java
@WebServiceRef(wsdlLocation = "http://localhost:8080/calc?wsdl")
private CalculatorService calculatorService;
```

What does the `@WebServiceRef` annotation do?

- a) Exports a service reference
- b) Injects a web service client proxy
- c) Creates a web service
- d) Registers a service endpoint

---

### Question 19

Which binding style is default in JAX-WS?

- a) RPC/encoded
- b) RPC/literal
- c) Document/encoded
- d) Document/literal wrapped

---

### Question 20

What is the purpose of a SOAP handler?

- a) To intercept and modify SOAP messages
- b) To handle HTTP connections
- c) To generate WSDL
- d) To manage transactions

---

## Bean Validation (Questions 21-25)

### Question 21

```java
public class User {
    @NotNull
    @Size(min = 3, max = 50)
    private String username;
    
    @Email
    private String email;
    
    @Min(18)
    private int age;
}
```

Which validation constraint checks that a string is a valid email format?

- a) `@ValidEmail`
- b) `@Pattern`
- c) `@Email`
- d) `@EmailFormat`

---

### Question 22

How do you programmatically validate a bean?

- a) Validation is always automatic
- b) Using `ValidatorFactory` to get a `Validator` and call `validate()`
- c) Using `Validator.validate(bean)`
- d) Using `@Valid` annotation

---

### Question 23

```java
public class Order {
    @NotNull
    @Valid
    private Customer customer;
    
    @NotEmpty
    private List<OrderItem> items;
}
```

What does the `@Valid` annotation on the `customer` field do?

- a) Cascades validation to the Customer object's fields
- b) Validates only the customer reference
- c) Makes the customer field optional
- d) Enables database validation

---

### Question 24

Which annotation ensures a numeric value is within a range?

- a) `@Between`
- b) `@Min` and `@Max`
- c) `@Range`
- d) `@Size`

---

### Question 25

```java
@PUT
@Path("/{id}")
public Response updateUser(@PathParam("id") Long id, @Valid User user) {
    // Update user
    return Response.ok().build();
}
```

What happens if the user object fails validation in this JAX-RS method?

- a) A 400 Bad Request is returned automatically
- b) The method executes normally
- c) A `ConstraintViolationException` is thrown
- d) Nothing, validation is ignored in JAX-RS

---

## JSON-P (Questions 26-30)

### Question 26

```java
JsonObject jsonObject = Json.createObjectBuilder()
    .add("name", "John Doe")
    .add("age", 30)
    .add("email", "john@example.com")
    .build();
```

What does this code create?

- a) An XML document
- b) A database record
- c) A JSON object
- d) A Java object

---

### Question 27

Which API in Java EE 7 provides JSON processing capabilities?

- a) Jackson
- b) JSON-P (Java API for JSON Processing)
- c) JSON-B
- d) JAXB

---

### Question 28

```java
JsonReader reader = Json.createReader(new StringReader(jsonString));
JsonObject obj = reader.readObject();
String name = obj.getString("name");
```

What is this approach called?

- a) Object Model API
- b) Streaming API
- c) Binding API
- d) Parser API

---

### Question 29

Which method creates a JSON array using JSON-P?

- a) `Json.createArrayBuilder()`
- b) `Json.createArray()`
- c) `JsonArray.create()`
- d) `Json.newArray()`

---

### Question 30

```java
JsonParser parser = Json.createParser(inputStream);
while (parser.hasNext()) {
    JsonParser.Event event = parser.next();
    if (event == JsonParser.Event.KEY_NAME) {
        String key = parser.getString();
    }
}
```

What type of JSON processing is this?

- a) DOM API
- b) Streaming API
- c) Object Model API
- d) Binding API

---

## Advanced JPA (Questions 31-40)

### Question 31

```java
@Entity
@NamedQuery(
    name = "Product.findByCategory",
    query = "SELECT p FROM Product p WHERE p.category = :category"
)
public class Product {
    @Id
    private Long id;
    private String name;
    private String category;
}
```

How do you execute this named query?

- a) `em.createNamedQuery("Product.findByCategory")`
- b) `em.createQuery("Product.findByCategory")`
- c) `em.executeQuery("Product.findByCategory")`
- d) `em.findByName("Product.findByCategory")`

---

### Question 32

What is the purpose of `@EntityGraph` in JPA 2.1?

- a) To define entity relationships
- b) To generate database schemas
- c) To control which attributes are fetched in a query
- d) To create entity diagrams

---

### Question 33

```java
@Entity
public class Employee {
    @Id
    private Long id;
    
    @Convert(converter = PhoneNumberConverter.class)
    private PhoneNumber phone;
}

@Converter
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {
    // Implementation
}
```

What is the purpose of the `AttributeConverter`?

- a) To convert between different databases
- b) To convert between Java attribute types and database column types
- c) To convert between entity and DTO
- d) To enable data encryption

---

### Question 34

Which lock mode in JPA forces a database lock to be acquired?

- a) `LockModeType.PESSIMISTIC_WRITE`
- b) `LockModeType.OPTIMISTIC`
- c) `LockModeType.READ`
- d) `LockModeType.NONE`

---

### Question 35

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Product> cq = cb.createQuery(Product.class);
Root<Product> product = cq.from(Product.class);
cq.where(cb.gt(product.get("price"), 100));
TypedQuery<Product> query = em.createQuery(cq);
```

What API is being used here?

- a) HQL
- b) JPQL
- c) Criteria API
- d) Native SQL

---

### Question 36

What happens when an entity is in the "detached" state?

- a) It's no longer managed by the EntityManager
- b) It's still synchronized with the database
- c) It's automatically deleted
- d) It cannot be used

---

### Question 37

```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
}
```

What does `orphanRemoval = true` do?

- a) Prevents orphan entities
- b) Optimizes database queries
- c) Deletes OrderItem entities when removed from the collection
- d) Removes null items from the list

---

### Question 38

Which persistence context type is used in container-managed transactions?

- a) `PersistenceContextType.APPLICATION`
- b) `PersistenceContextType.REQUEST`
- c) `PersistenceContextType.TRANSACTION`
- d) `PersistenceContextType.EXTENDED`

---

### Question 39

```java
@Entity
@Table(name = "products", 
       indexes = {@Index(columnList = "category,name")})
public class Product {
    // Fields
}
```

What does this configuration create?

- a) A database index on category and name columns
- b) A unique constraint
- c) A foreign key
- d) A primary key

---

### Question 40

What is the difference between `persist()` and `merge()` in EntityManager?

- a) `persist()` works only with transactions
- b) `merge()` is faster
- c) `persist()` is for new entities, `merge()` is for detached entities
- d) There is no difference

---

## Advanced EJB (Questions 41-50)

### Question 41

```java
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class CounterBean {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public int getCount() {
        return count;
    }
}
```

What does `ConcurrencyManagementType.BEAN` mean?

- a) The bean manages concurrency using Java synchronization
- b) The container manages concurrency
- c) Concurrency is disabled
- d) Multiple instances are created

---

### Question 42

```java
@Stateless
public class EmailService {
    
    @Asynchronous
    public Future<String> sendEmail(String to, String subject, String body) {
        // Send email
        return new AsyncResult<>("Email sent");
    }
}
```

What is the return type for asynchronous methods that return results?

- a) `CompletableFuture<T>`
- b) `Future<T>`
- c) `void`
- d) `Callable<T>`

---

### Question 43

Which timer type executes at fixed intervals?

- a) Persistent timer
- b) Single-action timer
- c) Interval timer
- d) Calendar-based timer

---

### Question 44

```java
@Stateless
@Interceptors(LoggingInterceptor.class)
public class BusinessService {
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void sensitiveOperation() {
        // No interceptors applied
    }
}
```

What do the `@Exclude` annotations do?

- a) Make the method private
- b) Disable transactions
- c) Prevent interceptors from being applied to this method
- d) Exclude the method from compilation

---

### Question 45

What is the purpose of `@DependsOn` annotation in Singleton beans?

- a) To create circular dependencies
- b) To enable caching
- c) To specify initialization order
- d) To inject dependencies

---

### Question 46

```java
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(
        propertyName = "destinationType",
        propertyValue = "javax.jms.Topic"
    )
})
public class NewsSubscriber implements MessageListener {
    public void onMessage(Message message) {
        // Process message
    }
}
```

What type of JMS destination is this MDB listening to?

- a) Channel
- b) Topic
- c) Queue
- d) Exchange

---

### Question 47

Which transaction attribute suspends the current transaction and executes without a transaction?

- a) `NEVER`
- b) `REQUIRES_NEW`
- c) `NOT_SUPPORTED`
- d) `REQUIRED`

---

### Question 48

```java
@Schedule(hour = "2", minute = "0", persistent = false)
public void dailyCleanup() {
    // Cleanup task
}
```

What does `persistent = false` mean?

- a) The timer doesn't write to database
- b) The timer cannot be cancelled
- c) The timer runs only once
- d) The timer doesn't survive server restarts

---

### Question 49

What is the maximum number of Singleton EJB instances per application?

- a) One per JVM
- b) Configurable
- c) One per application
- d) Unlimited

---

### Question 50

```java
@Stateless
@LocalBean
public class CalculatorBean {
    public int add(int a, int b) {
        return a + b;
    }
}
```

What does `@LocalBean` indicate?

- a) The bean is only accessible locally
- b) The bean stores local data
- c) The bean is not distributed
- d) The bean exposes a no-interface view

---

## Advanced Servlets (Questions 51-55)

### Question 51

```java
@WebFilter(urlPatterns = "/*")
public class EncodingFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse resp, 
                        FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        chain.doFilter(req, resp);
    }
}
```

When are servlet filters executed?

- a) At server startup
- b) Only on errors
- c) After the servlet
- d) Before and after the servlet

---

### Question 52

Which listener interface is used to track session creation and destruction?

- a) `ServletRequestListener`
- b) `HttpSessionListener`
- c) `ServletContextListener`
- d) `SessionLifecycleListener`

---

### Question 53

```java
@MultipartConfig(
    maxFileSize = 1024 * 1024 * 5,  // 5MB
    maxRequestSize = 1024 * 1024 * 10  // 10MB
)
@WebServlet("/upload")
public class FileUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        Part filePart = req.getPart("file");
        // Handle upload
    }
}
```

What is the maximum file size allowed by this configuration?

- a) Unlimited
- b) 10MB
- c) 1MB
- d) 5MB

---

### Question 54

What method is used to include another resource's output in the response?

- a) `RequestDispatcher.include()`
- b) `RequestDispatcher.forward()`
- c) `response.include()`
- d) `request.redirect()`

---

### Question 55

```java
HttpSession session = request.getSession(false);
```

What does the `false` parameter mean?

- a) Invalidate the session
- b) Make the session read-only
- c) Don't create a new session if one doesn't exist
- d) Create a new session

---

## Advanced JSF (Questions 56-60)

### Question 56

```xhtml
<h:dataTable value="#{userBean.users}" var="user">
    <h:column>
        <f:facet name="header">Name</f:facet>
        #{user.name}
    </h:column>
    <h:column>
        <f:facet name="header">Email</f:facet>
        #{user.email}
    </h:column>
</h:dataTable>
```

What component is used to display tabular data in JSF?

- a) `<h:list>`
- b) `<h:dataTable>`
- c) `<h:table>`
- d) `<h:grid>`

---

### Question 57

Which phase of the JSF lifecycle updates the model with user input values?

- a) Process Validations
- b) Invoke Application
- c) Update Model Values
- d) Apply Request Values

---

### Question 58

```java
public void validateAge(FacesContext context, UIComponent component, 
                       Object value) throws ValidatorException {
    Integer age = (Integer) value;
    if (age < 18) {
        throw new ValidatorException(
            new FacesMessage("Must be 18 or older"));
    }
}
```

What type of validation is this?

- a) Built-in validation
- b) Custom validator method
- c) Bean Validation
- d) Client-side validation

---

### Question 59

What is the purpose of `<ui:composition>` in Facelets?

- a) To define a template composition
- b) To compose music
- c) To create custom components
- d) To group components

---

### Question 60

```xhtml
<h:form>
    <h:commandButton value="Save" action="#{bean.save}">
        <f:ajax execute="@form" render="messages" />
    </h:commandButton>
    <h:messages id="messages" />
</h:form>
```

What does `execute="@form"` mean?

- a) Validate the form
- b) Execute JavaScript
- c) Submit form data for the entire form
- d) Execute the entire form

---

## Integration & Advanced Topics (Questions 61-70)

### Question 61

```java
@Path("/products")
public class ProductResource {
    
    @EJB
    private ProductService productService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getAll() {
        return productService.findAll();
    }
}
```

What Java EE component integration is shown here?

- a) JSF with EJB
- b) JAX-RS with EJB
- c) JAX-WS with JPA
- d) Servlet with CDI

---

### Question 62

Which annotation enables CORS (Cross-Origin Resource Sharing) in JAX-RS?

- a) `@AllowOrigin`
- b) `@CORS`
- c) Custom filter with `@Provider`
- d) `@CrossOrigin`

---

### Question 63

```java
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ManualTransactionBean {
    
    @Resource
    private UserTransaction userTransaction;
    
    public void doWork() throws Exception {
        userTransaction.begin();
        try {
            // Do work
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
    }
}
```

What type of transaction management is this?

- a) Automatic transaction management
- b) Bean-Managed Transaction (BMT)
- c) Container-Managed Transaction (CMT)
- d) Distributed transaction management

---

### Question 64

Which deployment descriptor is used to configure servlet mappings?

- a) `web.xml`
- b) `application.xml`
- c) `ejb-jar.xml`
- d) `persistence.xml`

---

### Question 65

```java
@WebSocketClient
@ClientEndpoint
public class ChatClient {
    
    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }
}
```

What is this class?

- a) A JMS consumer
- b) A WebSocket client endpoint
- c) A REST client
- d) A WebSocket server endpoint

---

### Question 66

What is the purpose of the `META-INF/beans.xml` file?

- a) To enable CDI in the module
- b) To define EJBs
- c) To configure database connections
- d) To map REST endpoints

---

### Question 67

```java
@Path("/files")
public class FileResource {
    
    @GET
    @Produces("image/png")
    public StreamingOutput getImage() {
        return output -> {
            // Write image data to output stream
        };
    }
}
```

What is `StreamingOutput` used for?

- a) To compress data
- b) To cache responses
- c) To stream data to the client efficiently
- d) To log output

---

### Question 68

Which archive type contains both web and EJB modules?

- a) RAR
- b) EAR
- c) WAR
- d) JAR

---

### Question 69

```java
@Schedule(hour = "*", minute = "*/15", persistent = true)
public void runEvery15Minutes() {
    // Task
}
```

How often does this timer execute?

- a) Once every 15 days
- b) Every 15 minutes
- c) 15 times per hour
- d) Every 15 hours

---

### Question 70

What is the standard port for HTTP connections in Java EE application servers?

- a) It varies by server implementation
- b) 8080
- c) 80
- d) 8443

---
