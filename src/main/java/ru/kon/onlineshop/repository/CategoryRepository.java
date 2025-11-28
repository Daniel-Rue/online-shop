package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kon.onlineshop.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findByParentIsNull();

    @Query(value = "WITH RECURSIVE category_tree AS (" +
                   "    SELECT id FROM categories WHERE id = :categoryId " +
                   "    UNION ALL " +
                   "    SELECT c.id FROM categories c " +
                   "    JOIN category_tree ct ON c.parent_id = ct.id " +
                   ") " +
                   "SELECT id FROM category_tree",
            nativeQuery = true)
    List<Long> findCategoryAndAllChildrenIds(@Param("categoryId") Long categoryId);
}
