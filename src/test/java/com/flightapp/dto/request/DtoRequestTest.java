package com.flightapp.dto.request;

import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.exception.GlobalErrorHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I kept all the tests that i could think of.
 * These are intentionally simple  getters, setters, a tiny JSON round-trip,
 * and a basic validation check using ParameterMessageInterpolator so tests run without EL.
 *
 * I used everyday names and kept the checks readable, not robotic.
 */
@Import(GlobalErrorHandler.class)
class DtoRequestTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void bookingRequest_allConstructors_getters_setters_and_jsonRoundTrip() throws Exception {
        // I create a booking request with the all-args constructor and check fields.
        BookingRequest br = new BookingRequest("FL-1", 2, "Alice", "alice@x.com", Gender.FEMALE, MealType.VEG);
        assertEquals("FL-1", br.getFlightId());
        assertEquals(2, br.getSeats());
        assertEquals("Alice", br.getName());
        assertEquals("alice@x.com", br.getEmail());
        assertEquals(Gender.FEMALE, br.getGender());
        assertEquals(MealType.VEG, br.getMealPreference());

        // Now check the no-args + setters path too, so both constructors are covered.
        BookingRequest br2 = new BookingRequest();
        br2.setFlightId("FL-2");
        br2.setSeats(1);
        br2.setName("Bob");
        br2.setEmail("bob@x.com");
        br2.setGender(Gender.MALE);
        br2.setMealPreference(MealType.NONVEG);

        assertEquals("FL-2", br2.getFlightId());
        assertEquals(1, br2.getSeats());
        assertEquals("Bob", br2.getName());

        // Short JSON round-trip to exercise Jackson serialization/deserialization.
        String json = mapper.writeValueAsString(br);
        BookingRequest des = mapper.readValue(json, BookingRequest.class);
        assertEquals(br.getFlightId(), des.getFlightId());
        assertEquals(br.getSeats(), des.getSeats());
    }

    @Test
    void bookingRequest_validation_detects_missingOrInvalidFields() {
        // I create a clearly invalid request: empty flightId, zero seats, bad email, null enums.
        BookingRequest invalid = new BookingRequest("", 0, "", "not-an-email", null, null);

        // Build a ValidatorFactory that doesn't need EL (avoids jakarta.el on classpath).
        ValidatorFactory vf = jakarta.validation.Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        Validator validator = vf.getValidator();

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(invalid);
        assertFalse(violations.isEmpty(), "I expect validation issues for the invalid booking request");

        // Check that at least one of the expected fields (flightId, seats, email) is reported.
        boolean found = violations.stream()
                .anyMatch(v -> {
                    String p = v.getPropertyPath().toString();
                    return "flightId".equals(p) || "seats".equals(p) || "email".equals(p);
                });
        assertTrue(found, "Validation should flag flightId, seats or email problems");
    }

    @Test
    void bookingRequest_simple_json_and_mutation() throws Exception {
        // Another lightweight test to keep coverage obvious and readable.
        BookingRequest r = new BookingRequest("FL-123", 2, "Maya", "maya@x.com", Gender.FEMALE, MealType.VEG);
        assertEquals("FL-123", r.getFlightId());
        assertEquals(2, r.getSeats());
        assertEquals("Maya", r.getName());

        // mutate and check
        r.setSeats(3);
        assertEquals(3, r.getSeats());

        // jackson roundtrip
        String json = mapper.writeValueAsString(r);
        BookingRequest out = mapper.readValue(json, BookingRequest.class);
        assertEquals(r.getFlightId(), out.getFlightId());
    }

    @Test
    void addFlightRequest_serialize_mutate_and_basic_checks() throws Exception {
        // I use Indian city names and realistic values for readability.
        AddFlightRequest add = new AddFlightRequest(
                "AirIndia", "AI-101", "Chennai", "Hyderabad",
                "2025-12-01T10:00:00Z", "2025-12-01T12:00:00Z",
                4500.0f, 150, "A320"
        );

        assertEquals("AirIndia", add.getAirline());
        assertEquals("AI-101", add.getFlightNumber());
        assertEquals("Chennai", add.getFromPlace());
        assertEquals("Hyderabad", add.getToPlace());
        assertEquals(150, add.getTotalSeats());
        assertEquals(4500.0f, add.getPrice());

        // mutate a field and confirm setter works
        add.setAircraft("B737");
        assertEquals("B737", add.getAircraft());

        // JSON round-trip
        String json = mapper.writeValueAsString(add);
        AddFlightRequest back = mapper.readValue(json, AddFlightRequest.class);
        assertEquals(add.getFlightNumber(), back.getFlightNumber());
        assertEquals(add.getTotalSeats(), back.getTotalSeats());
    }

    @Test
    void addFlightRequest_ctor_and_fields_quick() {
        // Minimal smoke test for constructor / getters / setters
        AddFlightRequest r = new AddFlightRequest(
                "IndiGo", "6E-250", "Bengaluru", "Delhi",
                "2025-12-05T07:00:00Z", "2025-12-05T09:30:00Z",
                3200.0f, 120, "A320"
        );

        assertEquals("IndiGo", r.getAirline());
        assertEquals("6E-250", r.getFlightNumber());
        assertEquals("Bengaluru", r.getFromPlace());
        assertEquals("Delhi", r.getToPlace());
        assertEquals(120, r.getTotalSeats());

        r.setAircraft("A321");
        assertEquals("A321", r.getAircraft());
    }
}
