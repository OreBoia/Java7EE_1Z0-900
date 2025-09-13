# Gestione delle Richieste e Risposte HTTP in una Servlet

Il cuore di ogni Servlet è l'elaborazione della richiesta HTTP in arrivo (`HttpServletRequest`) e la costruzione della risposta da inviare al client (`HttpServletResponse`). Questi due oggetti, forniti dal Web Container, offrono un'API completa per interagire con tutti gli aspetti del protocollo HTTP.

## L'Oggetto `HttpServletRequest`

Rappresenta la richiesta inviata dal client. Permette di accedere a tutte le informazioni in essa contenute.

- **Parametri della Richiesta**: Ottenuti dalla query string (es. `?id=123`) o dal corpo di un form (`POST` con `application/x-www-form-urlencoded`).
  - `String getParameter(String name)`: Restituisce il valore di un parametro come stringa.
  - `String[] getParameterValues(String name)`: Restituisce un array di stringhe, utile per parametri con valori multipli (es. checkbox).

- **Header HTTP**:
  - `String getHeader(String name)`: Restituisce il valore di un header (es. "User-Agent", "Accept-Language").
  - `long getDateHeader(String name)`: Metodo di convenienza per leggere header di tipo data.

- **Cookie**:
  - `Cookie[] getCookies()`: Restituisce un array di tutti i cookie inviati dal client.

- **Sessione**:
  - `HttpSession getSession()`: Restituisce la sessione corrente associata al client. Se non esiste, ne crea una nuova.
  - `HttpSession getSession(boolean create)`: Se `false`, restituisce `null` se non esiste una sessione, invece di crearla.

- **Corpo della Richiesta (Body)**: Per leggere dati grezzi inviati nel corpo di una richiesta (es. JSON, XML, upload di file).
  - `ServletInputStream getInputStream()`: Per leggere dati binari.
  - `BufferedReader getReader()`: Per leggere dati testuali.

- **Informazioni sul Percorso**:
  - `String getRequestURI()`: Restituisce l'URL completo dalla radice del server (es. `/my-app/user/profile`).
  - `String getContextPath()`: Restituisce il percorso del contesto dell'applicazione (es. `/my-app`).
  - `String getServletPath()`: Restituisce la parte dell'URL che ha attivato la Servlet (es. `/user`).

## L'Oggetto `HttpServletResponse`

Permette di costruire la risposta da inviare al client.

- **Codice di Stato HTTP**:
  - `void setStatus(int sc)`: Imposta il codice di stato (es. `200` OK, `404` Not Found).
  - `void sendError(int sc, String msg)`: Invia un codice di errore al client.
  - `void sendRedirect(String location)`: Invia una risposta di reindirizzamento (codice 302) al client, che effettuerà una nuova richiesta all'URL specificato.

- **Header HTTP**:
  - `void setHeader(String name, String value)`: Imposta il valore di un header.
  - `void setContentType(String type)`: Un metodo comune per impostare l'header `Content-Type` (es. "text/html", "application/json").

- **Corpo della Risposta (Body)**:
  - `PrintWriter getWriter()`: Restituisce un `PrintWriter` per scrivere risposte testuali (HTML, JSON, ecc.).
  - `ServletOutputStream getOutputStream()`: Restituisce un `ServletOutputStream` per scrivere risposte binarie (immagini, PDF, ecc.).
    *Importante: si può chiamare solo uno di questi due metodi per una data risposta.*

- **Cookie**:
  - `void addCookie(Cookie cookie)`: Aggiunge un cookie alla risposta.

## Esempio di Codice Completo

Questa Servlet mostra come leggere parametri, gestire una sessione e impostare cookie.

```java
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Gestione della Sessione
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 1;
        } else {
            visitCount++;
        }
        session.setAttribute("visitCount", visitCount);

        // 2. Lettura Parametri
        String username = request.getParameter("user");
        if (username == null) username = "Ospite";

        // 3. Aggiunta di un Cookie
        Cookie userCookie = new Cookie("lastUser", username);
        userCookie.setMaxAge(60 * 60 * 24); // Dura 1 giorno
        response.addCookie(userCookie);

        // 4. Scrittura della Risposta
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<html><body>");
            out.println("<h1>Profilo di " + username + "</h1>");
            out.println("<p>Questa è la tua visita numero: " + visitCount + "</p>");
            out.println("<p>User-Agent: " + request.getHeader("User-Agent") + "</p>");
            out.println("</body></html>");
        }
    }
}
```

## Tabella dei Termini e Metodi Chiave

| Termine/Metodo | Descrizione |
| :--- | :--- |
| **`request.getParameter(String)`** | Ottiene un parametro dalla richiesta (query string o form). |
| **`request.getHeader(String)`** | Legge il valore di un header HTTP. |
| **`request.getCookies()`** | Ottiene un array di tutti i cookie inviati dal client. |
| **`request.getSession()`** | Ottiene l'oggetto `HttpSession` associato al client (e lo crea se non esiste). |
| **`session.setAttribute(key, value)`** | Memorizza un oggetto nella sessione. |
| **`session.getAttribute(key)`** | Recupera un oggetto dalla sessione. |
| **`response.setStatus(int)`** | Imposta il codice di stato della risposta HTTP (es. 200, 404). |
| **`response.sendRedirect(String)`** | Invia una risposta di reindirizzamento (302) al browser. |
| **`response.setContentType(String)`** | Imposta il tipo MIME del corpo della risposta. |
| **`response.getWriter()`** | Ottiene un `PrintWriter` per scrivere una risposta testuale. |
| **`response.getOutputStream()`** | Ottiene un `ServletOutputStream` per scrivere una risposta binaria. |
| **`response.addCookie(Cookie)`** | Aggiunge un cookie alla risposta da inviare al client. |
| **`Cookie`** | Classe che rappresenta un cookie HTTP. |
| **`HttpSession`** | Oggetto che permette di mantenere lo stato e i dati di un utente tra più richieste. |
