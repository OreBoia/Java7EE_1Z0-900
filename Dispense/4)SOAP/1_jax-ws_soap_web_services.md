# SOAP, JAX-WS e JAXB in Java EE 7

## Introduzione

SOAP (Simple Object Access Protocol) è un protocollo standard per web service basato su XML. In Java EE 7, la specifica JAX-WS (Java API for XML-Web Services) consente di creare servizi SOAP e client in Java con relativa semplicità, astraendo i dettagli di basso livello (creazione di XML, gestione di messaggi SOAP, ecc.).

JAXB (Java Architecture for XML Binding) è la tecnologia complementare che effettua il mapping tra classi Java e rappresentazione XML, usata da JAX-WS per serializzare/deserializzare oggetti nei messaggi SOAP.

Un servizio SOAP è tipicamente definito da un WSDL (Web Services Description Language), un documento XML che descrive le operazioni offerte, i parametri, i tipi di dati (tramite XSD) e come accedervi (endpoint URL, binding). Con JAX-WS, possiamo codificare il web service in Java e lasciare che il runtime generi automaticamente il WSDL (approccio code-first), oppure a partire da un WSDL generare scheletro di codice (approccio contract-first).

## Creare un Web Service JAX-WS (Server Side)

In Java EE 7, un servizio SOAP può essere implementato come:

- Un POJO annotato con `@WebService`.
- Un EJB Stateless per integrare transazioni e pool (spesso è consigliato usare `@Stateless` + `@WebService` insieme).

Ogni metodo pubblico della classe diventa un'operazione SOAP, a meno che non sia annotato con `@WebMethod(exclude=true)` per escluderlo.
Gli eventuali parametri e valori di ritorno vengono automaticamente convertiti in XML usando JAXB. Tipi semplici come `String` e `int` hanno un mapping immediato, mentre oggetti complessi devono rispettare le regole JAXB (avere un costruttore di default, campi/proprietà accessibili, ecc.).

### Esempio di Codice: Web Service

Ecco un semplice esempio di un web service che offre un'operazione per salutare un utente.

```java
// Import delle annotazioni necessarie
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;

/**
* Esempio di un semplice Web Service SOAP implementato come EJB Stateless.
*/
@Stateless // Rende il servizio un EJB per beneficiare di pooling, transazioni, etc.
@WebService(serviceName = "HelloService") // Definisce il nome del servizio nel WSDL
public class HelloService {

    /**
     * Operazione del web service che restituisce un saluto.
     * @param name Il nome da salutare.
     * @return Una stringa di saluto.
     */
    @WebMethod(operationName = "sayHello") // Definisce il nome dell'operazione nel WSDL
    public String sayHello(@WebParam(name = "name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Hello, World!";
        }
        return "Hello, " + name + "!";
    }

    /**
     * Un altro metodo di esempio che non viene esposto come operazione SOAP.
     */
    @WebMethod(exclude = true)
    public void internalLogic() {
        // Logica di business interna non esposta via SOAP
    }
}
```

### Esempio di Codice: Client JAX-WS

Un client può essere generato automaticamente a partire dal WSDL oppure creato manualmente.

```java
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

public class HelloClient {

    public static void main(String[] args) throws Exception {
        // URL del WSDL del servizio (generalmente pubblicato dal server)
        URL wsdlUrl = new URL("http://localhost:8080/YourAppName/HelloService?wsdl");

        // QName (Qualified Name) del servizio: (namespace, localpart)
        // Il namespace è solitamente il package invertito, il localpart è il serviceName.
        QName serviceQName = new QName("http://soap.dispense/", "HelloService");

        // Creazione della factory del servizio
        Service service = Service.create(wsdlUrl, serviceQName);

        // Ottenimento del proxy del servizio (la porta)
        HelloService helloProxy = service.getPort(HelloService.class);

        // Invocazione del metodo remoto
        String response = helloProxy.sayHello("John Doe");

        System.out.println("Response from server: " + response);
    }
}
```

*Nota: L'interfaccia `HelloService` usata nel client è la stessa definita sul server o generata da `wsimport`.*

## Comandi Utili

Il JDK include alcuni strumenti a riga di comando per lavorare con JAX-WS:

- **`wsimport`**: Questo è il comando più comune. Viene utilizzato per generare gli artefatti Java (classi, interfacce) a partire da un file WSDL. È la base dell'approccio *contract-first*.
  
  **Sintassi di base:**
  
  ```shell
  wsimport -keep -p com.example.client http://localhost:8080/HelloService?wsdl
  ```
  
  - `-keep`: Mantiene i file sorgente `.java` generati.
  - `-p <package>`: Specifica il package di destinazione per le classi generate.
  - L'ultimo argomento è l'URL del WSDL.

- **`wsgen`**: Utilizzato nell'approccio *code-first* per generare gli artefatti necessari per la pubblicazione di un web service, incluso un WSDL e gli schemi XSD. Nelle moderne applicazioni Java EE, il container applicativo (come WildFly, GlassFish) gestisce questa operazione automaticamente durante il deploy, quindi l'uso manuale di `wsgen` è diventato raro.

  **Sintassi di base:**
  
  ```shell
  wsgen -cp . -wsdl -d generated com.example.server.HelloService
  ```
  
  - `-cp <classpath>`: Specifica il classpath dove trovare la classe del servizio.
  - `-wsdl`: Genera il file WSDL.
  - `-d <directory>`: Specifica la directory di output per gli artefatti generati.
  - L'ultimo argomento è il nome completo della classe che implementa il servizio.

## Glossario dei Termini

| Termine | Descrizione |
|---|---|
| **SOAP (Simple Object Access Protocol)** | Un protocollo basato su XML per lo scambio di informazioni strutturate nell'implementazione di web service. |
| **JAX-WS (Java API for XML-Web Services)** | La specifica Java EE per la creazione di web service SOAP. Fornisce un'API basata su annotazioni per semplificare lo sviluppo. |
| **JAXB (Java Architecture for XML Binding)** | Tecnologia utilizzata per il mapping tra oggetti Java e documenti XML (e viceversa). È il motore di data binding predefinito in JAX-WS. |
| **WSDL (Web Services Description Language)** | Un linguaggio basato su XML per descrivere le funzionalità offerte da un web service. Funge da "contratto" tra il provider e il consumer del servizio. |
| **XSD (XML Schema Definition)** | Un linguaggio per definire la struttura e i tipi di dati di un documento XML. Il WSDL utilizza XSD per descrivere i dati scambiati nelle operazioni SOAP. |
| **Endpoint** | L'indirizzo (URL) a cui i messaggi del client vengono inviati. In Java EE, l'endpoint è gestito dal container. |
| **`@WebService`** | Annotazione Java che espone una classe come web service JAX-WS. |
| **`@WebMethod`** | Annotazione usata per personalizzare un metodo che viene esposto come operazione di un web service. |
| **`@WebParam`** | Annotazione usata per personalizzare il mapping di un parametro di un metodo a un messaggio WSDL e a un elemento XML. |
| **`wsimport`** | Strumento a riga di comando del JDK che genera gli artefatti JAX-WS (come le classi stub del client) a partire da un file WSDL. |
| **`wsgen`** | Strumento a riga di comando del JDK che genera gli artefatti JAX-WS (incluso il WSDL) a partire da una classe Java che implementa il servizio. |
| **Code-First** | Un approccio allo sviluppo di web service in cui si scrive prima la classe di implementazione Java e poi si generano gli artefatti (come il WSDL) da essa. |
| **Contract-First** | Un approccio in cui si definisce prima il contratto del servizio (il file WSDL) e poi si generano le classi Java da esso. |