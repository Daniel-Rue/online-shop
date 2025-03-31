package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kon.onlineshop.entity.Cart;

import java.util.Optional;


public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
