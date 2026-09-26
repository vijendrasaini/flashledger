package com.flashledger.flashledgerengine.controller;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.service.FineGrainedLockHandlerService;
import com.flashledger.flashledgerengine.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flashledger.flashledgerengine.dto.CreateProductRequest;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    private final FineGrainedLockHandlerService orderService;

    @PostMapping
    public ResponseEntity<ApiEnvelope> createOrder(@RequestBody CreateOrderRequest request, @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        if(idempotencyKey != null && !idempotencyKey.isBlank()) {
            request.setIdempotentKey(idempotencyKey);
        }

        OrderDetailsDTO response = orderService.createOrderSafely(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.create("Order placed Successfully", response));
    }
}
