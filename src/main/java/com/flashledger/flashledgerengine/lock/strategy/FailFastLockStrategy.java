package com.flashledger.flashledgerengine.lock.strategy;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class FailFastLockStrategy implements LockAcquisitionStrategy{
    @Override
    public boolean tryAcquire(RLock lock) throws InterruptedException {
        return lock.tryLock(0, TimeUnit.SECONDS);
    }
}
