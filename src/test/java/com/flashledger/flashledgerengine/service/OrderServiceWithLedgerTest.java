package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.exception.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceWithLedgerTest extends BaseIntegrationTest{
    @Test
    void createOrder_shouldRollbackOrderAndInventory_whenUserHasInsufficientFunds() {
        // Arrange
        ProductEntity product = createProductWithInventory("Mouse", 500, 10);
        UserEntity user = createTestUser("Narendra", "narendra@example.com");
        AccountEntity userWallet = createFundedWallet(user, 100); // User only has rs 100, but product is rs 500!

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(user.getId());
        request.setProductId(product.getId());

        //Act
        Assertions.assertThrows(InsufficientFundsException.class, () -> {
            orderService.createOrder(request);
        });

        // Assert
        Assertions.assertEquals(0, orderRepository.count());
        InventoryEntity inventory = inventoryRepository.getByProductId(product.getId());
        Assertions.assertEquals(10, inventory.getQuantity());
        Assertions.assertEquals(100, ledgerService.getBalance(userWallet.getId()));
    }
}
