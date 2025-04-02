package ru.kon.onlineshop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kon.onlineshop.dto.cart.CartItemRequest;
import ru.kon.onlineshop.dto.cart.CartResponse;
import ru.kon.onlineshop.dto.cart.OrderResponse;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.exceptions.cart.CartItemNotFoundException;
import ru.kon.onlineshop.exceptions.cart.EmptyCartException;
import ru.kon.onlineshop.exceptions.user.UserNotFoundException;
import ru.kon.onlineshop.repository.*;
import ru.kon.onlineshop.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private CartServiceImpl cartService;

    private final Long userId = 1L;
    private final Long productId = 100L;
    private final Product testProduct = Product.builder()
            .id(productId)
            .name("Test Product")
            .basePrice(BigDecimal.valueOf(100))
            .stockQuantity(10)
            .build();

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        Cart existingCart = createTestCartWithItems(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        CartResponse response = cartService.getCart(userId);

        assertEquals(2, response.getTotalItems());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCart_NewCart_CreatesNewCart() {
        User user = User.builder().id(userId).build();
        Cart cart = createTestCart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.getCart(userId);

        assertEquals(0, response.getTotalItems());
        verify(cartRepository).save(any());
    }

    @Test
    void addItem_ExistingItem_UpdatesQuantity() {
        Cart cart = createTestCart(userId);
        CartItem existingItem = CartItem.builder()
                .product(testProduct)
                .quantity(1)
                .build();
        cart.getItems().add(existingItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(cart, testProduct))
                .thenReturn(Optional.of(existingItem));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.addItem(userId, new CartItemRequest(productId, 3));

        assertEquals(4, existingItem.getQuantity());
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void checkout_ValidCart_CreatesOrder() {
        User testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        Cart cart = createTestCartWithItems(userId);
        cart.setUser(testUser);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = cartService.checkout(userId);

        verify(productRepository, times(2)).save(any(Product.class));
        verify(cartItemRepository).deleteAllByCart(cart);
        verify(emailService).sendOrderConfirmation(any(Order.class), eq("test@example.com"));
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        Cart cart = createTestCart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(EmptyCartException.class, () -> cartService.checkout(userId));
    }

    private Cart createTestCart(Long userId) {
        return Cart.builder()
                .user(User.builder().id(userId).build())
                .items(new ArrayList<>())
                .build();
    }

    private Cart createTestCartWithItems(Long userId) {
        Cart cart = createTestCart(userId);
        cart.getItems().addAll(Arrays.asList(
                CartItem.builder().product(testProduct).quantity(2).build(),
                CartItem.builder().product(testProduct).quantity(3).build()
        ));
        return cart;
    }

    @Test
    void addItem_InvalidProduct_ThrowsException() {
        assertThrows(UserNotFoundException.class,
                () -> cartService.addItem(userId, new CartItemRequest(999L, 1)));
    }

    @Test
    void updateItem_InvalidItem_ThrowsException() {
        Cart cart = createTestCart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductId(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateItem(userId, new CartItemRequest(999L, 5)));
    }
}
