package com.ride_service.dto;

import com.ride_service.model.RideStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RideResponse {
    private String id;
    private String passengerId;
    private String driverId;
    private String driverUserId;
    private String pickup;
    private String destination;
    private double distanceKm;
    private int estimatedMinutes;
    private Double estimatedFare;
    private Double finalFare;
    private RideStatus status;
    private Instant requestedAt;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;
}
