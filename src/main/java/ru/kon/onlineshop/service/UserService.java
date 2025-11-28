package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.auth.RegisterRequest;
import ru.kon.onlineshop.dto.user.UpdateUserRequest;
import ru.kon.onlineshop.dto.user.UserResponse;

public interface UserService {

    /**
     * Создание нового пользователя
     *
     * @param request данные для регистрации
     */
    void registerUser(RegisterRequest request);

    /**
     * Получение информации о пользователе
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     */
    UserResponse getUserDetails(Long userId);

    /**
     * Обновление данных пользователя
     *
     * @param userId  идентификатор пользователя
     * @param request запрос с обновленными данными
     * @return обновленные данные пользователя
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    /**
     * Удаление пользователя из системы
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);

    /**
     * Проверка существования пользователя по email
     *
     * @param email адрес электронной почты
     * @return true если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Получение текущего аутентифицированного пользователя
     *
     * @return данные текущего пользователя
     */
    UserResponse getCurrentUser();
}