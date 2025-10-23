# Oracle Certified Expert, Java EE 7 Application Developer (1Z0-900)

## Exam Simulation Quiz 1 of 3

**Instructions:**

- This quiz contains 70 multiple-choice questions covering all Java EE 7 topics
- Each question has 4 options (a, b, c, d)
- Some questions may have multiple correct answers
- Time limit: 150 minutes (simulating real exam conditions)
- Passing score: 66% (46 correct answers out of 70)

---

## JPA - Java Persistence API (Questions 1-10)

### Question 1

What is the correct annotation to mark a field as the primary key in a JPA entity?

- a) `@Key`
- b) `@Identifier`
- c) `@Id`
- d) `@PrimaryKey`

---

### Question 2

Which fetch type should be used for a `@OneToMany` relationship to avoid loading unnecessary data?

- a) `FetchType.IMMEDIATE`
- b) `FetchType.AUTO`
- c) `FetchType.EAGER`
- d) `FetchType.LAZY`

---

### Question 3

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private int version;
    
    private String name;
    private BigDecimal price;
}
```

What is the purpose of the `@Version` annotation in this entity?

- a) To create a version history table
- b) To enable versioning of the database schema
- c) To track the JPA specification version
- d) To implement optimistic locking for concurrency control

---

### Question 4

Which method of EntityManager is used to merge a detached entity back into the persistence context?

- a) `merge()`
- b) `persist()`
- c) `attach()`
- d) `refresh()`

---

### Question 5

```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "order")
    private List<OrderItem> items;
}
```

What happens when you call `entityManager.persist(order)` with this configuration?

- a) The items are persisted but the Order is not
- b) An exception is thrown because items must be persisted separately
- c) Only the Order entity is persisted
- d) The Order and all associated OrderItem entities are persisted

---

### Question 6

In which state is an entity after calling `entityManager.remove(entity)`?

- a) New
- b) Detached
- c) Removed
- d) Managed

---

### Question 7

Which JPQL query syntax is correct for selecting all products with a price greater than 100?

- a) `SELECT p FROM Product WHERE p.price > 100`
- b) `SELECT * FROM Product WHERE price > 100`
- c) `SELECT p FROM Product p WHERE p.price > 100`
- d) `SELECT p FROM Product p WHERE price > 100`

---

### Question 8

What is the correct way to define a named query in JPA?

- a) Using `@EntityQuery` annotation
- b) Defining it in the `web.xml` file
- c) Using `@Query` annotation on the entity class
- d) Using `@NamedQuery` annotation on the entity class

---

### Question 9

```java
@Entity
public class Employee {
    @Id
    private Long id;
    
    @Temporal(TemporalType.DATE)
    private Date birthDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date hireDate;
}
```

What is the purpose of the `@Temporal` annotation?

- a) To track entity modification timestamps
- b) To set expiration times for entities
- c) To specify how temporal (date/time) types should be persisted to the database
- d) To enable time-based caching

---

### Question 10

Which transaction attribute in EJB causes a transaction to be suspended if one exists?

- a) `MANDATORY`
- b) `REQUIRED`
- c) `NOT_SUPPORTED`
- d) `REQUIRES_NEW`

---

## EJB - Enterprise JavaBeans (Questions 11-20)

### Question 11

```java
@Stateless
public class CalculatorService {
    private int counter = 0;
    
    public int increment() {
        return ++counter;
    }
}
```

What will happen if two different clients call `increment()` on this bean?

- a) The container will serialize the calls automatically
- b) A concurrency exception will be thrown
- c) Both clients might get the same value (e.g., both get 1)
- d) Both clients will always get different values

---

### Question 12

Which type of EJB maintains conversational state with a specific client?

- a) Message-Driven Bean
- b) Singleton Session Bean
- c) Stateful Session Bean
- d) Stateless Session Bean

---

### Question 13

```java
@Stateful
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    @Remove
    public void checkout() {
        // Process checkout
    }
}
```

What is the effect of the `@Remove` annotation on the `checkout()` method?

- a) It deletes data from the database
- b) It has no effect; it's just documentation
- c) It removes items from the cart
- d) It causes the bean instance to be removed after the method completes

---

### Question 14

Which annotation is used to configure transaction attributes in EJB?

- a) `@TxAttribute`
- b) `@Transactional`
- c) `@TransactionAttribute`
- d) `@Transaction`

---

### Question 15

What is the default transaction attribute for EJB methods if not explicitly specified?

- a) `NEVER`
- b) `REQUIRED`
- c) `NOT_SUPPORTED`
- d) `REQUIRES_NEW`

---

### Question 16

```java
@Singleton
@Startup
@Lock(LockType.READ)
public class ConfigService {
    private Map<String, String> config = new HashMap<>();
    
    public String getProperty(String key) {
        return config.get(key);
    }
    
    @Lock(LockType.WRITE)
    public void setProperty(String key, String value) {
        config.put(key, value);
    }
}
```

What is the purpose of `@Lock` annotations in this Singleton bean?

- a) To implement pessimistic locking
- b) To prevent memory leaks
- c) To enable database locking
- d) To control concurrent access to bean methods

---

### Question 17

Which annotation makes an EJB method execute asynchronously?

- a) `@Background`
- b) `@AsyncMethod`
- c) `@Asynchronous`
- d) `@Async`

---

### Question 18

What type of EJB should be used to process JMS messages?

- a) Singleton Session Bean
- b) Message-Driven Bean
- c) Stateful Session Bean
- d) Stateless Session Bean

---

### Question 19

```java
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LogService {
    @PersistenceContext
    private EntityManager em;
    
    public void log(String message) {
        LogEntry entry = new LogEntry(message);
        em.persist(entry);
    }
}
```

Why might you use `REQUIRES_NEW` for a logging service?

- a) It's required for all Stateless beans
- b) To improve performance
- c) To ensure logs are saved even if the calling transaction rolls back
- d) To enable parallel processing

---

### Question 20

Which lifecycle callback is called when an EJB instance is created but before the first business method?

- a) `@OnCreate`
- b) `@PostConstruct`
- c) `@PreConstruct`
- d) `@PreInit`

---

## Servlets (Questions 21-25)

### Question 21

```java
@WebServlet(urlPatterns = {"/api/users/*"}, loadOnStartup = 1)
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Implementation
    }
}
```

What does `loadOnStartup = 1` indicate?

- a) The servlet will restart automatically if it crashes
- b) The servlet can handle only one request at a time
- c) The servlet will be loaded and initialized when the application starts
- d) The servlet will be loaded when the first request arrives

---

### Question 22

Which method is called by the servlet container to initialize a servlet?

- a) `initialize()`
- b) `doGet()`
- c) `init()`
- d) `service()`

---

### Question 23

What is the correct way to forward a request from one servlet to another?

- a) `RequestDispatcher.forward(request, response, "/other")`
- b) `request.getRequestDispatcher("/other").forward(request, response)`
- c) `request.forward("/other")`
- d) `response.forward("/other")`

---

### Question 24

```java
@WebServlet("/async")
@WebServlet(asyncSupported = true)
public class AsyncServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            // Long running task
            try {
                Thread.sleep(5000);
                asyncContext.getResponse().getWriter().write("Done");
                asyncContext.complete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
```

What is the benefit of using asynchronous processing in this servlet?

- a) It prevents timeout exceptions
- b) It enables multi-threading automatically
- c) It frees up the request thread to handle other requests
- d) It makes the code run faster

---

### Question 25

Which scope stores attributes that are shared across all users of the application?

- a) Page scope
- b) Session scope
- c) Request scope
- d) Application scope (ServletContext)

---

## JSP - JavaServer Pages (Questions 26-30)

### Question 26

Which EL expression correctly accesses a request parameter named "id"?

- a) `${requestScope.id}`
- b) `${request.getParameter("id")}`
- c) `${param.id}`
- d) `${request.id}`

---

### Question 27

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:forEach var="product" items="${products}">
    <p>${product.name} - ${product.price}</p>
</c:forEach>
```

What does this JSTL code do?

- a) Sorts products by name
- b) Creates a new collection of products
- c) Filters products by price
- d) Iterates over a collection of products and displays their name and price

---

### Question 28

Which JSP directive is used to include another JSP file at translation time?

- a) `<jsp:forward>`
- b) `<%@ include %>`
- c) `<jsp:include>`
- d) `<c:include>`

---

### Question 29

What is the purpose of the `<%@ page errorPage="error.jsp" %>` directive?

- a) To log errors to error.jsp
- b) To prevent errors from occurring
- c) To specify which page to display if an exception occurs
- d) To redirect all errors to error.jsp

---

### Question 30

Which implicit object in JSP represents the ServletConfig?

- a) `request`
- b) `pageContext`
- c) `config`
- d) `application`

---

## JAX-RS REST Services (Questions 31-40)

### Question 31

```java
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {
    
    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        Product product = findProduct(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }
}
```

What HTTP status code is returned when a product is not found?

- a) 500
- b) 404
- c) 400
- d) 204

---

### Question 32

Which annotation is used to configure the base URI path for all REST resources in an application?

- a) `@ApplicationPath`
- b) `@BasePath`
- c) `@RootPath`
- d) `@RestApplication`

---

### Question 33

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createProduct(Product product) {
    productService.save(product);
    return Response.status(Response.Status.CREATED)
                   .entity(product)
                   .build();
}
```

What HTTP status code indicates successful resource creation?

- a) 202 Accepted
- b) 201 Created
- c) 200 OK
- d) 204 No Content

---

### Question 34

Which annotation injects a query parameter into a JAX-RS method?

- a) `@RequestParam`
- b) `@QueryParam`
- c) `@Param`
- d) `@Query`

---

### Question 35

What is the purpose of `@Produces` annotation in JAX-RS?

- a) To specify the HTTP method
- b) To enable production mode
- c) To create new resources
- d) To specify the media type the method can produce

---

### Question 36

```java
@GET
@Path("/search")
public List<Product> searchProducts(
    @QueryParam("name") String name,
    @QueryParam("minPrice") @DefaultValue("0") Double minPrice) {
    return productService.search(name, minPrice);
}
```

What happens if the `minPrice` query parameter is not provided in the request?

- a) An exception is thrown
- b) The value is 0.0
- c) The method is not called
- d) The value is null

---

### Question 37

Which HTTP method is idempotent and should be used for updating resources?

- a) POST
- b) PUT
- c) PATCH
- d) GET

---

### Question 38

```java
@Provider
public class CustomExceptionMapper implements ExceptionMapper<CustomException> {
    @Override
    public Response toResponse(CustomException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(exception.getMessage())
                       .build();
    }
}
```

What is the purpose of this `ExceptionMapper`?

- a) To log exceptions
- b) To prevent exceptions from occurring
- c) To validate input data
- d) To map exceptions to HTTP responses

---

### Question 39

Which class is used to build HTTP responses in JAX-RS?

- a) `Response`
- b) `HttpResponse`
- c) `RestResponse`
- d) `ResponseBuilder`

---

### Question 40

What does the `@HeaderParam` annotation do in JAX-RS?

- a) Sets response headers
- b) Injects HTTP header values into method parameters
- c) Validates header values
- d) Removes headers from the response

---

## WebSocket (Questions 41-45)

### Question 41

```java
@ServerEndpoint("/chat")
public class ChatEndpoint {
    
    @OnOpen
    public void onOpen(Session session) {
        // Handle connection opened
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle received message
    }
}
```

What annotation marks a class as a WebSocket server endpoint?

- a) `@WebSocket`
- b) `@ServerEndpoint`
- c) `@Endpoint`
- d) `@SocketServer`

---

### Question 42

Which WebSocket lifecycle annotation is called when a message is received?

- a) `@OnReceive`
- b) `@OnMessage`
- c) `@OnData`
- d) `@MessageReceived`

---

### Question 43

How do you send a message to a specific WebSocket client?

- a) `session.send(message)`
- b) `session.getBasicRemote().sendText(message)`
- c) `session.write(message)`
- d) `session.sendMessage(message)`

---

### Question 44

```java
@OnClose
public void onClose(Session session, CloseReason reason) {
    System.out.println("Connection closed: " + reason);
}
```

When is the `@OnClose` method called?

- a) When a WebSocket connection is closed
- b) When the server shuts down
- c) When a message fails to send
- d) When the session times out

---

### Question 45

What is the difference between WebSocket and traditional HTTP?

- a) HTTP is more secure
- b) WebSocket is faster
- c) WebSocket provides full-duplex communication over a single connection
- d) WebSocket only works with JSON

---

## JSF - JavaServer Faces (Questions 46-50)

### Question 46

```java
@Named
@ViewScoped
public class ProductBean implements Serializable {
    private Product product;
    
    public String save() {
        productService.save(product);
        return "products?faces-redirect=true";
    }
}
```

What does `faces-redirect=true` do in the navigation string?

- a) Enables AJAX navigation
- b) Forces a client-side redirect
- c) Disables navigation
- d) Creates a bookmark

---

### Question 47

Which JSF lifecycle phase validates user input?

- a) Apply Request Values
- b) Process Validations
- c) Update Model Values
- d) Invoke Application

---

### Question 48

What is the purpose of the `immediate="true"` attribute on a JSF component?

- a) Processes the component in the Apply Request Values phase instead of later phases
- b) Makes the component render faster
- c) Enables AJAX for the component
- d) Makes the component required

---

### Question 49

```xhtml
<h:form>
    <h:inputText value="#{userBean.name}" />
    <h:commandButton value="Submit" action="#{userBean.submit}" />
    <f:ajax execute="@form" render="@form" />
</h:form>
```

What does the `<f:ajax>` tag enable?

- a) Form validation
- b) Asynchronous partial page updates
- c) Synchronous form submission
- d) Security features

---

### Question 50

Which annotation is used to create a CDI-managed JSF backing bean?

- a) `@Bean`
- b) `@BackingBean`
- c) `@Named`
- d) `@ManagedBean`

---

## JMS - Java Message Service (Questions 51-55)

### Question 51

```java
@Resource(lookup = "java:/jms/queue/OrderQueue")
private Queue orderQueue;

@Inject
private JMSContext context;

public void sendOrder(Order order) {
    context.createProducer().send(orderQueue, order);
}
```

What is the simplified API feature demonstrated here?

- a) Using @Resource for queue injection
- b) Using JMSContext instead of creating Connection and Session manually
- c) Automatic serialization of the Order object
- d) Transaction management

---

### Question 52

Which messaging model ensures that only one consumer receives each message?

- a) Topic
- b) Point-to-Point (Queue)
- c) Publish-Subscribe
- d) Broadcast

---

### Question 53

```java
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", 
                             propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", 
                             propertyValue = "java:/jms/queue/OrderQueue")
})
public class OrderProcessor implements MessageListener {
    @Override
    public void onMessage(Message message) {
        // Process message
    }
}
```

What type of bean is used to consume JMS messages asynchronously?

- a) Stateless Session Bean
- b) Message-Driven Bean
- c) Singleton Bean
- d) Stateful Session Bean

---

### Question 54

In JMS 2.0, which class replaces the need for creating Connection, Session, and MessageProducer separately?

- a) `JMSContext`
- b) `JMSProducer`
- c) `JMSHelper`
- d) `JMSSimple`

---

### Question 55

What is the difference between durable and non-durable subscriptions in JMS?

- a) Durable subscriptions are faster
- b) Non-durable subscriptions are more reliable
- c) There is no difference
- d) Durable subscriptions retain messages for disconnected subscribers

---

## Security (Questions 56-60)

### Question 56

```java
@Stateless
@RolesAllowed({"ADMIN", "MANAGER"})
public class ReportService {
    
    @PermitAll
    public List<Report> getPublicReports() {
        return reportDAO.findPublic();
    }
    
    @RolesAllowed("ADMIN")
    public void deleteReport(Long id) {
        reportDAO.delete(id);
    }
}
```

Who can execute the `getPublicReports()` method?

- a) Only ADMIN and MANAGER
- b) Everyone (authenticated or not)
- c) Only ADMIN
- d) Only authenticated users

---

### Question 57

Which annotation restricts access to a resource based on user roles?

- a) `@RolesAllowed`
- b) `@Secured`
- c) `@SecurityRole`
- d) `@Authorize`

---

### Question 58

What does `@DenyAll` annotation do?

- a) Logs all access attempts
- b) Prevents any user from accessing the method
- c) Denies all network traffic
- d) Requires authentication

---

### Question 59

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Admin Area</web-resource-name>
        <url-pattern>/admin/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>admin</role-name>
    </auth-constraint>
</security-constraint>
```

What does this `web.xml` configuration do?

- a) Creates an admin user
- b) Restricts access to /admin/* URLs to users with the admin role
- c) Encrypts admin communications
- d) Enables admin logging

---

### Question 60

Which Java EE security mechanism provides programmatic access to security information?

- a) `SecurityManager`
- b) `SecurityContext`
- c) `SecurityHelper`
- d) `SecurityInfo`

---

## Concurrency Utilities (Questions 61-65)

### Question 61

```java
@Resource
private ManagedExecutorService executor;

public void processDataAsync() {
    executor.submit(() -> {
        // Long running task
        processLargeDataset();
    });
}
```

What is the advantage of using `ManagedExecutorService` over standard Java `ExecutorService`?

- a) It's faster
- b) It's required by the specification
- c) It propagates Java EE context (security, transactions) to the spawned threads
- d) It supports more concurrent threads

---

### Question 62

Which resource is used to schedule tasks at fixed intervals in Java EE?

- a) `ManagedExecutorService`
- b) `ManagedScheduledExecutorService`
- c) `ManagedTimerService`
- d) `ScheduledService`

---

### Question 63

What does `ManagedThreadFactory` provide?

- a) Context-aware threads managed by the container
- b) Thread synchronization
- c) Thread monitoring
- d) Automatic thread pooling

---

### Question 64

```java
@Resource
private ManagedScheduledExecutorService scheduler;

public void scheduleTask() {
    scheduler.scheduleAtFixedRate(
        () -> cleanupTempFiles(),
        0, 1, TimeUnit.HOURS
    );
}
```

What does this code do?

- a) Runs cleanupTempFiles() once after 1 hour
- b) Runs cleanupTempFiles() every hour starting immediately
- c) Runs cleanupTempFiles() once immediately
- d) Runs cleanupTempFiles() every hour after a 1-hour delay

---

### Question 65

What is `ContextService` used for in Java EE Concurrency Utilities?

- a) To manage servlet contexts
- b) To configure security contexts
- c) To capture and propagate Java EE context to non-managed threads
- d) To create new contexts

---

## Batch Processing (Questions 66-70)

### Question 66

```xml
<job id="dataImportJob" xmlns="http://xmlns.jcp.org/xml/ns/javaee">
    <step id="importStep">
        <chunk item-count="100">
            <reader ref="fileReader"/>
            <processor ref="dataProcessor"/>
            <writer ref="databaseWriter"/>
        </chunk>
    </step>
</job>
```

What does `item-count="100"` mean in this batch job?

- a) Process exactly 100 items total
- b) Commit the transaction after every 100 items
- c) Skip 100 items
- d) Run 100 parallel threads

---

### Question 67

Which interface must be implemented to create a batch item reader?

- a) `BatchReader`
- b) `ItemReader`
- c) `ChunkReader`
- d) `DataReader`

---

### Question 68

What is a `batchlet` in Java Batch API?

- a) A type of item processor
- b) A task-oriented step that doesn't use chunk processing
- c) A batch configuration file
- d) A small batch job

---

### Question 69

```java
@Named
public class DataProcessor implements ItemProcessor {
    @Override
    public Object processItem(Object item) throws Exception {
        Record record = (Record) item;
        // Transform record
        return transformedRecord;
    }
}
```

What is the role of an `ItemProcessor` in batch processing?

- a) To write items to a destination
- b) To transform/process items between reading and writing
- c) To manage transactions
- d) To read items from a source

---

### Question 70

How do you start a batch job programmatically?

- a) Using `@Scheduled` annotation
- b) Using `BatchRuntime.getJobOperator().start(jobXML, parameters)`
- c) Using `JobOperator.start()`
- d) Batch jobs start automatically
