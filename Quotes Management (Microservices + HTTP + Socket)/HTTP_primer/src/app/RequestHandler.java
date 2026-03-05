package app;

import http.HttpMethod;
import http.Request;
import http.response.Response;

public class RequestHandler {
    public Response handle(Request request) throws Exception {
        System.out.println("Received request: " + request.getHttpMethod() + " " + request.getPath());

        if (request.getPath().equals("/newsletter") && request.getHttpMethod() == HttpMethod.GET) {
            return new NewsletterController(request).doGet();
        }

        if (request.getPath().equals("/save-quote") && request.getHttpMethod() == HttpMethod.POST) {
            return new NewsletterController(request).doPost();
        }


        throw new Exception("Page: " + request.getPath() + ". Method: " + request.getHttpMethod() + " not found!");
    }

}
