package com.nguyenquyen.searchservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AggregationResponse(
    List<CategoryCount> categories,
    PriceStats priceStats,
    List<PriceRangeBucket> priceRanges
) {
}
