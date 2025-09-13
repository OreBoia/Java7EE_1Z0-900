# Confronto tra SOAP e REST

SOAP e REST sono due approcci per la progettazione di web service, ma si basano su filosofie molto diverse. La scelta tra i due dipende dai requisiti specifici del progetto, come il livello di sicurezza, la necessità di transazioni e l'ambiente in cui il servizio opererà.

## Caratteristiche Principali di SOAP

SOAP (Simple Object Access Protocol) è un **protocollo** standardizzato dal W3C. È caratterizzato da:

- **Standard Rigidi**: Si basa su standard ben definiti come XML per il formato dei messaggi, WSDL per la descrizione del servizio, XSD per la definizione dei tipi di dati e il protocollo SOAP stesso per la struttura dell'envelope del messaggio.
- **Orientato alle Operazioni**: Un servizio SOAP espone un insieme di operazioni (o metodi), in modo simile a una chiamata di procedura remota (RPC).
- **Sicurezza Avanzata**: Supporta le specifiche WS-Security, che consentono di implementare funzionalità di sicurezza complesse come la firma digitale e la crittografia a livello di messaggio, garantendo integrità e confidenzialità end-to-end.
- **Transazionalità**: Supporta standard come WS-Transaction per gestire transazioni distribuite complesse, garantendo l'atomicità delle operazioni su più servizi.
- **Indipendenza dal Trasporto**: Sebbene comunemente utilizzi HTTP, SOAP può funzionare su qualsiasi protocollo di trasporto (es. SMTP, JMS).
- **Verboso**: L'uso di XML rende i messaggi SOAP più pesanti e verbosi rispetto a REST/JSON.

JAX-WS è l'API Java EE per l'implementazione di servizi SOAP e supporta molte di queste specifiche avanzate, come:

- **Handlers**: Simili a filtri, permettono di intercettare e manipolare i messaggi SOAP in entrata e in uscita.
- **MTOM (Message Transmission Optimization Mechanism)**: Per l'invio efficiente di dati binari (allegati).
- **WS-Addressing**: Per gestire l'indirizzamento dei messaggi in scenari complessi.

## Caratteristiche Principali di REST

REST (Representational State Transfer) è uno **stile architetturale**, non un protocollo. Si basa su un insieme di principi che sfruttano le funzionalità del protocollo HTTP.

- **Orientato alle Risorse**: Invece di operazioni, REST si concentra sulle risorse (es. un utente, un prodotto). Ogni risorsa è identificata da un URI univoco.
- **Utilizzo dei Metodi HTTP**: Le operazioni sulle risorse vengono eseguite utilizzando i verbi HTTP standard:
  - `GET`: per recuperare una risorsa.
  - `POST`: per creare una nuova risorsa.
  - `PUT`: per aggiornare/sostituire una risorsa.
  - `DELETE`: per eliminare una risorsa.
- **Formato Flessibile**: Sebbene JSON sia il formato più comune per la sua leggerezza, REST può utilizzare qualsiasi formato di dati (XML, HTML, testo semplice).
- **Stateless**: Ogni richiesta da un client a un server deve contenere tutte le informazioni necessarie per essere compresa, senza fare affidamento su sessioni memorizzate sul server.
- **Leggero e Veloce**: L'uso di JSON e l'assenza di envelope complessi rendono i servizi REST generalmente più performanti e meno verbosi di SOAP.

## Tabella di Confronto: SOAP vs REST

| Caratteristica | SOAP | REST |
| :--- | :--- | :--- |
| **Tipo** | Protocollo standard | Stile architetturale |
| **Formato Dati** | XML (obbligatorio) | Flessibile (JSON, XML, testo, ecc.) |
| **Contratto** | WSDL (formale e rigido) | Informale (es. OpenAPI/Swagger) o assente |
| **Sicurezza** | WS-Security (crittografia e firma a livello di messaggio) | Si affida al protocollo di trasporto (HTTPS/TLS) |
| **Transazioni** | Supporta transazioni distribuite (WS-Transaction) | Non ha uno standard per le transazioni |
| **Performance** | Più verboso e lento a causa di XML | Generalmente più leggero e veloce |
| **Stato** | Può essere stateful | Stateless (senza stato) |
| **Utilizzo Tipico** | Sistemi enterprise, integrazioni B2B, applicazioni che richiedono alta sicurezza e affidabilità | API pubbliche, applicazioni web e mobile, microservizi |

## Glossario dei Termini

| Termine | Descrizione |
| :--- | :--- |
| **WSDL** | Web Services Description Language. Il contratto formale di un servizio SOAP. |
| **WS-Security** | Specifica per la sicurezza dei messaggi SOAP, che include firma, crittografia e token di sicurezza. |
| **WS-Transaction** | Specifica per la gestione di transazioni distribuite tra più servizi. |
| **RPC (Remote Procedure Call)** | Stile di comunicazione in cui un client invoca una procedura (o metodo) su un server remoto. |
| **Risorsa** | Concetto centrale in REST. È un'entità (es. un oggetto, un documento) identificata da un URI. |
| **Stateless** | Principio secondo cui il server non memorizza lo stato della sessione del client tra una richiesta e l'altra. |
| **JSON (JavaScript Object Notation)** | Formato di interscambio dati leggero, molto popolare nelle API REST. |
| **OpenAPI (Swagger)** | Specifica per descrivere, produrre, consumare e visualizzare API RESTful, analoga al WSDL per SOAP. |
