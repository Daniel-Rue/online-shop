package ru.kon.onlineshop.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.kon.onlineshop.service.PhotoStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileSystemPhotoStorageService implements PhotoStorageService {

    private final Path storageLocation;
    private final String baseUrl;

    public FileSystemPhotoStorageService(
            @Value("${photo.storage.location:./uploads}") String uploadDir,
            @Value("${photo.storage.base-url:/media}") String baseUrl
    ) {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(this.storageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не удалось создать каталог, в котором будут храниться загруженные файлы.", ex);
        }
    }

    @Override
    public String storePhoto(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть нулевым или пустым.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception ignored) {
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetDirectory = this.storageLocation.resolve(subfolder).normalize();
        Files.createDirectories(targetDirectory);

        Path targetLocation = targetDirectory.resolve(uniqueFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IOException("Не удалось сохранить файл " + originalFilename + ". Пожалуйста, попробуйте снова!", ex);
        }

        return baseUrl + "/" + subfolder + "/" + uniqueFilename;
    }

    @Override
    public List<String> storePhotos(List<MultipartFile> files, String subfolder) throws IOException {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> storedFileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                storedFileUrls.add(storePhoto(file, subfolder));
            } catch (IOException e) {
                throw new IOException("Не удалось сохранить один или несколько файлов.", e);
            }
        }
        return storedFileUrls;
    }

    @Override
    public void deletePhoto(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            System.err.println("Не удается удалить файл с недопустимым URL-адресом: " + fileUrl);
            return;
        }
        String relativePath = fileUrl.substring(baseUrl.length());
        Path filePath = this.storageLocation.resolve(relativePath).normalize();

        if (!filePath.startsWith(this.storageLocation)) {
            throw new SecurityException("Невозможно удалить файл за пределами указанного места хранения.");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IOException("Не удалось удалить файл: " + fileUrl, ex);
        }
    }
}
