package ru.kon.onlineshop.dto.product.attribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kon.onlineshop.entity.AttributeType;

@Data
@NoArgsConstructor
public class ProductAttributeValueDto {
    private Long attributeId; 
    private String attributeName;
    private AttributeType attributeType;
    private String value;
    private String unit;
}
