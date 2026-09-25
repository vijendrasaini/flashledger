package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.exception.ConcurrencyConflictException;
import com.flashledger.flashledgerengine.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class FineGrainedLockHandlerService {
    private final OrderService orderService;
    private final RedissonClient redissonClient;
    private final InventoryRepository inventoryRepository;

    public OrderDetailsDTO createOrderSafely(CreateOrderRequest request) {
        int productId = request.getProductId();
        RLock lock = redissonClient.getFairLock("lock:inventory:product:" + productId);
        boolean isAcquired = false;
        try {
            isAcquired = lock.tryLock(3, TimeUnit.SECONDS);
            if(!isAcquired) {
                throw new ConcurrencyConflictException("System is busy! Please try again later");
            }

            return orderService.createOrder(request);

        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while waiting for lock", e);
        } finally {
            if(isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
