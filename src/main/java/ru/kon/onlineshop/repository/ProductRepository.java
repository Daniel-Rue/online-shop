package ru.kon.onlineshop.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kon.onlineshop.entity.Product;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.categories c " +
           "WHERE c.id IN :categoryIds")
    List<Product> findByCategoryIds(@Param("categoryIds") Set<Long> categoryIds, Sort sort);
}