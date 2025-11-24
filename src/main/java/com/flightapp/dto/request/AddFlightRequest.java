package com.flightapp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFlightRequest {
    @NotBlank(message = "airline must not be blank")
    private String airline;

    @NotBlank(message = "flightNumber must not be blank")
    private String flightNumber;

    @NotBlank(message = "fromPlace must not be blank")
    private String fromPlace;

    @NotBlank(message = "toPlace must not be blank")
    private String toPlace;

    @NotBlank(message = "departureDateTime must not be blank")
    private String departureDateTime;

    @NotBlank(message = "arrivalDateTime must not be blank")
    private String arrivalDateTime;

    @Positive(message = "price must be > 0")
    private Float price;

    @Positive(message = "totalSeats must be > 0")
    private Integer totalSeats;

    @NotBlank(message = "aircraft must not be blank")
    private String aircraft;
}
