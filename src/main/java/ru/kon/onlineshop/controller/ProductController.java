package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.product.CreateProductRequest;
import ru.kon.onlineshop.dto.product.ProductDetailsDto;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.product.UpdateProductRequest;
import ru.kon.onlineshop.service.ProductService;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "basePrice") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        List<ProductDto> products = productService.getAllProducts(sortBy, sortOrder);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> getProductById(@PathVariable Long id) {
        ProductDetailsDto product = productService.getProductDetails(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDto> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDetailsDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailsDto updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/categories")
    public ResponseEntity<Void> addProductToCategories(
            @PathVariable Long productId,
            @RequestBody Set<Long> categoryIds) {
        productService.updateProductCategories(productId, categoryIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}/categories")
    public ResponseEntity<Set<CategoryDto>> getProductCategories(
            @PathVariable Long productId) {
        Set<CategoryDto> categories = productService.getProductCategories(productId);
        return ResponseEntity.ok(categories);
    }
}
