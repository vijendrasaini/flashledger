package com.flashledger.flashledgerengine.service;

import java.util.List;

import com.flashledger.flashledgerengine.dto.CreateOrderRequest;
import org.springframework.stereotype.Service;
import com.flashledger.flashledgerengine.repository.OrderRepository;
import lombok.AllArgsConstructor;


import com.flashledger.flashledgerengine.entity.OrderEntity;

@Service 
@AllArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;

    public List<OrderEntity> getAllOrder() {
        return orderRepository.findAll();
    }

    public void createOrder(CreateOrderRequest request) {

    }
}
