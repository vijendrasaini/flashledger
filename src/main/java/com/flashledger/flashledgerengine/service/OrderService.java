package com.flashledger.flashledgerengine.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.dto.OrderDetailsDTO;
import com.flashledger.flashledgerengine.entity.*;
import com.flashledger.flashledgerengine.exception.InsufficientFundsException;
import com.flashledger.flashledgerengine.exception.ProductOutOfStockException;
import com.flashledger.flashledgerengine.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;


import org.springframework.transaction.annotation.Transactional;

@Service 
@AllArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final LedgerService ledgerService;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public List<OrderEntity> getAllOrder() {
        return orderRepository.findAll();
    }

    @Transactional
    public OrderDetailsDTO createOrder(CreateOrderRequest request) {
        logger.info("Creating order.....");
        int productId = request.getProductId();
        Optional<ProductEntity> productOp = productRepository.findById(productId);
        if(productOp.isEmpty()) {
            throw new NoSuchElementException("Invalid product id");
        }

        ProductEntity product = productOp.get();
        Optional<UserEntity> userOp = userRepository.findById(request.getUserId());
        if(userOp.isEmpty()) {
            throw new NoSuchElementException("Invalid user id");
        }

        UserEntity user = userOp.get();
        logger.info("Checking inventory.....");
        InventoryEntity found = inventoryRepository.getByProductId(productId);
        if(found == null) {
            throw new NoSuchElementException("Inventory does not have product: %s".formatted(product.getName()));
        }

        if(found.getQuantity() < 1) {
            logger.info("Product is out of the stock. Aborting ...");
            throw new ProductOutOfStockException("Product is out of the stock!");
        }

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            logger.error("some issue with thread", e);
        }

        logger.info("Updating inventory.....");
        int updateCount = inventoryRepository.decrementQuantity(productId, 1);
        if(updateCount != 1) {
            throw new RuntimeException("Internal error!");
        }

        logger.info("preparing Order object ...");
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUser(user);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        logger.info("Saved Order object ...");

        logger.info("preparing Order Item object ...");
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(savedOrder);
        orderItem.setProduct(product);
        orderItemRepository.save(orderItem);
        logger.info("saved Order Item object ...");

        // create the transaction
        LedgerTransactionEntity ledgerTransactionEntity = ledgerService.recordTransfer(user.getId(), product.getPrice(), orderEntity.getId(), "Transaction to buy : %s".formatted(product.getName()));
        return to(orderEntity, ledgerTransactionEntity.getTransactionReference());
    }

    private OrderDetailsDTO to(OrderEntity orderEntity, String txnRefId) {
        OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();

        orderDetailsDTO.setId(orderEntity.getId());
        orderDetailsDTO.setUserId(orderEntity.getUser().getId());
        orderDetailsDTO.setTxnRefId(txnRefId);
        return orderDetailsDTO;
    }
}
