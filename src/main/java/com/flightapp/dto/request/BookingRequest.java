package com.flightapp.dto.request;

import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
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
    private Gender gender;
    private MealType mealPreference;

}
