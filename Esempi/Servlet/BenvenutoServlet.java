package Esempi.Servlet;

@WebServlet("/benvenuto")
public class BenvenutoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String user = (session != null) ? (String)
        session.getAttribute("user") : null;
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        if (user != null) {
            out.println("<h1>Benvenuto, " + user + "!</h1>");
            out.println("<a href='logout'>Logout</a>");
        } else {
            out.println("<h1>Utente non loggato</h1>");
            out.println("<a href='login'>Vai al login</a>");
        }
        out.println("</body></html>");
    }
}
