package com.nguyenquyen.searchservice.document;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument implements Serializable {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private String categoryName;
    private String thumbnail;
    private String status;
    private Boolean inStock;
    private Instant createdAt;
}
