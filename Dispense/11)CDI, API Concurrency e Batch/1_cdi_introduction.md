# CDI (Contexts and Dependency Injection)

CDI (Contexts and Dependency Injection) è un set di servizi di Java EE che consente l’iniezione di dipendenze tra componenti in modo tipizzato e loosely-coupled.

Con CDI si definiscono bean gestiti (POJO con scope e annotazioni specifiche) e li si inietta in altri bean tramite `@Inject`.

Gli oggetti con scope contesto sono creati e distrutti dal container secondo il loro ciclo di vita: ad esempio, un bean `@SessionScoped` viene ricreato all’inizio di una sessione HTTP e mantenuto fino alla chiusura della sessione. Grazie ai context, un’istanza di bean nello stesso scope viene automaticamente condivisa tra tutte le iniezioni nello stesso contesto d’esecuzione.

## Scope dei Bean CDI

I principali scope CDI sono descritti nella tabella qui sotto:

| Scope | Descrizione |
| :--- | :--- |
| `@RequestScoped` | Durata di una singola richiesta HTTP. |
| `@SessionScoped` | Durata della sessione utente. |
| `@ApplicationScoped` | Vita condivisa tra tutti gli utenti nell’applicazione. |
| `@ConversationScoped` | Durata di una conversazione JSF controllata dall’utente. |
| `@Dependent` | Scope predefinito, l’istanza dura quanto il bean iniettante. |

## Esempi di Codice

Ecco alcuni esempi di come utilizzare CDI.

### Esempio 1: Creare un Bean Semplice

Questo è un semplice bean CDI con scope `@RequestScoped`.

```java
import javax.enterprise.context.RequestScoped;

@RequestScoped
public class SimpleGreeting {
    public String greet(String name) {
        return "Hello, " + name;
    }
}
```

### Esempio 2: Iniezione del Bean

Questo servlet inietta e utilizza il bean `SimpleGreeting`.

```java
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/greet")
public class GreetingServlet extends HttpServlet {

    @Inject
    private SimpleGreeting greeting;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        if (name == null) {
            name = "World";
        }
        resp.getWriter().write(greeting.greet(name));
    }
}
```

Per far funzionare l'iniezione CDI, è necessario avere un file `beans.xml` vuoto nella directory `WEB-INF` (o `META-INF` per le librerie JAR).

`WEB-INF/beans.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
</beans>
```

## Termini e Annotazioni Importanti

| Termine/Annotazione | Descrizione |
| :--- | :--- |
| `@Inject` | Utilizzata per richiedere l'iniezione di un'istanza di un bean. |
| `@Produces` | Specifica che un metodo produce un bean che può essere iniettato. Utile per oggetti che non possono essere istanziati direttamente dal container (es. da librerie esterne). |
| `@Disposes` | Specifica un metodo per la pulizia di un bean creato con `@Produces`. |
| `@Qualifier` | Usata per disambiguare tra diverse implementazioni dello stesso tipo di bean. |
| `@Alternative` | Permette di specificare un'implementazione alternativa di un bean, che può essere attivata globalmente nel `beans.xml`. |
| `@Named` | Permette di dare un nome a un bean, rendendolo accessibile tramite Expression Language (EL) nelle pagine JSF o JSP. |
| `beans.xml` | File di configurazione (opzionale in molte configurazioni di default) che abilita CDI e permette configurazioni avanzate. |
| `Bean` | Un componente gestito dal container CDI. Può essere un POJO annotato. |
| `Scope` | Definisce il ciclo di vita e la visibilità di un'istanza di un bean. |
| `Context` | Il contesto in cui vive un'istanza di un bean con un certo scope. |
