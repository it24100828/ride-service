package com.ride_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignDriverRequest {
    @NotBlank
    private String driverId;
}
