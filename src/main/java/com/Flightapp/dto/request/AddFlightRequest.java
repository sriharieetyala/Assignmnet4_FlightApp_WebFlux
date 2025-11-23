package com.Flightapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AddFlightRequest {
    private String airline;
    private String flightNumber;
    private String fromPlace;
    private String toPlace;
    private String departureDateTime;
    private String arrivalDateTime;
    private float price;
    private int totalSeats;
    private String aircraft;
}
