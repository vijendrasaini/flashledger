package com.flashledger.flashledgerengine.controller;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHander {
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHander.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiEnvelope> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiEnvelope(false, 422, "Validation Failed", null, errors));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiEnvelope> handleNoSuchElementException(NoSuchElementException exception) {
        String message = exception.getMessage().isEmpty() ? "Resource not found!" : exception.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiEnvelope(false, 404, message, null, null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiEnvelope> handleException(Exception exception) {
        logger.error("Exception: {}", exception.getMessage());
        String message = exception.getMessage().isEmpty() ? "Internal error!" : exception.getMessage();
        return ResponseEntity.internalServerError().body(ApiEnvelope.serverError(message));
    }
}
