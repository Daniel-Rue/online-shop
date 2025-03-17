package ru.kon.onlineshop.dto.category;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private Long parentId;
    private int productCount;
}