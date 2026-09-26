package com.ride_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("RideLink Ride Management Service")
                .description("Ride requests, driver assignment (via Driver Service), lifecycle and fare integration")
                .version("1.0"));
    }
}
