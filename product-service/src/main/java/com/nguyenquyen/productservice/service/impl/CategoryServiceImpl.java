package com.nguyenquyen.productservice.service.impl;

import com.nguyenquyen.productservice.dto.request.CreateCategoryRequest;
import com.nguyenquyen.productservice.dto.request.UpdateCategoryRequest;
import com.nguyenquyen.productservice.dto.response.CategoryDetailResponse;
import com.nguyenquyen.productservice.dto.response.CreateCategoryResponse;
import com.nguyenquyen.productservice.dto.response.UpdateCategoryResponse;
import com.nguyenquyen.productservice.entity.Category;
import com.nguyenquyen.productservice.exception.ErrorCode;
import com.nguyenquyen.productservice.exception.ProductServiceException;
import com.nguyenquyen.productservice.repository.CategoryRepository;
import com.nguyenquyen.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

//    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public CreateCategoryResponse createCategory(CreateCategoryRequest request) {
        if(categoryRepository.existsByNameIgnoreCase(request.name()))
            throw new ProductServiceException(ErrorCode.CATEGORY_EXISTED);

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        categoryRepository.save(category);

        return CreateCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    @Override
    public List<CategoryDetailResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryDetailResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .createdAt(category.getCreatedAt())
                        .build())
                .toList();
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public UpdateCategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));

        Optional.ofNullable(request.name()).ifPresent(category::setName);
        Optional.ofNullable(request.description()).ifPresent(category::setDescription);

        categoryRepository.save(category);
        return UpdateCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public void deleteCategory(String id) {
        categoryRepository.findById(id)
                .ifPresent(categoryRepository::delete);
        log.info("Category deleted successfully");
    }

}
