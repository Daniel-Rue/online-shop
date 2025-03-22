package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.category.CategoryTreeDto;
import ru.kon.onlineshop.dto.category.CreateCategoryRequest;
import ru.kon.onlineshop.dto.category.UpdateCategoryRequest;
import ru.kon.onlineshop.dto.entity.Category;
import ru.kon.onlineshop.dto.entity.Product;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.repository.CategoryRepository;
import ru.kon.onlineshop.dto.repository.ProductRepository;
import ru.kon.onlineshop.exceptions.category.CategoryNotFoundException;
import ru.kon.onlineshop.service.CategoryService;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryTreeDto> getFullCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToTreeDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryTreeDto getCategorySubtree(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return convertToTreeDto(category);
    }

    @Override
    public CategoryDto createCategory(CreateCategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
        }

        Category newCategory = Category.builder()
                .name(request.getName())
                .parent(parent)
                .build();

        Category savedCategory = categoryRepository.save(newCategory);
        return convertToCategoryDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
        }

        category.setName(request.getName());
        category.setParent(parent);
        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }

    @Override
    public List<ProductDto> getProductsInCategory(Long categoryId, String sortBy, String sortOrder) {
        Set<Long> categoryIds = getCategoryWithAllChildrenIds(categoryId);
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        List<Product> products = productRepository.findByCategoryIds(categoryIds, sort);
        return products.stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    private CategoryTreeDto convertToTreeDto(Category category) {
        CategoryTreeDto dto = new CategoryTreeDto();
        dto.setId(category.getId());
        dto.setName(category.getName());

        if (!category.getChildren().isEmpty()) {
            List<CategoryTreeDto> children = category.getChildren().stream()
                    .map(this::convertToTreeDto)
                    .collect(Collectors.toList());
            dto.setChildren(children);
        } else {
            dto.setChildren(Collections.emptyList());
        }
        return dto;
    }

    private CategoryDto convertToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        dto.setProductCount(category.getProducts().size());
        return dto;
    }

    private ProductDto convertToProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setBasePrice(product.getBasePrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStockQuantity(product.getStockQuantity());
        return dto;
    }

    private Set<Long> getCategoryWithAllChildrenIds(Long categoryId) {
        List<Long> categoryIds = categoryRepository.findCategoryAndAllChildrenIds(categoryId);
        return new HashSet<>(categoryIds);
    }
}
