package dtu.compute.roborally.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public abstract class RequestCenter {
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final JsonParser jsonParser = new JsonParser();

    public static Response<String> postRequest(URI location, Map<String, Object> args) throws IOException, InterruptedException{
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(args);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(location)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response<>(httpResponse);
    }

    public static Response<String> getRequest(URI location) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder(location).GET().build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response<>(httpResponse);
    }

    public static Response<JsonObject> postRequestJson(URI location, JsonElement json) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(location)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        Response<String> response = new Response<>(httpResponse);
        try {
            if (response.getItem().isEmpty())
                return new Response<>(response.getStatusCode(), null);
            return new Response<>(response.getStatusCode(), jsonParser.parse(response.item).getAsJsonObject());
        } catch (IllegalStateException e) {
            System.out.println("posted to: " + location + "with payload: " + json);
            System.out.println("response: " + response);
            throw e;
        }
    }

    public static Response<JsonObject> getRequestJson(URI location) throws IOException, InterruptedException{
        Response<String> response = getRequest(location);
        try {
            if (response.getStatusCode() == HttpStatus.NOT_FOUND)
                return new Response<>(response.getStatusCode(), null);
            return new Response<>(response.getStatusCode(), jsonParser.parse(response.item).getAsJsonObject());
        } catch (IllegalStateException e) {
            System.out.println("got from: " + location);
            System.out.println("response: " + response);
            throw e;
        }
    }
}
