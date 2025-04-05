package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.cart.CartItemRequest;
import ru.kon.onlineshop.dto.cart.CartResponse;
import ru.kon.onlineshop.dto.cart.OrderResponse;

public interface CartService {

    /**
     * Получение содержимого корзины пользователя
     *
     * @param userId идентификатор пользователя
     * @return корзина с товарами и суммами
     */
    CartResponse getCart(Long userId);

    /**
     * Добавление товара в корзину пользователя
     *
     * @param userId  идентификатор пользователя
     * @param request запрос с данными товара
     * @return обновленная корзина
     */
    CartResponse addItem(Long userId, CartItemRequest request);

    /**
     * Обновление количества товара в корзине пользователя
     *
     * @param userId  идентификатор пользователя
     * @param request запрос с новым количеством
     * @return обновленная корзина
     */
    CartResponse updateItem(Long userId, CartItemRequest request);

    /**
     * Удаление товара из корзины пользователя
     *
     * @param userId    идентификатор пользователя
     * @param productId идентификатор товара
     */
    void removeItem(Long userId, Long productId);

    /**
     * Оформление заказа из корзины пользователя
     *
     * @param userId идентификатор пользователя
     * @return информация о созданном заказе
     */
    OrderResponse checkout(Long userId);

    /**
     * Перенос гостевой корзины в корзину пользователя
     *
     * @param guestCart временная корзина гостя
     * @param userId    идентификатор целевого пользователя
     */
    void mergeCarts(CartResponse guestCart, Long userId);
}
