package com.flightapp.dto.request;

import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "flightId is required")
    private String flightId;

    @Min(value = 1, message = "seats must be at least 1")
    private int seats;

    @NotBlank(message = "name is required")
    private String name;

    @Email(message = "invalid email")
    private String email;

    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "mealPreference is required")
    private MealType mealPreference;
}
