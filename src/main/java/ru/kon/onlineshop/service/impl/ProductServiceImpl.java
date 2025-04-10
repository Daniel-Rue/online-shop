package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.product.CreateProductRequest;
import ru.kon.onlineshop.dto.product.ProductDetailsDto;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.product.UpdateProductRequest;
import ru.kon.onlineshop.dto.product.attribute.AttributeValueInputDto;
import ru.kon.onlineshop.dto.product.attribute.ProductAttributeValueDto;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.exceptions.BadRequestException;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.exceptions.product.attribute.ResourceNotFoundException;
import ru.kon.onlineshop.repository.AttributeRepository;
import ru.kon.onlineshop.repository.CategoryRepository;
import ru.kon.onlineshop.repository.ProductAttributeValueRepository;
import ru.kon.onlineshop.repository.ProductRepository;
import ru.kon.onlineshop.service.ProductService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;

    @Override
    public List<ProductDto> getAllProducts(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        return productRepository.findAll(sort)
                .stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDetailsDto getProductDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return convertToProductDetailsDto(product);
    }

    @Override
    public ProductDetailsDto createProduct(CreateProductRequest request) {
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .basePrice(request.getBasePrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .categories(categories)
                .attributeValues(new HashSet<>())
                .build();

        Product savedProduct = productRepository.save(product);
        return convertToProductDetailsDto(savedProduct);
    }

    @Override
    public ProductDetailsDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product updatedProduct = productRepository.save(product);
        return convertToProductDetailsDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
    }

    @Override
    public void updateProductCategories(Long productId, Set<Long> categoryIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        product.setCategories(new HashSet<>(categories));
        productRepository.save(product);
    }

    @Override
    public Set<CategoryDto> getProductCategories(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return product.getCategories().stream()
                .map(this::convertToCategoryDto)
                .collect(Collectors.toSet());
    }

    public void updateProductAttributeValues(Long productId, List<AttributeValueInputDto> values) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productAttributeValueRepository.deleteByProductId(productId);

        Set<ProductAttributeValue> newAttributeValues = new HashSet<>();
        for (AttributeValueInputDto inputDto : values) {
            Attribute attribute = attributeRepository.findById(inputDto.getAttributeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Attribute", "id", inputDto.getAttributeId()
                    ));

            validateAttributeValue(attribute, inputDto.getValue());

            ProductAttributeValue pav = ProductAttributeValue.builder()
                    .product(product)
                    .attribute(attribute)
                    .value(inputDto.getValue())
                    .build();
            newAttributeValues.add(pav);
        }

        product.getAttributeValues().clear();
        product.getAttributeValues().addAll(newAttributeValues);

        productRepository.save(product);
    }

    @Override
    @Transactional
    public List<ProductAttributeValueDto> getProductAttributeValues(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return product.getAttributeValues().stream()
                .map(this::mapToProductAttributeValueDto)
                .collect(Collectors.toList());
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

    private ProductDetailsDto convertToProductDetailsDto(Product product) {
        ProductDetailsDto dto = new ProductDetailsDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setBasePrice(product.getBasePrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryIds(
                product.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet())
        );
        dto.setAttributeValues(
                product.getAttributeValues().stream()
                        .map(this::mapToProductAttributeValueDto)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private CategoryDto convertToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    private ProductAttributeValueDto mapToProductAttributeValueDto(ProductAttributeValue pav) {
        ProductAttributeValueDto dto = new ProductAttributeValueDto();
        dto.setAttributeId(pav.getAttribute().getId());
        dto.setAttributeName(pav.getAttribute().getName());
        dto.setAttributeType(pav.getAttribute().getType());
        dto.setValue(pav.getValue());
        dto.setUnit(pav.getAttribute().getUnit());
        return dto;
    }

    private void validateAttributeValue(Attribute attribute, String value) {
        AttributeType type = attribute.getType();
        try {
            switch (type) {
                case NUMBER:
                    new BigDecimal(value);
                    break;
                case BOOLEAN:
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Значение должно быть 'true' или 'false'");
                    }
                    break;
                case STRING:
                    if (value == null) {
                        throw new IllegalArgumentException("Строковое значение не может быть null");
                    }
                    break;
                default:
                    throw new IllegalStateException("Неподдерживаемый тип атрибута: " + type);
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Неверный формат числового значения '" + value + "' для атрибута '" + attribute.getName() + "'");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Неверное значение '" + value + "' для атрибута '" + attribute.getName() + "': " + e.getMessage());
        }
    }
}
