package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kon.onlineshop.entity.Cart;
import ru.kon.onlineshop.entity.CartItem;
import ru.kon.onlineshop.entity.Product;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
    void deleteAllByCart(Cart cart);
}
