package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    BookingServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBookingByPnr_found() {
        Booking b = new Booking();
        b.setPnr("PNR1");

        when(bookingRepository.findByPnr("PNR1")).thenReturn(Mono.just(b));

        StepVerifier.create(bookingService.getBookingByPnr("PNR1"))
                .expectNext(b)
                .verifyComplete();
    }

    @Test
    void getBookingHistoryByEmail() {
        Booking b1 = new Booking();
        Booking b2 = new Booking();

        when(bookingRepository.findByEmail("mail@gmail.com"))
                .thenReturn(Flux.just(b1, b2));

        StepVerifier.create(bookingService.getBookingHistoryByEmail("mail@gmail.com"))
                .expectNextCount(2)
                .verifyComplete();
    }
}
