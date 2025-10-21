# Oracle Certified Expert, Java EE 7 Application Developer (1Z0-900)

# Exam Simulation Quiz 3 of 3

**Instructions:**

- This quiz contains 70 multiple-choice questions covering all Java EE 7 topics
- Each question has 4 options (a, b, c, d)
- Some questions may have multiple correct answers
- Time limit: 150 minutes (simulating real exam conditions)
- Passing score: 66% (46 correct answers out of 70)

---

## Comprehensive Mixed Topics (Questions 1-70)

### Question 1

What is the primary purpose of the Java EE platform?

- a) To replace Java SE
- b) To provide enterprise-level specifications and APIs for distributed applications
- c) To create desktop applications
- d) To develop mobile applications

---

### Question 2

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
                   generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", 
                      sequenceName = "employee_seq",
                      allocationSize = 1)
    private Long id;
}
```

What does `allocationSize = 1` indicate?

- a) Only one employee can exist
- b) The sequence increments by 1 in the database
- c) JPA allocates IDs one at a time
- d) The cache size is 1

---

### Question 3

Which HTTP method is idempotent and safe?

- a) POST
- b) GET
- c) PUT
- d) DELETE

---

### Question 4

```java
@Stateless
@LocalBean
public class InventoryService {
    
    @PersistenceContext
    private EntityManager em;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recordLowStockAlert(Long productId) {
        Alert alert = new Alert(productId, "Low stock");
        em.persist(alert);
    }
}
```

Why use `REQUIRES_NEW` for logging/alerting operations?

- a) For better performance
- b) To ensure the alert is saved even if the parent transaction rolls back
- c) It's mandatory for all Stateless beans
- d) To enable parallel processing

---

### Question 5

Which annotation marks a method to be executed before an entity is persisted?

- a) `@BeforePersist`
- b) `@PrePersist`
- c) `@OnPersist`
- d) `@PreCreate`

---

### Question 6

```java
@Path("/api")
@ApplicationScoped
public class ApiResource {
    
    @Context
    private UriInfo uriInfo;
    
    @GET
    @Path("/current-path")
    public String getCurrentPath() {
        return uriInfo.getPath();
    }
}
```

What does the `@Context` annotation do?

- a) Creates a new context
- b) Injects JAX-RS context information
- c) Defines a CDI context
- d) Enables caching

---

### Question 7

What is the correct way to define a bidirectional `@OneToMany` relationship?

- a) Use `@OneToMany` on both sides
- b) Use `@OneToMany` on one side and `@ManyToOne` with `@JoinColumn` on the other
- c) Use `@OneToMany` with `mappedBy` on the owning side
- d) Bidirectional relationships are not supported

---

### Question 8

```java
@ServerEndpoint(value = "/notifications",
                encoders = {NotificationEncoder.class},
                decoders = {NotificationDecoder.class})
public class NotificationEndpoint {
    
    @OnMessage
    public void handleMessage(Notification notification, Session session) {
        // Process notification
    }
}
```

What is the purpose of encoders and decoders in WebSocket?

- a) To encrypt and decrypt messages
- b) To convert between Java objects and WebSocket messages
- c) To compress messages
- d) To validate messages

---

### Question 9

Which scope in CDI ensures a bean lives for the entire application lifecycle?

- a) `@Dependent`
- b) `@ApplicationScoped`
- c) `@Singleton`
- d) `@SessionScoped`

---

### Question 10

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Vehicle {
    @Id
    private Long id;
    private String manufacturer;
}

@Entity
public class Car extends Vehicle {
    private int numberOfDoors;
}
```

What does `InheritanceType.JOINED` mean?

- a) All classes share one table
- b) Each class has its own table joined by foreign keys
- c) Only concrete classes have tables
- d) Inheritance is not persisted

---

### Question 11

Which JAX-RS annotation handles HTTP PUT requests?

- a) `@POST`
- b) `@UPDATE`
- c) `@PUT`
- d) `@MODIFY`

---

### Question 12

```java
@Singleton
@Startup
public class CacheService {
    
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        // Load cache
    }
}
```

What does `@Startup` do?

- a) Makes the bean start faster
- b) Initializes the bean eagerly at application startup
- c) Enables startup listeners
- d) Makes the bean restart automatically

---

### Question 13

In JPA, which relationship owns the foreign key?

- a) Always the `@OneToMany` side
- b) The side with `mappedBy`
- c) Always the `@ManyToOne` side
- d) The side with `@JoinColumn`

---

### Question 14

```java
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    
    @Inject
    private ProductService productService;
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        List<Product> products = productService.findAll();
        req.setAttribute("products", products);
        req.getRequestDispatcher("/WEB-INF/products.jsp").forward(req, resp);
    }
}
```

What pattern is demonstrated here?

- a) DAO pattern
- b) MVC pattern
- c) Factory pattern
- d) Singleton pattern

---

### Question 15

Which transaction attribute throws an exception if no transaction exists?

- a) `REQUIRED`
- b) `MANDATORY`
- c) `REQUIRES_NEW`
- d) `NEVER`

---

### Question 16

```java
@MessageDriven(mappedName = "jms/OrderQueue")
public class OrderProcessor implements MessageListener {
    
    @Inject
    private OrderService orderService;
    
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String orderId = textMessage.getText();
            orderService.processOrder(Long.parseLong(orderId));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
```

What type of message consumption is this?

- a) Synchronous
- b) Asynchronous
- c) Polling
- d) Request-Response

---

### Question 17

Which annotation enables method-level security in EJB?

- a) `@Secured`
- b) `@RolesAllowed`
- c) `@Security`
- d) `@Protected`

---

### Question 18

```java
@Named
@ViewScoped
public class UserFormBean implements Serializable {
    private User user = new User();
    
    public String save() {
        userService.save(user);
        return "users?faces-redirect=true";
    }
}
```

Why is `Serializable` implemented for `@ViewScoped` beans?

- a) For database persistence
- b) For session replication and passivation
- c) It's optional
- d) For XML serialization

---

### Question 19

What does JPQL stand for?

- a) Java Persistence Query Language
- b) Java Persistent Query Library
- c) JPA Query Language
- d) Java Processing Query Language

---

### Question 20

```java
@Stateless
public class NotificationService {
    
    @Resource
    private ManagedExecutorService executor;
    
    public void sendNotifications(List<User> users) {
        for (User user : users) {
            executor.submit(() -> sendEmail(user));
        }
    }
}
```

What is the advantage of using `ManagedExecutorService`?

- a) Faster execution
- b) Context propagation (security, transactions) to async tasks
- c) Unlimited threads
- d) Automatic error handling

---

### Question 21

Which HTTP status code indicates a successful POST that created a resource?

- a) 200 OK
- b) 201 Created
- c) 204 No Content
- d) 202 Accepted

---

### Question 22

```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @ElementCollection
    @CollectionTable(name = "order_notes")
    private List<String> notes;
}
```

What does `@ElementCollection` do?

- a) Defines a collection of entities
- b) Defines a collection of basic types or embeddables
- c) Creates a many-to-many relationship
- d) Enables lazy loading

---

### Question 23

Which JSF phase converts submitted string values to component types?

- a) Apply Request Values
- b) Process Validations
- c) Update Model Values
- d) Restore View

---

### Question 24

```java
@Path("/products")
public class ProductResource {
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Product> getProducts() {
        return productService.findAll();
    }
}
```

How is the response format determined?

- a) Always JSON
- b) Based on the `Accept` header in the request
- c) Always XML
- d) Randomly selected

---

### Question 25

What is the purpose of `persistence.xml`?

- a) To define servlets
- b) To configure JPA persistence units
- c) To map REST endpoints
- d) To define security roles

---

### Question 26

```java
@WebFilter(filterName = "AuthFilter", 
           urlPatterns = {"/admin/*"},
           dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class AuthenticationFilter implements Filter {
    // Implementation
}
```

When is this filter invoked?

- a) Only on direct requests
- b) On requests and forwards to /admin/*
- c) On all requests
- d) Only on errors

---

### Question 27

Which annotation makes an EJB timer persistent across server restarts?

- a) `@Persistent`
- b) `@Schedule(persistent = true)`
- c) `@PersistentTimer`
- d) `@DurableTimer`

---

### Question 28

```java
public interface Repository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    void save(T entity);
    void delete(T entity);
}

@Stateless
public class ProductRepository implements Repository<Product, Long> {
    @PersistenceContext
    private EntityManager em;
    // Implementation
}
```

What design pattern is this?

- a) Factory pattern
- b) Repository pattern
- c) Observer pattern
- d) Strategy pattern

---

### Question 29

Which annotation is used to inject a message producer in JMS 2.0?

- a) `@Inject` with `JMSContext`
- b) `@Resource` with `MessageProducer`
- c) `@ProducerInject`
- d) `@JMSProducer`

---

### Question 30

```java
@Entity
@NamedEntityGraph(
    name = "Product.withCategory",
    attributeNodes = @NamedAttributeNode("category")
)
public class Product {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
}
```

What is the purpose of `@NamedEntityGraph`?

- a) To create database indexes
- b) To control fetch strategies and avoid N+1 queries
- c) To define relationships
- d) To enable caching

---

### Question 31

Which method in `EntityManager` merges a detached entity back into the persistence context?

- a) `persist()`
- b) `refresh()`
- c) `merge()`
- d) `synchronize()`

---

### Question 32

```java
@ApplicationPath("/api")
public class RestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProductResource.class);
        classes.add(UserResource.class);
        return classes;
    }
}
```

What is the base URL for REST resources?

- a) /
- b) /api
- c) /rest
- d) /resources

---

### Question 33

Which lifecycle callback is called when a Stateful bean is passivated?

- a) `@PreDestroy`
- b) `@PrePassivate`
- c) `@BeforePassivate`
- d) `@OnPassivate`

---

### Question 34

```java
@Converter(autoApply = true)
public class BooleanToYNConverter 
        implements AttributeConverter<Boolean, String> {
    
    public String convertToDatabaseColumn(Boolean value) {
        return (value != null && value) ? "Y" : "N";
    }
    
    public Boolean convertToEntityAttribute(String value) {
        return "Y".equals(value);
    }
}
```

What does `autoApply = true` do?

- a) Applies the converter to all Boolean attributes automatically
- b) Enables auto-conversion
- c) Makes the converter mandatory
- d) Optimizes performance

---

### Question 35

Which WebSocket annotation handles the opening of a new connection?

- a) `@OnConnect`
- b) `@OnError`
- c) `@OnOpen`
- d) `@OnStart`

---

### Question 36

```java
@Stateless
public class AuditService {
    
    @Resource
    private SessionContext context;
    
    public void logAccess(String resource) {
        String user = context.getCallerPrincipal().getName();
        System.out.println(user + " accessed " + resource);
    }
}
```

What does `SessionContext` provide access to?

- a) HTTP session
- b) EJB runtime context and caller information
- c) Database session
- d) User session data

---

### Question 37

Which annotation creates a producer method in CDI?

- a) `@Factory`
- b) `@Produces`
- c) `@Provider`
- d) `@Create`

---

### Question 38

```xml
<web-app>
    <context-param>
        <param-name>javax.faces.PROJECT_STAGE</param-name>
        <param-value>Development</param-value>
    </context-param>
</web-app>
```

What does setting JSF PROJECT_STAGE to Development do?

- a) Enables production optimizations
- b) Enables detailed error messages and debugging features
- c) Disables the application
- d) Nothing, it's just documentation

---

### Question 39

Which JPA annotation defines a composite primary key?

- a) `@CompositeId`
- b) `@IdClass` or `@EmbeddedId`
- c) `@MultipleId`
- d) `@CompoundKey`

---

### Question 40

```java
@Path("/secure")
public class SecureResource {
    
    @Context
    private SecurityContext securityContext;
    
    @GET
    public Response getData() {
        if (securityContext.isUserInRole("ADMIN")) {
            return Response.ok("Admin data").build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
```

What method checks if the current user has a specific role?

- a) `hasRole()`
- b) `checkRole()`
- c) `isUserInRole()`
- d) `validateRole()`

---

### Question 41

Which transaction attribute executes without a transaction?

- a) `NEVER`
- b) `NOT_SUPPORTED`
- c) `SUPPORTS`
- d) Both a and b

---

### Question 42

```java
@Entity
@Table(name = "products",
       uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
public class Product {
    @Id
    private Long id;
    
    private String code;
}
```

What does this configuration enforce?

- a) The code must be unique in the database
- b) The id must be unique
- c) Both id and code must be unique
- d) Nothing, it's just a hint

---

### Question 43

Which HTTP method completely replaces a resource?

- a) PUT
- b) PATCH
- c) POST
- d) UPDATE

---

### Question 44

```java
@Named
@SessionScoped
public class ShoppingCartBean implements Serializable {
    private List<Item> items = new ArrayList<>();
    
    @Inject
    private Event<CheckoutEvent> checkoutEvent;
    
    public void checkout() {
        checkoutEvent.fire(new CheckoutEvent(items));
    }
}
```

What CDI feature is demonstrated with `Event<T>`?

- a) Dependency injection
- b) Event-driven communication between beans
- c) Transaction management
- d) Scope management

---

### Question 45

Which JPQL keyword is used for sorting results?

- a) `SORT BY`
- b) `ORDER BY`
- c) `ARRANGE BY`
- d) `SEQUENCE BY`

---

### Question 46

```java
@Stateless
@Interceptors(TimingInterceptor.class)
public class BusinessService {
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void fastMethod() {
        // No interceptors
    }
}
```

Which interceptors apply to `fastMethod()`?

- a) All interceptors
- b) Only TimingInterceptor
- c) No interceptors
- d) Only default interceptors

---

### Question 47

What is the purpose of `@Embeddable` in JPA?

- a) To embed files in the database
- b) To create a class that can be embedded in entities
- c) To enable lazy loading
- d) To create entity relationships

---

### Question 48

```java
@Path("/users/{userId}/orders/{orderId}")
public class UserOrderResource {
    
    @GET
    public Order getOrder(@PathParam("userId") Long userId,
                         @PathParam("orderId") Long orderId) {
        return orderService.findOrder(userId, orderId);
    }
}
```

Which annotation extracts path parameters from the URI?

- a) `@UriParam`
- b) `@PathVariable`
- c) `@PathParam`
- d) `@RequestParam`

---

### Question 49

Which bean validation constraint checks if a string is not empty?

- a) `@NotNull`
- b) `@NotEmpty`
- c) `@NotBlank`
- d) Both b and c

---

### Question 50

```java
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class ConfigService {
    
    @Lock(LockType.WRITE)
    public void updateConfig(String key, String value) {
        // Update configuration
    }
}
```

What is the default lock type for methods without `@Lock` annotation?

- a) READ
- b) WRITE
- c) NONE
- d) OPTIMISTIC

---

### Question 51

Which JSP directive includes content at translation time?

- a) `<%@ include %>`
- b) `<jsp:include>`
- c) `<c:include>`
- d) `<jsp:directive.include>`

---

### Question 52

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    
    public void filter(ContainerRequestContext requestContext) {
        // Authentication logic
    }
}
```

What does `@Priority` determine in JAX-RS filters?

- a) The importance of the filter
- b) The execution order of filters
- c) The priority in thread scheduling
- d) The caching priority

---

### Question 53

Which annotation marks a class as a JAX-RS provider?

- a) `@Component`
- b) `@Provider`
- c) `@Resource`
- d) `@Service`

---

### Question 54

```java
@Entity
public class Book {
    @Id
    private Long id;
    
    @Lob
    private String description;
    
    @Lob
    private byte[] coverImage;
}
```

What does `@Lob` indicate?

- a) Large Object - suitable for BLOBs and CLOBs
- b) Lazy Object Binding
- c) Local Object Base
- d) Long Object Buffer

---

### Question 55

Which batch API interface writes processed items?

- a) `ItemReader`
- b) `ItemProcessor`
- c) `ItemWriter`
- d) `ItemHandler`

---

### Question 56

```java
@Stateful
public class WizardBean {
    
    @Inject
    private Conversation conversation;
    
    public void startWizard() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }
    
    public void completeWizard() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }
}
```

What is a CDI Conversation?

- a) A chat feature
- b) A long-running interaction spanning multiple requests
- c) A database transaction
- d) A WebSocket connection

---

### Question 57

Which JMS destination type ensures only one consumer receives each message?

- a) Queue
- b) Topic
- c) Channel
- d) Stream

---

### Question 58

```java
@Path("/products")
public class ProductResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl(maxAge = 3600)
    public Response getProducts() {
        List<Product> products = productService.findAll();
        return Response.ok(products).build();
    }
}
```

What does `@CacheControl` do (if available via extension)?

- a) Clears the cache
- b) Sets HTTP cache control headers
- c) Enables server-side caching
- d) Disables caching

---

### Question 59

Which method in `BatchRuntime` is used to start a batch job?

- a) `start()`
- b) `execute()`
- c) `getJobOperator().start()`
- d) `run()`

---

### Question 60

```java
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ComplexService {
    
    @Resource
    private UserTransaction tx;
    
    public void complexOperation() throws Exception {
        tx.begin();
        try {
            // Multiple operations
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
```

What type of transaction management is this?

- a) Container-Managed Transaction (CMT)
- b) Bean-Managed Transaction (BMT)
- c) Automatic transaction
- d) Distributed transaction

---

### Question 61

Which JSF tag enables AJAX for a component?

- a) `<f:ajax>`
- b) `<h:ajax>`
- c) `<a:ajax>`
- d) `<jsf:ajax>`

---

### Question 62

```java
TypedQuery<Product> query = em.createQuery(
    "SELECT p FROM Product p WHERE p.price > :minPrice", 
    Product.class
);
query.setParameter("minPrice", 100.0);
List<Product> products = query.getResultList();
```

What is the advantage of using parameter binding (`:minPrice`)?

- a) Better performance
- b) Prevention of SQL injection
- c) Easier to read
- d) All of the above

---

### Question 63

Which WebSocket method sends a message synchronously?

- a) `session.getBasicRemote().sendText()`
- b) `session.getAsyncRemote().sendText()`
- c) `session.send()`
- d) `session.write()`

---

### Question 64

```java
@Entity
@EntityListeners(AuditListener.class)
public class Product {
    // Fields
}

public class AuditListener {
    @PrePersist
    public void prePersist(Object entity) {
        // Audit logic
    }
}
```

What is an entity listener?

- a) A class that listens for entity lifecycle events
- b) A database trigger
- c) A JMS listener
- d) A CDI observer

---

### Question 65

Which HTTP status code indicates forbidden access (authenticated but not authorized)?

- a) 400
- b) 401
- c) 403
- d) 404

---

### Question 66

```java
@ManagedBean
@CustomScoped("#{viewScope}")
public class LegacyBean {
    // JSF managed bean (pre-CDI)
}
```

What is the modern replacement for `@ManagedBean`?

- a) `@Bean`
- b) `@Named` with CDI
- c) `@Component`
- d) `@Controller`

---

### Question 67

Which concurrency utility provides scheduled task execution?

- a) `ManagedExecutorService`
- b) `ManagedScheduledExecutorService`
- c) `ManagedTimerService`
- d) `ScheduledExecutor`

---

### Question 68

```java
@Entity
public class Customer {
    @Id
    private Long id;
    
    @OneToMany(mappedBy = "customer", 
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}
```

What happens when you call `removeOrder()` with `orphanRemoval = true`?

- a) Nothing
- b) The order is deleted from the database
- c) The order is detached
- d) An exception is thrown

---

### Question 69

Which JSF tag displays all messages for all components?

- a) `<h:errors>`
- b) `<h:messages>`
- c) `<f:messages>`
- d) `<jsf:errors>`

---

### Question 70

```java
@Schedule(dayOfWeek = "Mon-Fri", hour = "9", minute = "0")
public void dailyReport() {
    // Generate report
}
```

When does this timer execute?

- a) Every day at 9:00 AM
- b) Monday through Friday at 9:00 AM
- c) Every hour on weekdays
- d) Once per week

---

## Answer Key

1. b) To provide enterprise-level specifications and APIs for distributed applications
2. c) JPA allocates IDs one at a time
3. b) GET
4. b) To ensure the alert is saved even if the parent transaction rolls back
5. b) `@PrePersist`
6. b) Injects JAX-RS context information
7. c) Use `@OneToMany` with `mappedBy` on the owning side
8. b) To convert between Java objects and WebSocket messages
9. b) `@ApplicationScoped`
10. b) Each class has its own table joined by foreign keys
11. c) `@PUT`
12. b) Initializes the bean eagerly at application startup
13. d) The side with `@JoinColumn`
14. b) MVC pattern
15. b) `MANDATORY`
16. b) Asynchronous
17. b) `@RolesAllowed`
18. b) For session replication and passivation
19. a) Java Persistence Query Language
20. b) Context propagation (security, transactions) to async tasks
21. b) 201 Created
22. b) Defines a collection of basic types or embeddables
23. a) Apply Request Values
24. b) Based on the `Accept` header in the request
25. b) To configure JPA persistence units
26. b) On requests and forwards to /admin/*
27. b) `@Schedule(persistent = true)`
28. b) Repository pattern
29. a) `@Inject` with `JMSContext`
30. b) To control fetch strategies and avoid N+1 queries
31. c) `merge()`
32. b) /api
33. b) `@PrePassivate`
34. a) Applies the converter to all Boolean attributes automatically
35. c) `@OnOpen`
36. b) EJB runtime context and caller information
37. b) `@Produces`
38. b) Enables detailed error messages and debugging features
39. b) `@IdClass` or `@EmbeddedId`
40. c) `isUserInRole()`
41. d) Both a and b
42. a) The code must be unique in the database
43. a) PUT
44. b) Event-driven communication between beans
45. b) `ORDER BY`
46. c) No interceptors
47. b) To create a class that can be embedded in entities
48. c) `@PathParam`
49. d) Both b and c
50. a) READ
51. a) `<%@ include %>`
52. b) The execution order of filters
53. b) `@Provider`
54. a) Large Object - suitable for BLOBs and CLOBs
55. c) `ItemWriter`
56. b) A long-running interaction spanning multiple requests
57. a) Queue
58. b) Sets HTTP cache control headers
59. c) `getJobOperator().start()`
60. b) Bean-Managed Transaction (BMT)
61. a) `<f:ajax>`
62. d) All of the above
63. a) `session.getBasicRemote().sendText()`
64. a) A class that listens for entity lifecycle events
65. c) 403
66. b) `@Named` with CDI
67. b) `ManagedScheduledExecutorService`
68. b) The order is deleted from the database
69. b) `<h:messages>`
70. b) Monday through Friday at 9:00 AM

---

## Scoring Guide

**Score Interpretation:**

- 66-70 correct (94-100%): **Excellent!** You're well-prepared for the exam
- 60-65 correct (86-93%): **Very Good!** Review the topics you missed
- 56-59 correct (80-85%): **Good!** Some additional study recommended
- 46-55 correct (66-79%): **Pass**, but significant review needed
- Below 46 (< 66%): More preparation required before taking the exam

**Topic Distribution:**

- JPA and Database: Questions 2, 5, 7, 10, 13, 19, 22, 25, 30, 31, 34, 39, 42, 45, 47, 54, 62, 64, 68
- EJB: Questions 4, 12, 15, 17, 27, 33, 36, 41, 46, 50, 60
- JAX-RS REST: Questions 3, 6, 11, 21, 24, 32, 40, 43, 48, 52, 53, 58
- WebSocket: Questions 8, 35, 63
- CDI: Questions 9, 37, 44, 56, 66
- JSF: Questions 18, 23, 38, 61, 69
- JMS: Questions 16, 29, 57
- Servlets: Questions 14, 26, 51
- Security: Questions 65
- Concurrency: Questions 20, 67
- Batch: Questions 55, 59
- Timers: Questions 70
- Mixed/General: Questions 1, 28, 49

**Study Recommendations Based on Performance:**

- If you scored low on JPA questions (< 15/19): Review entity mappings, lifecycle, JPQL, and relationships
- If you scored low on EJB questions (< 8/11): Review transaction management, lifecycle, and different bean types
- If you scored low on JAX-RS questions (< 9/12): Review annotations, HTTP methods, and REST principles
- If you scored low on CDI questions: Review scopes, injection, producers, and events
- If you scored low on JSF questions: Review lifecycle, components, and navigation

**Final Exam Tips:**

1. Read each question carefully - watch for keywords like "NOT", "EXCEPT", "ALWAYS"
2. Eliminate obviously wrong answers first
3. Pay attention to code examples - they often contain important details
4. Remember the default behaviors (e.g., FetchType.LAZY for @OneToMany)
5. Know the differences between similar annotations (@Inject vs @Resource, @Named vs @ManagedBean)
6. Understand transaction attributes and their behaviors
7. Know HTTP status codes and their meanings
8. Be familiar with XML configuration vs annotation-based configuration

Good luck with your Oracle 1Z0-900 certification exam!
