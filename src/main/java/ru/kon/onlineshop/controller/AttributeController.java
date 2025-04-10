package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kon.onlineshop.dto.product.attribute.AttributeDto;
import ru.kon.onlineshop.dto.product.attribute.CreateAttributeRequest;
import ru.kon.onlineshop.dto.product.attribute.UpdateAttributeRequest;
import ru.kon.onlineshop.dto.product.attribute.UpdateProductAttributesRequest;
import ru.kon.onlineshop.service.AttributeService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @PostMapping
    public ResponseEntity<AttributeDto> createAttribute(
            @Valid @RequestBody CreateAttributeRequest request) {
        AttributeDto createdAttribute = attributeService.createAttribute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAttribute);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeDto> getAttributeById(@PathVariable Long id) {
        AttributeDto attributeDto = attributeService.getAttributeById(id);
        return ResponseEntity.ok(attributeDto);
    }

    @GetMapping
    public ResponseEntity<List<AttributeDto>> getAllAttributes() {
        List<AttributeDto> attributes = attributeService.getAllAttributes();
        return ResponseEntity.ok(attributes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeDto> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request) {
        AttributeDto updatedAttribute = attributeService.updateAttribute(id, request);
        return ResponseEntity.ok(updatedAttribute);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }
}