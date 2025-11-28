package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kon.onlineshop.entity.Cart;
import ru.kon.onlineshop.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCart(Cart cart);
}
