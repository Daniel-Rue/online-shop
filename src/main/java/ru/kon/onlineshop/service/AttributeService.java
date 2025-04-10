package ru.kon.onlineshop.service;

import ru.kon.onlineshop.dto.product.attribute.AttributeDto;
import ru.kon.onlineshop.dto.product.attribute.CreateAttributeRequest;
import ru.kon.onlineshop.dto.product.attribute.UpdateAttributeRequest;
import ru.kon.onlineshop.exceptions.product.attribute.AttributeAlreadyExistsException;
import ru.kon.onlineshop.exceptions.product.attribute.ResourceNotFoundException;

import java.util.List;

public interface AttributeService {

    /**
     * Создание нового атрибута.
     *
     * @param request DTO с данными для создания атрибута.
     * @return DTO созданного атрибута.
     * @throws AttributeAlreadyExistsException если атрибут с таким именем уже существует.
     */
    AttributeDto createAttribute(CreateAttributeRequest request);

    /**
     * Получение атрибута по его идентификатору.
     *
     * @param id Идентификатор атрибута.
     * @return DTO найденного атрибута.
     * @throws ResourceNotFoundException если атрибут с указанным ID не найден.
     */
    AttributeDto getAttributeById(Long id);

    /**
     * Получение списка всех существующих атрибутов.
     *
     * @return Список DTO всех атрибутов.
     */
    List<AttributeDto> getAllAttributes();

    /**
     * Обновление существующего атрибута.
     *
     * @param id      Идентификатор обновляемого атрибута.
     * @param request DTO с новыми данными для атрибута.
     * @return DTO обновленного атрибута.
     * @throws ResourceNotFoundException если атрибут с указанным ID не найден.
     * @throws AttributeAlreadyExistsException если новое имя конфликтует с именем другого существующего атрибута.
     */
    AttributeDto updateAttribute(Long id, UpdateAttributeRequest request);

    /**
     * Удаление атрибута по его идентификатору.
     * ВАЖНО: Простая реализация может не проверять, используется ли атрибут в товарах.
     *          Более сложная реализация должна либо запрещать удаление используемого атрибута,
     *          либо каскадно удалять связанные ProductAttributeValue.
     *
     * @param id Идентификатор удаляемого атрибута.
     * @throws ResourceNotFoundException если атрибут с указанным ID не найден.
     */
    void deleteAttribute(Long id);
}
