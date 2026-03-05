package rs.raf.demo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@WebServlet(name = "mealServlet", urlPatterns = {"/meal-select", "/order-confirmed", "/selected-meals"})
public class MealServlet extends HttpServlet {

    private Map<String, List<String>> jelaZaDane;
    private List<PorudzbinaJela> porudzbine;
    private String lozinka;
    private Set<String> zavrseneSesije;

    @Override
    public void init() throws ServletException {
        System.out.println("MealServlet initialized");
        jelaZaDane = new HashMap<>();
        porudzbine = new ArrayList<>();
        zavrseneSesije = Collections.synchronizedSet(new HashSet<>());
        ucitajJelaZaDan("ponedeljak");
        ucitajJelaZaDan("utorak");
        ucitajJelaZaDan("sreda");
        ucitajJelaZaDan("cetvrtak");
        ucitajJelaZaDan("petak");
        ucitajLozinku();
    }

    private void ucitajJelaZaDan(String dan) {
        List<String> jela = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dan + ".txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String linija;
            while ((linija = reader.readLine()) != null) {
                if (!linija.trim().isEmpty()) {
                    jela.add(linija.trim());
                }
            }
            jelaZaDane.put(dan, jela);
        } catch (IOException | NullPointerException e) {
            jela.add("Jelo 1 za " + dan);
            jela.add("Jelo 2 za " + dan);
            jela.add("Jelo 3 za " + dan);
            jelaZaDane.put(dan, jela);
        }
    }

    private void ucitajLozinku() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("password.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            lozinka = reader.readLine();
            if (lozinka == null) {
                lozinka = "admin";
            } else {
                lozinka = lozinka.trim();
            }
        } catch (IOException | NullPointerException e) {
            lozinka = "admin";
        }
    }

    @Override
    protected void doGet(HttpServletRequest zahtev, HttpServletResponse odgovor) throws ServletException, IOException {
        zahtev.setCharacterEncoding("UTF-8");
        odgovor.setCharacterEncoding("UTF-8");

        String putanja = zahtev.getServletPath();
        switch (putanja) {
            case "/meal-select":
                prikaziFormuZaOdabirJela(zahtev, odgovor);
                break;
            case "/order-confirmed":
                prikaziPotvrduPorudzbine(odgovor);
                break;
            case "/selected-meals":
                prikaziSvePorudzbine(zahtev, odgovor);
                break;
            default:
                odgovor.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest zahtev, HttpServletResponse odgovor) throws ServletException, IOException {
        zahtev.setCharacterEncoding("UTF-8");
        odgovor.setCharacterEncoding("UTF-8");

        String putanja = zahtev.getServletPath();
        if ("/meal-select".equals(putanja)) {
            obradiPorudzbinu(zahtev, odgovor);
        } else if ("/selected-meals".equals(putanja) && "delete".equals(zahtev.getParameter("action"))) {
            obrisiSvePorudzbine(zahtev, odgovor);
        } else {
            odgovor.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void prikaziFormuZaOdabirJela(HttpServletRequest zahtev, HttpServletResponse odgovor) throws IOException {
        HttpSession sesija = zahtev.getSession();
        String idSesije = sesija.getId();

        if (zavrseneSesije.contains(idSesije)) {
            prikaziPorukuVecNaruceno(odgovor, sesija);
            return;
        }

        prikaziFormu(odgovor);
    }

    private void prikaziPorukuVecNaruceno(HttpServletResponse odgovor, HttpSession sesija) throws IOException {
        odgovor.setContentType("text/html;charset=UTF-8");
        PrintWriter out = odgovor.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Narudžbina je već napravljena</title>");
        out.println("<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>");
        out.println("</head><body>");
        out.println("<h1>Narudžbina je već napravljena</h1>");
        out.println("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px;'>");
        out.println("<h2>Vaša odabrana jela:</h2><ul>");

        Map<String, String> izabranaJela = (Map<String, String>) sesija.getAttribute("selectedMeals");
        if (izabranaJela != null) {
            izabranaJela.forEach((dan, jelo) ->
                    out.println("<li><strong>" + velikaSlova(dan) + "</strong>: " + jelo + "</li>"));
        }

        out.println("</ul></div>");
        out.println("<p>Ne možete menjati svoju porudžbinu dok administrator ne obriše sve porudžbine.</p>");
        out.println("</body></html>");
    }

    private void prikaziFormu(HttpServletResponse odgovor) throws IOException {
        odgovor.setContentType("text/html;charset=UTF-8");
        PrintWriter out = odgovor.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Odabir jela</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println(".form-group { margin-bottom: 20px; }");
        out.println("select { width: 100%; padding: 8px; }");
        out.println("button { background-color: #3498db; color: white; padding: 10px 15px; border: none; }");
        out.println("</style></head><body>");
        out.println("<h1>Odabir jela za narednu nedelju</h1>");
        out.println("<form method='post' action='meal-select'>");

        Arrays.asList("ponedeljak", "utorak", "sreda", "cetvrtak", "petak").forEach(dan -> {
            out.println("<div class='form-group'>");
            out.println("<label for='" + dan + "'>" + velikaSlova(dan) + ":</label>");
            out.println("<select name='" + dan + "' id='" + dan + "' required>");
            out.println("<option value=''>Izaberite jelo</option>");

            jelaZaDane.getOrDefault(dan, Collections.emptyList()).forEach(jelo ->
                    out.println("<option value='" + htmlEscape(jelo) + "'>" + jelo + "</option>"));

            out.println("</select></div>");
        });

        out.println("<button type='submit'>Potvrdi izbor</button>");
        out.println("</form></body></html>");
    }

    private void prikaziPotvrduPorudzbine(HttpServletResponse odgovor) throws IOException {
        odgovor.setContentType("text/html;charset=UTF-8");
        PrintWriter out = odgovor.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Porudžbina potvrđena</title>");
        out.println("<style>body { text-align: center; }</style>");
        out.println("</head><body>");
        out.println("<h1>Porudžbina uspešno potvrđena</h1>");
        out.println("<p style='color: #27ae60;'>Vaša narudžbina je uspešno zabeležena.</p>");
        out.println("<p>Hvala Vam!</p>");
        out.println("</body></html>");
    }

    private void prikaziSvePorudzbine(HttpServletRequest zahtev, HttpServletResponse odgovor) throws IOException {
        String unetaLozinka = zahtev.getParameter("lozinka");
        if (!lozinka.equals(unetaLozinka)) {
            prikaziOdbijenPristup(odgovor);
            return;
        }

        odgovor.setContentType("text/html;charset=UTF-8");
        PrintWriter out = odgovor.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Pregled porudžbina</title>");
        out.println("<style>");
        out.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        out.println("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        out.println(".delete-button { background-color: #e74c3c; color: white; padding: 10px 15px; border: none; }");
        out.println("</style></head><body>");
        out.println("<h1>Pregled svih porudžbina</h1>");

        if (porudzbine.isEmpty()) {
            out.println("<p>Trenutno nema porudžbina.</p>");
        } else {
            Arrays.asList("ponedeljak", "utorak", "sreda", "cetvrtak", "petak").forEach(dan -> {
                Map<String, Integer> brojanja = new HashMap<>();
                porudzbine.forEach(porudzbina -> {
                    String jelo = porudzbina.vratiJeloZaDan(dan);
                    if (jelo != null) {
                        brojanja.put(jelo, brojanja.getOrDefault(jelo, 0) + 1);
                    }
                });

                out.println("<h2>" + velikaSlova(dan) + "</h2>");
                out.println("<table><tr><th>Jelo</th><th>Broj porudžbina</th></tr>");

                if (brojanja.isEmpty()) {
                    out.println("<tr><td colspan='2'>Nema porudžbina</td></tr>");
                } else {
                    brojanja.forEach((jelo, broj) ->
                            out.println("<tr><td>" + jelo + "</td><td>" + broj + "</td></tr>"));
                }

                out.println("</table>");
            });
        }

        out.println("<form method='post' action='selected-meals?lozinka=" + lozinka + "'>");
        out.println("<input type='hidden' name='action' value='delete'>");
        out.println("<button type='submit' class='delete-button'>Obriši sve porudžbine</button>");
        out.println("</form></body></html>");
    }

    private void prikaziOdbijenPristup(HttpServletResponse odgovor) throws IOException {
        odgovor.setContentType("text/html;charset=UTF-8");
        PrintWriter out = odgovor.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Pristup odbijen</title>");
        out.println("</head><body>");
        out.println("<h1 style='color: #e74c3c;'>Pristup odbijen</h1>");
        out.println("<p>Niste uneli ispravnu lozinku.</p>");
        out.println("</body></html>");
    }

    private void obradiPorudzbinu(HttpServletRequest zahtev, HttpServletResponse odgovor) throws IOException {
        HttpSession sesija = zahtev.getSession();
        String idSesije = sesija.getId();

        if (zavrseneSesije.contains(idSesije)) {
            odgovor.sendRedirect("meal-select");
            return;
        }

        Map<String, String> izabranaJela = new HashMap<>();
        boolean sveOdabrano = true;

        for (String dan : Arrays.asList("ponedeljak", "utorak", "sreda", "cetvrtak", "petak")) {
            String jelo = zahtev.getParameter(dan);
            if (jelo == null || jelo.trim().isEmpty()) {
                sveOdabrano = false;
                break;
            }
            izabranaJela.put(dan, jelo);
        }

        if (!sveOdabrano) {
            odgovor.sendRedirect("meal-select");
            return;
        }

        synchronized (this) {
            porudzbine.add(new PorudzbinaJela(idSesije, izabranaJela));
            zavrseneSesije.add(idSesije);
        }

        sesija.setAttribute("selectedMeals", izabranaJela);
        odgovor.sendRedirect("order-confirmed");
    }

    private void obrisiSvePorudzbine(HttpServletRequest zahtev, HttpServletResponse odgovor) throws IOException {
        String unetaLozinka = zahtev.getParameter("lozinka");
        if (lozinka.equals(unetaLozinka)) {
            synchronized (this) {
                porudzbine.clear();
                zavrseneSesije.clear();
            }
            odgovor.sendRedirect("selected-meals?lozinka=" + lozinka);
        } else {
            odgovor.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private String velikaSlova(String tekst) {
        return tekst.substring(0, 1).toUpperCase() + tekst.substring(1);
    }

    private String htmlEscape(String tekst) {
        return tekst.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static class PorudzbinaJela {
        private final String korisnikId;
        private final Map<String, String> jela;

        public PorudzbinaJela(String korisnikId, Map<String, String> jela) {
            this.korisnikId = korisnikId;
            this.jela = new HashMap<>(jela);
        }

        public String vratiJeloZaDan(String dan) {
            return jela.get(dan);
        }
    }
}
