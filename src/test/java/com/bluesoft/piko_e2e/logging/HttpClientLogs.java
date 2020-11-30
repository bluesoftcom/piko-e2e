package com.bluesoft.piko_e2e.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

@Slf4j
public class HttpClientLogs {

    private static final List<String> MASKED_HEADER_VALUE = List.of("***");
    private static final Set<String> LOGGABLE_FORMATS = Set.of(
            "application/json",
            "application/xml",
            "text/html",
            "text/plain"
    );

    public static void logRequest(Logger log, HttpRequest request) {
        final StringBuilder sink = new StringBuilder();

        sink.append(String.format("Making request to: %s %s", request.method(), request.uri())).append('\n');
        logHeaders(sink, request.headers());

        if (request.bodyPublisher().isEmpty()) {
            log.info(sink.toString());
            return;
        }

        final String mediaType = findBodyContentType(request.headers());
        if (!formatIsAvailableForLogging(mediaType)) {
            sink.append("Body: Binary content or format is not loggable").append('\n');
            return;
        }

        request.bodyPublisher().orElseThrow()
                .subscribe(new LoggingSubscriber(log, sink));
    }

    public static void logResponse(Logger log, HttpResponse<?> response) {
        final StringBuilder sink = new StringBuilder();

        sink.append(String.format("Response from: %s %s", response.request().method(), response.uri())).append('\n');
        sink.append(String.format("Status code: %s", response.statusCode())).append('\n');
        logHeaders(sink, response.headers());

        final Object body = response.body();
        if (body == null) {
            log.info(sink.toString());
            return;
        }

        final String mediaType = findBodyContentType(response.headers());
        if (!formatIsAvailableForLogging(mediaType)) {
            sink.append("Body: Binary content or format is not loggable").append('\n');
            log.info(sink.toString());
            return;
        }

        sink.append("Body: \n").append(response.body().toString());
        log.info(sink.toString());
    }

    private static String findBodyContentType(HttpHeaders headers) {
        return headers.firstValue("Content-Type")
                .orElse("application/octet-stream");
    }

    private static void logHeaders(StringBuilder sink, HttpHeaders headers) {
        sink.append("Headers:").append('\n');
        for (var entry : headers.map().entrySet()) {
            final String header = entry.getKey();
            final List<String> values = isSensitiveHeader(header)
                    ? MASKED_HEADER_VALUE
                    : entry.getValue();
            sink.append(String.format("  %s: %s", header, values)).append('\n');
        }
    }

    private static boolean formatIsAvailableForLogging(String mediaType) {
        return LOGGABLE_FORMATS.stream()
                .anyMatch(mediaType::equals);
    }

    private static boolean isSensitiveHeader(String header) {
        return header.equalsIgnoreCase("Authorization");
    }

}
