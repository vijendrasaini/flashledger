package com.flashledger.flashledgerengine.lock.config;

import com.flashledger.flashledgerengine.lock.strategy.BoundedWaitLockStrategy;
import com.flashledger.flashledgerengine.lock.strategy.FailFastLockStrategy;
import com.flashledger.flashledgerengine.lock.strategy.LockAcquisitionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockStrategyConfig {
    @Bean
    LockAcquisitionStrategy lockAcquisitionStrategy(LockProperties lockProperties) {
        LockAcquisitionStrategy strategy = null;
        if (lockProperties.getStrategy().equals("fail-fast")) {
            strategy = new FailFastLockStrategy();
        } else {
            strategy = new BoundedWaitLockStrategy(lockProperties.getWaitTimeSeconds());
        }

        return strategy;
    }
}
