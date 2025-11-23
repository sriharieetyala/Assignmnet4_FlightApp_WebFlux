package com.flightapp.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddFlightRequestDtoTest {

    @Test
    void ctor_and_fields() {
        AddFlightRequest r = new AddFlightRequest("Air", "FN", "SFO", "LAX", "2025-12-01T10:00:00Z", "2025-12-01T12:00:00Z", 50.0f, 100, "A320");

        assertEquals("Air", r.getAirline());
        assertEquals("FN", r.getFlightNumber());
        assertEquals("SFO", r.getFromPlace());
        assertEquals("LAX", r.getToPlace());
        assertEquals(100, r.getTotalSeats());
        assertEquals(50.0f, r.getPrice());
        r.setAircraft("B737");
        assertEquals("B737", r.getAircraft());
    }
}
