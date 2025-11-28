package ru.kon.onlineshop.dto.product.attribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kon.onlineshop.entity.AttributeType;

@Data
@NoArgsConstructor
public class AttributeDto {
    private Long id;
    private String name;
    private AttributeType type;
    private String unit;
}

