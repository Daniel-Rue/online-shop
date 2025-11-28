package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.category.CategoryDto;
import ru.kon.onlineshop.dto.product.CreateProductRequest;
import ru.kon.onlineshop.dto.product.ProductDetailsDto;
import ru.kon.onlineshop.dto.product.ProductDto;
import ru.kon.onlineshop.dto.product.UpdateProductRequest;
import ru.kon.onlineshop.dto.product.attribute.AttributeValueInputDto;
import ru.kon.onlineshop.dto.product.attribute.ProductAttributeValueDto;

import java.util.List;
import java.util.Set;

public interface ProductService {

    /**
     * Получение всех товаров с сортировкой
     *
     * @param sortBy    поле для сортировки (например, "basePrice", "name")
     * @param sortOrder порядок сортировки ("asc" или "desc")
     * @return список товаров в формате DTO
     */
    List<ProductDto> getAllProducts(String sortBy, String sortOrder);

    /**
     * Получение детальной информации о товаре
     *
     * @param id идентификатор товара
     * @return детали товара с категориями
     */
    ProductDetailsDto getProductDetails(Long id);

    /**
     * Создание нового товара
     *
     * @param request данные для создания товара
     * @return созданный товар с деталями
     */
    ProductDetailsDto createProduct(CreateProductRequest request);

    /**
     * Обновление существующего товара
     *
     * @param id      идентификатор товара
     * @param request данные для обновления
     * @return обновленный товар с деталями
     */
    ProductDetailsDto updateProduct(Long id, UpdateProductRequest request);

    /**
     * Удаление товара
     *
     * @param id идентификатор товара
     */
    void deleteProduct(Long id);

    /**
     * Обновление привязки товара к категориям
     *
     * @param productId   идентификатор товара
     * @param categoryIds набор идентификаторов категорий
     */
    void updateProductCategories(Long productId, Set<Long> categoryIds);

    /**
     * Получение категорий товара
     *
     * @param productId идентификатор товара
     * @return набор категорий в формате DTO
     */
    Set<CategoryDto> getProductCategories(Long productId);

    /**
     * Обновляет значения атрибутов для указанного товара.
     * Старые значения атрибутов для данного товара удаляются и заменяются новыми.
     *
     * @param productId Идентификатор товара.
     * @param values    Список DTO со значениями атрибутов (ID атрибута и его строковое значение).
     */
    void updateProductAttributeValues(Long productId, List<AttributeValueInputDto> values);

    /**
     * Получает список значений атрибутов для указанного товара.
     *
     * @param productId Идентификатор товара.
     * @return Список DTO со значениями атрибутов (ID, имя, тип атрибута и его значение для продукта).
     */
    List<ProductAttributeValueDto> getProductAttributeValues(Long productId);
}