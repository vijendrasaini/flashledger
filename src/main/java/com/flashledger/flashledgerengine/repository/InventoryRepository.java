package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {
    InventoryEntity getByProductId(int productId);

    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.quantity = i.quantity - :quantity
        WHERE i.product.id = :productId
    """)
    int decrementQuantity(@Param("productId") int productId, @Param("quantity") int quantity);
}
