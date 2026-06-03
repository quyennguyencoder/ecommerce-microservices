package com.nguyenquyen.userservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfiguration {

    private final HttpClientInterceptor httpClientInterceptor;

    @Bean
    @Primary // Eureka dùng cái này
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder builder() {
        return RestClient.builder();
    }

    @Bean
    public MediaClient mediaClient() {
        RestClient restClient = builder()
                .baseUrl("http://MEDIA-SERVICE")
                .requestInterceptor(httpClientInterceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(MediaClient.class);
    }
}
