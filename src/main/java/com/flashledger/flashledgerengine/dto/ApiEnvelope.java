package com.flashledger.flashledgerengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ApiEnvelope {
    private boolean success;
    private int statusCode;
    private String message;
    private Map<String, Object> data;
    private Map<String, Object> errors;

    public static ApiEnvelope ok(String message) {
        return new ApiEnvelope(true, 200, message, null, null);
    }

    public static ApiEnvelope ok(String message, int statusCode) {
        return new ApiEnvelope(true, statusCode, message, null, null);
    }

    public static ApiEnvelope ok(String message, Map<String, Object> data) {
        return new ApiEnvelope(true, 200, message, data, null);
    }

    public static ApiEnvelope notFound(String message) {
        return new ApiEnvelope(true, 401, message, null, null);
    }
}
