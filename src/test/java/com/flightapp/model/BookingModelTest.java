package com.flightapp.model;

import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BookingModelTest {

    @Test
    void lombok_gettersAndSetters_and_toString_work() {
        Instant now = Instant.now();

        Booking b = new Booking();
        b.setId("mongo-id-1");
        b.setPnr("PNR123");
        b.setFlightId("flight-1");
        b.setSeatsBooked(3);
        b.setName("Alice");
        b.setEmail("alice@example.com");
        b.setGender(Gender.FEMALE);
        b.setMealPreference(MealType.VEG);
        b.setStatus(BookingStatus.BOOKED);
        b.setCreatedAt(now);

        assertEquals("mongo-id-1", b.getId());
        assertEquals("PNR123", b.getPnr());
        assertEquals("flight-1", b.getFlightId());
        assertEquals(3, b.getSeatsBooked());
        assertEquals("Alice", b.getName());
        assertEquals("alice@example.com", b.getEmail());
        assertEquals(Gender.FEMALE, b.getGender());
        assertEquals(MealType.VEG, b.getMealPreference());
        assertEquals(BookingStatus.BOOKED, b.getStatus());
        assertEquals(now, b.getCreatedAt());

        // toString should contain a few key pieces (Lombok-generated)
        String s = b.toString();
        assertTrue(s.contains("PNR123"));
        assertTrue(s.contains("Alice"));
    }

    @Test
    void allArgsConstructor_and_equalsHashcode() {
        Instant now = Instant.now();
        Booking b1 = new Booking("id1", "PNR1", "flightX", 1, "Bob", "bob@x.com", Gender.MALE, MealType.NONVEG, BookingStatus.BOOKED, now);
        Booking b2 = new Booking("id1", "PNR1", "flightX", 1, "Bob", "bob@x.com", Gender.MALE, MealType.NONVEG, BookingStatus.BOOKED, now);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }
}
