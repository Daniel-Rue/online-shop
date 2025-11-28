package ru.kon.onlineshop.dto.product.attribute;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class UpdateProductAttributesRequest {
    @NotNull
    private List<AttributeValueInputDto> attributeValues;
}
