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

    // return booking by its PNR
    public Mono<Booking> getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr);
    }
    // return booking by its Email
    public Flux<Booking> getBookingHistoryByEmail(String email) {
        return bookingRepository.findByEmail(email);
    }



}
