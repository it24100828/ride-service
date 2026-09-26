package com.ride_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FareClient {

    private final RestTemplate restTemplate;

    @Value("${services.fare-url}")
    private String fareBaseUrl;

    public Map<String, Object> estimateFare(String pickup, String destination,
                                            double distanceKm, int minutes) {
        Map<String, Object> body = new HashMap<>();
        body.put("pickup", pickup);
        body.put("destination", destination);
        body.put("distanceKm", distanceKm);
        body.put("estimatedMinutes", minutes);

        ResponseEntity<Map> response = restTemplate.exchange(
                fareBaseUrl + "/api/fares/estimate",
                HttpMethod.POST,
                new HttpEntity<>(body),
                Map.class
        );
        return response.getBody();
    }

    public Map<String, Object> recordPayment(String rideId, String passengerId, double amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("rideId", rideId);
        body.put("passengerId", passengerId);
        body.put("amount", amount);
        body.put("simulateFailure", false);

        ResponseEntity<Map> response = restTemplate.exchange(
                fareBaseUrl + "/api/fares/payments",
                HttpMethod.POST,
                new HttpEntity<>(body),
                Map.class
        );
        return response.getBody();
    }
}
