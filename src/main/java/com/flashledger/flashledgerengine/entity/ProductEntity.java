package com.flashledger.flashledgerengine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "products")
@NoArgsConstructor 
@Getter 
@Setter 
public class ProductEntity {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private int price;
}
