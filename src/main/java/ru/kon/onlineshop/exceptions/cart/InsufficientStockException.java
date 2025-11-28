package ru.kon.onlineshop.exceptions.cart;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
