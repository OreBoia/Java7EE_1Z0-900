# Gestione dei Messaggi in WebSocket (Message Handling)

La Java API for WebSocket (JSR 356) offre un meccanismo flessibile per la gestione dei messaggi in arrivo, supportando nativamente messaggi di testo, dati binari, e la possibilità di definire trasformazioni personalizzate per convertire i messaggi in oggetti Java complessi.

La gestione avviene tramite uno o più metodi annotati con `@OnMessage` all'interno della classe dell'endpoint. Il container WebSocket invocherà il metodo corretto in base al tipo di messaggio ricevuto.

## Gestione dei Messaggi di Testo

I messaggi di testo sono il tipo più comune di comunicazione in molte applicazioni WebSocket (es. chat).

### Messaggi Completi (`String`)

Per messaggi di testo di piccole e medie dimensioni, è sufficiente definire un metodo `@OnMessage` che accetta una `String` come parametro.

```java
@OnMessage
public void handleTextMessage(String message, Session session) {
    System.out.println("Messaggio di testo ricevuto: " + message);
    // Logica per processare il messaggio
}
```

### Messaggi Grandi in Streaming (`Reader`)

Se ci si aspetta di ricevere messaggi di testo molto grandi, caricarli interamente in una `String` potrebbe consumare troppa memoria. In questo caso, si può ricevere un `java.io.Reader` per leggere il messaggio in streaming.

```java
@OnMessage
public void handleStreamingText(Reader reader, Session session) {
    // Legge il messaggio dal reader a blocchi (chunk)
    try (BufferedReader br = new BufferedReader(reader)) {
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println("Parte del messaggio: " + line);
        }
    } catch (IOException e) {
        // Gestisci l'errore di I/O
    }
}
```

## Gestione dei Messaggi Binari

Per lo scambio di file, immagini o qualsiasi dato non testuale, si usano i messaggi binari.

### Messaggi Completi (`ByteBuffer` o `byte[]`)

Similmente ai messaggi di testo, i messaggi binari completi possono essere ricevuti come `java.nio.ByteBuffer` o `byte[]`.

```java
// Usando ByteBuffer (più efficiente per operazioni di I/O)
@OnMessage
public void handleBinaryMessage(ByteBuffer message, Session session) {
    System.out.println("Messaggio binario ricevuto: " + message.remaining() + " bytes.");
    // Logica per processare i dati binari
}

// Usando byte[] (più semplice da manipolare)
@OnMessage
public void handleBinaryMessage(byte[] message, Session session) {
    System.out.println("Messaggio binario ricevuto: " + message.length + " bytes.");
}
```

### Messaggi Grandi in Streaming (`InputStream`)

Per messaggi binari di grandi dimensioni, si può usare un `java.io.InputStream` per leggerli in streaming ed evitare un consumo eccessivo di memoria.

```java
@OnMessage
public void handleStreamingBinary(InputStream inputStream, Session session) {
    try {
        // Processa i dati dallo stream
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            // ... fai qualcosa con il buffer ...
        }
    } catch (IOException e) {
        // Gestisci l'errore
    }
}
```

## Messaggi Frammentati (Parziali)

WebSocket permette di inviare un singolo messaggio suddiviso in più frame (frammentazione). Per gestire questa casistica, si può aggiungere un parametro `boolean` al metodo `@OnMessage`. Il container lo imposterà a `true` solo per l'ultimo frammento del messaggio.

```java
private StringBuilder textBuffer = new StringBuilder();

@OnMessage
public void handlePartialTextMessage(String partialMessage, boolean last) {
    textBuffer.append(partialMessage);
    if (last) {
        // Messaggio completo ricevuto
        System.out.println("Messaggio completo: " + textBuffer.toString());
        textBuffer.setLength(0); // Resetta il buffer per il prossimo messaggio
    }
}
```

## Encoder e Decoder: Messaggi come Oggetti Java

Una delle funzionalità più potenti di JSR 356 è la capacità di convertire automaticamente i messaggi in oggetti Java (POJO) e viceversa, tramite **Decoder** e **Encoder**. Questo permette di lavorare con un modello a oggetti anziché con stringhe JSON o dati binari grezzi.

- **Decoder**: Converte un messaggio in entrata (testo o binario) in un oggetto Java.
- **Encoder**: Converte un oggetto Java in uscita in un messaggio (testo o binario) da inviare.

### Esempio: Decoder per Messaggi JSON

Supponiamo di voler scambiare oggetti `ChatMessage` in formato JSON.

**1. Il POJO `ChatMessage`**

```java
public class ChatMessage {
    private String from;
    private String content;
    // Getter e Setter...
}
```

**2. Il `ChatMessageDecoder`**
Implementa `Decoder.Text<T>`, dove `T` è il tipo di oggetto da produrre. Si usa una libreria come Jackson o GSON per il parsing del JSON.

```java
import com.google.gson.Gson; // Esempio con GSON
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class ChatMessageDecoder implements Decoder.Text<ChatMessage> {
    private static Gson gson = new Gson();

    @Override
    public ChatMessage decode(String s) {
        return gson.fromJson(s, ChatMessage.class);
    }

    @Override
    public boolean willDecode(String s) {
        // Ritorna true se il messaggio può essere decodificato
        return (s != null);
    }
    
    @Override public void init(EndpointConfig config) { /* Inizializzazione */ }
    @Override public void destroy() { /* Pulizia */ }
}
```

**3. L'Endpoint con il Decoder**
Si registra il decoder nell'annotazione `@ServerEndpoint`.

```java
@ServerEndpoint(
    value = "/chat",
    decoders = { ChatMessageDecoder.class } // Registra il decoder
    // encoders = { ChatMessageEncoder.class } // E un potenziale encoder
)
public class ChatEndpoint {

    // Il container usa il decoder per convertire il messaggio JSON in un oggetto ChatMessage
    @OnMessage
    public void handleChatMessage(ChatMessage message, Session session) {
        System.out.println("Messaggio da " + message.getFrom() + ": " + message.getContent());
        // ...
    }
}
```

## Tabella Riassuntiva dei Parametri `@OnMessage`

| Tipo di Messaggio     | Parametro del Metodo                               | Uso                                                              |
| --------------------- | -------------------------------------------------- | ---------------------------------------------------------------- |
| Testo completo        | `String message`                                   | Per messaggi di testo di dimensioni contenute.                   |
| Testo in streaming    | `Reader reader`                                    | Per messaggi di testo di grandi dimensioni.                      |
| Binario completo      | `ByteBuffer buffer` o `byte[] data`                | Per messaggi binari di dimensioni contenute.                     |
| Binario in streaming  | `InputStream stream`                               | Per messaggi binari di grandi dimensioni.                        |
| Oggetto personalizzato| `MyObject obj`                                     | Per messaggi che vengono decodificati automaticamente da un `Decoder`. |
| Frammentato           | `(String/ByteBuffer part, boolean last)`           | Per gestire messaggi inviati in più parti.                       |

## Glossario dei Termini Importanti

| Termine                 | Definizione                                                                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Message Handling**    | Il processo di ricezione, interpretazione e reazione ai messaggi inviati da un client WebSocket.                                       |
| **Decoder**             | Un componente che trasforma un messaggio WebSocket grezzo (testo o binario) in un oggetto Java complesso (POJO).                       |
| **Encoder**             | Un componente che trasforma un oggetto Java (POJO) in un messaggio WebSocket grezzo (testo o binario) da inviare a un client.          |
| **Messaggio Frammentato** | Un singolo messaggio logico che viene suddiviso e inviato in più frame WebSocket. Utile per messaggi molto grandi o in streaming.      |
| **Streaming**           | La pratica di processare i dati (testo o binari) a blocchi (chunk) man mano che arrivano, invece di caricarli tutti in memoria.         |
