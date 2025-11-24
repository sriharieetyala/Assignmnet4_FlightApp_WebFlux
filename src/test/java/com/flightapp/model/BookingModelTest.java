package com.flightapp.model;

import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I check Booking getters/setters and basic object behaviour here.
 */
class BookingModelTest {

    @Test
    void lombok_gettersAndSetters_and_toString_work() {
        Instant now = Instant.now();

        // I fill all fields so nothing is left untouched.
        Booking b = new Booking();
        b.setId("mongo-id-1");
        b.setPnr("PNR555");
        b.setFlightId("F-CHN-BLR");
        b.setSeatsBooked(3);
        b.setName("Aparna");
        b.setEmail("aparna@example.com");
        b.setGender(Gender.FEMALE);
        b.setMealPreference(MealType.NONVEG);
        b.setStatus(BookingStatus.BOOKED);
        b.setCreatedAt(now);

        assertEquals("mongo-id-1", b.getId());
        assertEquals("PNR555", b.getPnr());
        assertEquals(3, b.getSeatsBooked());
        assertEquals(Gender.FEMALE, b.getGender());
        assertEquals(now, b.getCreatedAt());

        assertTrue(b.toString().contains("PNR555"));
        assertTrue(b.toString().contains("Aparna"));
    }

    @Test
    void allArgsConstructor_and_equalsHashcode() {
        Instant now = Instant.now();

        Booking b1 = new Booking("IDZ", "PNR9", "F-BOM-DEL", 1,
                "Ravi", "ravi@x.com", Gender.MALE,
                MealType.VEG, BookingStatus.BOOKED, now);

        Booking b2 = new Booking("IDZ", "PNR9", "F-BOM-DEL", 1,
                "Ravi", "ravi@x.com", Gender.MALE,
                MealType.VEG, BookingStatus.BOOKED, now);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }
}
