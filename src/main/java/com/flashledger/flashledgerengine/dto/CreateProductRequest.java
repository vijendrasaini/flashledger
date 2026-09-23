package com.flashledger.flashledgerengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Setter 
@Getter 
public class CreateProductRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Min(value = 1)
    private int price;    
}
