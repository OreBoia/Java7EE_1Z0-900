# Mappatura delle Servlet a URL

Perché una Servlet possa rispondere a una richiesta HTTP, il Web Container deve sapere quale URL è associato a quale Servlet. Questo processo è chiamato "mappatura". In Java EE 7 (con Servlet 3.1), esistono due modi principali per configurare questa mappatura.

## 1. Mappatura tramite Annotazione `@WebServlet` (Approccio Moderno)

Introdotta con Servlet 3.0, l'annotazione `@WebServlet` è il modo più semplice e comune per dichiarare una Servlet e mapparla a uno o più URL. Si applica direttamente alla classe della Servlet.

Questo approccio riduce la necessità di configurazione XML e mantiene le informazioni di mapping vicine al codice della Servlet stessa.

### Esempio di Codice con `@WebServlet`

```java
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
// ... altri import

/**
 * Questa Servlet risponde a due URL diversi: "/user/profile" e "/account".
 * L'attributo 'urlPatterns' accetta un array di stringhe.
 */
@WebServlet(urlPatterns = {"/user/profile", "/account"})
public class ProfileServlet extends HttpServlet {
    // Implementazione di doGet, doPost, etc.
}

/**
 * Esempio con un singolo URL.
 */
@WebServlet("/saluto")
public class SalutoServlet extends HttpServlet {
    // ...
}
```

## 2. Mappatura tramite `web.xml` (Approccio Tradizionale)

Prima di Servlet 3.0, l'unico modo per mappare le Servlet era attraverso il file descrittore di deployment, `web.xml`, che si trova nella directory `WEB-INF/` dell'applicazione.

Questo metodo è ancora valido e può essere utile in alcuni scenari, ad esempio per sovrascrivere le annotazioni o per configurare applicazioni legacy. La configurazione è divisa in due parti:

1.  **`<servlet>`**: Dichiara la Servlet, assegnandole un nome logico e specificando la classe Java completa.
2.  **`<servlet-mapping>`**: Associa il nome logico della Servlet a uno o più `url-pattern`.

### Esempio di Configurazione in `web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

    <!-- 1. Dichiarazione della Servlet -->
    <servlet>
        <servlet-name>LoginServlet</servlet-name>
        <servlet-class>com.example.auth.LoginServlet</servlet-class>
    </servlet>

    <!-- 2. Mappatura della Servlet a un URL -->
    <servlet-mapping>
        <servlet-name>LoginServlet</servlet-name>
        <url-pattern>/login</url-pattern>
    </servlet-mapping>

</web-app>
```

## Tipi di URL Pattern

Il container utilizza delle regole per associare l'URL di una richiesta al pattern corretto. I tipi di pattern più comuni sono:

1.  **Corrispondenza Esatta**: Il pattern corrisponde esattamente a un URL.
    *   Esempio: `/login` corrisponde solo a `http://<host>/<app>/login`.

2.  **Corrispondenza di Percorso (Path Match)**: Utilizza un asterisco (`*`) alla fine del pattern per mappare tutti gli URL che iniziano con un certo percorso.
    *   Esempio: `/api/*` corrisponde a `/api/users`, `/api/products/123`, ecc.

3.  **Corrispondenza di Estensione (Extension Match)**: Utilizza un asterisco (`*`) all'inizio del pattern per mappare tutti gli URL che terminano con una certa estensione.
    *   Esempio: `*.jsp` era classicamente usato per mappare tutte le richieste di pagine JSP al motore JSP.

Il container sceglie sempre la corrispondenza più specifica per una data richiesta. Ad esempio, tra `/api/*` e `/api/users`, una richiesta a `/api/users` verrà gestita dalla Servlet mappata a `/api/users` (corrispondenza esatta).

## Tabella dei Termini Chiave

| Termine | Descrizione |
| :--- | :--- |
| **`@WebServlet`** | Annotazione per dichiarare una Servlet e i suoi `urlPatterns` direttamente nel codice Java. |
| **`web.xml`** | Il descrittore di deployment (Deployment Descriptor) di un'applicazione web Java, usato per la configurazione, inclusa la mappatura delle Servlet. |
| **`url-pattern`** | Una stringa che definisce a quali URL una Servlet, un filtro o una risorsa di sicurezza deve rispondere. |
| **`<servlet>`** | Elemento in `web.xml` che dichiara una Servlet, assegnandole un nome logico e specificando la classe. |
| **`<servlet-mapping>`** | Elemento in `web.xml` che associa un nome di Servlet a un `url-pattern`. |
| **Corrispondenza Esatta** | Un `url-pattern` senza caratteri jolly che corrisponde a un singolo URL. |
| **Corrispondenza di Percorso** | Un `url-pattern` che termina con `/*` per mappare un intero sotto-percorso. |
| **Corrispondenza di Estensione** | Un `url-pattern` che inizia con `*.` per mappare tutte le richieste con una specifica estensione. |
