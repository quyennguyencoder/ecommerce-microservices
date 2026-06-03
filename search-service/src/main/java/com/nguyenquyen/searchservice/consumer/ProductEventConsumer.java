package com.nguyenquyen.searchservice.consumer;

import com.nguyenquyen.event.ProductCreatedEvent;
import com.nguyenquyen.event.ProductDeletedEvent;
import com.nguyenquyen.searchservice.document.ProductDocument;
import com.nguyenquyen.searchservice.service.ProductDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "product-events", groupId = "search-service-consumer")
public class ProductEventConsumer {

    private final ProductDocumentService productDocumentService;

    @KafkaHandler
    public void consumeProductCreatedEvents(@Payload ProductCreatedEvent event) {
        productDocumentService.saveProductDocument(convertToDocument(event));

    }

    @KafkaHandler
    public void consumeProductDeletedEvents(@Payload ProductDeletedEvent event) {
        productDocumentService.deleteProductDocument(event.getId());
    }

    private ProductDocument convertToDocument(ProductCreatedEvent event) {
        return ProductDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .price(event.getPrice())
                .categoryId(event.getCategoryId())
                .categoryName(event.getCategoryName())
                .thumbnail(event.getThumbnail())
                .status(event.getStatus())
                .inStock(event.getInStock())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
