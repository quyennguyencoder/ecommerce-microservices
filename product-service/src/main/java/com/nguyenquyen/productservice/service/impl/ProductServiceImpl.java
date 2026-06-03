package com.nguyenquyen.productservice.service.impl;

import com.nguyenquyen.event.ProductCreatedEvent;
import com.nguyenquyen.event.ProductDeletedEvent;
import com.nguyenquyen.productservice.dto.request.CreateProductRequest;
import com.nguyenquyen.productservice.dto.request.SearchRequest;
import com.nguyenquyen.productservice.dto.response.CreateProductResponse;
import com.nguyenquyen.productservice.dto.response.PageResponse;
import com.nguyenquyen.productservice.dto.response.ProductDetailResponse;
import com.nguyenquyen.productservice.entity.Category;
import com.nguyenquyen.productservice.entity.Product;
import com.nguyenquyen.productservice.entity.ProductImage;
import com.nguyenquyen.productservice.exception.ErrorCode;
import com.nguyenquyen.productservice.exception.ProductServiceException;
import com.nguyenquyen.productservice.repository.CategoryRepository;
import com.nguyenquyen.productservice.repository.ProductRepository;
import com.nguyenquyen.productservice.repository.specification.ProductSpecification;
import com.nguyenquyen.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-SERVICE")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PreAuthorize("hasAnyAuthority('SELLER','ADMIN')")
    @Override
    public CreateProductResponse createProduct(String sellerId, CreateProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .sellerId(sellerId)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .images(request.images())
                .status(request.status())
                .category(category)
                .build();

        productRepository.save(product);
        log.info("Product created successfully");

        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .thumbnail(product.getImages().stream().filter(ProductImage::getIsPrimary).map(ProductImage::getUrl).findFirst().orElse(""))
                .status(product.getStatus().name())
                .inStock(product.getQuantity() > 0)
                .createdAt(product.getCreatedAt())
                .build();

        kafkaTemplate.send("product-events", product.getId(), event);

        return CreateProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .images(product.getImages())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }

    @Override
    public PageResponse<ProductDetailResponse> getAllProducts(int page, int size, SearchRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "name"));

        Specification<Product> specification = Specification.allOf(
                ProductSpecification.hasName(request.name()),
                ProductSpecification.hasPrice(request.minPrice(), request.maxPrice()),
                ProductSpecification.hasStatus(request.status()),
                ProductSpecification.inStock(request.inStock()),
                ProductSpecification.hasCategory(request.categoryId())
        );

        Page<Product> productPage = productRepository.findAll(specification, pageable);
        List<Product> products = productPage.getContent();

        List<ProductDetailResponse> responses = products.stream()
                .map(product -> ProductDetailResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .images(product.getImages())
                        .status(product.getStatus())
                        .createdAt(product.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.<ProductDetailResponse>builder()
                .currentPage(page)
                .pageSize(pageable.getPageSize())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .content(responses)
                .build();
    }

    @Override
    public ProductDetailResponse getProductById(String id) {
        return productRepository.findById(id)
                .map(product -> ProductDetailResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .images(product.getImages())
                        .status(product.getStatus())
                        .createdAt(product.getCreatedAt())
                        .build())
                .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @PreAuthorize("hasAnyAuthority('SELLER','ADMIN')")
    @Override
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null)
            throw new ProductServiceException(ErrorCode.UNAUTHORIZED);

        String userId = authentication.getName();

        if (!product.getSellerId().equals(userId)) {

            Set<String> authorities = authentication.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            if(!authorities.contains("ADMIN")) {
                throw new ProductServiceException(ErrorCode.PRODUCT_ACCESS_DENIED);
            }
        }
        productRepository.delete(product);

        ProductDeletedEvent event = ProductDeletedEvent.builder()
                .id(product.getId())
                .build();

        kafkaTemplate.send("product-events", product.getId(), event);

        log.info("Product deleted successfully");
    }
}
