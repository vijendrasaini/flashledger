package com.flashledger.flashledgerengine.service;

import org.springframework.stereotype.Service;

import com.flashledger.flashledgerengine.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import com.flashledger.flashledgerengine.dto.CreateProductRequest;
import com.flashledger.flashledgerengine.dto.ProductDTO;
import com.flashledger.flashledgerengine.entity.ProductEntity;

@Service 
@AllArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional 
    public ProductDTO createProduct(CreateProductRequest request) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(request.getName());
        productEntity.setPrice(request.getPrice());

        ProductEntity saved = productRepository.save(productEntity);
        return toProduct(saved);
    }

	private ProductDTO toProduct(ProductEntity productEntity) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productEntity.getId());
        productDTO.setPrice(productEntity.getPrice());
        return productDTO;
	}
}
