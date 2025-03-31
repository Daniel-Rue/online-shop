package ru.kon.onlineshop.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeDto {
    private Long id;
    private String name;
    private List<CategoryTreeDto> children;
}