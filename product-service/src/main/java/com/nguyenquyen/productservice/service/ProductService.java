package com.nguyenquyen.productservice.service;

import com.nguyenquyen.productservice.dto.request.CreateProductRequest;
import com.nguyenquyen.productservice.dto.request.SearchRequest;
import com.nguyenquyen.productservice.dto.response.CreateProductResponse;
import com.nguyenquyen.productservice.dto.response.PageResponse;
import com.nguyenquyen.productservice.dto.response.ProductDetailResponse;

public interface ProductService {
    CreateProductResponse createProduct(String sellerId, CreateProductRequest request);
    PageResponse<ProductDetailResponse> getAllProducts(int page, int size, SearchRequest request);
    ProductDetailResponse getProductById(String id);
    void deleteProduct(String id);
}
