package com.flightapp.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightModelTest {

    @Test
    void gettersSetters_and_toString() {
        Flight f = new Flight();
        f.setId("f1");
        f.setAirline("TestAir");
        f.setFlightNumber("TA100");
        f.setFromPlace("SFO");
        f.setToPlace("LAX");
        f.setDepartureDateTime("2025-12-01T10:00:00Z");
        f.setArrivalDateTime("2025-12-01T12:00:00Z");
        f.setPrice(99.99f);
        f.setTotalSeats(120);
        f.setAvailableSeats(120);
        f.setAircraft("A320");

        assertEquals("f1", f.getId());
        assertEquals("TestAir", f.getAirline());
        assertEquals("TA100", f.getFlightNumber());
        assertEquals("SFO", f.getFromPlace());
        assertEquals("LAX", f.getToPlace());
        assertEquals("2025-12-01T10:00:00Z", f.getDepartureDateTime());
        assertEquals("2025-12-01T12:00:00Z", f.getArrivalDateTime());
        assertEquals(99.99f, f.getPrice());
        assertEquals(120, f.getTotalSeats());
        assertEquals(120, f.getAvailableSeats());
        assertTrue(f.toString().contains("TA100"));
    }

    @Test
    void allArgsConstructor_matchesValues() {
        Flight f = new Flight("id2","Air","FN1","A","B","d","a",10.0f,10,8,"aircraft");
        assertEquals("id2", f.getId());
        assertEquals(8, f.getAvailableSeats());
    }
}
