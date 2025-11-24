package com.flightapp.model;


import org.junit.jupiter.api.Test;
import com.flightapp.exception.GlobalErrorHandler;
import org.springframework.context.annotation.Import;


import static org.junit.jupiter.api.Assertions.*;

/**
 * I use this to quickly verify that the Flight POJO behaves like I expect.
 */
@Import(GlobalErrorHandler.class)
class FlightModelTest {

    @Test
    void gettersSetters_and_toString() {
        // I fill up a fresh flight and confirm everything sits correctly.
        Flight f = new Flight();
        f.setId("F11");
        f.setAirline("Vistara");
        f.setFlightNumber("UK810");
        f.setFromPlace("Chennai");
        f.setToPlace("Kolkata");
        f.setDepartureDateTime("2025-07-01T06:00:00Z");
        f.setArrivalDateTime("2025-07-01T08:30:00Z");
        f.setPrice(6500f);
        f.setTotalSeats(150);
        f.setAvailableSeats(150);
        f.setAircraft("A321");

        assertEquals("UK810", f.getFlightNumber());
        assertTrue(f.toString().contains("UK810"));
    }

    @Test
    void allArgsConstructor_matchesValues() {
        // I check once that the all-args constructor builds a flight correctly.
        Flight f = new Flight("IDX", "SpiceJet", "SG401",
                "Pune", "Goa", "d", "a", 3200f,
                180, 160, "Q400");

        assertEquals("IDX", f.getId());
        assertEquals(160, f.getAvailableSeats());
    }
}
