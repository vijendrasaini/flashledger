package com.flashledger.flashledgerengine.lock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "flash-ledger.lock")
@Setter
@Getter
public class LockProperties {
    private String strategy = "fail-fast";

    private int waitTimeSeconds = 3;
}
