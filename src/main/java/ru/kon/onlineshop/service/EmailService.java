package ru.kon.onlineshop.service;

import ru.kon.onlineshop.entity.Order;

public interface EmailService {

    /**
     * Отправляет подтверждение заказа на email пользователя.
     *
     * @param order информация о заказе
     * @param email адрес получателя
     */
    void sendOrderConfirmation(Order order, String email);
}
