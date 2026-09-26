package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.bootstrap.SystemAccountSeeder;
import com.flashledger.flashledgerengine.entity.*;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.entity.enums.EntryType;
import com.flashledger.flashledgerengine.entity.enums.LedgerTransactionStatus;
import com.flashledger.flashledgerengine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.UUID;

public class BaseIntegrationTest {
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected InventoryRepository inventoryRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderItemRepository orderItemRepository;
    @Autowired protected AccountRepository accountRepository;
    @Autowired protected LedgerEntryRepository ledgerEntryRepository;
    @Autowired protected LedgerTransactionRepository ledgerTransactionRepository;
    @Autowired protected OrderService orderService;
    @Autowired protected FineGrainedLockHandlerService fineGrainedLockHandlerService;
    @Autowired protected SystemAccountSeeder systemAccountSeeder;
    @Autowired protected LedgerService ledgerService;

    @BeforeEach
    void cleanDatabase() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();

        inventoryRepository.deleteAll();
        productRepository.deleteAll();

        ledgerEntryRepository.deleteAll();
        ledgerTransactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        systemAccountSeeder.run(new DefaultApplicationArguments());
    }

    protected ProductEntity createProductWithInventory(String name, int price, int quantity) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(name);
        productEntity.setPrice(price);
        productEntity = productRepository.save(productEntity);
        int productId = productEntity.getId();

        InventoryEntity inventoryEntity = new InventoryEntity();
        inventoryEntity.setProduct(productEntity);
        inventoryEntity.setQuantity(quantity);
        inventoryRepository.save(inventoryEntity);

        return productEntity;
    }
    protected UserEntity createTestUser(String name, String email, int initialBalance) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity = userRepository.save(userEntity);
        // Auto-fund wallet 1,000,00
        createFundedWallet(userEntity, initialBalance);
        return userEntity;
    }

    protected AccountEntity createFundedWallet(UserEntity user, int initialBalance) {
        AccountEntity account = new AccountEntity();
        account.setUser(user);
        account.setAccountType(AccountType.USER_WALLET);
        account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        account = accountRepository.save(account);
        if (initialBalance > 0) {
            // parent transaction for the initial deposit
            LedgerTransactionEntity depositTxn = new LedgerTransactionEntity();
            depositTxn.setTransactionReference("TXN-INIT-" + UUID.randomUUID().toString().substring(0, 7));
            depositTxn.setStatus(LedgerTransactionStatus.COMMITED);
            depositTxn.setUserId(user.getId());
            depositTxn.setOrderId(0);
            depositTxn = ledgerTransactionRepository.save(depositTxn);

            // Create the CREDIT entry linked to the transaction
            LedgerEntryEntity deposit = new LedgerEntryEntity();
            deposit.setAccount(account);
            deposit.setEntryType(EntryType.CREDIT);
            deposit.setAmount(initialBalance);

            deposit.setLedgerTransaction(depositTxn);
            ledgerEntryRepository.save(deposit);
        }
        return account;
    }

}
