package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.model.Flight;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * I test CancelService behaviour with small, practical scenarios:
 * - successful cancel restores seats
 * - missing PNR returns an error
 * - cancelling after 24h is rejected
 *
 * I keep examples local (F1 = Mumbai flight, etc.) so it feels real.
 */
import com.flightapp.exception.GlobalErrorHandler;
import org.springframework.context.annotation.Import;
@Import(GlobalErrorHandler.class)
class CancelServiceTest {

    private BookingRepository bookingRepository;
    private FlightRepository flightRepository;
    private CancelService cancelService;

    @BeforeEach
    void setup() {
        bookingRepository = mock(BookingRepository.class);
        flightRepository = mock(FlightRepository.class);
        cancelService = new CancelService();
        // inject mocks into private fields
        TestUtils.setField(cancelService, "bookingRepository", bookingRepository);
        TestUtils.setField(cancelService, "flightRepository", flightRepository);
    }

    @Test
    void cancelBooking_success_restoresSeats() {
        // I simulate a normal cancel for a Mumbai flight — seats should be restored.
        Booking b = new Booking();
        b.setPnr("PNRMUM1");
        b.setFlightId("F-MUM-1");
        b.setSeatsBooked(2);
        b.setCreatedAt(Instant.now());

        Flight f = new Flight();
        f.setId("F-MUM-1");
        f.setAvailableSeats(3);

        when(bookingRepository.findByPnr("PNRMUM1")).thenReturn(Mono.just(b));
        when(flightRepository.findById("F-MUM-1")).thenReturn(Mono.just(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(a -> Mono.just(a.getArgument(0)));
        when(bookingRepository.delete(b)).thenReturn(Mono.empty());

        StepVerifier.create(cancelService.cancelBooking("PNRMUM1"))
                .expectNext("Booking cancelled")
                .verifyComplete();
    }

    @Test
    void cancelBooking_pnrNotFound_throws() {
        // If the PNR is missing, I expect the service to fail with a clear error.
        when(bookingRepository.findByPnr("NOPE")).thenReturn(Mono.empty());

        StepVerifier.create(cancelService.cancelBooking("NOPE"))
                .expectErrorMatches(e -> (e instanceof IllegalArgumentException) || e.getMessage().toLowerCase().contains("pnr"))
                .verify();
    }

    @Test
    void cancelBooking_tooLate_throwsIllegalState() {
        // If booking is older than 24 hours (e.g., 25 hours), cancellation should be rejected.
        Booking b = new Booking();
        b.setPnr("OLDPNR");
        b.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 25)); // 25 hours ago

        when(bookingRepository.findByPnr("OLDPNR")).thenReturn(Mono.just(b));

        StepVerifier.create(cancelService.cancelBooking("OLDPNR"))
                .expectErrorMatches(e -> e instanceof IllegalStateException)
                .verify();
    }

    @Test
    void cancelBooking_whenFlightSaveFails_propagatesError() {
        // Real-world: if updating flight fails (DB issue), we must propagate the error back to caller.
        Booking b = new Booking();
        b.setPnr("PNR-ERR");
        b.setFlightId("F-DEL-1");
        b.setSeatsBooked(1);
        b.setCreatedAt(Instant.now());

        when(bookingRepository.findByPnr("PNR-ERR")).thenReturn(Mono.just(b));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(b));
        when(bookingRepository.delete(any(Booking.class))).thenReturn(Mono.empty());
        when(flightRepository.findById("F-DEL-1")).thenReturn(Mono.just(new Flight()));
        when(flightRepository.save(any(Flight.class))).thenReturn(Mono.error(new RuntimeException("db fail")));

        StepVerifier.create(cancelService.cancelBooking("PNR-ERR"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().contains("db fail"))
                .verify();
    }
}
