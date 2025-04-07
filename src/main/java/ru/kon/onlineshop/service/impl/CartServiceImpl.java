package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.cart.*;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.exceptions.cart.CartItemNotFoundException;
import ru.kon.onlineshop.exceptions.cart.EmptyCartException;
import ru.kon.onlineshop.exceptions.cart.InsufficientStockException;
import ru.kon.onlineshop.exceptions.user.UserNotFoundException;
import ru.kon.onlineshop.repository.*;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.security.model.UserDetailsImpl;
import ru.kon.onlineshop.service.CartService;
import ru.kon.onlineshop.service.EmailService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
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

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new IllegalStateException("Невозможно определить аутентифицированного пользователя.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (userDetails.getId() == null) {
            throw new IllegalStateException("ID пользователя не найден в деталях аутентификации.");
        }
        return userDetails.getId();
    }

    @Override
    @Transactional
    public CartResponse getCart() {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItem(CartItemRequest request) {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        Product product = getProduct(request.getProductId());

        validateStock(product, request.getQuantity());
        updateOrCreateCartItem(cart, product, request.getQuantity());

        return mapToCartResponse(saveCart(cart));
    }

    @Override
    public CartResponse updateItem(CartItemRequest request) {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        CartItem item = getCartItem(cart, request.getProductId());

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());

        Cart updatedCart = saveCart(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public void removeItem(Long productId) {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        CartItem item = getCartItem(cart, productId);
        cartItemRepository.delete(item);
        saveCart(cart);
    }

    @Override
    @Transactional
    public OrderResponse checkout() {
        Long userId = getCurrentUserId();
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
    @Transactional
    public void mergeCarts(CartResponse guestCart, Long userId) {
        Cart targetUserCart = getOrCreateCart(userId);

        guestCart.getItems().forEach(itemDto -> {
            try {
                Product product = getProduct(itemDto.getProductId());
                int quantityToAdd = itemDto.getQuantity();

                validateStock(product, quantityToAdd);

                updateOrCreateCartItem(targetUserCart, product, quantityToAdd);

            } catch (InsufficientStockException e) {
                System.err.println("Не удалось добавить товар при слиянии корзин (недостаточно на складе): "
                                   + itemDto.getProductName() + ", ID: " + itemDto.getProductId());
            } catch (ProductNotFoundException e) {
                System.err.println("Не удалось добавить товар при слиянии корзин (товар не найден): ID "
                                   + itemDto.getProductId());
            }
        });

        saveCart(targetUserCart);
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
                .items(new java.util.ArrayList<>())
                .build());
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateStock(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество товара должно быть положительным.");
        }
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Недостаточно товара '" + product.getName() + "' на складе. Доступно: " + product.getStockQuantity()
            );
        }
    }

    private void updateOrCreateCartItem(Cart cart, Product product, int quantity) {
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            validateStock(product, newQuantity);
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            validateStock(product, quantity);
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
    }

    private CartItem getCartItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Товар с ID " + productId + " не найден в корзине пользователя " + cart.getUser().getId()
                ));
    }

    private Cart saveCart(Cart cart) {
        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    private void validateCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Корзина пуста. Невозможно оформить заказ.");
        }
        for (CartItem item : cart.getItems()) {
            validateStock(item.getProduct(), item.getQuantity());
        }
    }

    private Order createOrder(Cart cart) {
        Order order = Order.builder()
                .user(cart.getUser())
                .totalAmount(calculateTotal(cart))
                .createdAt(Instant.now())
                .items(new java.util.ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(getCurrentPrice(cartItem.getProduct()))
                    .build();
            order.getItems().add(orderItem);
        }

        return orderRepository.save(order);
    }

    private BigDecimal calculateTotal(Cart cart) {
        if (cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(item -> getCurrentPrice(item.getProduct())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getCurrentPrice(Product product) {
        return Optional.ofNullable(product.getDiscountPrice())
                .filter(dp -> dp.compareTo(BigDecimal.ZERO) > 0)
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
