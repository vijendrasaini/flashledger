package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.exception.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceWithLedgerTest extends BaseIntegrationTest{
    @Test
    void createOrder_shouldRollbackOrderAndInventory_whenUserHasInsufficientFunds() {
        // Arrange
        ProductEntity product = createProductWithInventory("Mouse", 500000, 10);
        UserEntity user = createTestUser("Narendra", "narendra@example.com");
        AccountEntity userWallet = accountRepository.findByUserIdAndAccountType(user.getId(), AccountType.USER_WALLET).orElseThrow();

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
        Assertions.assertEquals(100000, ledgerService.getBalance(userWallet.getId()));
    }

    @Test
    void createOrder_shouldCreateOrderAndDebitUserWallet_whenInventoryAndFundsAreSufficient() {
        // 1. Arrange
        AccountEntity systemAccount = accountRepository.findByAccountType(AccountType.SYSTEM_REVENUE).orElseThrow();

        ProductEntity product = createProductWithInventory("Keyboard", 60000, 10);
        UserEntity user = createTestUser("Dakku", "dakku@example.com");
        AccountEntity userWallet = accountRepository.findByUserIdAndAccountType(user.getId(), AccountType.USER_WALLET).orElseThrow();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(user.getId());
        request.setProductId(product.getId());

        // 2. Act
        OrderDetailsDTO orderDetails = orderService.createOrder(request);

        // 3. Assert
        Assertions.assertNotNull(orderDetails);
        Assertions.assertTrue(orderDetails.getId() > 0);
        Assertions.assertNotNull(orderDetails.getTxnRefId());

        // Inventory decremented
        InventoryEntity inventory = inventoryRepository.getByProductId(product.getId());
        Assertions.assertEquals(9, inventory.getQuantity());

        // User wallet balance debited
        Assertions.assertEquals(40000, ledgerService.getBalance(userWallet.getId()));

        // System revenue balance credited
        Assertions.assertEquals(60000, ledgerService.getBalance(systemAccount.getId()));
    }

}
