package com.flashledger.flashledgerengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flashledger.flashledgerengine.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Integer>{
}
