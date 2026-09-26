package com.flashledger.flashledgerengine.service;


import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.OrderEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.exception.ConcurrencyConflictException;
import com.flashledger.flashledgerengine.exception.ProductOutOfStockException;
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
public class OrderServiceTest extends BaseIntegrationTest{
    @Test
    void createOrder_shouldCreateTheOrder() {
        //Arrange
        ProductEntity productEntity = createProductWithInventory("Product xyz", 100, 1);
        UserEntity userEntity = createTestUser("Test User", "test@test.com", 10000);

        //Act
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setProductId(productEntity.getId());
        createOrderRequest.setUserId(userEntity.getId());
        OrderDetailsDTO orderDetailsDTO = orderService.createOrder(createOrderRequest);

        //Assert
        InventoryEntity inventoryEntity = inventoryRepository.findById(inventoryRepository.getByProductId(productEntity.getId()).getId()).orElseThrow();
        assertNotNull(orderDetailsDTO);
        assertEquals(userEntity.getId(), orderDetailsDTO.getUserId());
        assertEquals(0, inventoryEntity.getQuantity());
    }
}
