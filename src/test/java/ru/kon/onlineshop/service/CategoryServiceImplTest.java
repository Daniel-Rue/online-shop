package ru.kon.onlineshop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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
import ru.kon.onlineshop.service.impl.CategoryServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getFullCategoryTree_ShouldReturnTreeStructure() {
        Category root = createCategory(1L, "Root", null);
        Category child = createCategory(2L, "Child", root);
        root.setChildren(new ArrayList<>(Collections.singletonList(child)));
        child.setParent(root);

        when(categoryRepository.findByParentIsNull()).thenReturn(Collections.singletonList(root));

        List<CategoryTreeDto> result = categoryService.getFullCategoryTree();

        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("Child", result.get(0).getChildren().get(0).getName());
    }

    @Test
    void getCategorySubtree_WhenCategoryExists_ShouldReturnTree() {
        Category root = createCategory(1L, "Root", null);
        Category child = createCategory(2L, "Child", root);
        root.setChildren(new ArrayList<>(Collections.singletonList(child)));
        child.setParent(root);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

        CategoryTreeDto result = categoryService.getCategorySubtree(1L);

        assertEquals("Root", result.getName());
        assertEquals(1, result.getChildren().size());
    }

    @Test
    void getCategorySubtree_WhenCategoryNotFound_ShouldThrowException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategorySubtree(999L));
    }

    @Test
    void createCategory_WithParent_ShouldSaveWithParent() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("New Category");
        request.setParentId(1L);

        Category parent = createCategory(1L, "Parent", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.createCategory(request);

        assertEquals("New Category", result.getName());
        assertEquals(1L, result.getParentId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldUpdateNameAndParent() {
        Category category = createCategory(1L, "Old Name", null);
        Category newParent = createCategory(2L, "New Parent", null);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Name");
        request.setParentId(2L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertEquals("New Name", result.getName());
        assertEquals(2L, result.getParentId());
    }

    @Test
    void deleteCategory_ShouldCallRepositoryDelete() {
        Category category = createCategory(1L, "To Delete", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void getProductsInCategory_ShouldReturnSortedProducts() {
        Product p1 = createProduct(1L, "Product 1");
        Product p2 = createProduct(2L, "Product 2");
        List<Long> categoryIds = Arrays.asList(1L, 2L);

        when(categoryRepository.findCategoryAndAllChildrenIds(1L)).thenReturn(categoryIds);
        when(productRepository.findByCategoryIds(anySet(), any(Sort.class)))
                .thenReturn(Arrays.asList(p2, p1));

        List<ProductDto> result = categoryService.getProductsInCategory(
                1L, "name", "DESC");

        assertEquals(2, result.size());
        verify(productRepository).findByCategoryIds(
                new HashSet<>(categoryIds),
                Sort.by(Sort.Direction.DESC, "name")
        );
    }

    private Category createCategory(Long id, String name, Category parent) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParent(parent);
        category.setChildren(new ArrayList<>());
        category.setProducts(new HashSet<>());

        if (parent != null) {
            parent.getChildren().add(category);
        }
        return category;
    }

    private Product createProduct(Long id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .imageUrl("image.jpg")
                .basePrice(new BigDecimal("100.00"))
                .discountPrice(new BigDecimal("90.00"))
                .stockQuantity(10)
                .categories(new HashSet<>())
                .build();
    }
}