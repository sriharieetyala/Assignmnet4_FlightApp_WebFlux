package com.flightapp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AddFlightRequest {
    @NotBlank(message = "airline required")
    private String airline;

    @NotBlank(message = "flightNumber required")
    private String flightNumber;

    @NotBlank(message = "fromPlace required")
    private String fromPlace;

    @NotBlank(message = "toPlace required")
    private String toPlace;

    @NotNull(message = "departureDateTime required")
    private String departureDateTime;

    @NotNull(message = "arrivalDateTime required")
    private String arrivalDateTime;

    @Positive(message = "price must be positive")
    private float price;

    @Positive(message = "totalSeats must be > 0")
    private int totalSeats;
    private String aircraft;
}
