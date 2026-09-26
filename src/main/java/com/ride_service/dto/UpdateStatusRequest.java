package com.ride_service.dto;

import com.ride_service.model.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private RideStatus status;
    private String reason;  // for cancellation
}
