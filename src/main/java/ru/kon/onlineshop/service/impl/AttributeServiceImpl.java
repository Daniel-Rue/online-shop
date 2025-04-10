package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kon.onlineshop.dto.product.attribute.AttributeDto;
import ru.kon.onlineshop.dto.product.attribute.CreateAttributeRequest;
import ru.kon.onlineshop.dto.product.attribute.UpdateAttributeRequest;
import ru.kon.onlineshop.entity.Attribute;
import ru.kon.onlineshop.exceptions.product.attribute.AttributeAlreadyExistsException;
import ru.kon.onlineshop.exceptions.product.attribute.ResourceNotFoundException;
import ru.kon.onlineshop.repository.AttributeRepository;
import ru.kon.onlineshop.repository.ProductAttributeValueRepository;
import ru.kon.onlineshop.service.AttributeService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;

    @Override
    @Transactional
    public AttributeDto createAttribute(CreateAttributeRequest request) {
        attributeRepository.findByNameIgnoreCase(request.getName()).ifPresent(a -> {
            throw new AttributeAlreadyExistsException("Атрибут с именем '" + request.getName() + "' уже существует.");
        });

        Attribute attribute = mapToEntity(request);
        Attribute savedAttribute = attributeRepository.save(attribute);
        return mapToDto(savedAttribute);
    }

    @Override
    public AttributeDto getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Атрибут с ID " + id + " не найден."));
        return mapToDto(attribute);
    }

    @Override
    public List<AttributeDto> getAllAttributes() {
        return attributeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttributeDto updateAttribute(Long id, UpdateAttributeRequest request) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Атрибут с ID " + id + " не найден."));

        if (!attribute.getName().equalsIgnoreCase(request.getName())) {
            attributeRepository.findByNameIgnoreCase(request.getName()).ifPresent(existingAttribute -> {
                if (!existingAttribute.getId().equals(id)) {
                    throw new AttributeAlreadyExistsException("Атрибут с именем '" + request.getName() + "' уже существует.");
                }
            });
        }

        attribute.setName(request.getName());
        attribute.setType(request.getType());
        attribute.setUnit(request.getUnit());

        Attribute updatedAttribute = attributeRepository.save(attribute);
        return mapToDto(updatedAttribute);
    }

    @Override
    @Transactional
    public void deleteAttribute(Long id) {
        if (!attributeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Атрибут с ID " + id + " не найден.");
        }

        long usageCount = productAttributeValueRepository.countByAttributeId(id);
        if (usageCount > 0) {
            throw new IllegalStateException("Невозможно удалить атрибут с ID " + id + ", так как он используется в " + usageCount + " товарах.");
        }

        attributeRepository.deleteById(id);
    }


    private AttributeDto mapToDto(Attribute attribute) {
        AttributeDto dto = new AttributeDto();
        dto.setId(attribute.getId());
        dto.setName(attribute.getName());
        dto.setType(attribute.getType());
        dto.setUnit(attribute.getUnit());
        return dto;
    }

    private Attribute mapToEntity(CreateAttributeRequest request) {
        return Attribute.builder()
                .name(request.getName())
                .type(request.getType())
                .unit(request.getUnit())
                .build();
    }
}