package com.flightapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 I used Lombok so getters/setters/ctors are clean and short.
 Basic validation annotations so controller can reject bad input quickly.
 City names should be real (I used Chennai/Bengaluru examples in tests).
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFlightRequest {

    @NotBlank
    private String airline;

    @NotBlank
    private String flightNumber;

    @NotBlank
    private String fromPlace;

    @NotBlank
    private String toPlace;

    @NotBlank
    private String departureDateTime; // ISO-8601 string expected, e.g. 2025-12-01T10:00:00Z

    @NotBlank
    private String arrivalDateTime;   // ISO-8601 string expected

    @NotNull
    @Positive
    private Float price;

    @Min(1)
    private Integer totalSeats;

    @NotBlank
    private String aircraft;
}
