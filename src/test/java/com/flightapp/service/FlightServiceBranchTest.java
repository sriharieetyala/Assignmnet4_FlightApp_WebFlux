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
        assertEquals(50, cap.getValue().getAvailableSeats());
    }

    @Test
    void existsByFlightNumber_trueAndFalse() {
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
        Flight f1 = new Flight();
        f1.setId("f1");
        Mockito.when(flightRepository.findAll()).thenReturn(Flux.just(f1));

        StepVerifier.create(flightService.getAllFlights())
                .expectNextMatches(f -> "f1".equals(f.getId()))
                .verifyComplete();
    }

    @Test
    void bookTicket_flightNotFound_throwsNoSuchElement() {
        Mockito.when(flightRepository.findById("bad")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.bookTicket("bad", 1, "n", "e", null, null))
                .expectErrorSatisfies(err -> assertTrue(err instanceof java.util.NoSuchElementException))
                .verify();
    }

    @Test
    void bookTicket_invalidSeats_throwsIllegalArgument() {
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
        assertEquals(3, capF.getValue().getAvailableSeats());

        ArgumentCaptor<Booking> capB = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository).save(capB.capture());
        assertEquals(2, capB.getValue().getSeatsBooked());
        assertEquals("f3", capB.getValue().getFlightId());
    }
}