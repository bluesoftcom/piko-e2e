package com.bluesoft.piko_e2e.piko;

import com.bluesoft.piko_e2e.http.JsonResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.bluesoft.piko_e2e.http.HttpPreconditions.checkSuccessfulStatusCode;
import static com.bluesoft.piko_e2e.logging.HttpClientLogs.logRequest;
import static com.bluesoft.piko_e2e.logging.HttpClientLogs.logResponse;

@Component
@Slf4j
public class PikoMapsClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String url;
    private final ObjectMapper objectMapper;

    public PikoMapsClient(@Value("${piko-maps.url}") String url, ObjectMapper objectMapper) {
        this.url = url;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public JsonResponse listLocations() {
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/locations"))
                .timeout(Duration.ofSeconds(2))
                .build();

        logRequest(log, request);
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logResponse(log, response);

        checkSuccessfulStatusCode(response, "public-list-locations");
        return toJsonResponse(
                response,
                objectMapper.readValue(response.body(), JsonNode.class)
        );
    }

    private static JsonResponse toJsonResponse(HttpResponse<String> response, JsonNode body) {
        return new JsonResponse(
                response.statusCode(),
                response.headers().map(),
                body,
                response.body()
        );
    }

}
