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
@RequestMapping("/api/cart") // Базовый путь теперь без {userId}
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(
            @RequestBody @Valid CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateItem(
            @RequestBody @Valid CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(request));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long productId
    ) {
        cartService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout() {
        return ResponseEntity.ok(cartService.checkout());
    }
}