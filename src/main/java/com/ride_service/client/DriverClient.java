package com.ride_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DriverClient {

    private final RestTemplate restTemplate;

    @Value("${services.driver-url}")
    private String driverBaseUrl;

    public List<Map<String, Object>> getAvailableDrivers(String serviceArea) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(driverBaseUrl + "/api/drivers/available");
        if (serviceArea != null && !serviceArea.isBlank()) {
            builder.queryParam("serviceArea", serviceArea);
        }
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }
}
