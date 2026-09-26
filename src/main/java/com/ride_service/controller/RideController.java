package com.ride_service.controller;

import com.ride_service.dto.*;
import com.ride_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Rides", description = "Ride request, assignment and lifecycle management")
public class RideController {

    private final RideService rideService;

    @PostMapping
    @Operation(summary = "Create a new ride request (calls Fare Service for estimate)")
    public ResponseEntity<RideResponse> create(@Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Auto-assign first available driver (calls Driver Service)")
    public ResponseEntity<RideResponse> autoAssign(@PathVariable String id,
                                                   @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(rideService.assignDriver(id, serviceArea));
    }

    @PostMapping("/{id}/assign-driver")
    @Operation(summary = "Manually assign a specific driver")
    public ResponseEntity<RideResponse> assignSpecific(@PathVariable String id,
                                                       @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.ok(rideService.assignSpecificDriver(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ride status (enforces valid transitions)")
    public ResponseEntity<RideResponse> updateStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(rideService.updateStatus(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID")
    public ResponseEntity<RideResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getById(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Get rides for a passenger")
    public ResponseEntity<List<RideResponse>> getByPassenger(@PathVariable String passengerId) {
        return ResponseEntity.ok(rideService.getByPassenger(passengerId));
    }
}
