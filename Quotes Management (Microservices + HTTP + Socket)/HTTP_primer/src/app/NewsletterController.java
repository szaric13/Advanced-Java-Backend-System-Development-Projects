package app;

import http.Request;
import http.response.HtmlResponse;
import http.response.RedirectResponse;
import http.response.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NewsletterController extends Controller {
    private static List<String> quotes = new ArrayList<>();

    public NewsletterController(Request request) {
        super(request);
    }

    private String fetchQuoteOfTheDay() {
        try (Socket socket = new Socket("localhost", 9001);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("GET_QUOTE");
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "Greska pri preuzimanju citata";
        }
    }

    @Override
    public Response doGet() {
        String quoteOfTheDay = fetchQuoteOfTheDay();
        StringBuilder savedQuotes = new StringBuilder();
        for (String quote : quotes) {
            savedQuotes.append("<div class='quote-box'>").append(quote).append("</div>");
        }

        String htmlBody = "" +
                "<style>"
                + "body { font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: auto; }"
                + "form { margin-bottom: 20px; }"
                + "input, button { display: block; margin-top: 5px; width: 100%; padding: 8px; }"
                + "button { background-color: blue; color: white; border: none; cursor: pointer; }"
                + "h2 { margin-top: 20px; }"
                + ".quote-box { background: #f0f0f0; padding: 10px; margin: 5px 0; border-radius: 5px; }"
                + "</style>"
                + "<form method=\"POST\" action=\"/save-quote\">"
                + "<label>Autor:</label><input name=\"author\" type=\"text\" required>"
                + "<label>Citat:</label><input name=\"quote\" type=\"text\" required>"
                + "<button type=\"submit\">Sacuvaj citat</button>"
                + "</form>"
                + "<h2>Citat dana:</h2>"
                + "<p><i>" + quoteOfTheDay + "</i></p>"
                + "<h2>Sacuvani citati</h2>" + savedQuotes.toString();

        String content = "<html><head><title>Citati</title></head><body>" + htmlBody + "</body></html>";
        return new HtmlResponse(content);
    }

    @Override
    public Response doPost() {
        String author = request.getParameter("author");
        String quote = request.getParameter("quote");
        if (author != null && quote != null) {
            quotes.add("<strong>" + author + ":</strong> \"" + quote + "\"");
        }
        return new RedirectResponse("/newsletter");
    }
}
