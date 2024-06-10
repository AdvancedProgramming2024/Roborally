package dk.dtu.compute.se.pisd.roborally.online;

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
import java.util.HashMap;
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

    public static Response<JsonObject> postRequestJson(URI location, Map<String, JsonElement> args) throws IOException, InterruptedException{
        Map<String, Object> args2 = new HashMap<>(args.size());
        args.forEach((key, value) -> {
            args2.put(key, value.toString());
        });
        Response<String> response = postRequest(location, args2);
        try {
            return new Response<>(response.getStatusCode(), jsonParser.parse(response.item).getAsJsonObject());
        } catch (IllegalStateException e) {
            System.out.println("posted to: " + location + "with payload: " + args);
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
