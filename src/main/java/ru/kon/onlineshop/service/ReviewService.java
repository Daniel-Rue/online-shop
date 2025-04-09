package ru.kon.onlineshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.kon.onlineshop.dto.review.CreateReviewRequest;
import ru.kon.onlineshop.dto.review.ProductRatingDto;
import ru.kon.onlineshop.dto.review.ReviewDto;

import java.io.IOException;
import java.util.List;

public interface ReviewService {

    /**
     * Добавляет новый отзыв к продукту.
     * Требует аутентификации пользователя.
     *
     * @param productId ID продукта, к которому добавляется отзыв.
     * @param request   DTO с данными отзыва (рейтинг, комментарий).
     * @param photos    Список файлов с фотографиями для прикрепления к отзыву (может быть null или пустым).
     * @return DTO созданного отзыва.
     * @throws IOException             если произошла ошибка при сохранении фото.
     * @throws javax.persistence.EntityNotFoundException если пользователь или продукт не найдены.
     * @throws IllegalArgumentException если пользователь уже оставлял отзыв на этот продукт.
     * @throws org.springframework.security.access.AccessDeniedException   если пользователь не аутентифицирован.
     */
    ReviewDto addReview(Long productId, CreateReviewRequest request, List<MultipartFile> photos) throws IOException;

    /**
     * Получает страницу с отзывами для указанного продукта.
     * Позволяет фильтровать по рейтингу и использовать пагинацию/сортировку.
     * Доступно для всех пользователей.
     *
     * @param productId    ID продукта.
     * @param ratingFilter Опциональный фильтр по рейтингу (1-5). Если null, фильтрация не применяется.
     * @param pageable     Объект Pageable для пагинации и сортировки.
     * @return Страница (Page) с DTO отзывов.
     * @throws javax.persistence.EntityNotFoundException если продукт не найден.
     */
    Page<ReviewDto> getReviewsForProduct(Long productId, Integer ratingFilter, Pageable pageable);

    /**
     * Рассчитывает и возвращает средний рейтинг и количество отзывов для продукта.
     * Доступно для всех пользователей.
     *
     * @param productId ID продукта.
     * @return DTO с информацией о рейтинге продукта.
     * @throws javax.persistence.EntityNotFoundException если продукт не найден.
     */
    ProductRatingDto getProductAverageRating(Long productId);

    /**
     * Удаляет отзыв по его ID.
     * Также удаляет связанные с отзывом фотографии из хранилища.
     * Требует прав администратора.
     *
     * @param reviewId ID отзыва для удаления.
     * @throws IOException             если произошла ошибка при удалении файлов фотографий.
     * @throws javax.persistence.EntityNotFoundException если отзыв с указанным ID не найден.
     * @throws org.springframework.security.access.AccessDeniedException если у пользователя нет прав на удаление (не администратор).
     */
    void deleteReview(Long reviewId) throws IOException;

    /**
     * Удаляет конкретную фотографию из указанного отзыва.
     * Удаляет файл фотографии из хранилища и связь из базы данных.
     * Требует прав администратора.
     *
     * @param reviewId ID отзыва, из которого удаляется фотография.
     * @param photoId  ID фотографии для удаления.
     * @throws IOException             если произошла ошибка при удалении файла фотографии.
     * @throws javax.persistence.EntityNotFoundException если отзыв или фотография с указанными ID не найдены.
     * @throws org.springframework.security.access.AccessDeniedException если у пользователя нет прав на удаление (не администратор).
     */
    void deleteReviewPhoto(Long reviewId, Long photoId) throws IOException;
}