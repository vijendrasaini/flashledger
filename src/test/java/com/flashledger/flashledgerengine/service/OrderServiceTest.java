package com.flashledger.flashledgerengine.service;


import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.OrderEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.exception.ConcurrencyConflictException;
import com.flashledger.flashledgerengine.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void cleanDatabase() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();

        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createOrder_shouldCreateTheOrder() {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName("Product xyz");
        productEntity.setPrice(100);
        productEntity = productRepository.save(productEntity);
        int productId = productEntity.getId();

        InventoryEntity inventoryEntity = new InventoryEntity();
        inventoryEntity.setProduct(productEntity);
        inventoryEntity.setQuantity(1);
        inventoryRepository.save(inventoryEntity);

        UserEntity userEntity = new UserEntity();
        String name = "User" + 2000;
        userEntity.setName(name);
        userEntity.setEmail(name.toLowerCase() + "@test.com");
        userEntity = userRepository.save(userEntity);

        int userId = userEntity.getId();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setProductId(productId);
        createOrderRequest.setUserId(userId);
        OrderDetailsDTO orderDetailsDTO = orderService.createOrder(createOrderRequest);

        assertNotNull(orderDetailsDTO);
        assertEquals(userId, orderDetailsDTO.getUserId());

        inventoryEntity = inventoryRepository.findById(inventoryEntity.getId()).orElseThrow();
        assertEquals(0, inventoryEntity.getQuantity());
    }

    @Test
    void createOrder_OneThreadShouldPlaceOrderWhenCalledConcurrently() throws InterruptedException {
        int usersCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(usersCount);

        CountDownLatch readyLatch = new CountDownLatch(usersCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(usersCount);

        ProductEntity productEntity = new ProductEntity();
        productEntity.setName("Product xyz");
        productEntity.setPrice(100);
        productEntity = productRepository.save(productEntity);
        int productId = productEntity.getId();

        InventoryEntity inventoryEntity = new InventoryEntity();
        inventoryEntity.setProduct(productEntity);
        inventoryEntity.setQuantity(1);
        inventoryRepository.save(inventoryEntity);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictedCount = new AtomicInteger(0);
        AtomicInteger unknownCount = new AtomicInteger(0);


        for(int i = 0; i < usersCount; i++) {
            UserEntity userEntity = new UserEntity();
            String name = "User" + (1000 + i);
            userEntity.setName(name);
            userEntity.setEmail(name.toLowerCase() + "@test.com");
            userEntity = userRepository.save(userEntity);

            int userId = userEntity.getId();

            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                    createOrderRequest.setProductId(productId);
                    createOrderRequest.setUserId(userId);

                    orderService.createOrder(createOrderRequest);
                    successCount.incrementAndGet();
                } catch (ConcurrencyConflictException e) {
                    conflictedCount.incrementAndGet();
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
        assertEquals(49, conflictedCount.get());
        assertEquals(49, unknownCount.get());
        executor.shutdown();
    }
}
