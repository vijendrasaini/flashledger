package com.flashledger.flashledgerengine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Setter 
@Getter 
public class ProductDTO {
    private long id;
    private String name;
    private int price;
}
