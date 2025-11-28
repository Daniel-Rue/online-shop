package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.cart.CartItemRequest;
import ru.kon.onlineshop.dto.cart.CartResponse;
import ru.kon.onlineshop.dto.cart.OrderResponse;

public interface CartService {

    /**
     * Получение содержимого корзины ТЕКУЩЕГО аутентифицированного пользователя
     *
     * @return корзина с товарами и суммами
     */
    CartResponse getCart();

    /**
     * Добавление товара в корзину ТЕКУЩЕГО аутентифицированного пользователя
     *
     * @param request запрос с данными товара
     * @return обновленная корзина
     */
    CartResponse addItem(CartItemRequest request);

    /**
     * Обновление количества товара в корзине ТЕКУЩЕГО аутентифицированного пользователя
     *
     * @param request запрос с новым количеством
     * @return обновленная корзина
     */
    CartResponse updateItem(CartItemRequest request);

    /**
     * Удаление товара из корзины ТЕКУЩЕГО аутентифицированного пользователя
     *
     * @param productId идентификатор товара
     */
    void removeItem(Long productId);

    /**
     * Оформление заказа из корзины ТЕКУЩЕГО аутентифицированного пользователя
     *
     * @return информация о созданном заказе
     */
    OrderResponse checkout();

    /**
     * Перенос гостевой корзины в корзину пользователя
     * (Оставляем userId, т.к. операция происходит в контексте конкретного пользователя)
     *
     * @param guestCart временная корзина гостя
     * @param userId    идентификатор целевого пользователя
     */
    void mergeCarts(CartResponse guestCart, Long userId);
}
