package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kon.onlineshop.dto.cart.CartItemRequest;
import ru.kon.onlineshop.dto.cart.CartResponse;
import ru.kon.onlineshop.dto.cart.OrderResponse;
import ru.kon.onlineshop.service.CartService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @RequestBody @Valid CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long userId,
            @RequestBody @Valid CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(userId, request));
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId
    ) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(cartService.checkout(userId));
    }
}