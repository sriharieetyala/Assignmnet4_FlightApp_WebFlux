package com.flightapp.dto.repsonse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I kept all the response related tests which i could think of
 * These tests are basic on purpose just enough to touch all getters, setters,
 * equals/hashCode, and a small JSON round trip.
 */
class DtoResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void bookingResponse_basicChecks() throws Exception {
        // I start with a normal PNR value and check the flow.
        BookingResponse r = new BookingResponse("PNR-100");
        assertEquals("PNR-100", r.getPnr());

        // I now change it once so setter also gets covered.
        r.setPnr("PNR-200");
        assertEquals("PNR-200", r.getPnr());
        assertTrue(r.toString().contains("PNR-200"));

        // A short JSON round-trip so that Jacoco touches serialization.
        String json = mapper.writeValueAsString(r);
        BookingResponse back = mapper.readValue(json, BookingResponse.class);
        assertEquals("PNR-200", back.getPnr());
    }

    @Test
    void bookingResponse_equality_behaviour() throws Exception {
        // I keep two objects with the same PNR and expect them to be equal.
        BookingResponse a = new BookingResponse("PNR-999");
        BookingResponse b = new BookingResponse("PNR-999");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void addFlightResponse_basicChecks() throws Exception {
        AddFlightResponse r = new AddFlightResponse("ID-11");
        assertEquals("ID-11", r.getId());

        // JSON round-trip
        String json = mapper.writeValueAsString(r);
        AddFlightResponse back = mapper.readValue(json, AddFlightResponse.class);

        assertEquals("ID-11", back.getId());
        assertEquals(r, back);
        assertEquals(r.hashCode(), back.hashCode());
        assertTrue(r.toString().contains("ID-11"));
    }

    @Test
    void addFlightResponse_setterBehaviour() {
        // I just poke the setter once so it shows up in Jacoco.
        AddFlightResponse r = new AddFlightResponse("X1");
        r.setId("X2");
        assertEquals("X2", r.getId());
    }
}
