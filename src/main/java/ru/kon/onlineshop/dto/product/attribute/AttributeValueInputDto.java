package ru.kon.onlineshop.dto.product.attribute;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class AttributeValueInputDto {
    @NotNull(message = "ID атрибута должен быть указан")
    private Long attributeId;

    @NotNull(message = "Значение атрибута должно быть указано")
    private String value;
}
