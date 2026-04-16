package com.orderflow.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${services.order-service.url:http://localhost:8081}")
    private String orderServiceUrl;

    @Value("${services.payment-service.url:http://localhost:8082}")
    private String paymentServiceUrl;

    @Value("${services.inventory-service.url:http://localhost:8083}")
    private String inventoryServiceUrl;

    @Value("${services.notification-service.url:http://localhost:8084}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri(orderServiceUrl))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri(orderServiceUrl))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri(orderServiceUrl))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri(paymentServiceUrl))
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .uri(inventoryServiceUrl))
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .uri(inventoryServiceUrl))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri(notificationServiceUrl))
                .build();
    }
}
