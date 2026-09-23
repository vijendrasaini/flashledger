package com.flashledger.flashledgerengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flashledger.flashledgerengine.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{
    
}
