# Esempio di Configurazione XML per JMS

Questo è un esempio di configurazione per JMS (Java Message Service) in un server di applicazioni Java EE come WildFly.
Questo file (solitamente `standalone.xml`, `standalone-full.xml`, o un file di configurazione simile) definisce le destinazioni JMS (code e argomenti) e le connection factory necessarie per le applicazioni per inviare e ricevere messaggi.

```xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:13.0">
    <server name="default">
        <!--
        Impostazioni di sicurezza. Qui definiamo i ruoli e i permessi.
        In questo esempio, il ruolo 'guest' ha permessi per inviare, consumare, creare e cancellare code non durature.
        -->
        <security-setting name="#">
            <role name="guest" send="true" consume="true" create-non-durable-queue="true" delete-non-durable-queue="true"/>
        </security-setting>

        <!-- Impostazioni generali per gli indirizzi, come la gestione delle lettere morte (DLQ) e dei messaggi scaduti. -->
        <address-setting name="#" dead-letter-address="jms.queue.DLQ" expiry-address="jms.queue.ExpiryQueue" max-size-bytes="10485760" page-size-bytes="2097152" message-counter-history-day-limit="10"/>

        <!--
        Definizione di una Coda (Queue) JMS.
        L'attributo 'name' è il nome della coda nel sistema di messaging.
        L'attributo 'entries' definisce i nomi JNDI con cui l'applicazione può cercare questa coda.
        'java:/jms/queue/MyQueue' è un nome JNDI locale.
        'java:jboss/exported/jms/queue/MyQueue' espone la coda a client remoti.
        -->
        <jms-queue name="MyQueue" entries="java:/jms/queue/MyQueue java:jboss/exported/jms/queue/MyQueue"/>

        <!--
        Definizione di un Argomento (Topic) JMS.
        Simile alla coda, 'name' è il nome del topic e 'entries' sono i nomi JNDI.
        I messaggi inviati a un topic vengono ricevuti da tutti i sottoscrittori.
        -->
        <jms-topic name="MyTopic" entries="java:/jms/topic/MyTopic java:jboss/exported/jms/topic/MyTopic"/>

        <!-- Code di servizio per messaggi non recapitabili (Dead Letter Queue) e messaggi scaduti. -->
        <jms-queue name="DLQ" entries="java:/jms/queue/DLQ"/>
        <jms-queue name="ExpiryQueue" entries="java:/jms/queue/ExpiryQueue"/>

        <!--
        Definizione delle Connection Factory.
        Una Connection Factory è utilizzata dalle applicazioni client per creare connessioni al provider JMS.
        -->

        <!-- Connection Factory per client locali (in-VM). -->
        <connection-factory name="InVmConnectionFactory" entries="java:/ConnectionFactory" connectors="in-vm"/>

        <!-- Connection Factory per client remoti, che si connettono tramite HTTP. -->
        <connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>

        <!--
        Pooled Connection Factory per l'integrazione con il container EJB (es. per i Message-Driven Beans).
        Supporta transazioni distribuite (XA).
        'java:jboss/DefaultJMSConnectionFactory' è il nome JNDI standard per la connection factory di default.
        -->
        <pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>
    </server>
</subsystem>
```
