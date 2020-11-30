package com.bluesoft.piko_e2e.http;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class JsonResponse {

    int statusCode;
    Map<String, List<String>> headers;
    JsonNode body;
    String rawBody;

}
