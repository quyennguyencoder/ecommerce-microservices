package com.nguyenquyen.searchservice.controller;

import com.nguyenquyen.searchservice.document.ProductDocument;
import com.nguyenquyen.searchservice.dto.request.SearchRequest;
import com.nguyenquyen.searchservice.dto.response.AggregationResponse;
import com.nguyenquyen.searchservice.dto.response.ApiResponse;
import com.nguyenquyen.searchservice.dto.response.PageResponse;
import com.nguyenquyen.searchservice.service.ProductDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class ProductDocumentController {

    private final ProductDocumentService productDocumentService;

    @GetMapping("/products")
    ApiResponse<PageResponse<ProductDocument>> searchProducts(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            SearchRequest request
    ) {
        var data = productDocumentService.getAllWithSearch(page, size, request);
        return ApiResponse.<PageResponse<ProductDocument>>builder()
                .code(HttpStatus.OK.value())
                .message("Products retrieved successfully")
                .data(data)
                .build();
    }

    @GetMapping("/products/aggregations")
    ApiResponse<AggregationResponse> getAggregations(SearchRequest request) {
        var data = productDocumentService.getAggregations(request);
        return ApiResponse.<AggregationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Aggregations retrieved successfully")
                .data(data)
                .build();
    }

}
