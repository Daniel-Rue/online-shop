package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kon.onlineshop.entity.ReviewPhoto;

@Repository
public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
}