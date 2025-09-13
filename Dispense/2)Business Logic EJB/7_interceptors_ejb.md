# Intercettori (Interceptors) in EJB

Gli EJB (Enterprise JavaBeans) supportano la logica AOP (Aspect-Oriented Programming) tramite interceptor. Questo permette di separare le problematiche trasversali (cross-cutting concerns) dalla logica di business principale.

## Concetti Chiave

- **Interceptor**: Una classe che contiene la logica da eseguire prima, dopo o "intorno" all'invocazione di un metodo di business di un EJB.
- **InvocationContext**: Un oggetto fornito dal container all'interceptor che dà accesso ai metadati della chiamata, come il metodo invocato, i parametri e il bean di destinazione. Permette anche di controllare il flusso di esecuzione.
- **Interceptor Binding**: Un'annotazione personalizzata che viene utilizzata per associare un interceptor a un EJB o a un suo metodo specifico.

## Come funzionano

Si definisce una classe interceptor con l'annotazione `@Interceptor`. All'interno di questa classe, si crea un metodo annotato con `@AroundInvoke` che accetta un `InvocationContext` come parametro. Questo metodo può eseguire logica prima e dopo l'invocazione del metodo di business originale.

Per proseguire con la catena di invocazione (e quindi eseguire il metodo di business), è necessario chiamare il metodo `ctx.proceed()`.

Gli intercettori sono ideali per implementare funzionalità come:

- Logging
- Auditing
- Sicurezza personalizzata
- Caching
- Monitoraggio delle performance

## Esempi di Codice - Interceptor

Questo esempio illustra una catena di due intercettori:

1. `AuditingInterceptor`: Registra (fa auditing) chi sta chiamando quale metodo, con quali parametri e ne misura il tempo di esecuzione.
2. `ParameterSanitizerInterceptor`: "Pulisce" i parametri di tipo `String` prima che vengano passati al metodo di business.

### 1. Creazione dell'Interceptor Binding

Creiamo un'annotazione personalizzata per legare i nostri intercettori agli EJB.

```java
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Monitored {
}
```

### 2. Creazione degli Intercettori

#### Interceptor per la "pulizia" dei parametri (`ParameterSanitizerInterceptor`)

Questo interceptor modifica i parametri in ingresso. Scorre i parametri della chiamata e, se ne trova uno di tipo `String`, rimuove gli spazi bianchi iniziali e finali.

```java
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Monitored // Associa questo interceptor al binding @Monitored
public class ParameterSanitizerInterceptor {

    @AroundInvoke
    public Object sanitizeParameters(InvocationContext ctx) throws Exception {
        System.out.println("[Sanitizer] Controllo parametri...");
        Object[] parameters = ctx.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof String) {
                String original = (String) parameters[i];
                String sanitized = original.trim();
                parameters[i] = sanitized;
                System.out.println("[Sanitizer] Parametro " + i + " pulito. Originale: '" + original + "', Pulito: '" + sanitized + "'");
            }
        }
        // Aggiorna i parametri della chiamata con quelli "puliti"
        ctx.setParameters(parameters);

        // Prosegue nella catena
        return ctx.proceed();
    }
}
```

#### Interceptor per Auditing e Performance (`AuditingInterceptor`)

Questo interceptor registra i dettagli della chiamata e misura il tempo di esecuzione. Utilizza `getContextData()` per passare informazioni (in questo caso, il tempo di inizio) lungo la catena.

```java
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Arrays;

@Interceptor
@Monitored // Associa anche questo interceptor al binding @Monitored
public class AuditingInterceptor {

    @AroundInvoke
    public Object auditAndMeasure(InvocationContext ctx) throws Exception {
        // 1. Log prima dell'esecuzione
        String beanName = ctx.getTarget().getClass().getSimpleName();
        String methodName = ctx.getMethod().getName();
        String params = Arrays.toString(ctx.getParameters());

        System.out.println("[Audit] Invocazione: " + beanName + "." + methodName + " con parametri: " + params);

        // 2. Misurazione delle performance
        long startTime = System.currentTimeMillis();
        
        Object result = null;
        try {
            // 3. Prosegue con l'invocazione
            result = ctx.proceed();
            return result;
        } finally {
            // 4. Log dopo l'esecuzione (anche in caso di eccezione)
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[Audit] Esecuzione di " + methodName + " completata in " + duration + "ms. Valore restituito: " + result);
        }
    }
}
```

### 3. Applicazione degli Intercettori a un EJB

Applichiamo il binding `@Monitored` al nostro EJB. Entrambi gli intercettori verranno attivati per tutti i metodi pubblici.

```java
import javax.ejb.Stateless;

@Stateless
@Monitored // Applica tutti gli intercettori associati a questo EJB
public class CustomerServiceBean {

    public String createCustomer(String name, String email) {
        System.out.println("  >> Dentro CustomerServiceBean.createCustomer()...");
        try {
            // Simula un'operazione che richiede tempo
            Thread.sleep(50); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Cliente " + name + " creato con email " + email;
    }
}
```

### 4. Attivazione e Ordinamento in `beans.xml`

Perché gli intercettori funzionino e vengano eseguiti nell'ordine corretto, dobbiamo dichiararli nel file `beans.xml`. L'ordine di dichiarazione definisce l'ordine di esecuzione.

In questo caso, vogliamo che `ParameterSanitizerInterceptor` venga eseguito *prima* di `AuditingInterceptor`, in modo che i log di audit mostrino i parametri già "puliti".

```xml
<!-- src/main/webapp/WEB-INF/beans.xml -->
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
    <interceptors>
        <!-- Eseguito per primo -->
        <class>com.example.interceptors.ParameterSanitizerInterceptor</class>
        <!-- Eseguito per secondo -->
        <class>com.example.interceptors.AuditingInterceptor</class>
    </interceptors>
</beans>
```

#### Output Atteso

Quando un client invoca `createCustomer("  John Doe  ", " john@email.com ")`, l'output sulla console del server sarà simile a questo:

[Sanitizer] Controllo parametri...
[Sanitizer] Parametro 0 pulito. Originale: '  John Doe  ', Pulito: 'John Doe'
[Sanitizer] Parametro 1 pulito. Originale: ' <john@email.com> ', Pulito: '<john@email.com>'
[Audit] Invocazione: CustomerServiceBean.createCustomer con parametri: [John Doe, john@email.com]
  >> Dentro CustomerServiceBean.createCustomer()...
[Audit] Esecuzione di createCustomer completata in 52ms. Valore restituito: Cliente John Doe creato con email <john@email.com>

### 4. Attivazione dell'Interceptor in `beans.xml`

Perché il container EJB possa riconoscere e applicare l'interceptor, è necessario dichiararlo nel file `beans.xml` (o `ejb-jar.xml` per configurazioni più specifiche).

```xml
<!-- src/main/webapp/WEB-INF/beans.xml -->
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
    <interceptors>
        <class>com.example.LoggingInterceptor</class>
    </interceptors>
</beans>
```

## Lista dei Comandi e Annotazioni Principali

| Annotazione/Comando | Descrizione |
| --- | --- |
| `@Interceptor` | Marca una classe come un interceptor. |
| `@InterceptorBinding` | Crea un nuovo tipo di binding per gli intercettori. |
| `@AroundInvoke` | Definisce un metodo in un interceptor che "avvolge" l'invocazione di un metodo di business. |
| `@AroundTimeout` | Definisce un metodo in un interceptor che intercetta le chiamate ai metodi di timeout (associati a `@Timeout`). |
| `InvocationContext` | Interfaccia passata ai metodi `@AroundInvoke` per controllare la catena di invocazione. |
| `ctx.proceed()` | Metodo di `InvocationContext` che, se chiamato, passa il controllo al prossimo interceptor nella catena o al metodo di business stesso. |
| `ctx.getMethod()` | Restituisce l'oggetto `Method` che rappresenta il metodo di business intercettato. |
| `ctx.getParameters()` | Restituisce i parametri passati al metodo di business. |
| `ctx.setParameters()` | Permette di modificare i parametri prima che il metodo di business venga eseguito. |
| `ctx.getTarget()` | Restituisce l'istanza del bean (EJB) su cui il metodo è invocato. |
| `ctx.getContextData()` | Fornisce una mappa per passare dati tra gli intercettori nella stessa catena di invocazione. |
| `@Interceptors` | Annotazione per applicare uno o più intercettori direttamente a un EJB o a un metodo, senza usare i binding. Es: `@Interceptors(LoggingInterceptor.class)` |

L'uso degli interceptor binding (`@LoggingInterceptorBinding` nell'esempio) è generalmente preferito rispetto all'uso di `@Interceptors` perché offre maggiore flessibilità e disaccoppiamento.
