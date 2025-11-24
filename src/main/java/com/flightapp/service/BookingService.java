package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Return a single booking by PNR. If not found the Mono will be empty.
     * Tests expect Mono.empty() when not found.
     */
    public Mono<Booking> getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr);
    }

    /**
     * Return booking history by email. If none found returns empty Flux.
     */
    public Flux<Booking> getBookingHistoryByEmail(String email) {
        return bookingRepository.findByEmail(email);
    }

    // keep your existing booking/other methods (book/cancel) below or above
}
