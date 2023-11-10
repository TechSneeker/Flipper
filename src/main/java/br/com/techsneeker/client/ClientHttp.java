package br.com.techsneeker.client;

import br.com.techsneeker.object.enums.Endpoint;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ClientHttp {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public long getLastUpdated(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return jsonObject.get("lastUpdated").getAsLong();
    }

    public String getAuction() {
        return executeRequest(Endpoint.AUCTION);
    }

    public String getLowestBin() {
        return executeRequest(Endpoint.LOWEST_BIN);
    }

    private String executeRequest(String endpoint) {
        HttpRequest request = HttpRequest
                .newBuilder().uri(URI.create(endpoint)).GET().build();

        HttpResponse<String> response;

        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Request interrupted", e);
        }

        return response.body();
    }

}
