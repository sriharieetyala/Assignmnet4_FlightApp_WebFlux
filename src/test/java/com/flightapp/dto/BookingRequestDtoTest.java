package com.flightapp.dto;


import com.flightapp.dto.request.BookingRequest;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestDtoTest {

    @Test
    void constructor_and_getters_work() {
        BookingRequest req = new BookingRequest("flight-1", 2, "Sam", "sam@x.com", Gender.MALE, MealType.VEG);

        assertEquals("flight-1", req.getFlightId());
        assertEquals(2, req.getSeats());
        assertEquals("Sam", req.getName());
        assertEquals("sam@x.com", req.getEmail());
        assertEquals(Gender.MALE, req.getGender());
        assertEquals(MealType.VEG, req.getMealPreference());

        req.setSeats(3);
        assertEquals(3, req.getSeats());
    }
}

