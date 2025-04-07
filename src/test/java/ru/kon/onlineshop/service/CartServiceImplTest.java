package ru.kon.onlineshop.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.kon.onlineshop.dto.cart.CartItemDto;
import ru.kon.onlineshop.dto.cart.CartItemRequest;
import ru.kon.onlineshop.dto.cart.CartResponse;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.exceptions.cart.CartItemNotFoundException;
import ru.kon.onlineshop.exceptions.cart.InsufficientStockException;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.repository.CartItemRepository;
import ru.kon.onlineshop.repository.CartRepository;
import ru.kon.onlineshop.repository.ProductRepository;
import ru.kon.onlineshop.repository.UserRepository;
import ru.kon.onlineshop.security.model.UserDetailsImpl;
import ru.kon.onlineshop.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product product1;
    private CartItem cartItem1;
    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID_1 = 101L;

    private MockedStatic<SecurityContextHolder> mockedStaticContextHolder;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(USER_ID).email("test@example.com").firstName("Test").lastName("User").password("password").role(Role.ROLE_USER).createdAt(Instant.now()).build();
        product1 = Product.builder().id(PRODUCT_ID_1).name("Test Product 1").basePrice(new BigDecimal("100.00")).discountPrice(new BigDecimal("90.00")).stockQuantity(10).build();
        testCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        cartItem1 = CartItem.builder().id(1L).cart(testCart).product(product1).quantity(2).build();

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(testUser);

        mockedStaticContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedStaticContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @AfterEach
    void tearDown() {
        mockedStaticContextHolder.close();
    }

    @Test
    void getCart_whenCartExists_shouldReturnCartResponse() {
        testCart.getItems().add(cartItem1);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart();

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(PRODUCT_ID_1, response.getItems().get(0).getProductId());
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotalAmount()));
        verify(cartRepository).findByUserId(USER_ID);
        verify(userRepository, never()).findById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }


    @Test
    void getCart_whenCartNotExists_shouldCreateAndReturnNewCartResponse() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart newCart = invocation.getArgument(0);
            newCart.setId(2L);
            newCart.setUser(testUser);
            newCart.setItems(new ArrayList<>());
            return newCart;
        });

        CartResponse response = cartService.getCart();

        assertNotNull(response);
        assertTrue(response.getItems().isEmpty(), "Новая корзина должна быть пустой");
        assertEquals(0, response.getTotalItems());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalAmount()), "Сумма новой корзины должна быть 0");

        verify(cartRepository).findByUserId(USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartRepository).save(argThat(cart -> cart.getUser().getId().equals(USER_ID) && cart.getId() == 2L));
    }

    @Test
    void addItem_whenProductIsNewToCart_shouldAddItemAndReturnUpdatedCart() {
        CartItemRequest request = new CartItemRequest(PRODUCT_ID_1, 1);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(PRODUCT_ID_1)).thenReturn(Optional.of(product1));

        when(cartRepository.save(testCart)).thenReturn(testCart);

        CartResponse response = cartService.addItem(request);

        assertNotNull(response);
        assertEquals(1, response.getTotalItems());
        assertEquals(1, response.getItems().size());
        CartItemDto addedItemDto = response.getItems().get(0);
        assertEquals(PRODUCT_ID_1, addedItemDto.getProductId());
        assertEquals(1, addedItemDto.getQuantity());
        assertEquals(0, new BigDecimal("90.00").compareTo(response.getTotalAmount()));

        verify(cartRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID_1);
        verify(cartItemRepository).save(argThat(item -> item.getProduct().getId().equals(PRODUCT_ID_1) && item.getQuantity() == 1 && item.getId() == null)); // ID должен быть null при первом сохранении
        verify(cartRepository).save(testCart);
    }

    @Test
    void addItem_whenProductExistsInCart_shouldUpdateQuantityAndReturnUpdatedCart() {
        testCart.getItems().add(cartItem1);
        CartItemRequest request = new CartItemRequest(PRODUCT_ID_1, 3);
        int expectedQuantity = cartItem1.getQuantity() + request.getQuantity();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(PRODUCT_ID_1)).thenReturn(Optional.of(product1));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem updatedItem = invocation.getArgument(0);
            cartItem1.setQuantity(updatedItem.getQuantity());
            return updatedItem;
        });
        when(cartRepository.save(testCart)).thenReturn(testCart);

        CartResponse response = cartService.addItem(request);

        assertNotNull(response);
        assertEquals(1, response.getTotalItems());
        assertEquals(1, response.getItems().size());
        CartItemDto updatedItemDto = response.getItems().get(0);
        assertEquals(PRODUCT_ID_1, updatedItemDto.getProductId());
        assertEquals(expectedQuantity, updatedItemDto.getQuantity()); // Ожидаем 5
        assertEquals(0, new BigDecimal("450.00").compareTo(response.getTotalAmount()));

        verify(cartRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID_1);
        verify(cartItemRepository).save(argThat(item -> item.getId().equals(cartItem1.getId()) && item.getQuantity() == expectedQuantity));
        verify(cartRepository).save(testCart);
    }

    @Test
    void addItem_whenInsufficientStock_shouldThrowInsufficientStockException() {
        CartItemRequest request = new CartItemRequest(PRODUCT_ID_1, product1.getStockQuantity() + 1);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(PRODUCT_ID_1)).thenReturn(Optional.of(product1));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> cartService.addItem(request)
        );
        assertTrue(exception.getMessage().contains("Недостаточно товара"));
        assertTrue(exception.getMessage().contains(product1.getName()));

        verify(cartRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID_1);
        verify(cartItemRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItem_whenProductNotFound_shouldThrowProductNotFoundException() {
        Long nonExistentProductId = 999L;
        CartItemRequest request = new CartItemRequest(nonExistentProductId, 1);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> cartService.addItem(request)
        );
        assertEquals("Продукт не найден: " + nonExistentProductId, exception.getMessage());

        verify(cartRepository).findByUserId(USER_ID);
        verify(productRepository).findById(nonExistentProductId);
        verify(cartItemRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateItem_whenItemExists_shouldUpdateQuantityAndReturnCart() {
        testCart.getItems().add(cartItem1);
        int newQuantity = 5;
        CartItemRequest request = new CartItemRequest(PRODUCT_ID_1, newQuantity);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(testCart)).thenReturn(testCart);

        CartResponse response = cartService.updateItem(request);

        assertNotNull(response);
        assertEquals(1, response.getTotalItems());
        assertEquals(newQuantity, response.getItems().get(0).getQuantity());
        assertEquals(0, new BigDecimal("450.00").compareTo(response.getTotalAmount()));

        verify(cartRepository).findByUserId(USER_ID);
        verify(cartRepository).save(testCart);
    }

    @Test
    void updateItem_whenItemNotFoundInCart_shouldThrowCartItemNotFoundException() {
        CartItemRequest request = new CartItemRequest(PRODUCT_ID_1, 5);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        CartItemNotFoundException exception = assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.updateItem(request)
        );
        assertTrue(exception.getMessage().contains("Товар с ID " + PRODUCT_ID_1 + " не найден в корзине"));

        verify(cartRepository).findByUserId(USER_ID);
        verify(cartItemRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }


    @Test
    void removeItem_whenItemExists_shouldRemoveItem() {
        testCart.getItems().add(cartItem1);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).delete(cartItem1);
        when(cartRepository.save(testCart)).thenReturn(testCart);

        cartService.removeItem(PRODUCT_ID_1);

        verify(cartRepository).findByUserId(USER_ID);
        verify(cartItemRepository).delete(cartItem1);
        verify(cartRepository).save(testCart);
    }

    @Test
    void removeItem_whenItemNotFound_shouldThrowCartItemNotFoundException() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        CartItemNotFoundException exception = assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.removeItem(PRODUCT_ID_1)
        );
        assertTrue(exception.getMessage().contains("Товар с ID " + PRODUCT_ID_1 + " не найден в корзине"));

        verify(cartRepository).findByUserId(USER_ID);
        verify(cartItemRepository, never()).delete(any());
        verify(cartRepository, never()).save(any());
    }
}
