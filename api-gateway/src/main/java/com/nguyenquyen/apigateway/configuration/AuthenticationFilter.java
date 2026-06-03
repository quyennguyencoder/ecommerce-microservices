package com.nguyenquyen.apigateway.configuration;

import com.nguyenquyen.apigateway.client.AuthenticationClient;
import com.nguyenquyen.apigateway.dto.request.IntrospectRequest;
import com.nguyenquyen.apigateway.dto.response.ErrorResponse;
import com.nguyenquyen.apigateway.dto.PublicEndpoint;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
            new PublicEndpoint("/api/v1/users", HttpMethod.POST),
            new PublicEndpoint("/api/v1/auth/login", HttpMethod.POST),
            new PublicEndpoint("/api/v1/auth/refresh-token", HttpMethod.POST),
            new PublicEndpoint("/api/v1/auth/introspect", HttpMethod.POST),
            new PublicEndpoint("/api/v1/search/**", HttpMethod.GET)
    );

    private final JsonMapper jsonMapper;
    private final AuthenticationClient authenticationClient;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if(isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        List<String> authorization = exchange.getRequest().getHeaders().get(AUTHORIZATION_HEADER);
        if(authorization == null || authorization.isEmpty()) {
            return unauthenticated(exchange, "Missing Authorization header");
        }

        if(!authorization.getFirst().startsWith(BEARER)) {
            return unauthenticated(exchange, "Invalid Authorization header");
        }

        String token = authorization.getFirst().substring(7);

        return authenticationClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build()).flatMap(introspect -> {
                    if(introspect.data().active()) {
                        return chain.filter(exchange);
                    } else {
                        return unauthenticated(exchange, "Invalid token");
                    }
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint ->
                        pathMatcher.match(endpoint.getPath(), path) &&
                                (endpoint.getMethod() == null || endpoint.getMethod().equals(method))
                );
    }

    private Mono<Void> unauthenticated(ServerWebExchange exchange, String message) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(exchange.getRequest().getURI().getPath())
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            byte[] bytes = jsonMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
