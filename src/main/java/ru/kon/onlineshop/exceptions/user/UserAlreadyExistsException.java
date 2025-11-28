package ru.kon.onlineshop.exceptions.user;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("Пользователь с почтой " + email + " уже зарегистрирован");
  }
}
