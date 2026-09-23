package com.flashledger.flashledgerengine.controller;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.flashledger.flashledgerengine.dto.CreateProductRequest;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @GetMapping("/")
    public ResponseEntity<ApiEnvelope> get() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok("Orders fetched Successfully", Map.of()));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiEnvelope> createOrder(@RequestBody CreateProductRequest request) {

        return ok("Order has been created Successfully", 201);
    }

    private ResponseEntity<ApiEnvelope> ok(String message) {
        return ResponseEntity.ok(ApiEnvelope.ok(message));
    }

    private ResponseEntity<ApiEnvelope> ok(String message, int statusCode) {
        return ResponseEntity.ok(ApiEnvelope.ok(message, statusCode));
    }
}
