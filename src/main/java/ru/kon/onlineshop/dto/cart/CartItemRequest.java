package ru.kon.onlineshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    @NotNull
    private Long productId;
    private int quantity;
}
