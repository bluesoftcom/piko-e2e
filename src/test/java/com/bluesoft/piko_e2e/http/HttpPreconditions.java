package com.bluesoft.piko_e2e.http;

import java.net.http.HttpResponse;

public class HttpPreconditions {

    public static void checkSuccessfulStatusCode(HttpResponse<String> response, String operation) {
        if (response.statusCode() != 200) {
            throw new HttpStatusCodeException("Unexpected status code: " + response.statusCode() + " received from operation: " + operation);
        }
    }

}
