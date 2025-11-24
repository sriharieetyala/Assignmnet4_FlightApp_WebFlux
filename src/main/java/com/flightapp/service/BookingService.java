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
    private BookingRepository bookingRepository; // I added this to talk to database for booking data

    public Mono<Booking> getBookingByPnr(String pnr) {
        // I am returning one booking using pnr
        // if not found repository gives empty so controller can send not found
        return bookingRepository.findByPnr(pnr);
    }

    public Flux<Booking> getBookingHistoryByEmail(String email) {
        // I am returning all bookings for given email
        // if no bookings then empty flux is returned
        return bookingRepository.findByEmail(email);
    }

    // I kept place for your other booking methods like book or cancel
}
