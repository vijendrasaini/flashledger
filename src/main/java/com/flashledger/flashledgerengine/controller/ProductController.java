package com.flashledger.flashledgerengine.controller;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;
import com.flashledger.flashledgerengine.dto.CreateProductRequest;
import com.flashledger.flashledgerengine.dto.ProductDTO;
import com.flashledger.flashledgerengine.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiEnvelope> get() {
        return ok("Products fetched successfully.", Map.of("products", Collections.emptyList()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiEnvelope> get(@PathVariable int productId) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiEnvelope.notFound("Product does not exist."));
    }

    @PostMapping("/")
    public ResponseEntity<ApiEnvelope> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.create("Product created successfully", productService.createProduct(request)));
    }

    private ResponseEntity<ApiEnvelope> ok(String message, Map<String, Object> data) {
        return ResponseEntity.ok(ApiEnvelope.ok(message, data));
    }
}
