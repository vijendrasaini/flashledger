package com.flashledger.flashledgerengine.lock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flash-ledger.lock")
@Setter
@Getter
public class LockProperties {
    private String strategy;

    private int waitTimeSeconds;
}
