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

- a) Application scope
- b) Session scope
- c) Request scope
- d) Conversation scope

---

### Question 2

Which annotation is used to inject a CDI bean into another bean?

- a) `@Resource`
- b) `@Inject`
- c) `@Autowired`
- d) `@Bean`

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

- a) PayPalProcessor
- b) CreditCardProcessor
- c) Both will be injected
- d) Neither, a qualifier is required

---

### Question 4

What is the purpose of the `@Produces` annotation in CDI?

- a) To produce factory methods for bean creation
- b) To mark methods as HTTP endpoints
- c) To enable serialization
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
- b) `${appVersion}`
- c) `${produces.appVersion}`
- d) `${config.version}`

---

### Question 6

Which CDI scope maintains state for the duration of a conversation?

- a) `@RequestScoped`
- b) `@SessionScoped`
- c) `@ConversationScoped`
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

- a) Nothing, it's enabled by default
- b) Add it to beans.xml
- c) Annotate the target class with @EnableInterceptors
- d) Register it in web.xml

---

### Question 8

What is the purpose of `@Qualifier` annotation in CDI?

- a) To mark beans as qualified for production
- b) To disambiguate between multiple implementations of the same type
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

- a) Before the bean constructor
- b) After dependency injection is complete
- c) Before each method invocation
- d) When the application shuts down

---

### Question 10

Which CDI event mechanism allows beans to communicate without direct coupling?

- a) `@Event` and `@Observer`
- b) `@Fire` and `@Listen`
- c) `@Observes` (for observer) and `Event<T>` (for firing)
- d) `@Publish` and `@Subscribe`

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

- a) Both add and subtract
- b) Only add
- c) Only subtract
- d) Neither

---

### Question 12

What protocol does SOAP use for message exchange?

- a) Only HTTP
- b) XML over various protocols (HTTP, SMTP, JMS, etc.)
- c) JSON over HTTP
- d) Binary over TCP

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

- a) `javac`
- b) `wsimport`
- c) `wsdl2java`
- d) `jaxws-gen`

---

### Question 14

What does WSDL stand for?

- a) Web Service Description Language
- b) Web Services Definition List
- c) Wireless Service Data Link
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

- a) Better performance only
- b) Automatic transaction management and pooling
- c) Required by specification
- d) No advantage, they cannot be combined

---

### Question 16

Which annotation is used to customize the XML representation of a Java class in JAXB?

- a) `@XmlElement`
- b) `@XmlType`
- c) `@XmlRootElement`
- d) All of the above

---

### Question 17

What is MTOM in the context of JAX-WS?

- a) A security protocol
- b) Message Transmission Optimization Mechanism for efficient binary data transfer
- c) A message routing protocol
- d) A transaction coordination protocol

---

### Question 18

```java
@WebServiceRef(wsdlLocation = "http://localhost:8080/calc?wsdl")
private CalculatorService calculatorService;
```

What does the `@WebServiceRef` annotation do?

- a) Creates a web service
- b) Injects a web service client proxy
- c) Exports a service reference
- d) Registers a service endpoint

---

### Question 19

Which binding style is default in JAX-WS?

- a) RPC/encoded
- b) RPC/literal
- c) Document/literal wrapped
- d) Document/encoded

---

### Question 20

What is the purpose of a SOAP handler?

- a) To handle HTTP connections
- b) To intercept and modify SOAP messages
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

- a) `@Email`
- b) `@ValidEmail`
- c) `@EmailFormat`
- d) `@Pattern`

---

### Question 22

How do you programmatically validate a bean?

- a) Using `Validator.validate(bean)`
- b) Using `ValidatorFactory` to get a `Validator` and call `validate()`
- c) Using `@Valid` annotation
- d) Validation is always automatic

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

- a) Validates only the customer reference
- b) Cascades validation to the Customer object's fields
- c) Makes the customer field optional
- d) Enables database validation

---

### Question 24

Which annotation ensures a numeric value is within a range?

- a) `@Range`
- b) `@Between`
- c) `@Min` and `@Max`
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

- a) The method executes normally
- b) A `ConstraintViolationException` is thrown
- c) A 400 Bad Request is returned automatically
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

- a) A Java object
- b) A JSON object
- c) An XML document
- d) A database record

---

### Question 27

Which API in Java EE 7 provides JSON processing capabilities?

- a) JSON-B
- b) JSON-P (Java API for JSON Processing)
- c) JAXB
- d) Jackson

---

### Question 28

```java
JsonReader reader = Json.createReader(new StringReader(jsonString));
JsonObject obj = reader.readObject();
String name = obj.getString("name");
```

What is this approach called?

- a) Streaming API
- b) Object Model API
- c) Binding API
- d) Parser API

---

### Question 29

Which method creates a JSON array using JSON-P?

- a) `Json.createArray()`
- b) `Json.createArrayBuilder()`
- c) `Json.newArray()`
- d) `JsonArray.create()`

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

- a) Object Model API
- b) Streaming API
- c) Binding API
- d) DOM API

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

- a) `em.createQuery("Product.findByCategory")`
- b) `em.createNamedQuery("Product.findByCategory")`
- c) `em.executeQuery("Product.findByCategory")`
- d) `em.findByName("Product.findByCategory")`

---

### Question 32

What is the purpose of `@EntityGraph` in JPA 2.1?

- a) To create entity diagrams
- b) To control which attributes are fetched in a query
- c) To define entity relationships
- d) To generate database schemas

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

- a) To convert between entity and DTO
- b) To convert between Java attribute types and database column types
- c) To convert between different databases
- d) To enable data encryption

---

### Question 34

Which lock mode in JPA forces a database lock to be acquired?

- a) `LockModeType.OPTIMISTIC`
- b) `LockModeType.PESSIMISTIC_WRITE`
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

- a) JPQL
- b) Native SQL
- c) Criteria API
- d) HQL

---

### Question 36

What happens when an entity is in the "detached" state?

- a) It's still synchronized with the database
- b) It's no longer managed by the EntityManager
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

- a) Removes null items from the list
- b) Deletes OrderItem entities when removed from the collection
- c) Prevents orphan entities
- d) Optimizes database queries

---

### Question 38

Which persistence context type is used in container-managed transactions?

- a) `PersistenceContextType.EXTENDED`
- b) `PersistenceContextType.TRANSACTION`
- c) `PersistenceContextType.APPLICATION`
- d) `PersistenceContextType.REQUEST`

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

- a) A unique constraint
- b) A database index on category and name columns
- c) A foreign key
- d) A primary key

---

### Question 40

What is the difference between `persist()` and `merge()` in EntityManager?

- a) There is no difference
- b) `persist()` is for new entities, `merge()` is for detached entities
- c) `merge()` is faster
- d) `persist()` works only with transactions

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

- a) The container manages concurrency
- b) The bean manages concurrency using Java synchronization
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

- a) `void`
- b) `Future<T>`
- c) `Callable<T>`
- d) `CompletableFuture<T>`

---

### Question 43

Which timer type executes at fixed intervals?

- a) Calendar-based timer
- b) Interval timer
- c) Single-action timer
- d) Persistent timer

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

- a) Exclude the method from compilation
- b) Prevent interceptors from being applied to this method
- c) Make the method private
- d) Disable transactions

---

### Question 45

What is the purpose of `@DependsOn` annotation in Singleton beans?

- a) To inject dependencies
- b) To specify initialization order
- c) To create circular dependencies
- d) To enable caching

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

- a) Queue
- b) Topic
- c) Channel
- d) Exchange

---

### Question 47

Which transaction attribute suspends the current transaction and executes without a transaction?

- a) `REQUIRED`
- b) `REQUIRES_NEW`
- c) `NOT_SUPPORTED`
- d) `NEVER`

---

### Question 48

```java
@Schedule(hour = "2", minute = "0", persistent = false)
public void dailyCleanup() {
    // Cleanup task
}
```

What does `persistent = false` mean?

- a) The timer doesn't survive server restarts
- b) The timer cannot be cancelled
- c) The timer runs only once
- d) The timer doesn't write to database

---

### Question 49

What is the maximum number of Singleton EJB instances per application?

- a) Unlimited
- b) One per JVM
- c) One per application
- d) Configurable

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
- b) The bean exposes a no-interface view
- c) The bean stores local data
- d) The bean is not distributed

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

- a) After the servlet
- b) Before and after the servlet
- c) Only on errors
- d) At server startup

---

### Question 52

Which listener interface is used to track session creation and destruction?

- a) `ServletContextListener`
- b) `HttpSessionListener`
- c) `ServletRequestListener`
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

- a) 1MB
- b) 5MB
- c) 10MB
- d) Unlimited

---

### Question 54

What method is used to include another resource's output in the response?

- a) `RequestDispatcher.forward()`
- b) `RequestDispatcher.include()`
- c) `response.include()`
- d) `request.redirect()`

---

### Question 55

```java
HttpSession session = request.getSession(false);
```

What does the `false` parameter mean?

- a) Create a new session
- b) Don't create a new session if one doesn't exist
- c) Invalidate the session
- d) Make the session read-only

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

- a) `<h:table>`
- b) `<h:dataTable>`
- c) `<h:grid>`
- d) `<h:list>`

---

### Question 57

Which phase of the JSF lifecycle updates the model with user input values?

- a) Apply Request Values
- b) Process Validations
- c) Update Model Values
- d) Invoke Application

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

- a) Bean Validation
- b) Custom validator method
- c) Built-in validation
- d) Client-side validation

---

### Question 59

What is the purpose of `<ui:composition>` in Facelets?

- a) To compose music
- b) To define a template composition
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

- a) Execute the entire form
- b) Execute JavaScript
- c) Submit form data for the entire form
- d) Validate the form

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

- a) JAX-RS with EJB
- b) JAX-WS with JPA
- c) Servlet with CDI
- d) JSF with EJB

---

### Question 62

Which annotation enables CORS (Cross-Origin Resource Sharing) in JAX-RS?

- a) `@CORS`
- b) `@CrossOrigin`
- c) Custom filter with `@Provider`
- d) `@AllowOrigin`

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

- a) Container-Managed Transaction (CMT)
- b) Bean-Managed Transaction (BMT)
- c) Automatic transaction management
- d) Distributed transaction management

---

### Question 64

Which deployment descriptor is used to configure servlet mappings?

- a) `application.xml`
- b) `web.xml`
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

- a) A WebSocket server endpoint
- b) A WebSocket client endpoint
- c) A REST client
- d) A JMS consumer

---

### Question 66

What is the purpose of the `META-INF/beans.xml` file?

- a) To define EJBs
- b) To enable CDI in the module
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

- a) To stream data to the client efficiently
- b) To log output
- c) To compress data
- d) To cache responses

---

### Question 68

Which archive type contains both web and EJB modules?

- a) WAR
- b) JAR
- c) EAR
- d) RAR

---

### Question 69

```java
@Schedule(hour = "*", minute = "*/15", persistent = true)
public void runEvery15Minutes() {
    // Task
}
```

How often does this timer execute?

- a) Every 15 hours
- b) Every 15 minutes
- c) 15 times per hour
- d) Once every 15 days

---

### Question 70

What is the standard port for HTTP connections in Java EE application servers?

- a) 8080
- b) 80
- c) 8443
- d) It varies by server implementation

---

## Answer Key

### CDI (1-10)

1. c) Request scope
2. b) `@Inject`
3. b) CreditCardProcessor
4. a) To produce factory methods for bean creation
5. b) `${appVersion}`
6. c) `@ConversationScoped`
7. b) Add it to beans.xml
8. b) To disambiguate between multiple implementations of the same type
9. b) After dependency injection is complete
10. c) `@Observes` (for observer) and `Event<T>` (for firing)

### SOAP (11-20)

11. b) Only add
12. b) XML over various protocols (HTTP, SMTP, JMS, etc.)
13. b) `wsimport`
14. a) Web Service Description Language
15. b) Automatic transaction management and pooling
16. d) All of the above
17. b) Message Transmission Optimization Mechanism for efficient binary data transfer
18. b) Injects a web service client proxy
19. c) Document/literal wrapped
20. b) To intercept and modify SOAP messages

### Bean Validation (21-25)

21. a) `@Email`
22. b) Using `ValidatorFactory` to get a `Validator` and call `validate()`
23. b) Cascades validation to the Customer object's fields
24. c) `@Min` and `@Max`
25. b) A `ConstraintViolationException` is thrown

### JSON-P (26-30)

26. b) A JSON object
27. b) JSON-P (Java API for JSON Processing)
28. b) Object Model API
29. b) `Json.createArrayBuilder()`
30. b) Streaming API

### Advanced JPA (31-40)

31. b) `em.createNamedQuery("Product.findByCategory")`
32. b) To control which attributes are fetched in a query
33. b) To convert between Java attribute types and database column types
34. b) `LockModeType.PESSIMISTIC_WRITE`
35. c) Criteria API
36. b) It's no longer managed by the EntityManager
37. b) Deletes OrderItem entities when removed from the collection
38. b) `PersistenceContextType.TRANSACTION`
39. b) A database index on category and name columns
40. b) `persist()` is for new entities, `merge()` is for detached entities

### Advanced EJB (41-50)

41. b) The bean manages concurrency using Java synchronization
42. b) `Future<T>`
43. b) Interval timer
44. b) Prevent interceptors from being applied to this method
45. b) To specify initialization order
46. b) Topic
47. c) `NOT_SUPPORTED`
48. a) The timer doesn't survive server restarts
49. c) One per application
50. b) The bean exposes a no-interface view

### Advanced Servlets (51-55)

51. b) Before and after the servlet
52. b) `HttpSessionListener`
53. b) 5MB
54. b) `RequestDispatcher.include()`
55. b) Don't create a new session if one doesn't exist

### Advanced JSF (56-60)

56. b) `<h:dataTable>`
57. c) Update Model Values
58. b) Custom validator method
59. b) To define a template composition
60. c) Submit form data for the entire form

### Integration & Advanced Topics (61-70)

61. a) JAX-RS with EJB
62. c) Custom filter with `@Provider`
63. b) Bean-Managed Transaction (BMT)
64. b) `web.xml`
65. b) A WebSocket client endpoint
66. b) To enable CDI in the module
67. a) To stream data to the client efficiently
68. c) EAR
69. b) Every 15 minutes
70. d) It varies by server implementation

---

**Scoring Guide:**

- 66-70 correct: Excellent! Ready for the exam
- 56-65 correct: Good! Review topics where you made mistakes
- 46-55 correct: Pass, but more study recommended
- Below 46: More preparation needed

**Topics covered in this quiz:**

- CDI (Contexts and Dependency Injection)
- SOAP Web Services with JAX-WS
- Bean Validation
- JSON Processing (JSON-P)
- Advanced JPA topics
- Advanced EJB features
- Advanced Servlet concepts
- Advanced JSF features
- Integration patterns and advanced topics
