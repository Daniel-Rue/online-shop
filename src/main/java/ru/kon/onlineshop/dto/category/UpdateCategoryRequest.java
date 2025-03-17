package ru.kon.onlineshop.dto.category;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String name;
    private Long parentId;
}