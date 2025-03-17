package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.entity.Category;
import ru.kon.onlineshop.dto.entity.Product;
import ru.kon.onlineshop.dto.product.CreateProductRequest;
import ru.kon.onlineshop.dto.product.ProductDetailsDto;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.product.UpdateProductRequest;
import ru.kon.onlineshop.dto.repository.CategoryRepository;
import ru.kon.onlineshop.dto.repository.ProductRepository;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.service.ProductService;

import javax.transaction.Transactional;
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
    private final ModelMapper modelMapper;

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
                .build();

        Product savedProduct = productRepository.save(product);
        return convertToProductDetailsDto(savedProduct);
    }

    @Override
    public ProductDetailsDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        modelMapper.map(request, product);
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
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toSet());
    }

    private ProductDto convertToProductDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    private ProductDetailsDto convertToProductDetailsDto(Product product) {
        return modelMapper.map(product, ProductDetailsDto.class);
    }
}
