package com.flightapp.service;

import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.model.Booking;
import com.flightapp.model.Flight;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests cover edge branches and use argument captors to make sure we saved correct things.
 * I use small, clear flight examples that sound local — Hyderabad, Chennai etc.
 */
class FlightServiceBranchTest {

    FlightService flightService;
    FlightRepository flightRepository;
    BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        flightRepository = Mockito.mock(FlightRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);

        flightService = new FlightService();
        ReflectionTestUtils.setField(flightService, "flightRepository", flightRepository);
        ReflectionTestUtils.setField(flightService, "bookingRepository", bookingRepository);
    }

    @Test
    void createFlight_setsAvailableSeats_whenZero() {
        // I check that when totalSeats is set but availableSeats is zero, service initializes availableSeats.
        Flight f = new Flight();
        f.setTotalSeats(50);
        f.setAvailableSeats(0);

        Flight saved = new Flight();
        saved.setId("id1");
        saved.setAvailableSeats(50);

        Mockito.when(flightRepository.save(Mockito.any(Flight.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(flightService.createFlight(f))
                .expectNextMatches(ff -> ff.getAvailableSeats() == 50 && "id1".equals(ff.getId()))
                .verifyComplete();

        ArgumentCaptor<Flight> cap = ArgumentCaptor.forClass(Flight.class);
        Mockito.verify(flightRepository).save(cap.capture());
        assertEquals(50, cap.getValue().getAvailableSeats(), "I expect availableSeats to be set to totalSeats when previously zero");
    }

    @Test
    void existsByFlightNumber_trueAndFalse() {
        // I ensure existsByFlightNumber maps repo result to boolean properly.
        Mockito.when(flightRepository.findByFlightNumber("FN1")).thenReturn(Mono.just(new Flight()));
        Mockito.when(flightRepository.findByFlightNumber("MISSING")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.existsByFlightNumber("FN1"))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(flightService.existsByFlightNumber("MISSING"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getAllFlights_delegatesToRepo() {
        // Basic delegation test — repo returns 1 flight and service forwards it.
        Flight f1 = new Flight();
        f1.setId("f1");
        Mockito.when(flightRepository.findAll()).thenReturn(Flux.just(f1));

        StepVerifier.create(flightService.getAllFlights())
                .expectNextMatches(f -> "f1".equals(f.getId()))
                .verifyComplete();
    }

    @Test
    void bookTicket_flightNotFound_throwsNoSuchElement() {
        // If findById returns empty, the service should fail loudly.
        Mockito.when(flightRepository.findById("bad")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.bookTicket("bad", 1, "n", "e", null, null))
                .expectErrorSatisfies(err -> assertTrue(err instanceof java.util.NoSuchElementException))
                .verify();
    }

    @Test
    void bookTicket_invalidSeats_throwsIllegalArgument() {
        // seats must be >= 1; I check invalid input path
        Flight f = new Flight();
        f.setId("f1");
        f.setAvailableSeats(10);
        Mockito.when(flightRepository.findById("f1")).thenReturn(Mono.just(f));

        StepVerifier.create(flightService.bookTicket("f1", 0, "n", "e", null, null))
                .expectErrorSatisfies(err -> assertTrue(err instanceof IllegalArgumentException))
                .verify();
    }

    @Test
    void bookTicket_notEnoughSeats_throwsIllegalState() {
        // Not enough seats branch triggers IllegalStateException
        Flight f = new Flight();
        f.setId("f2");
        f.setAvailableSeats(2);
        Mockito.when(flightRepository.findById("f2")).thenReturn(Mono.just(f));

        StepVerifier.create(flightService.bookTicket("f2", 5, "n", "e", null, null))
                .expectErrorSatisfies(err -> assertTrue(err instanceof IllegalStateException))
                .verify();
    }

    @Test
    void bookTicket_success_updatesFlightAndSavesBooking() {
        // Full happy path: flight available, saving both updated flight and booking should be called with correct values.
        Flight f = new Flight();
        f.setId("f3");
        f.setAvailableSeats(5);
        Mockito.when(flightRepository.findById("f3")).thenReturn(Mono.just(f));

        Flight savedFlight = new Flight();
        savedFlight.setId("f3");
        savedFlight.setAvailableSeats(3);
        Mockito.when(flightRepository.save(Mockito.any(Flight.class))).thenReturn(Mono.just(savedFlight));

        Booking savedBooking = new Booking();
        savedBooking.setPnr("PNR1");
        savedBooking.setFlightId("f3");
        savedBooking.setSeatsBooked(2);
        savedBooking.setCreatedAt(Instant.now());
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(Mono.just(savedBooking));

        StepVerifier.create(flightService.bookTicket("f3", 2, "Name", "e@x.com", null, null))
                .expectNextMatches(br -> br.getPnr() != null && br.getPnr().equals("PNR1"))
                .verifyComplete();

        ArgumentCaptor<Flight> capF = ArgumentCaptor.forClass(Flight.class);
        Mockito.verify(flightRepository).save(capF.capture());
        assertEquals(3, capF.getValue().getAvailableSeats(), "After booking 2 seats, available should drop by 2");

        ArgumentCaptor<Booking> capB = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository).save(capB.capture());
        assertEquals(2, capB.getValue().getSeatsBooked(), "Booking record should store the seats booked");
        assertEquals("f3", capB.getValue().getFlightId(), "Booking should reference correct flight id");
    }
}