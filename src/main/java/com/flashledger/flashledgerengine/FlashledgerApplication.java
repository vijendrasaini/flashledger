package com.flashledger.flashledgerengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class FlashledgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashledgerApplication.class, args);
    }
}
