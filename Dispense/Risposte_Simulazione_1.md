## Answer Key

### JPA (1-10)

1. c) `@Id`
2. d) `FetchType.LAZY`
3. d) To implement optimistic locking for concurrency control
4. a) `merge()`
5. d) The Order and all associated OrderItem entities are persisted
6. c) Removed
7. c) `SELECT p FROM Product p WHERE p.price > 100`
8. d) Using `@NamedQuery` annotation on the entity class
9. c) To specify how temporal (date/time) types should be persisted to the database
10. c) `NOT_SUPPORTED`

### EJB (11-20)

11. c) Both clients might get the same value (e.g., both get 1)
12. c) Stateful Session Bean
13. d) It causes the bean instance to be removed after the method completes
14. c) `@TransactionAttribute`
15. b) `REQUIRED`
16. d) To control concurrent access to bean methods
17. c) `@Asynchronous`
18. b) Message-Driven Bean
19. c) To ensure logs are saved even if the calling transaction rolls back
20. b) `@PostConstruct`

### Servlets (21-25)

21. c) The servlet will be loaded and initialized when the application starts
22. c) `init()`
23. b) `request.getRequestDispatcher("/other").forward(request, response)`
24. c) It frees up the request thread to handle other requests
25. d) Application scope (ServletContext)

### JSP (26-30)

26. c) `${param.id}`
27. d) Iterates over a collection of products and displays their name and price
28. b) `<%@ include %>`
29. c) To specify which page to display if an exception occurs
30. c) `config`

### JAX-RS (31-40)

31. b) 404
32. a) `@ApplicationPath`
33. b) 201 Created
34. b) `@QueryParam`
35. d) To specify the media type the method can produce
36. b) The value is 0.0
37. b) PUT
38. d) To map exceptions to HTTP responses
39. a) `Response`
40. b) Injects HTTP header values into method parameters

### WebSocket (41-45)

41. b) `@ServerEndpoint`
42. b) `@OnMessage`
43. b) `session.getBasicRemote().sendText(message)`
44. a) When a WebSocket connection is closed
45. c) WebSocket provides full-duplex communication over a single connection

### JSF (46-50)

46. b) Forces a client-side redirect
47. b) Process Validations
48. a) Processes the component in the Apply Request Values phase instead of later phases
49. b) Asynchronous partial page updates
50. c) `@Named`

### JMS (51-55)

51. b) Using JMSContext instead of creating Connection and Session manually
52. b) Point-to-Point (Queue)
53. b) Message-Driven Bean
54. a) `JMSContext`
55. d) Durable subscriptions retain messages for disconnected subscribers

### Security (56-60)

56. b) Everyone (authenticated or not)
57. a) `@RolesAllowed`
58. b) Prevents any user from accessing the method
59. b) Restricts access to /admin/* URLs to users with the admin role
60. b) `SecurityContext`

### Concurrency (61-65)

61. c) It propagates Java EE context (security, transactions) to the spawned threads
62. b) `ManagedScheduledExecutorService`
63. a) Context-aware threads managed by the container
64. b) Runs cleanupTempFiles() every hour starting immediately
65. c) To capture and propagate Java EE context to non-managed threads

### Batch Processing (66-70)

66. b) Commit the transaction after every 100 items
67. b) `ItemReader`
68. b) A task-oriented step that doesn't use chunk processing
69. b) To transform/process items between reading and writing
70. b) Using `BatchRuntime.getJobOperator().start(jobXML, parameters)`

---

**Scoring Guide:**

- 66-70 correct: Excellent! Ready for the exam
- 56-65 correct: Good! Review topics where you made mistakes
- 46-55 correct: Pass, but more study recommended
- Below 46: More preparation needed

**Topics to review based on your performance:**

- If you scored low on questions 1-10: Review JPA concepts
- If you scored low on questions 11-20: Review EJB concepts
- If you scored low on questions 21-30: Review Servlet and JSP concepts
- If you scored low on questions 31-45: Review JAX-RS and WebSocket
- If you scored low on questions 46-55: Review JSF and JMS
- If you scored low on questions 56-70: Review Security, Concurrency, and Batch Processing
