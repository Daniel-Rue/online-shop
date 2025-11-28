package ru.kon.onlineshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.category.CategoryTreeDto;
import ru.kon.onlineshop.dto.category.CreateCategoryRequest;
import ru.kon.onlineshop.dto.category.UpdateCategoryRequest;
import ru.kon.onlineshop.dto.product.ProductDto;

import java.util.List;
import java.util.Map;

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

    /**
     * Получение страницы товаров, принадлежащих указанной категории (и всем ее подкатегориям),
     * с возможностью применения фильтров и сортировки.
     *
     * @param categoryId ID категории, товары которой (включая подкатегории) нужно получить.
     * @param filters    Карта фильтров, где ключ - название фильтра (например, "minPrice", "maxPrice", "minRating", "attr_1"),
     *                   а значение - значение фильтра.
     * @param pageable   Объект, содержащий информацию для пагинации (номер страницы, размер) и сортировки.
     * @return Страница (Page) с товарами в формате ProductDto, удовлетворяющими критериям.
     */
    Page<ProductDto> getProductsInCategoryFiltered(Long categoryId, Map<String, String> filters, Pageable pageable);
}
