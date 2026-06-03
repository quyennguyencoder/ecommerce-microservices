package com.nguyenquyen.searchservice.service;

import com.nguyenquyen.searchservice.document.ProductDocument;
import com.nguyenquyen.searchservice.dto.request.SearchRequest;
import com.nguyenquyen.searchservice.dto.response.AggregationResponse;
import com.nguyenquyen.searchservice.dto.response.PageResponse;

public interface ProductDocumentService {
    void saveProductDocument(ProductDocument document);
    void deleteProductDocument(String id);
    PageResponse<ProductDocument> getAllWithSearch(int page, int size, SearchRequest request);
    AggregationResponse getAggregations(SearchRequest request);
}
