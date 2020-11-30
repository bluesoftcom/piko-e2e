package com.bluesoft.piko_e2e.piko;

import com.bluesoft.piko_e2e.cognito.CognitoUser;
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
public class PikoAdminClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String url;
    private final ObjectMapper objectMapper;

    public PikoAdminClient(@Value("${piko-admin.url}") String url, ObjectMapper objectMapper) {
        this.url = url;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public JsonResponse listLocations(CognitoUser user) {
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/locations"))
                .timeout(Duration.ofSeconds(2))
                .header("Authorization", user.getIdToken())
                .build();

        logRequest(log, request);
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logResponse(log, response);

        checkSuccessfulStatusCode(response, "admin-list-locations");
        return toJsonResponse(
                response,
                objectMapper.readValue(response.body(), JsonNode.class)
        );
    }

    @SneakyThrows
    public JsonResponse changeLocationStatus(CognitoUser user, String locationId, JsonNode body) {
        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .uri(URI.create(url + "/locations/" + locationId + "/status"))
                .timeout(Duration.ofSeconds(2))
                .header("Authorization", user.getIdToken())
                .header("Content-Type", "application/json")
                .build();

        logRequest(log, request);
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logResponse(log, response);

        checkSuccessfulStatusCode(response, "admin-change-location-status");
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
