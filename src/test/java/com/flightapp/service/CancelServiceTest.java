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

class CancelServiceTest {

    private BookingRepository bookingRepository;
    private FlightRepository flightRepository;
    private CancelService cancelService;

    @BeforeEach
    void setup() {
        bookingRepository = mock(BookingRepository.class);
        flightRepository = mock(FlightRepository.class);
        cancelService = new CancelService();
        TestUtils.setField(cancelService, "bookingRepository", bookingRepository);
        TestUtils.setField(cancelService, "flightRepository", flightRepository);
    }

    @Test
    void cancelBooking_success_restoresSeats() {
        Booking b = new Booking();
        b.setPnr("PNR1");
        b.setFlightId("F1");
        b.setSeatsBooked(2);
        b.setCreatedAt(Instant.now());

        Flight f = new Flight();
        f.setId("F1");
        f.setAvailableSeats(3);

        when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(b));
        when(flightRepository.findById("F1")).thenReturn(Mono.just(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(a -> Mono.just(a.getArgument(0)));
        when(bookingRepository.delete(b)).thenReturn(Mono.empty());

        StepVerifier.create(cancelService.cancelBooking("PNR1"))
                .expectNext("Booking cancelled")
                .verifyComplete();
    }

    @Test
    void cancelBooking_pnrNotFound_throws() {
        when(bookingRepository.findByPnr("NOPE")).thenReturn(Mono.empty());

        StepVerifier.create(cancelService.cancelBooking("NOPE"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException || e.getMessage().contains("PNR not found"))
                .verify();
    }

    @Test
    void cancelBooking_tooLate_throwsIllegalState() {
        Booking b = new Booking();
        b.setPnr("OLD");
        b.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 25)); // older than 24h

        when(bookingRepository.findByPnr("OLD")).thenReturn(Mono.just(b));

        StepVerifier.create(cancelService.cancelBooking("OLD"))
                .expectErrorMatches(e -> e instanceof IllegalStateException)
                .verify();
    }
}
