package com.flightapp.service;

import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.flightapp.model.Booking;
import com.flightapp.model.Flight;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightServiceTest {

    private FlightRepository flightRepository;
    private BookingRepository bookingRepository;
    private FlightService flightService;

    @BeforeEach
    void setup() {
        flightRepository = mock(FlightRepository.class);
        bookingRepository = mock(BookingRepository.class);
        flightService = new FlightService();
        // inject with reflection or setter if you have one; otherwise use field access
        TestUtils.setField(flightService, "flightRepository", flightRepository);
        TestUtils.setField(flightService, "bookingRepository", bookingRepository);
    }

    @Test
    void bookTicket_success() {
        Flight f = new Flight();
        f.setId("F1");
        f.setAvailableSeats(5);

        when(flightRepository.findById("F1")).thenReturn(Mono.just(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setCreatedAt(Instant.now());
            return Mono.just(b);
        });

        Mono<BookingResponse> out = flightService.bookTicket("F1", 2, "A", "a@x.com", Gender.MALE, MealType.VEG);

        StepVerifier.create(out)
                .expectNextMatches(br -> br.getPnr() != null && br.getPnr().length() == 6)
                .verifyComplete();
    }

    @Test
    void bookTicket_notEnoughSeats() {
        Flight f = new Flight();
        f.setId("F2");
        f.setAvailableSeats(1);

        when(flightRepository.findById("F2")).thenReturn(Mono.just(f));

        Mono<BookingResponse> out = flightService.bookTicket("F2", 2, "A", "a@x.com", Gender.MALE, MealType.VEG);

        StepVerifier.create(out)
                .expectErrorMatches(e -> e instanceof IllegalStateException && e.getMessage().contains("Not enough seats"))
                .verify();
    }

    @Test
    void bookTicket_flightNotFound() {
        when(flightRepository.findById("NOPE")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.bookTicket("NOPE", 1, "A", "a@x.com", Gender.MALE, MealType.VEG))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException || e instanceof java.util.NoSuchElementException)
                .verify();
    }
}
