package br.com.techsneeker.client;

import br.com.techsneeker.object.enums.Endpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientHttp {

    private final HttpClient CLIENT = HttpClient.newBuilder().build();;

    public String getAuction() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Endpoint.AUCTION)).GET().build();

        return execute(request);
    }

    public String getLowestBin() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Endpoint.LOWEST_BIN)).GET().build();

        return execute(request);
    }

    private String execute(HttpRequest request) {
        HttpResponse<String> response;

        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Request interrupted");
        }

        return response.body();
    }

}
