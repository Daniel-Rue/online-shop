package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.category.CategoryTreeDto;
import ru.kon.onlineshop.dto.category.CreateCategoryRequest;
import ru.kon.onlineshop.dto.category.UpdateCategoryRequest;
import ru.kon.onlineshop.dto.product.ProductDto;

import java.util.List;

public interface CategoryService {

    /**
     * Возвращает полное дерево категорий.
     *
     * @return список корневых категорий с вложенными подкатегориями
     */
    List<CategoryTreeDto> getFullCategoryTree();

    /**
     * Возвращает поддерево категорий, начиная с указанной категории.
     *
     * @param id идентификатор категории
     * @return поддерево категорий
     */
    CategoryTreeDto getCategorySubtree(Long id);

    /**
     * Создает новую категорию.
     *
     * @param request данные для создания категории
     * @return созданная категория
     */
    CategoryDto createCategory(CreateCategoryRequest request);

    /**
     * Обновляет существующую категорию.
     *
     * @param id      идентификатор категории
     * @param request данные для обновления
     * @return обновленная категория
     */
    CategoryDto updateCategory(Long id, UpdateCategoryRequest request);

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param id идентификатор категории
     */
    void deleteCategory(Long id);

    /**
     * Возвращает список товаров в категории и её подкатегориях.
     *
     * @param categoryId идентификатор категории
     * @param sortBy     поле для сортировки (например, "basePrice")
     * @param sortOrder  порядок сортировки ("asc" или "desc")
     * @return список товаров
     */
    List<ProductDto> getProductsInCategory(Long categoryId, String sortBy, String sortOrder);
}
