package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

/**
 * I keep BookingService tests short and practical:
 * - lookup by PNR
 * - booking history by email (empty and non-empty)
 */
import com.flightapp.exception.GlobalErrorHandler;
import org.springframework.context.annotation.Import;
@Import(GlobalErrorHandler.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService();
        // inject mock repository into service
        TestUtils.setField(bookingService, "bookingRepository", bookingRepository);
    }

    @Test
    void getBookingByPnr_found() {
        Booking b = new Booking();
        b.setPnr("PNR1");
        b.setEmail("user@xyz.com");

        when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(b));

        StepVerifier.create(bookingService.getBookingByPnr("PNR1"))
                .expectNextMatches(found -> "PNR1".equals(found.getPnr()) && "user@xyz.com".equals(found.getEmail()))
                .verifyComplete();
    }

    @Test
    void getBookingByPnr_notFound_completesEmpty() {
        when(bookingRepository.findByPnr("NONE")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.getBookingByPnr("NONE"))
                .expectComplete()
                .verify();
    }

    @Test
    void getBookingHistoryByEmail_returnsList() {
        Booking b1 = new Booking();
        b1.setEmail("mail@gmail.com");
        Booking b2 = new Booking();
        b2.setEmail("mail@gmail.com");

        when(bookingRepository.findByEmail("mail@gmail.com")).thenReturn(Flux.just(b1, b2));

        StepVerifier.create(bookingService.getBookingHistoryByEmail("mail@gmail.com"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getBookingHistoryByEmail_empty_returnsNoElements() {
        when(bookingRepository.findByEmail("no@one.com")).thenReturn(Flux.empty());

        StepVerifier.create(bookingService.getBookingHistoryByEmail("no@one.com"))
                .expectComplete()
                .verify();
    }
}
