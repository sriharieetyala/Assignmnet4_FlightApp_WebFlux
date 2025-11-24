package com.flightapp.dto.request;

import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotBlank(message = "flightId must not be blank")
    private String flightId;

    @Min(value = 1, message = "seats must be >= 1")
    private int seats;

    @NotBlank(message = "name must not be blank")
    private String name;

    @Email(message = "email must be valid")
    private String email;

    @NotNull(message = "gender must not be null")
    private Gender gender;

    @NotNull(message = "mealPreference must not be null")
    private MealType mealPreference;
}