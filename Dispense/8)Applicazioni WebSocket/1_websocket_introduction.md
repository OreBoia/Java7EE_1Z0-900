# Introduzione a WebSocket (JSR 356)

**WebSocket** è un protocollo di comunicazione che fornisce un canale di comunicazione **full-duplex** (bidirezionale) su una singola connessione TCP a lunga durata. Introdotto come parte di HTML5 e standardizzato in Java EE 7 tramite la specifica **JSR 356 (Java API for WebSocket)**, WebSocket è progettato per superare i limiti del modello richiesta-risposta di HTTP in applicazioni che richiedono interazioni in tempo reale.

Esempi tipici di utilizzo includono:

- Applicazioni di chat
- Sistemi di notifica push in tempo reale
- Dashboard finanziari con aggiornamenti live
- Giochi online multiplayer
- Applicazioni di IoT (Internet of Things)

## Differenze Fondamentali con HTTP

| Caratteristica      | HTTP (Request-Response)                                                              | WebSocket                                                                                             |
| ------------------- | ------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------- |
| **Modello**         | **Half-duplex**: Il client invia una richiesta, il server invia una risposta.          | **Full-duplex**: Client e server possono inviare dati indipendentemente e simultaneamente.            |
| **Connessione**     | **Stateless e a breve durata**: Una nuova connessione viene tipicamente aperta per ogni richiesta/risposta. | **Stateful e persistente**: La connessione rimane aperta, permettendo una comunicazione continua.     |
| **Latenza**         | **Più alta**: Ogni messaggio richiede l'overhead di una nuova richiesta HTTP (header, etc.). | **Più bassa**: Dopo l'handshake iniziale, i dati vengono scambiati in "frame" leggeri.                |
| **Comunicazione**   | **Iniziata dal client**: Il server può inviare dati solo in risposta a una richiesta del client. | **Bidirezionale**: Sia il client che il server possono iniziare una comunicazione in qualsiasi momento. |
| **Casi d'uso**      | Recupero di documenti, invio di form, API REST.                                      | Chat, notifiche in tempo reale, giochi, streaming di dati.                                            |

## Il Protocollo WebSocket: Handshake e Comunicazione

La comunicazione WebSocket inizia con un "aggiornamento" del protocollo HTTP, noto come **handshake**.

1. **Richiesta di Handshake del Client**: Il client (solitamente un browser tramite JavaScript) invia una normale richiesta HTTP GET al server, ma con degli header speciali che segnalano l'intenzione di passare al protocollo WebSocket.

    ```http
    GET /chat-app/chat HTTP/1.1
    Host: server.example.com
    Upgrade: websocket
    Connection: Upgrade
    Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
    Sec-WebSocket-Version: 13
    ```

    - `Upgrade: websocket`: Indica la volontà di cambiare protocollo.
    - `Connection: Upgrade`: Segnala che la connessione deve essere "aggiornata".
    - `Sec-WebSocket-Key`: Una chiave casuale usata per confermare che il server supporta WebSocket.

2. **Risposta di Handshake del Server**: Se il server supporta WebSocket e accetta la connessione, risponde con un codice di stato `101 Switching Protocols`.

    ```http
    HTTP/1.1 101 Switching Protocols
    Upgrade: websocket
    Connection: Upgrade
    Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
    ```

    - `101 Switching Protocols`: Conferma il passaggio di protocollo.
    - `Sec-WebSocket-Accept`: Una chiave calcolata a partire dalla `Sec-WebSocket-Key` del client, a dimostrazione che il server ha compreso la richiesta.

3. **Comunicazione Full-Duplex**: Una volta completato l'handshake, la connessione TCP sottostante viene mantenuta aperta e "promossa" a canale WebSocket. Da questo momento in poi, client e server possono scambiarsi messaggi (chiamati **frame**) in modo asincrono, senza l'overhead degli header HTTP.

## Tipi di Messaggi (Frame)

I messaggi WebSocket possono essere di due tipi principali:

- **Testo (Text)**: Messaggi codificati in UTF-8.
- **Binari (Binary)**: Dati binari grezzi.

Esistono anche frame di controllo per gestire la connessione, come `Close` (per chiudere la connessione) e `Ping`/`Pong` (per mantenerla attiva).

## Sessione WebSocket

Una volta stabilita la connessione, viene creata una **sessione WebSocket** (`javax.websocket.Session`). Questa sessione rappresenta la connessione attiva tra un client e un endpoint del server. È un oggetto distinto dalla sessione HTTP (`HttpSession`) e ha un proprio ciclo di vita, legato alla durata della connessione WebSocket. Attraverso la sessione è possibile inviare messaggi all'altro capo della connessione.

## Glossario dei Termini Importanti

| Termine                       | Definizione                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **Full-Duplex**               | Una comunicazione in cui i dati possono fluire in entrambe le direzioni simultaneamente.                                               |
| **Handshake**                 | Il processo di negoziazione iniziale, basato su HTTP, che stabilisce una connessione WebSocket tra client e server.                     |
| **`Upgrade` Header**          | L'header HTTP utilizzato nell'handshake per richiedere il passaggio dal protocollo HTTP al protocollo WebSocket.                        |
| **Codice di Stato `101`**     | La risposta HTTP `101 Switching Protocols` inviata dal server per confermare l'avvenuto passaggio al protocollo WebSocket.               |
| **Frame**                     | Un'unità di dati inviata su una connessione WebSocket. Può essere di tipo testo, binario o di controllo.                                |
| **Sessione WebSocket (`Session`)** | Un oggetto che rappresenta una singola connessione WebSocket tra un client e un endpoint server. Dura finché la connessione è aperta. |
| **JSR 356**                   | La specifica di Java EE 7 che definisce l'API standard per la creazione di applicazioni WebSocket in Java.                               |
| **URI WebSocket**             | L'indirizzo di un endpoint WebSocket. Utilizza gli schemi `ws://` (non sicuro) e `wss://` (sicuro, su TLS). Esempio: `wss://example.com/chat`. |
