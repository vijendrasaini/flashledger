package com.flashledger.flashledgerengine.lock.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Setter
@Getter
public class BoundedWaitLockStrategy implements LockAcquisitionStrategy{
    private int waitTimeSeconds;

    @Override
    public boolean tryAcquire(RLock lock) throws InterruptedException {
        return lock.tryLock(waitTimeSeconds, TimeUnit.SECONDS);
    }
}
