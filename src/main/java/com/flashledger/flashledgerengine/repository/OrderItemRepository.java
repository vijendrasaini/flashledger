package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Integer>{
}
