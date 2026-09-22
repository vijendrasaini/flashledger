package com.flashledger.flashledgerengine;

import com.flashledger.flashledgerengine.dto.ApiEnvelope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Welcome {
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("<h1>Welcome to project \"Flash Sale Ledger!\"</h1>");
    }
}
