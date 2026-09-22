package com.flashledger.flashledgerengine.controller;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping
    public ResponseEntity<ApiEnvelope> get() {
        return ok("Products fetched successfully.", Map.of("products", Collections.emptyList()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiEnvelope> get(@PathVariable int productId) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiEnvelope.notFound("Product does not exist."));
    }

    private ResponseEntity<ApiEnvelope> ok(String message, Map<String, Object> data) {
        return ResponseEntity.ok(ApiEnvelope.ok(message, data));
    }
}
