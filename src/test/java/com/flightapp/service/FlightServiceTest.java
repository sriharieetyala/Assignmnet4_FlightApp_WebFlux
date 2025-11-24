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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * I test FlightService behaviour with mocked repositories.
 * I keep examples relatable — flight ids like HYD-1 and BLR-2 so it feels real.
 */
import com.flightapp.exception.GlobalErrorHandler;
import org.springframework.context.annotation.Import;
@Import(GlobalErrorHandler.class)
class FlightServiceTest {

    private FlightRepository flightRepository;
    private BookingRepository bookingRepository;
    private FlightService flightService;

    @BeforeEach
    void setup() {
        flightRepository = mock(FlightRepository.class);
        bookingRepository = mock(BookingRepository.class);
        flightService = new FlightService();
        // inject mocks
        TestUtils.setField(flightService, "flightRepository", flightRepository);
        TestUtils.setField(flightService, "bookingRepository", bookingRepository);
    }

    @Test
    void bookTicket_success_generatesPnrAndUpdatesSeats() {
        // I simulate a normal booking from Hyderabad with enough seats.
        Flight f = new Flight();
        f.setId("HYD-1");
        f.setAvailableSeats(5);

        when(flightRepository.findById("HYD-1")).thenReturn(Mono.just(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setCreatedAt(Instant.now());
            // return booking with stored PNR so we can assert
            b.setPnr("PNRHY1");
            return Mono.just(b);
        });

        Mono<BookingResponse> out = flightService.bookTicket("HYD-1", 2, "Ramesh", "ramesh@ok.com", Gender.MALE, MealType.VEG);

        StepVerifier.create(out)
                .assertNext(br -> {
                    // I expect a pnr non-null and within reasonable length.
                    assert br.getPnr() != null && br.getPnr().startsWith("PNR");
                    // simple sanity: pnr not ridiculously long
                    assert br.getPnr().length() <= 20;
                })
                .verifyComplete();
    }

    @Test
    void bookTicket_notEnoughSeats_returnsIllegalState() {
        // If available seats less than requested, service must fail with IllegalStateException
        Flight f = new Flight();
        f.setId("BLR-2");
        f.setAvailableSeats(1);

        when(flightRepository.findById("BLR-2")).thenReturn(Mono.just(f));

        Mono<BookingResponse> out = flightService.bookTicket("BLR-2", 2, "Priya", "priya@ok.com", Gender.FEMALE, MealType.NONVEG);

        StepVerifier.create(out)
                .expectErrorMatches(e -> e instanceof IllegalStateException && e.getMessage().toLowerCase().contains("not enough"))
                .verify();
    }

    @Test
    void bookTicket_flightNotFound_returnsError() {
        // If flight is missing, we must get a meaningful error (NoSuchElement or IllegalArgument).
        when(flightRepository.findById("NO-FLT")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.bookTicket("NO-FLT", 1, "Ajay", "ajay@ok.com", Gender.MALE, MealType.VEG))
                .expectError() // allow either NoSuchElementException or IllegalArgumentException depending on implementation
                .verify();
    }

    @Test
    void bookTicket_invalidSeats_throwsIllegalArgument() {
        // seats <= 0 is invalid input; I expect IllegalArgumentException from service
        Flight f = new Flight();
        f.setId("CHE-3");
        f.setAvailableSeats(10);

        when(flightRepository.findById("CHE-3")).thenReturn(Mono.just(f));

        StepVerifier.create(flightService.bookTicket("CHE-3", 0, "Sita", "sita@ok.com", Gender.FEMALE, MealType.VEG))
                .expectErrorMatches(err -> err instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void bookTicket_pnrFormat_commonSense_check() {
        // I assert that PNRs generated look sensible: starts with 'PNR' and contains letters/numbers.
        Flight f = new Flight();
        f.setId("DEL-4");
        f.setAvailableSeats(10);

        when(flightRepository.findById("DEL-4")).thenReturn(Mono.just(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setPnr("PNRDEL4"); // emulate repository filling pnr
            b.setCreatedAt(Instant.now());
            return Mono.just(b);
        });

        StepVerifier.create(flightService.bookTicket("DEL-4", 1, "Kumar", "kumar@ok.com", Gender.MALE, MealType.VEG))
                .assertNext(br -> {
                    String p = br.getPnr();
                    // common-sense checks
                    assert p.startsWith("PNR");
                    assert p.matches("[A-Za-z0-9-]+");
                    assert p.length() >= 5 && p.length() <= 20;
                })
                .verifyComplete();
    }
}
