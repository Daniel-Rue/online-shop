package ru.kon.onlineshop.dto.category;

import lombok.Data;

import java.util.List;

@Data
public class CategoryTreeDto {
    private Long id;
    private String name;
    private List<CategoryTreeDto> children;
}