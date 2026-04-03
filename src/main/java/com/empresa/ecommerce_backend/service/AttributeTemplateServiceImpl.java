package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.AttributeTemplateRequest;
import com.empresa.ecommerce_backend.dto.response.AttributeTemplateResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.model.AttributeTemplate;
import com.empresa.ecommerce_backend.model.AttributeValue;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.repository.AttributeTemplateRepository;
import com.empresa.ecommerce_backend.repository.CategoryRepository;
import com.empresa.ecommerce_backend.service.interfaces.AttributeTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeTemplateServiceImpl implements AttributeTemplateService {

    private final AttributeTemplateRepository repository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ServiceResult<AttributeTemplateResponse> createTemplate(AttributeTemplateRequest request) {
        AttributeTemplate template = AttributeTemplate.builder()
                .name(request.getName())
                .scope(request.getScope())
                .productType(request.getProductType())
                .build();

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
            template.setCategory(cat);
        }

        if (request.getValues() != null) {
            request.getValues().forEach(v -> {
                template.addValue(AttributeValue.builder()
                        .label(v.getLabel())
                        .value(v.getValue())
                        .build());
            });
        }

        AttributeTemplate saved = repository.save(template);
        return ServiceResult.created(mapToResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<AttributeTemplateResponse> updateTemplate(Long id, AttributeTemplateRequest request) {
        AttributeTemplate template = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plantilla no encontrada"));

        template.setName(request.getName());
        template.setScope(request.getScope());
        template.setProductType(request.getProductType());

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
            template.setCategory(cat);
        } else {
            template.setCategory(null);
        }

        // Simplificación: reemplazamos valores (en un sistema real se haría merge por ID)
        template.getValues().clear();
        if (request.getValues() != null) {
            request.getValues().forEach(v -> {
                template.addValue(AttributeValue.builder()
                        .label(v.getLabel())
                        .value(v.getValue())
                        .build());
            });
        }

        AttributeTemplate saved = repository.save(template);
        return ServiceResult.ok(mapToResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteTemplate(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Plantilla no encontrada");
        }
        repository.deleteById(id);
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<List<AttributeTemplateResponse>> getAllTemplates() {
        List<AttributeTemplateResponse> list = repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    public ServiceResult<List<AttributeTemplateResponse>> getApplicableTemplates(AttributeScope scope, ProductType type, Long categoryId) {
        List<AttributeTemplateResponse> list = repository.findApplicable(scope, type, categoryId).stream()
                .map(this::mapToResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    private AttributeTemplateResponse mapToResponse(AttributeTemplate entity) {
        AttributeTemplateResponse dto = new AttributeTemplateResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setScope(entity.getScope());
        dto.setProductType(entity.getProductType());
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        
        if (entity.getValues() != null) {
            dto.setValues(entity.getValues().stream().map(v -> {
                AttributeTemplateResponse.AttributeValueResponse vd = new AttributeTemplateResponse.AttributeValueResponse();
                vd.setId(v.getId());
                vd.setLabel(v.getLabel());
                vd.setValue(v.getValue());
                return vd;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
