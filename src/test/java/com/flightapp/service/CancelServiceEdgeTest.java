package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CancelService edge cases.
 * I check missing PNR, and DB error propagation when updating flight fails.
 */
import com.flightapp.exception.GlobalErrorHandler;
import org.springframework.context.annotation.Import;
@Import(GlobalErrorHandler.class)
class CancelServiceEdgeTest {

    CancelService cancelService;
    BookingRepository bookingRepository;
    FlightRepository flightRepository;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        flightRepository = Mockito.mock(FlightRepository.class);

        cancelService = new CancelService();
        ReflectionTestUtils.setField(cancelService, "bookingRepository", bookingRepository);
        ReflectionTestUtils.setField(cancelService, "flightRepository", flightRepository);
    }

    @Test
    void cancelBooking_whenPnrNotFound_throwsNoSuchElement() {
        // If booking is missing, service should throw NoSuchElementException.
        Mockito.when(bookingRepository.findByPnr("MISSING")).thenReturn(Mono.empty());

        StepVerifier.create(cancelService.cancelBooking("MISSING"))
                .expectErrorSatisfies(err -> assertTrue(err instanceof NoSuchElementException))
                .verify();
    }

    @Test
    void cancelBooking_whenFlightSaveFails_propagatesError() {
        // I simulate a case where booking exists but flight save fails (DB error).
        Booking b = new Booking();
        b.setPnr("PNR1");
        b.setFlightId("F1");
        b.setSeatsBooked(2);
        b.setCreatedAt(Instant.now());

        Mockito.when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(b));
        // ensure repository save/delete calls return non-null monos so flow reaches flight save
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(Mono.just(b));
        Mockito.when(bookingRepository.delete(Mockito.any())).thenReturn(Mono.empty());

        Mockito.when(flightRepository.findById("F1")).thenReturn(Mono.just(new com.flightapp.model.Flight()));
        Mockito.when(flightRepository.save(Mockito.any())).thenReturn(Mono.error(new RuntimeException("db fail")));

        StepVerifier.create(cancelService.cancelBooking("PNR1"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof RuntimeException);
                    assertTrue(err.getMessage().contains("db fail"));
                })
                .verify();
    }

    @Test
    void cancelBooking_whenDeleteBookingFails_propagatesError() {
        // Real-world: delete might fail; we must ensure error propagates to caller
        Booking b = new Booking();
        b.setPnr("PNR-DEL");
        b.setFlightId("F2");
        b.setSeatsBooked(1);
        b.setCreatedAt(Instant.now());

        Mockito.when(bookingRepository.findByPnr("PNR-DEL")).thenReturn(Mono.just(b));
        Mockito.when(flightRepository.findById("F2")).thenReturn(Mono.just(new com.flightapp.model.Flight()));
        Mockito.when(flightRepository.save(Mockito.any())).thenReturn(Mono.just(new com.flightapp.model.Flight()));
        // Simulate delete failure
        Mockito.when(bookingRepository.delete(Mockito.any())).thenReturn(Mono.error(new RuntimeException("delete fail")));

        StepVerifier.create(cancelService.cancelBooking("PNR-DEL"))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().contains("delete fail"))
                .verify();
    }
}
