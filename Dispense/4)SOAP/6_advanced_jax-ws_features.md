# Funzionalità Avanzate di JAX-WS: Integrazione con EJB, JAXB e MTOM

Questa sezione esplora alcune funzionalità più specifiche di JAX-WS, come l'integrazione con EJB, l'uso diretto di JAXB e la gestione di allegati binari.

## Integrazione di JAX-WS con EJB

È una pratica comune esporre un EJB (Enterprise JavaBean) come un web service SOAP. Questo permette di combinare la semplicità di JAX-WS con le potenti funzionalità del container EJB, come la gestione delle transazioni, la sicurezza e il pooling di istanze.

**Requisiti principali:**

- L'EJB deve essere di tipo **Stateless** (`@Stateless`) o **Singleton** (`@Singleton`).
- La classe di implementazione dell'EJB deve essere annotata con `@WebService`.

Il container EJB si occuperà di gestire il ciclo di vita del servizio e di generare automaticamente il WSDL e l'endpoint.

### Esempio di Codice: EJB Stateless come Web Service

```java
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * Un EJB Stateless esposto come Web Service SOAP.
 * Il container gestirà il pooling delle istanze e potrà gestire le transazioni.
 */
@Stateless
@WebService(serviceName = "ProductService")
public class ProductServiceEJB {

    @WebMethod
    public Product getProductById(int id) {
        // In un'applicazione reale, qui ci sarebbe la logica per recuperare
        // un prodotto da un database, magari usando JPA.
        // L'uso di un EJB permette di avere una transazione gestita dal container.
        System.out.println("Recupero prodotto con id: " + id);
        return new Product(id, "Sample Product", 99.99);
    }
    
    // Classe fittizia per l'esempio
    public static class Product {
        public int id;
        public String name;
        public double price;
        
        public Product(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        public Product() {} // Richiesto da JAXB
    }
}
```

## Marshalling e Unmarshalling con JAXB Stand-alone

Anche se JAX-WS utilizza JAXB "dietro le quinte" per convertire i parametri e i valori di ritorno in XML, è utile sapere come usare JAXB direttamente. Questo può servire per manipolare file XML, per testare o per qualsiasi scenario in cui è necessaria una conversione esplicita tra oggetti Java e XML.

I passaggi fondamentali sono:

1. Creare un `JAXBContext` per le classi che si vogliono processare.
2. Creare un `Marshaller` dal contesto per convertire un oggetto Java in XML.
3. Creare un `Unmarshaller` dal contesto per convertire XML in un oggetto Java.

### Esempio di Codice: JAXB Stand-alone

```java
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.io.StringReader;

public class JaxbExample {

    public static void main(String[] args) throws Exception {
        // Oggetto Java da convertire in XML
        ProductServiceEJB.Product product = new ProductServiceEJB.Product(101, "Laptop", 1200.50);

        // 1. Creare il contesto JAXB
        JAXBContext context = JAXBContext.newInstance(ProductServiceEJB.Product.class);

        // --- MARSHALLING (Java -> XML) ---
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // Per un output leggibile

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(product, stringWriter);
        String xmlString = stringWriter.toString();
        
        System.out.println("--- XML generato (Marshalling) ---");
        System.out.println(xmlString);

        // --- UNMARSHALLING (XML -> Java) ---
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader stringReader = new StringReader(xmlString);
        ProductServiceEJB.Product productFromXml = (ProductServiceEJB.Product) unmarshaller.unmarshal(stringReader);

        System.out.println("\n--- Oggetto Java ottenuto (Unmarshalling) ---");
        System.out.println("ID: " + productFromXml.id + ", Nome: " + productFromXml.name);
    }
}
```

## Gestione degli Allegati Binari (MTOM)

SOAP non è limitato al trasporto di solo testo XML. Può trasportare dati binari (come file, immagini, PDF) in modo efficiente utilizzando **MTOM (Message Transmission Optimization Mechanism)**. MTOM ottimizza l'invio di dati binari estraendoli dal corpo del messaggio SOAP e inviandoli come allegati MIME, in modo simile agli allegati di una email.

In JAX-WS, si può abilitare MTOM con l'annotazione `@MTOM` a livello di servizio. I dati binari vengono poi mappati a tipi come `byte[]` o, più comunemente, `javax.activation.DataHandler`.

### Esempio di Codice: Servizio con MTOM

```java
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;
import javax.activation.DataHandler;
import javax.jws.WebMethod;

@WebService
@MTOM(enabled = true, threshold = 1024) // Abilita MTOM per allegati > 1KB
public class FileTransferService {

    @WebMethod
    public void uploadFile(String fileName, DataHandler fileData) {
        // Logica per salvare il file ricevuto dal DataHandler
        System.out.println("Ricevuto file: " + fileName);
        // Esempio: salva il file su disco...
    }

    @WebMethod
    public DataHandler downloadFile(String fileName) {
        // Logica per caricare un file e restituirlo come DataHandler
        System.out.println("Invio file: " + fileName);
        // Esempio: crea un DataHandler da un file su disco...
        return null; // Implementazione omessa
    }
}
```

## Tabella dei Termini e Concetti

| Termine/Concetto | Descrizione |
| :--- | :--- |
| **`@WebService` su EJB** | Annotazione che espone un EJB Stateless o Singleton come un web service SOAP, unendo le funzionalità di entrambi. |
| **`JAXBContext`** | Il punto di ingresso per l'API JAXB. Gestisce le informazioni necessarie per il mapping delle classi Java. |
| **`Marshaller`** | Oggetto JAXB responsabile della conversione di un albero di oggetti Java in dati XML. |
| **`Unmarshaller`** | Oggetto JAXB responsabile della conversione di dati XML in un albero di oggetti Java. |
| **MTOM** | Message Transmission Optimization Mechanism. Specifica per l'invio efficiente di dati binari in messaggi SOAP. |
| **`@MTOM`** | Annotazione JAX-WS che abilita il supporto MTOM per un web service. |
| **`DataHandler`** | Classe standard Java (JAF) usata per gestire dati di tipi MIME arbitrari. In JAX-WS, è comunemente usata per rappresentare allegati binari. |
