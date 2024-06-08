package dk.dtu.compute.se.pisd.roborally.online;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.coyote.Response;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestCenter {
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final JsonParser jsonParser = new JsonParser();

    public static Response<String> postRequest(URI location, String string) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(location)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(string))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static Response<String> getRequest(URI location) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(location)
                .GET()
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static Response<JsonObject> postRequestJson(URI location, JsonElement json) throws IOException, InterruptedException{
        Response<String> response = postRequest(location, json);
        try {
            return new JsonResponse(response);
        } catch (IllegalStateException e) {
            System.out.println("posted to: " + location +"with payload: " + json);
            System.out.println("response: " + response);
            throw e;
        }
    }

    public static Response<JsonObject> getRequestJson(URI location) throws IOException, InterruptedException{
        Response<String> response = getRequest(location);
        try {
            return new JsonResponse(response);
        } catch (IllegalStateException e) {
            System.out.println("got from: " + location);
            System.out.println("response: " + response);
            throw e;
        }
    }
}
