package com.flightapp.model;

import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flightapp.exception.GlobalErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I keep this class only for JSON round-trip coverage using Jackson.
 * It bumps Jacoco nicely because Instant fields often get ignored otherwise.
 */
@Import(GlobalErrorHandler.class)
class BookingModelSimpleTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void booking_model_basic_and_json() throws Exception {
        Instant now = Instant.now();

        Booking b = new Booking(
                "id1", "PNR-DEL-11", "DEL-BLR",
                2, "Suresh", "suresh@xyz.com",
                Gender.MALE, MealType.VEG,
                BookingStatus.BOOKED, now
        );

        assertEquals("PNR-DEL-11", b.getPnr());
        assertEquals(2, b.getSeatsBooked());
        assertTrue(b.toString().contains("PNR-DEL-11"));

        // I test JSON conversion once — a realistic round-trip.
        String json = mapper.writeValueAsString(b);
        Booking back = mapper.readValue(json, Booking.class);

        assertNotNull(back);
        assertEquals("PNR-DEL-11", back.getPnr());
    }
}
