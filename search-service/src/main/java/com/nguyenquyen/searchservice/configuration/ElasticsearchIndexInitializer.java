package com.nguyenquyen.searchservice.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "ELASTICSEARCH-INDEX-INIT")
public class ElasticsearchIndexInitializer {

    public final static String PRODUCT_INDEX = "products";

    private final ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void createProductIndex() {
        try {
            boolean indexExists = elasticsearchClient.indices().exists(c -> c.index(PRODUCT_INDEX)).value();
            if(!indexExists) {
                elasticsearchClient.indices().create(c -> c
                        .index(PRODUCT_INDEX)
                        .mappings(m -> m
                                .properties("id", p -> p.keyword(k -> k))
                                .properties("name", p -> p.text(t -> t.analyzer("standard")))
                                .properties("description", p -> p.text(t -> t.analyzer("standard")))
                                .properties("price", p -> p.double_(d -> d))
                                .properties("categoryId", p -> p.keyword(k -> k))
                                .properties("categoryName", p -> p.keyword(k -> k))
                                .properties("thumbnail", p -> p.keyword(k -> k))
                                .properties("status", p -> p.keyword(k -> k))
                                .properties("inStock", p -> p.boolean_(b -> b))
                                .properties("createdAt", p -> p.date(d -> d))
                        )
                );
            } else {
                log.info("Index already exists: {}", PRODUCT_INDEX);
            }
        } catch (IOException e) {
            log.error("Failed to create index: {}", PRODUCT_INDEX, e);
        }
    }

}
