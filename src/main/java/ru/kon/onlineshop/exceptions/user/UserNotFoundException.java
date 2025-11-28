package ru.kon.onlineshop.exceptions.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("Пользователь не найден: " + userId);
    }

    public UserNotFoundException(String email) {
        super("Пользователь с такой почтой не найден: " + email);
    }
}
