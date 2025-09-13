# Interceptor in CDI

Gli intercettori in CDI (Contexts and Dependency Injection) sono un meccanismo potente per implementare la programmazione orientata agli aspetti (AOP), consentendo di aggiungere logica trasversale a un'applicazione in modo pulito e non invasivo. Funzionalità come il logging, la gestione delle transazioni, la sicurezza e il monitoraggio possono essere separate dalla logica di business principale.

## Concetti Fondamentali

Il funzionamento degli intercettori si basa su tre elementi chiave:

1. **Interceptor Binding**: Un'annotazione personalizzata, a sua volta annotata con `@InterceptorBinding`, che serve a "collegare" un intercettore a un bean o a un metodo specifico.
2. **Interceptor Class**: Una classe che contiene la logica trasversale. I metodi di questa classe, annotati con `@AroundInvoke`, `@AroundConstruct`, ecc., vengono eseguiti prima, dopo o "intorno" all'invocazione del metodo di business intercettato.
3. **Abilitazione dell'Interceptor**: L'intercettore deve essere abilitato globalmente per l'applicazione, solitamente tramite il file `beans.xml` o utilizzando l'annotazione `@Priority`.

## Come Funzionano

Quando un metodo di un bean annotato con un interceptor binding viene invocato, il container CDI intercetta la chiamata. Prima di eseguire il metodo originale, esegue il metodo corrispondente nell'intercettore (es. `@AroundInvoke`). Quest'ultimo ha il controllo completo e può ispezionare i parametri, modificare il risultato o persino impedire l'esecuzione del metodo di business.

### Esempi di Codice

Vediamo come implementare un intercettore per il logging.

**1. Definizione dell'Interceptor Binding**

Creiamo un'annotazione `@Logged` che useremo per marcare i metodi o le classi che vogliamo tracciare.

```java
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Logged {
}
```

**2. Implementazione dell'Interceptor**

La classe `LoggedInterceptor` contiene la logica di logging. Il metodo `@AroundInvoke` viene eseguito ogni volta che un metodo associato al binding `@Logged` viene chiamato.

```java
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Logged
@Interceptor
public class LoggedInterceptor {

    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        System.out.println("Invocazione del metodo: " + ctx.getMethod().getName());
        
        // Esegui il metodo di business originale
        Object result = ctx.proceed();
        
        System.out.println("Metodo " + ctx.getMethod().getName() + " completato.");
        
        return result;
    }
}
```

**3. Applicazione dell'Interceptor a un Bean**

Ora possiamo applicare l'annotazione `@Logged` a un intero bean o a un metodo specifico.

```java
@Logged // Applica l'intercettore a tutti i metodi della classe
public class MyService {

    public void performTask() {
        System.out.println("Esecuzione del task...");
    }

    // L'intercettore non verrebbe applicato qui se l'annotazione fosse solo sul metodo performTask
    public void anotherTask() {
        System.out.println("Esecuzione di un altro task...");
    }
}
```

**4. Abilitazione dell'Interceptor in `beans.xml`**

Perché l'intercettore sia attivo, deve essere dichiarato nel file `beans.xml` del progetto, che si trova solitamente in `WEB-INF/` o `META-INF/`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
                           http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
    
    <interceptors>
        <class>com.example.LoggedInterceptor</class>
    </interceptors>

</beans>
```

In alternativa, si può usare l'annotazione `@Priority` direttamente sulla classe dell'intercettore per abilitarlo globalmente senza modificare `beans.xml`. Un valore più basso indica una priorità più alta.

```java
import javax.annotation.Priority;

@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggedInterceptor {
    // ... implementazione
}
```

## Tabella dei Termini e Concetti Chiave

| Termine | Descrizione |
| --- | --- |
| **Interceptor** | Una classe che contiene logica trasversale (cross-cutting concern) da applicare a uno o più bean. |
| **`@InterceptorBinding`** | Meta-annotazione usata per creare annotazioni personalizzate che legano un intercettore a un target (classe o metodo). |
| **`@AroundInvoke`** | Annotazione per un metodo in un intercettore che viene eseguito "intorno" all'invocazione del metodo di business. |
| **`InvocationContext`** | Oggetto iniettato nel metodo `@AroundInvoke` che fornisce informazioni contestuali sulla chiamata (metodo, parametri) e permette di procedere con l'invocazione originale tramite `ctx.proceed()`. |
| **`@AroundConstruct`** | Annotazione per un metodo in un intercettore che viene eseguito "intorno" alla creazione di un'istanza del bean. |
| **`beans.xml`** | File di configurazione di CDI dove gli intercettori possono essere abilitati globalmente per l'applicazione. |
| **`@Priority`** | Annotazione che permette di abilitare un intercettore globalmente e di definirne l'ordine di esecuzione. |
| **Logica Trasversale** | Funzionalità che interessano più parti di un'applicazione (es. logging, sicurezza, transazioni) e che possono essere separate dalla logica di business. |
