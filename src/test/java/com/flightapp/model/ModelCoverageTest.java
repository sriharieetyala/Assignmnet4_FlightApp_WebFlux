package com.flightapp.model;

import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

/**
 * I mainly use this class to hit the deeper parts of equals/hashCode/toString.
 * These tests look simple, but they push Jacoco to count all the branches a POJO usually hides.
 */
class ModelCoverageTest {

    @Test
    void booking_equals_hashcode_toString_full_exercise() {
        // I create two bookings that look identical and expect them to behave the same.
        Instant now = Instant.now();
        Booking a = new Booking(
                "mongo-1", "PNR101", "BLR-DEL",
                2, "Sanjay", "sanjay@x.com",
                Gender.MALE, MealType.VEG,
                BookingStatus.BOOKED, now
        );

        Booking b = new Booking(
                "mongo-1", "PNR101", "BLR-DEL",
                2, "Sanjay", "sanjay@x.com",
                Gender.MALE, MealType.VEG,
                BookingStatus.BOOKED, now
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("PNR101"));

        // Now I tweak one field and expect them to differ.
        b.setEmail("other@x.com");
        assertNotEquals(a, b);
    }

    @Test
    void flight_allConstructors_getters_setters_and_string() {
        // I try out the no-args constructor properly.
        Flight f = new Flight();
        f.setId("F1");
        f.setFlightNumber("AI202");
        f.setTotalSeats(180);
        f.setAvailableSeats(160);
        f.setPrice(4999.0f);
        f.setFromPlace("Bengaluru");
        f.setToPlace("Mumbai");

        assertEquals("AI202", f.getFlightNumber());
        assertTrue(f.toString().contains("AI202"));

        // And now I check the all-args constructor one time.
        Flight f2 = new Flight("F2", "IndiGo", "6E501",
                "Hyderabad", "Delhi", "2025-06-01T10:00:00",
                "2025-06-01T12:30:00", 5500f,
                200, 200, "A320");

        assertEquals("F2", f2.getId());
        assertEquals(200, f2.getTotalSeats());
    }
}