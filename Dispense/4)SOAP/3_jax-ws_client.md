# Creare un Client JAX-WS per Chiamare un Servizio SOAP

Per interagire con un web service SOAP da un'applicazione Java, JAX-WS fornisce un'API client. Esistono principalmente due approcci per creare un client.

## 1. Generazione di Stub Client da WSDL (Approccio Statico)

Questo è l'approccio più comune e consigliato. Si utilizza uno strumento a riga di comando, `wsimport`, per generare classi Java (chiamate "stub") a partire dal WSDL del servizio. Questi stub nascondono la complessità della comunicazione SOAP e permettono di chiamare le operazioni remote come se fossero metodi locali.

### Flusso di Lavoro

1. **Ottenere il WSDL**: Il primo passo è avere l'URL del file WSDL del servizio che si vuole consumare.
2. **Eseguire `wsimport`**: Si esegue lo strumento `wsimport` (incluso nel JDK) fornendo l'URL del WSDL. Questo comando genera:
    * Una classe **Service**: Estende `javax.xml.ws.Service` e agisce come una factory per ottenere il proxy del servizio.
    * Un'interfaccia **Service Endpoint Interface (SEI)**: Un'interfaccia Java che mappa le operazioni definite nel WSDL.
    * **Classi JAXB**: Classi Java che mappano i tipi di dati complessi definiti nello schema XML (XSD) del WSDL.
3. **Scrivere il Codice Client**: Nel codice Java, si istanzia la classe Service e si richiede il "port" (il proxy), che è un'implementazione della SEI. A questo punto, si possono invocare i metodi sull'oggetto proxy.

### Esempio di Codice

Supponiamo di avere un WSDL per un `CiaoService` che espone un'operazione `saluta`.

**Passo 1: Generare gli stub con `wsimport`**

```shell
# Esegui questo comando nel terminale
wsimport -keep -p com.example.client.ciao http://example.com/CiaoService?wsdl
```

* `-keep`: Mantiene i file sorgente `.java` generati.

* `-p`: Specifica il package per le classi generate.

**Passo 2: Scrivere il codice del client utilizzando gli stub generati**

```java
// Il package è quello specificato con l'opzione -p di wsimport
import com.example.client.ciao.CiaoService;
import com.example.client.ciao.CiaoServicePortType;

public class CiaoClient {

    public static void main(String[] args) {
        try {
            // 1. Creare un'istanza della classe Service generata
            CiaoService serviceFactory = new CiaoService();

            // 2. Ottenere il port (il proxy) dal service
            // Il nome del metodo (es. getCiaoServicePort) dipende da quanto definito nel WSDL
            CiaoServicePortType ciaoProxy = serviceFactory.getCiaoServicePort();

            // 3. Invocare il metodo del web service come se fosse un metodo locale
            System.out.println("Chiamando il web service...");
            String risposta = ciaoProxy.saluta("Mondo");

            System.out.println("Risposta dal server: " + risposta);

        } catch (Exception e) {
            System.err.println("Errore durante la chiamata al web service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## 2. Approcci Alternativi e Dinamici

Oltre alla generazione statica degli stub, esistono approcci più dinamici per creare un client.

### Client Dinamico (Dispatch API o JAX-WS Proxy)

È possibile evitare la generazione di stub se si ha già a disposizione l'interfaccia del servizio annotata (SEI).

* **JAX-WS Proxy**: Si può usare `Service.create()` per creare un'istanza del servizio e poi `service.getPort(...)` per ottenere il proxy. Questo approccio è dinamico perché non richiede il comando `wsimport`.
* **Dispatch API**: Per un controllo di basso livello, si può usare l'API `Dispatch`. Questo permette di costruire manualmente i messaggi XML da inviare, ma è un approccio complesso e raramente necessario.

### Iniezione in un Ambiente Java EE (`@WebServiceRef`)

In un container Java EE (come WildFly o GlassFish), il modo più semplice per ottenere un client è usare l'iniezione di dipendenza con l'annotazione `@WebServiceRef`. Il container si occupa di creare e injectare il proxy del servizio, senza bisogno di usare `wsimport` manualmente.

#### Esempio di Codice con `@WebServiceRef`

```java
import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;
import com.example.client.ciao.CiaoService;
import com.example.client.ciao.CiaoServicePortType;

@Stateless
public class CiaoServiceConsumer {

    // Il container injecta un'istanza del servizio
    @WebServiceRef(wsdlLocation = "http://example.com/CiaoService?wsdl")
    private CiaoService serviceFactory;

    public String usaCiaoService(String nome) {
        // Ottieni il port dalla factory injectata
        CiaoServicePortType ciaoProxy = serviceFactory.getCiaoServicePort();
        
        // Chiama il servizio
        return ciaoProxy.saluta(nome);
    }
}
```

## Comandi e Termini Importanti

### Tabella dei Termini

| Termine | Descrizione |
|---|---|
| **Stub Client** | Classi Java generate da `wsimport` che agiscono come proxy per il web service remoto. Semplificano l'invocazione nascondendo i dettagli di SOAP. |
| **Service Endpoint Interface (SEI)** | L'interfaccia Java generata da `wsimport` che definisce i metodi corrispondenti alle operazioni del web service. |
| **Service Class** | La classe generata da `wsimport` (che estende `javax.xml.ws.Service`) usata come factory per ottenere un'istanza del proxy (il "port"). |
| **Port** | Un'istanza del proxy che implementa la SEI e maschera le chiamate SOAP come normali metodi Java. Si ottiene dalla classe Service e si usa per effettuare le chiamate remote. |
| **`wsimport`** | Lo strumento a riga di comando del JDK usato per generare gli stub client JAX-WS a partire da un file WSDL. |
| **DII (Dynamic Invocation Interface)** | Un'API client avanzata di JAX-WS che permette di chiamare web service senza stub pre-generati, costruendo le chiamate a runtime. |
| **`@WebServiceRef`** | Annotazione Java EE usata per injectare un riferimento a un web service in un componente gestito (es. un EJB o un Servlet). |
