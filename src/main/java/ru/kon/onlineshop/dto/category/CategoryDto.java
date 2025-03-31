package ru.kon.onlineshop.dto.category;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private Long parentId;
    private int productCount;
}