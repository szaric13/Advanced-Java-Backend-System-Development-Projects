package rs.raf.demo;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    private String message;

    // Promenljive u servletu nisu thread safe!
    private int counter = 0;

    public HelloServlet() {
        System.out.println("Constructor");
    }

    public void init() {
        System.out.println("init method");

        message = "Hello from Servlet!";
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("service method");
        super.service(req, resp);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("doGet method");

        response.setContentType("text/html");

        synchronized (this) {
            this.counter ++;
        }

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("<h2>Korisnik: " + request.getSession().getId() + "</h2>");
        if (request.getSession().getAttribute("jmbg") != null) {
            out.println("<h2>JMBG: " + request.getSession().getAttribute("jmbg") + "</h2>");
        }
        if (request.getParameter("ime") != null && request.getParameter("prezime") != null) {
            out.println("<h2>Ime i prezime: " + request.getParameter("ime") + " " + request.getParameter("prezime"));
        }
        out.println("<h3>Broj poseta: " + this.counter + "</h3>");

        out.println();

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("jmbg") == null || request.getParameter("jmbg").equals("")) {
            response.getOutputStream().println("JMBG je obavezno polje");
            response.setStatus(403);
            return;
        }

        request.getSession().setAttribute("jmbg", request.getParameter("jmbg"));

        response.sendRedirect("/hello-servlet");
    }

    public void destroy() {
        System.out.println("destroy method");
    }
}
