package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.cart.*;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.exceptions.cart.CartItemNotFoundException;
import ru.kon.onlineshop.exceptions.cart.EmptyCartException;
import ru.kon.onlineshop.exceptions.cart.InsufficientStockException;
import ru.kon.onlineshop.exceptions.user.UserNotFoundException;
import ru.kon.onlineshop.repository.*;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.service.CartService;
import ru.kon.onlineshop.service.EmailService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = getProduct(request.getProductId());

        validateStock(product, request.getQuantity());
        updateOrCreateCartItem(cart, product, request.getQuantity());

        return mapToCartResponse(saveCart(cart));
    }

    @Override
    public CartResponse updateItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getCartItem(cart, request.getProductId());

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());

        Cart updatedCart = saveCart(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getCartItem(cart, productId);
        cartItemRepository.delete(item);
        saveCart(cart);
    }

    @Override
    @Transactional
    public OrderResponse checkout(Long userId) {
        Cart cart = getOrCreateCart(userId);
        validateCart(cart);

        Order order = createOrder(cart);
        updateStockQuantities(cart);
        clearCart(cart);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        emailService.sendOrderConfirmation(order, user.getEmail());

        return mapToOrderResponse(order);
    }

    @Override
    public void mergeCarts(CartResponse guestCart, Long userId) {
        guestCart.getItems().forEach(item -> {
            try {
                addItem(userId, new CartItemRequest(
                        item.getProductId(),
                        item.getQuantity()
                ));
            } catch (InsufficientStockException ignored) {
            }
        });
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return cartRepository.save(Cart.builder()
                .user(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Недостаточно товара: " + product.getName()
            );
        }
    }

    private void updateOrCreateCartItem(Cart cart, Product product, int quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build()
            );
        }
    }

    private CartItem getCartItem(Cart cart, Long productId) {
        return cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Товар не найден в корзине: " + productId
                ));
    }

    private Cart saveCart(Cart cart) {
        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    private void validateCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Корзина пуста");
        }
        cart.getItems().forEach(item ->
                validateStock(item.getProduct(), item.getQuantity())
        );
    }

    private Order createOrder(Cart cart) {
        Order order = Order.builder()
                .user(cart.getUser())
                .totalAmount(calculateTotal(cart))
                .createdAt(Instant.now())
                .build();

        cart.getItems().forEach(item ->
                order.getItems().add(OrderItem.builder()
                        .order(order)
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .priceAtOrder(getCurrentPrice(item.getProduct()))
                        .build())
        );

        return orderRepository.save(order);
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> getCurrentPrice(item.getProduct())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getCurrentPrice(Product product) {
        return Optional.ofNullable(product.getDiscountPrice())
                .orElse(product.getBasePrice());
    }

    private void updateStockQuantities(Cart cart) {
        cart.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(
                    product.getStockQuantity() - item.getQuantity()
            );
            productRepository.save(product);
        });
    }

    private void clearCart(Cart cart) {
        cartItemRepository.deleteAllByCart(cart);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .items(cart.getItems().stream()
                        .map(this::mapToCartItemDto)
                        .collect(Collectors.toList()))
                .totalItems(cart.getItems().size())
                .totalAmount(calculateTotal(cart))
                .build();
    }

    private CartItemDto mapToCartItemDto(CartItem item) {
        return CartItemDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(getCurrentPrice(item.getProduct()))
                .basePrice(item.getProduct().getBasePrice())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .items(order.getItems().stream()
                        .map(this::mapToOrderItemDto)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemDto mapToOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .build();
    }
}
