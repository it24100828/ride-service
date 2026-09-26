package com.ride_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {
    @Id
    private String id;
    private String passengerId;
    private String driverId;          // Driver Service ID
    private String driverUserId;      // Account Service userId of driver

    private String pickup;
    private String destination;
    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;

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
