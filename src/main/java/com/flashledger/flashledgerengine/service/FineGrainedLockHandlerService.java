package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.exception.ConcurrencyConflictException;
import com.flashledger.flashledgerengine.lock.strategy.LockAcquisitionStrategy;
import com.flashledger.flashledgerengine.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class FineGrainedLockHandlerService {
    private final OrderService orderService;
    private final RedissonClient redissonClient;
    private final InventoryRepository inventoryRepository;
    private LockAcquisitionStrategy lockAcquisitionStrategy;

    private static final Logger logger = LoggerFactory.getLogger(FineGrainedLockHandlerService.class);

    public OrderDetailsDTO createOrderSafely(CreateOrderRequest request) {
        int productId = request.getProductId();
        String lockName = "lock:inventory:product:" + productId;
        logger.info("Attempting to acquire Redisson lock: {} for userId: {}", lockName, request.getUserId());
        RLock lock = redissonClient.getFairLock(lockName);
        boolean isAcquired = false;
        try {
            logger.info("Selected lock acquisition strategy: {}", lockAcquisitionStrategy.getClass().getName());
            isAcquired = lockAcquisitionStrategy.tryAcquire(lock);
            if(!isAcquired) {
                logger.warn("Failed to acquire Redisson lock (timeout/busy): {} for userId: {}", lockName, request.getUserId());
                throw new ConcurrencyConflictException("System is busy! Please try again later");
            }

            logger.info("Successfully acquired Redisson lock: {} for thread: {}", lockName, request.getUserId());
            return orderService.createOrder(request);

        } catch (InterruptedException e) {
            logger.error("Interrupted while acquiring Redisson lock: {} for thread: {}", lockName, request.getUserId(), e);
            throw new RuntimeException("Thread interrupted while waiting for lock", e);
        } finally {
            if(isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                logger.info("Released Redisson lock: {} for thread: {}", lockName, request.getUserId());
            }
        }
    }
}
