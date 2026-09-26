package com.flashledger.flashledgerengine.lock.strategy;

import org.redisson.api.RLock;

public interface LockAcquisitionStrategy {
    boolean tryAcquire(RLock lock) throws InterruptedException;
}
