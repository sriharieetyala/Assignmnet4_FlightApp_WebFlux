//package com.flightapp.service;
//
//import com.flightapp.model.Booking;
//import com.flightapp.repository.BookingRepository;
//import com.flightapp.repository.FlightRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.test.util.ReflectionTestUtils;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.time.Instant;
//import java.util.NoSuchElementException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Cover CancelService branches:
// *  - switchIfEmpty -> NoSuchElementException
// *  - repository save error is propagated
// */
//class CancelServiceEdgeTest {
//
//    CancelService cancelService;
//    BookingRepository bookingRepository;
//    FlightRepository flightRepository;
//
//    @BeforeEach
//    void setUp() {
//        bookingRepository = Mockito.mock(BookingRepository.class);
//        flightRepository = Mockito.mock(FlightRepository.class);
//
//        cancelService = new CancelService();
//        // inject private fields so service uses our mocks
//        ReflectionTestUtils.setField(cancelService, "bookingRepository", bookingRepository);
//        ReflectionTestUtils.setField(cancelService, "flightRepository", flightRepository);
//    }
//
//    @Test
//    void cancelBooking_whenPnrNotFound_throwsNoSuchElement() {
//        Mockito.when(bookingRepository.findByPnr("MISSING")).thenReturn(Mono.empty());
//
//        StepVerifier.create(cancelService.cancelBooking("MISSING"))
//                .expectErrorSatisfies(err -> assertTrue(err instanceof NoSuchElementException))
//                .verify();
//    }
//
//    @Test
//    void cancelBooking_whenFlightSaveFails_propagatesError() {
//        Booking b = new Booking();
//        b.setPnr("PNR1");
//        b.setFlightId("F1");
//        b.setSeatsBooked(2);
//        b.setCreatedAt(Instant.now());
//
//        // booking found
//        Mockito.when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(b));
//
//        // flightRepository.save will fail (simulate DB problem)
//        Mockito.when(flightRepository.findById("F1")).thenReturn(Mono.just(new com.flightapp.model.Flight()));
//        Mockito.when(flightRepository.save(Mockito.any())).thenReturn(Mono.error(new RuntimeException("db fail")));
//
//        // Even though bookingRepository.delete might exist, error should propagate from flightRepository.save
//        StepVerifier.create(cancelService.cancelBooking("PNR1"))
//                .expectErrorSatisfies(err -> assertTrue(err instanceof RuntimeException && err.getMessage().contains("db fail")))
//                .verify();
//    }
//}
