
## Answer Key

### CDI (1-10)

1. a) Request scope
2. d) `@Inject`
3. d) CreditCardProcessor
4. c) To produce factory methods for bean creation
5. c) `${appVersion}`
6. b) `@ConversationScoped`
7. d) Add it to beans.xml
8. a) To disambiguate between multiple implementations of the same type
9. d) After dependency injection is complete
10. c) `@Observes` (for observer) and `Event<T>` (for firing)

### SOAP (11-20)

11. a) Only add
12. d) XML over various protocols (HTTP, SMTP, JMS, etc.)
13. b) `wsimport`
14. c) Web Service Description Language
15. b) Automatic transaction management and pooling
16. d) All of the above
17. c) Message Transmission Optimization Mechanism for efficient binary data transfer
18. b) Injects a web service client proxy
19. d) Document/literal wrapped
20. a) To intercept and modify SOAP messages

### Bean Validation (21-25)

21. c) `@Email`
22. b) Using `ValidatorFactory` to get a `Validator` and call `validate()`
23. a) Cascades validation to the Customer object's fields
24. b) `@Min` and `@Max`
25. a) A 400 Bad Request is returned automatically

### JSON-P (26-30)

26. c) A JSON object
27. b) JSON-P (Java API for JSON Processing)
28. a) Object Model API
29. a) `Json.createArrayBuilder()`
30. b) Streaming API

### Advanced JPA (31-40)

31. a) `em.createNamedQuery("Product.findByCategory")`
32. c) To control which attributes are fetched in a query
33. b) To convert between Java attribute types and database column types
34. a) `LockModeType.PESSIMISTIC_WRITE`
35. c) Criteria API
36. a) It's no longer managed by the EntityManager
37. c) Deletes OrderItem entities when removed from the collection
38. c) `PersistenceContextType.TRANSACTION`
39. a) A database index on category and name columns
40. c) `persist()` is for new entities, `merge()` is for detached entities

### Advanced EJB (41-50)

41. a) The bean manages concurrency using Java synchronization
42. b) `Future<T>`
43. c) Interval timer
44. c) Prevent interceptors from being applied to this method
45. c) To specify initialization order
46. b) Topic
47. c) `NOT_SUPPORTED`
48. d) The timer doesn't survive server restarts
49. c) One per application
50. d) The bean exposes a no-interface view

### Advanced Servlets (51-55)

51. d) Before and after the servlet
52. b) `HttpSessionListener`
53. d) 5MB
54. a) `RequestDispatcher.include()`
55. c) Don't create a new session if one doesn't exist

### Advanced JSF (56-60)

56. b) `<h:dataTable>`
57. c) Update Model Values
58. b) Custom validator method
59. a) To define a template composition
60. c) Submit form data for the entire form

### Integration & Advanced Topics (61-70)

61. b) JAX-RS with EJB
62. c) Custom filter with `@Provider`
63. b) Bean-Managed Transaction (BMT)
64. a) `web.xml`
65. b) A WebSocket client endpoint
66. a) To enable CDI in the module
67. c) To stream data to the client efficiently
68. b) EAR
69. b) Every 15 minutes
70. a) It varies by server implementation

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
