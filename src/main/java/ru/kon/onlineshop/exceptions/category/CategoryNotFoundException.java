package ru.kon.onlineshop.exceptions.category;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Категория с id " + id + "не найдена");
    }
}