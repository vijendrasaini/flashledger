package com.flashledger.flashledgerengine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private String name;
    private int price;
}
