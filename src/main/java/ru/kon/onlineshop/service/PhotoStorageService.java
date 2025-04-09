package ru.kon.onlineshop.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PhotoStorageService {

    /**
     * Сохраняет одно фото и возвращает его URL или путь.
     * @param file Файл для сохранения.
     * @param subfolder Подпапка для организации (например, "reviews", "products").
     * @return Публичный URL или путь к сохраненному файлу.
     * @throws IOException Если произошла ошибка при сохранении.
     */
    String storePhoto(MultipartFile file, String subfolder) throws IOException;

    /**
     * Сохраняет несколько фотографий.
     * @param files Список файлов.
     * @param subfolder Подпапка.
     * @return Список URL или путей к сохраненным файлам.
     * @throws IOException Если произошла ошибка при сохранении хотя бы одного файла.
     */
    List<String> storePhotos(List<MultipartFile> files, String subfolder) throws IOException;

    /**
     * Удаляет фото по его URL или пути.
     * @param fileUrl URL или путь к файлу.
     * @throws IOException Если произошла ошибка при удалении.
     */
    void deletePhoto(String fileUrl) throws IOException;
}
