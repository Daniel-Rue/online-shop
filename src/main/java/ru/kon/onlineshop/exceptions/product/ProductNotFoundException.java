package ru.kon.onlineshop.exceptions.product;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Продукт не найден: " + id);
    }
}
