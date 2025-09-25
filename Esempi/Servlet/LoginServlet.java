package Esempi.Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Simuliamo un servizio di autenticazione, in scenario reale sarebbe EJB/CDI
    private Map<String, String> utenti = new HashMap<>();
    @Override
    public void init() throws ServletException {
        // Inizializza qualche utente fittizio
        utenti.put("alice", "1234");
        utenti.put("bob", "abcd");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Messaggio di errore se presente come param (es. ?error=1)
        String error = req.getParameter("error");
        out.println("<!DOCTYPE html><html><body>");

        if ("1".equals(error)) {
            out.println("<p style='color:red;'>Credenziali non valide,riprova.</p>");
        }

        out.println("<form method='POST' action='login'>");
        out.println("Utente: <input name='username'/><br/>");
        out.println("Password: <input type='password' name='password'/><br/>");
        out.println("<button type='submit'>Login</button>");
        out.println("</form></body></html>");
        
        Cookie[] cookies = req.getCookies();
        if(cookies != null){
            for(Cookie c: cookies){
                if(c.getName().equals("utente")){
                    out.println("<input name='username' value='"+c.getValue()+"'/>");
                    //...
                }
            }
        }
        
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

        String user = req.getParameter("username");
        String pass = req.getParameter("password");
        
        if (user != null && pass != null && pass.equals(utenti.get(user))) {
            // Credenziali valide
            req.getSession().setAttribute("user", user);
            resp.sendRedirect("benvenuto"); // pagina protetta
            
            Cookie ck = new Cookie("utente", user);
            ck.setMaxAge(60*60*24*30); // 30 giorni
            resp.addCookie(ck);
            
        } else {
            // Credenziali non valide, rimanda a login con parametro di errore
            resp.sendRedirect("login?error=1");
        }
    }
}

