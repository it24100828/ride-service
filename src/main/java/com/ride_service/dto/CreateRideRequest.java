package com.ride_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRideRequest {
    @NotBlank
    private String passengerId;
    @NotBlank
    private String pickup;
    @NotBlank
    private String destination;
    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;
    @Positive
    private double distanceKm = 5.0;
    @Positive
    private int estimatedMinutes = 15;
    private String serviceArea;  // for finding drivers
}
