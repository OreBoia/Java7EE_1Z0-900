
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
