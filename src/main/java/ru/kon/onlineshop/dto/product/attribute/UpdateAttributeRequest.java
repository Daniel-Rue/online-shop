package ru.kon.onlineshop.dto.product.attribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kon.onlineshop.entity.AttributeType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class UpdateAttributeRequest {
    @NotBlank(message = "Имя атрибута не может быть пустым")
    @Size(max = 100, message = "Имя атрибута не может превышать 100 символов")
    private String name;

    @NotNull(message = "Тип атрибута должен быть указан")
    private AttributeType type;

    @Size(max = 50, message = "Единица измерения не может превышать 50 символов")
    private String unit;
}
