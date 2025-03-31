package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kon.onlineshop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
