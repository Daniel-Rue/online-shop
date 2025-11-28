package ru.kon.onlineshop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.entity.Category;
import ru.kon.onlineshop.entity.Product;
import ru.kon.onlineshop.dto.product.CreateProductRequest;
import ru.kon.onlineshop.dto.product.ProductDetailsDto;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.product.UpdateProductRequest;
import ru.kon.onlineshop.repository.CategoryRepository;
import ru.kon.onlineshop.repository.ProductRepository;
import ru.kon.onlineshop.exceptions.product.ProductNotFoundException;
import ru.kon.onlineshop.service.impl.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAllProducts_ShouldReturnSortedProducts() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Product 1"),
                createTestProduct(2L, "Product 2")
        );

        when(productRepository.findAll(any(Sort.class))).thenReturn(products);

        List<ProductDto> result = productService.getAllProducts("name", "ASC");

        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        verify(productRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void getProductDetails_WhenProductExists_ShouldReturnDetails() {
        Product product = createTestProduct(1L, "Test Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailsDto result = productService.getProductDetails(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(2, result.getCategoryIds().size());
    }

    @Test
    void getProductDetails_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductDetails(999L));
    }

    @Test
    void createProduct_ShouldSaveNewProductWithCategories() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Product");
        request.setCategoryIds(new HashSet<>(Arrays.asList(1L, 2L)));

        Set<Category> categories = new HashSet<>(Arrays.asList(
                new Category(1L, "Category 1", null, new ArrayList<>(), new HashSet<>()),
                new Category(2L, "Category 2", null, new ArrayList<>(), new HashSet<>())
        ));

        when(categoryRepository.findAllById(any())).thenReturn(new ArrayList<>(categories));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            p.setCategories(categories);
            return p;
        });

        ProductDetailsDto result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(2, result.getCategoryIds().size());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateExistingProduct() {
        Product existingProduct = createTestProduct(1L, "Old Name");
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDetailsDto result = productService.updateProduct(1L, request);

        assertEquals("New Name", result.getName());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void deleteProduct_ShouldDeleteExistingProduct() {
        Product product = createTestProduct(1L, "To Delete");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void updateProductCategories_ShouldUpdateCategories() {
        Product product = createTestProduct(1L, "Product");
        Set<Long> newCategoryIds = new HashSet<>(Arrays.asList(3L, 4L)); // Исправлено для Java 8
        List<Category> newCategories = Arrays.asList(
                new Category(3L, "Cat3", null, new ArrayList<>(), new HashSet<>()),
                new Category(4L, "Cat4", null, new ArrayList<>(), new HashSet<>())
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(newCategoryIds)).thenReturn(newCategories);

        productService.updateProductCategories(1L, newCategoryIds);

        assertEquals(2, product.getCategories().size());
        verify(productRepository).save(product);
    }

    @Test
    void getProductCategories_ShouldReturnCategoryDtos() {
        Product product = createTestProduct(1L, "Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Set<CategoryDto> result = productService.getProductCategories(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Category 1")));
    }

    private Product createTestProduct(Long id, String name) {
        Set<Category> categories = new HashSet<>(Arrays.asList(
                new Category(1L, "Category 1", null, new ArrayList<>(), new HashSet<>()),
                new Category(2L, "Category 2", null, new ArrayList<>(), new HashSet<>())
        ));

        return Product.builder()
                .id(id)
                .name(name)
                .description("Test Description")
                .imageUrl("image.jpg")
                .basePrice(new BigDecimal("100.00"))
                .discountPrice(new BigDecimal("90.00"))
                .stockQuantity(10)
                .categories(categories)
                .build();
    }
}
