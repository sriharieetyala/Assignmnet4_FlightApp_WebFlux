package com.flightapp.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Simple request DTO for booking.
 * Contains passenger info and ref to flight.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String flightId;
    private int seats;

    // passenger details (basic)
    private String name;
    private String email;
    private String gender;
    private String mealPreference;
}
