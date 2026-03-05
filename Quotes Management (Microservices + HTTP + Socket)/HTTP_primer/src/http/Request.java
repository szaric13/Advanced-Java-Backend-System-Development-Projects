package http;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private final HttpMethod httpMethod;
    private final String path;
    private final Map<String, String> parameters;

    public Request(HttpMethod httpMethod, String path) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.parameters = new HashMap<>();
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setParameters(String body) {
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1].replace("+", " ").replace("%20", " "));
            }
        }
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
}
