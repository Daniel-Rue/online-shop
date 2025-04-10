package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.category.CategoryTreeDto;
import ru.kon.onlineshop.dto.category.CreateCategoryRequest;
import ru.kon.onlineshop.dto.category.UpdateCategoryRequest;
import ru.kon.onlineshop.entity.Attribute;
import ru.kon.onlineshop.entity.Category;
import ru.kon.onlineshop.entity.Product;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.exceptions.product.attribute.ResourceNotFoundException;
import ru.kon.onlineshop.repository.AttributeRepository;
import ru.kon.onlineshop.repository.CategoryRepository;
import ru.kon.onlineshop.repository.ProductRepository;
import ru.kon.onlineshop.exceptions.category.CategoryNotFoundException;
import ru.kon.onlineshop.service.CategoryService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;

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

    @Override
    public Page<ProductDto> getProductsInCategoryFiltered(Long categoryId, Map<String, String> filters, Pageable pageable) {
        Set<Long> allCategoryIds = getCategoryWithAllChildrenIds(categoryId);
        if (allCategoryIds.isEmpty()) {
            throw new CategoryNotFoundException(categoryId);
        }

        Specification<Product> spec = Specification.where(ProductSpecificationImpl.inCategories(allCategoryIds));

        BigDecimal minPrice = parseBigDecimal(filters.get("minPrice"));
        BigDecimal maxPrice = parseBigDecimal(filters.get("maxPrice"));
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecificationImpl.hasPriceBetween(minPrice, maxPrice));
        }

        Double minRating = parseDouble(filters.get("minRating"));
        Specification<Product> ratingSpec = ProductSpecificationImpl.hasMinRating(minRating);
        if (ratingSpec != null) {
            spec = spec.and(ratingSpec);
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith("attr_") && StringUtils.hasText(value)) {
                try {
                    String[] parts = key.split("_");
                    Long attrId = Long.parseLong(parts[1]);
                    Attribute attribute = attributeRepository.findById(attrId)
                            .orElseThrow(() -> new ResourceNotFoundException("Attribute", "id", attrId));

                    Specification<Product> attrSpec = ProductSpecificationImpl.hasAttributeValue(
                            attrId, value, attribute.getType()
                    );
                    if (attrSpec != null) {
                        spec = spec.and(attrSpec);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Ошибка парсинга ключа фильтра атрибута: " + key + " - " + e.getMessage());
                } catch (ResourceNotFoundException e) {
                    System.err.println("Фильтр по несуществующему атрибуту: " + e.getMessage());
                    spec = spec.and((r, q, cb) -> cb.disjunction());
                    break;
                }
            }
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());

        return new PageImpl<>(productDtos, pageable, productPage.getTotalElements());
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

    private BigDecimal parseBigDecimal(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
