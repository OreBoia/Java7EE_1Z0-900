# Test d'Ingresso Java EE 7

## Domanda 1

Quale affermazione descrive meglio Java EE 7?

- A. Un insieme di specifiche standard per applicazioni enterprise Java, implementate da vari fornitori di server applicativi.
- B. Un prodotto software specifico di Oracle per sviluppare applicazioni web Java.
- C. Un framework leggero per applicazioni monolitiche, alternativo a Spring Boot.
- D. Un toolkit grafico per applicazioni desktop Java.

---

## Domanda 2

Quale dei seguenti container non fa parte della piattaforma Java EE 7?

- A. Web Container.
- B. EJB Container.
- C. Applet Container.
- D. Swing Container (GUI Container).

---

## Domanda 3

In un'applicazione Java EE multi-tier, dove risiede tipicamente la logica di business?

- A. Nel client tier, presso l'applicazione desktop o nel browser.
- B. Nel server tier, in componenti come EJB o CDI bean che interagiscono con il database.
- C. Nel database tier, attraverso stored procedure e trigger che rimpiazzano completamente la logica applicativa.
- D. Nel web tier, all'interno delle JSP o Servlets, senza livelli aggiuntivi.

---

## Domanda 4

Qual è il vantaggio principale di utilizzare l'iniezione di dipendenze (es. `@Inject` , `@EJB`) invece di JNDI lookup nel codice?

- A. Nessuno, sono equivalenti in funzionalità e verbosità.
- B. L'iniezione permette codice più pulito e tipizzato, delegando al container la risoluzione delle risorse, mentre JNDI richiede stringhe di lookup e gestione manuale.
- C. JNDI funziona solo su Linux mentre l'iniezione è multipiattaforma.
- D. L'uso di JNDI è deprecato e rimosso in Java EE 7.

---

## Domanda 5

In un file EAR, quale di questi moduli potremmo non trovare?

- A. Un file WAR contenente Servlets e pagine web.
- B. Un file JAR con EJB session bean.
- C. Un file .jar di libreria condivisa.
- D. Un file .exe eseguibile contenente la logica di business.

---

## Domanda 6

Cosa accade quando un EJB Stateless viene iniettato in una Servlet Java EE?

- A. Il container EJB crea un'istanza per ogni chiamata e la distrugge immediatamente dopo.
- B. Il container web rifiuta l'avvio della Servlet perché le Servlets non possono usare EJB.
- C. Il container risolve la dipendenza alla deploy-time o startup: la Servlet riceve un riferimento proxy a un'istanza del bean gestita dal container EJB.
- D. Non è possibile iniettare un EJB in una Servlet; bisogna usare lookup JNDI manuale.

---

## Domanda 7

In Java EE, qual è il ruolo di un container?

- A. Fornire servizi di runtime (come gestione thread, transazioni, sicurezza) ai componenti applicativi in esso eseguiti.
- B. Impacchettare l'applicazione in un file deployabile.
- C. Assicurare che il garbage collector non elimini i bean.
- D. Tradurre il bytecode Java in codice macchina nativo.

---

## Domanda 8

Quale tipo di componente non è tipicamente eseguito nel Web Container?

- A. JSP (JavaServer Page).
- B. Servlet HTTP.
- C. JSF backing bean (gestito come CDI bean nel web container).
- D. Message-Driven Bean (MDB).

---

## Domanda 9

Quale caratteristica è vera riguardo agli Enterprise JavaBeans (EJB) rispetto ai CDI bean?

- A. Gli EJB supportano transazioni dichiarative out-of-the-box, mentre i CDI bean di default no (richiedono `@Transactional` o gestione esplicita).
- B. I CDI bean possono essere richiamati remotamente su un altro server, mentre gli EJB no.
- C. Gli EJB non possono usare l'iniezione CDI, mentre i CDI bean possono iniettare EJB liberamente.
- D. Un EJB deve sempre essere stateful, mentre i CDI bean sono sempre stateless.

---

## Domanda 10

In un'applicazione Java EE, come viene tipicamente gestita la persistenza degli oggetti nel database?

- A. Tramite oggetti Entity JPA gestiti da un Persistence Context, con operazioni mediate dall'EntityManager (fornito ad esempio da un EJB o CDI bean).
- B. Direttamente dalla Servlet mediante codici SQL JDBC in-line per ogni richiesta.
- C. Attraverso file di testo scritti su disco dal Web Container.
- D. Mediante l'uso di variabili statiche che mantengono i dati anche dopo il riavvio del server (persistenza in-memory).

---

## Risposte Corrette

1. **A**
2. **D**
3. **B**
4. **B**
5. **D**
6. **C**
7. **A**
8. **D**
9. **A**
10. **A**
