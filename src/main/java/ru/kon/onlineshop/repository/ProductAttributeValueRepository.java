package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kon.onlineshop.entity.ProductAttributeValue;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    @Modifying
    @Query("DELETE FROM ProductAttributeValue pav WHERE pav.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    long countByAttributeId(Long attributeId);
}
