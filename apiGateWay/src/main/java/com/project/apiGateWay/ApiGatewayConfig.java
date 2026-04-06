package com.project.apiGateWay;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/get")
                        .filters(f -> f.addRequestHeader("MyHeader", "MyURI")
                                .addRequestParameter("Param", "MyValue"))
                        .uri("http://httpbin.org:80")
                )
                .route(r -> r.path("/auth/**", "/auth-resources/**")
                        .uri("http://localhost:8081"))
                .route(r -> r.path("/profile/**")
                        .uri("http://localhost:8082"))
                .route(r -> r.path("/categories/**")
                        .uri("http://localhost:8083"))
                .route(r -> r.path("/expenses/**")
                        .uri("http://localhost:8084"))
                .route(r -> r.path("/incomes/**")
                .uri("http://localhost:8085"))
                .route(r -> r.path("/dashboard/**", "/filter/**")
//                        .uri("http://localhost:8086"))
                        .uri("lb://dashboard-service"))
                .route(r -> r.path("/notifications/**")
                        .uri("http://localhost:8087"))
                .build();
    }
}
