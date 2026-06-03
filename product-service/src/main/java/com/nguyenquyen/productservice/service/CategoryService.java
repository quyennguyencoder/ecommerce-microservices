package com.nguyenquyen.productservice.service;

import com.nguyenquyen.productservice.dto.request.CreateCategoryRequest;
import com.nguyenquyen.productservice.dto.request.UpdateCategoryRequest;
import com.nguyenquyen.productservice.dto.response.CategoryDetailResponse;
import com.nguyenquyen.productservice.dto.response.CreateCategoryResponse;
import com.nguyenquyen.productservice.dto.response.UpdateCategoryResponse;
import java.util.List;

public interface CategoryService {
    CreateCategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryDetailResponse> getAllCategories();
    UpdateCategoryResponse updateCategory(String id, UpdateCategoryRequest request);
    void deleteCategory(String id);
}
