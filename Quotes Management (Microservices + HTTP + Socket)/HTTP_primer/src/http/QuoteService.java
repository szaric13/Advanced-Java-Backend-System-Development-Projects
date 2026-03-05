package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuoteService {
    private static final List<String> quotes = Arrays.asList(
            "Covek koji nema neprijatelja i nije covek. - Ivo Andric",
            "Vreme radi za onoga ko zna da ceka. - Dositej Obradovic",
            "Ko drugome jamu kopa, sam u nju upada. - Narodna poslovica",
            "Ljudi se dele na one koji znaju i one koji veruju. - Jovan Ducic",
            "Samo onaj ko zna gde ide, moze stici tamo. - Mesa Selimovic",
            "Malo je onih koji vide svojim ocima i osećaju svojim srcem. - Nikola Tesla",
            "Nema te mraka koji moze ugasiti svetlost jedne svece. - Patrijarh Pavle",
            "Nije siromasan onaj koji malo ima, vec onaj koji mnogo zeli. - Narodna poslovica"
    );


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9001)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    String request = in.readLine();
                    if ("GET_QUOTE".equals(request)) {
                        String quote = quotes.get(new Random().nextInt(quotes.size()));
                        out.println(quote);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
