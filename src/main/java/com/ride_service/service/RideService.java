package com.ride_service.service;

import com.ride_service.client.DriverClient;
import com.ride_service.client.FareClient;
import com.ride_service.dto.*;
import com.ride_service.exception.ApiException;
import com.ride_service.model.Ride;
import com.ride_service.model.RideStatus;
import com.ride_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverClient;
    private final FareClient fareClient;

    // Valid status transitions
    private static final Map<RideStatus, Set<RideStatus>> TRANSITIONS = Map.of(
            RideStatus.REQUESTED, Set.of(RideStatus.ASSIGNED, RideStatus.CANCELLED),
            RideStatus.ASSIGNED, Set.of(RideStatus.ACCEPTED, RideStatus.CANCELLED),
            RideStatus.ACCEPTED, Set.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED),
            RideStatus.IN_PROGRESS, Set.of(RideStatus.COMPLETED, RideStatus.CANCELLED),
            RideStatus.COMPLETED, Set.of(),
            RideStatus.CANCELLED, Set.of()
    );

    public RideResponse createRide(CreateRideRequest request) {
        // Inter-service call 1: get fare estimate
        Double estimatedFare = null;
        try {
            Map<String, Object> fareResp = fareClient.estimateFare(
                    request.getPickup(), request.getDestination(),
                    request.getDistanceKm(), request.getEstimatedMinutes());
            if (fareResp != null && fareResp.get("estimatedFare") != null) {
                estimatedFare = ((Number) fareResp.get("estimatedFare")).doubleValue();
            }
        } catch (Exception e) {
            // continue without estimate if fare service down
        }

        Ride ride = Ride.builder()
                .passengerId(request.getPassengerId())
                .pickup(request.getPickup())
                .destination(request.getDestination())
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .destLat(request.getDestLat())
                .destLng(request.getDestLng())
                .distanceKm(request.getDistanceKm())
                .estimatedMinutes(request.getEstimatedMinutes())
                .estimatedFare(estimatedFare)
                .status(RideStatus.REQUESTED)
                .requestedAt(Instant.now())
                .build();
        ride = rideRepository.save(ride);
        return toResponse(ride);
    }

    /**
     * Simple assignment strategy: pick the first available driver in the service area.
     * Documented approach: first-available.
     */
    public RideResponse assignDriver(String rideId, String serviceArea) {
        Ride ride = findOrThrow(rideId);
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new ApiException("Ride must be in REQUESTED status to assign driver", HttpStatus.BAD_REQUEST);
        }

        // Inter-service call 2: get available drivers
        List<Map<String, Object>> available = driverClient.getAvailableDrivers(serviceArea);
        if (available == null || available.isEmpty()) {
            throw new ApiException("No available drivers found", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> chosen = available.get(0); // simple first-available strategy
        ride.setDriverId((String) chosen.get("id"));
        ride.setDriverUserId((String) chosen.get("userId"));
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse assignSpecificDriver(String rideId, AssignDriverRequest request) {
        Ride ride = findOrThrow(rideId);
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new ApiException("Ride must be in REQUESTED status", HttpStatus.BAD_REQUEST);
        }
        ride.setDriverId(request.getDriverId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse updateStatus(String rideId, UpdateStatusRequest request) {
        Ride ride = findOrThrow(rideId);
        RideStatus current = ride.getStatus();
        RideStatus next = request.getStatus();

        Set<RideStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new ApiException(
                    "Invalid status transition from " + current + " to " + next,
                    HttpStatus.BAD_REQUEST);
        }

        ride.setStatus(next);
        Instant now = Instant.now();
        switch (next) {
            case ACCEPTED -> ride.setAcceptedAt(now);
            case IN_PROGRESS -> ride.setStartedAt(now);
            case COMPLETED -> {
                ride.setCompletedAt(now);
                // calculate final fare (simple: use estimated or recompute)
                ride.setFinalFare(ride.getEstimatedFare() != null
                        ? ride.getEstimatedFare()
                        : 200.0);
                // optional: trigger payment
                try {
                    fareClient.recordPayment(ride.getId(), ride.getPassengerId(), ride.getFinalFare());
                } catch (Exception ignored) {}
            }
            case CANCELLED -> {
                ride.setCancelledAt(now);
                ride.setCancellationReason(request.getReason());
            }
            default -> {}
        }
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse getById(String id) {
        return toResponse(findOrThrow(id));
    }

    public List<RideResponse> getByPassenger(String passengerId) {
        return rideRepository.findByPassengerId(passengerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Ride findOrThrow(String id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ApiException("Ride not found", HttpStatus.NOT_FOUND));
    }

    private RideResponse toResponse(Ride r) {
        return RideResponse.builder()
                .id(r.getId())
                .passengerId(r.getPassengerId())
                .driverId(r.getDriverId())
                .driverUserId(r.getDriverUserId())
                .pickup(r.getPickup())
                .destination(r.getDestination())
                .distanceKm(r.getDistanceKm())
                .estimatedMinutes(r.getEstimatedMinutes())
                .estimatedFare(r.getEstimatedFare())
                .finalFare(r.getFinalFare())
                .status(r.getStatus())
                .requestedAt(r.getRequestedAt())
                .assignedAt(r.getAssignedAt())
                .acceptedAt(r.getAcceptedAt())
                .startedAt(r.getStartedAt())
                .completedAt(r.getCompletedAt())
                .cancelledAt(r.getCancelledAt())
                .cancellationReason(r.getCancellationReason())
                .build();
    }
}
