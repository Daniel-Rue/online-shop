package ru.kon.onlineshop.dto.category;

import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String name;
    private Long parentId;
}
