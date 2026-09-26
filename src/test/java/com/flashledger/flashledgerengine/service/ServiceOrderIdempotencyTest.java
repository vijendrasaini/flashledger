package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ServiceOrderIdempotencyTest extends BaseIntegrationTest {
    @Test
    void createOrder_shouldNotCreateDuplicateOrders_WhenRequestReachesWithSameIdempotencyKey() {
        // Arrange
        // 2 request with Same idempotent key
        int initialBalance = 1000;
        UserEntity userEntity = createTestUser("Idempotent User", "idempotent@user.in", initialBalance);

        int productQuantity = 10;
        int productPrice = 400;
        ProductEntity productEntity = createProductWithInventory("Book: Zero to One", productPrice, productQuantity);
        AccountEntity accountEntity = accountRepository.findByUserIdAndAccountType(userEntity.getId(), AccountType.USER_WALLET).orElseThrow();

        String idempotentKey = ("IDEM-" + UUID.randomUUID()).substring(0,16);
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setProductId(productEntity.getId());
        request1.setUserId(userEntity.getId());
        request1.setIdempotentKey(idempotentKey);

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setProductId(productEntity.getId());
        request2.setUserId(userEntity.getId());
        request2.setIdempotentKey(idempotentKey);

        // Act
        OrderDetailsDTO orderDetailsDTO1 = orderService.createOrder(request1);
        OrderDetailsDTO orderDetailsDTO2 = orderService.createOrder(request2);

        // Assert
        assertEquals(orderDetailsDTO1.getId(), orderDetailsDTO2.getId());
        assertEquals(1, orderRepository.count());

        int balanceAfterOrders = ledgerService.getBalance(accountEntity.getId());
        assertEquals(initialBalance - productPrice, balanceAfterOrders);

        InventoryEntity inventoryEntity = inventoryRepository.getByProductId(productEntity.getId());
        assertEquals(productQuantity - 1, inventoryEntity.getQuantity());
    }

    @Test
    void createOrder_shouldNotCreateDuplicateOrders_whenTwoThreadsHitSimultaneouslyWithSameIdempotencyKey() throws InterruptedException {
        // 1. Arrange
        int initialBalance = 1000;
        UserEntity user = createTestUser("Concurrent User", "concurrent@user.in", initialBalance);

        int productQuantity = 10;
        int productPrice = 400;
        ProductEntity product = createProductWithInventory("Design Patterns Book", productPrice, productQuantity);
        AccountEntity wallet = accountRepository.findByUserIdAndAccountType(user.getId(), AccountType.USER_WALLET).orElseThrow();

        String sharedIdempotentKey = ("IDEM-" + UUID.randomUUID()).substring(0, 16);

        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setProductId(product.getId());
        request1.setUserId(user.getId());
        request1.setIdempotentKey(sharedIdempotentKey);

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setProductId(product.getId());
        request2.setUserId(user.getId());
        request2.setIdempotentKey(sharedIdempotentKey);

        // Concurrency controls to release both threads at the exact same millisecond
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(2);
        AtomicReference<OrderDetailsDTO> result1 = new AtomicReference<>();
        AtomicReference<OrderDetailsDTO> result2 = new AtomicReference<>();

        new Thread(() -> {
            try {
                startGate.await(); // wait for the gun shot
                // result1.set(orderService.createOrder(request1));
                result1.set(fineGrainedLockHandlerService.createOrderSafely(request1));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endGate.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                startGate.await(); // wait for the gun shot
                // result2.set(orderService.createOrder(request2));
                result2.set(fineGrainedLockHandlerService.createOrderSafely(request2));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endGate.countDown();
            }
        }).start();

        // 2. Act: Fire both threads at the exact same moment!
        startGate.countDown();
        endGate.await(5, java.util.concurrent.TimeUnit.SECONDS);

        // 3. Assert (Zero duplicate orders, only 1 charged)
        assertEquals(1, orderRepository.count(), "Only 1 order must be created!");
        assertEquals(productQuantity - 1, inventoryRepository.getByProductId(product.getId()).getQuantity(), "Inventory must only be decremented once!");
        assertEquals(initialBalance - productPrice, ledgerService.getBalance(wallet.getId()), "Wallet must only be debited once!");

        // Both responses should return the SAME order ID
        assertEquals(result1.get().getId(), result2.get().getId(), "Both threads must receive the same order ID!");
    }
}
