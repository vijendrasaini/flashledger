package com.flashledger.flashledgerengine.service;

import java.util.List;
import java.util.Optional;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import com.flashledger.flashledgerengine.entity.InventoryEntity;
import com.flashledger.flashledgerengine.entity.OrderItemEntity;
import com.flashledger.flashledgerengine.entity.ProductEntity;
import com.flashledger.flashledgerengine.repository.InventoryRepository;
import com.flashledger.flashledgerengine.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.flashledger.flashledgerengine.repository.OrderRepository;
import lombok.AllArgsConstructor;


import com.flashledger.flashledgerengine.entity.OrderEntity;
import org.springframework.transaction.annotation.Transactional;

@Service 
@AllArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<OrderEntity> getAllOrder() {
        return orderRepository.findAll();
    }

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        int productId = request.getProductId();
        Optional<ProductEntity> productOp = productRepository.findById(productId);
        if(productOp.isEmpty()) {
            throw new RuntimeException("Internal error!");
        }

        ProductEntity product = productOp.get();
        InventoryEntity found = inventoryRepository.getByProductId(productId);
        if(found.getQuantity() < 1) {
            throw new RuntimeException("Product is out of the stock!");
        }

        int updateCount = inventoryRepository.decrementQuantity(productId, found.getQuantity() - 1);
        if(updateCount != 1) {
            throw new RuntimeException("Internal error!");
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(request.getUserId());
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        int orderId = savedOrder.getId();
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(savedOrder);
        orderItem.setProduct(product);
        orderRepository.save(orderEntity);
    }
}
