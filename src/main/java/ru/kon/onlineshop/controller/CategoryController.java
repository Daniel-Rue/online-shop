package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.category.CategoryTreeDto;
import ru.kon.onlineshop.dto.category.CreateCategoryRequest;
import ru.kon.onlineshop.dto.category.UpdateCategoryRequest;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.service.CategoryService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeDto>> getCategoryTree() {
        List<CategoryTreeDto> categoryTree = categoryService.getFullCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<CategoryTreeDto> getSubtree(@PathVariable Long id) {
        CategoryTreeDto subtree = categoryService.getCategorySubtree(id);
        return ResponseEntity.ok(subtree);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDto updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductDto>> getCategoryProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "basePrice") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        List<ProductDto> products = categoryService.getProductsInCategory(id, sortBy, sortOrder);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/products/filtered")
    public ResponseEntity<Page<ProductDto>> getCategoryProductsFiltered(
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, String> filters,
            @PageableDefault(size = 10, sort = "basePrice") Pageable pageable) {

        Page<ProductDto> productsPage = categoryService.getProductsInCategoryFiltered(id, filters, pageable);
        return ResponseEntity.ok(productsPage);
    }


}