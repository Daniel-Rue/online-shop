package ru.kon.onlineshop.exceptions.product.attribute;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AttributeAlreadyExistsException extends RuntimeException {

    public AttributeAlreadyExistsException(String message) {
        super(message);
    }
}