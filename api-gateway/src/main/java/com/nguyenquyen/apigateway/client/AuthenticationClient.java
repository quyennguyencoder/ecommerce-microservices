package com.nguyenquyen.apigateway.client;

import com.nguyenquyen.apigateway.dto.request.IntrospectRequest;
import com.nguyenquyen.apigateway.dto.response.ApiResponse;
import com.nguyenquyen.apigateway.dto.response.IntrospectResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface AuthenticationClient {

    @PostExchange("/api/v1/auth/introspect")
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
