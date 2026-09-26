package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.exception.ConcurrencyConflictException;
import com.flashledger.flashledgerengine.exception.ProductOutOfStockException;
import com.flashledger.flashledgerengine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {"flash-ledger.lock.strategy=fail-fast"})
public class OrderServiceFailFastTest extends BaseIntegrationTest{

    @Test
    void createOrder_shouldFailFastWithConcurrencyConflict_whenFailFastConfigured() throws InterruptedException {
        int usersCount = 12;
        ExecutorService executor = Executors.newFixedThreadPool(usersCount);

        CountDownLatch readyLatch = new CountDownLatch(usersCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(usersCount);

        ProductEntity productEntity = createProductWithInventory("Product xyz", 100, 1);
        int productId = productEntity.getId();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictedCount = new AtomicInteger(0);
        AtomicInteger unknownCount = new AtomicInteger(0);
        AtomicInteger outOfStockCount = new AtomicInteger(0);


        for(int i = 0; i < usersCount; i++) {
            UserEntity userEntity = createTestUser("Test User"+i, "test"+i+"@test.com");
            int userId = userEntity.getId();

            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                    createOrderRequest.setProductId(productId);
                    createOrderRequest.setUserId(userId);

                    fineGrainedLockHandlerService.createOrderSafely(createOrderRequest);
                    successCount.incrementAndGet();
                } catch (ConcurrencyConflictException e) {
                    conflictedCount.incrementAndGet();
                } catch (ProductOutOfStockException e) {
                    outOfStockCount.incrementAndGet();
                } catch (NoSuchElementException | InterruptedException e) {
                    unknownCount.incrementAndGet();
                }
                finally {
                    completeLatch.countDown();
                }

            });
        }

        readyLatch.await();
        startLatch.countDown();
        completeLatch.await();

        assertEquals(1, successCount.get());
        assertEquals(usersCount - 1, conflictedCount.get());
        assertEquals(0, outOfStockCount.get());
        assertEquals(0, inventoryRepository.getByProductId(productId).getQuantity());

        executor.shutdown();
    }
}
