package ru.kon.onlineshop.exceptions.cart;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}
