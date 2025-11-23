package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceAdditionalTest {

    BookingService bookingService;
    BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        bookingService = new BookingService();
        ReflectionTestUtils.setField(bookingService, "bookingRepository", bookingRepository);
    }

    @Test
    void getBookingByPnr_returnsMonoEmptyWhenNotFound() {
        Mockito.when(bookingRepository.findByPnr("none")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.getBookingByPnr("none"))
                .expectComplete()
                .verify();
    }

    @Test
    void getBookingByPnr_returnsBooking() {
        Booking b = new Booking();
        b.setPnr("PNR99");
        Mockito.when(bookingRepository.findByPnr("PNR99")).thenReturn(Mono.just(b));

        StepVerifier.create(bookingService.getBookingByPnr("PNR99"))
                .expectNextMatches(bb -> "PNR99".equals(bb.getPnr()))
                .verifyComplete();
    }

    @Test
    void getBookingHistoryByEmail_emptyAndNonEmpty() {
        Mockito.when(bookingRepository.findByEmail("a@x.com")).thenReturn(Flux.empty());
        StepVerifier.create(bookingService.getBookingHistoryByEmail("a@x.com").collectList())
                .expectNextMatches(list -> list.isEmpty())
                .verifyComplete();

        Booking b = new Booking();
        b.setEmail("b@x.com");
        Mockito.when(bookingRepository.findByEmail("b@x.com")).thenReturn(Flux.just(b));

        StepVerifier.create(bookingService.getBookingHistoryByEmail("b@x.com").collectList())
                .expectNextMatches(list -> list.size() == 1 && "b@x.com".equals(list.get(0).getEmail()))
                .verifyComplete();
    }
}