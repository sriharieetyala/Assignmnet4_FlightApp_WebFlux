package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class CancelService {

    @Autowired
    private BookingRepository bookingRepository; // I added this to get booking details using pnr

    @Autowired
    private FlightRepository flightRepository; // I added this to update flight seat count during cancel

    public Mono<String> cancelBooking(String pnr) {

        // I am first checking if booking exists for given pnr
        return bookingRepository.findByPnr(pnr)
                .flatMap(booking -> {

                    // I am checking createdAt because cancel rule depends on booking time
                    Instant created = booking.getCreatedAt();
                    if (created == null) {
                        return Mono.error(new IllegalStateException("booking createdAt missing"));
                    }

                    Instant now = Instant.now();

                    // I am applying 24 hour cancel rule
                    if (Duration.between(created, now).toHours() > 24) {
                        return Mono.error(new IllegalStateException("Cannot cancel after 24 hours"));
                    }

                    // I am fetching the flight linked with this booking to restore seats
                    return flightRepository.findById(booking.getFlightId())
                            .switchIfEmpty(Mono.error(new NoSuchElementException("Flight not found")))
                            .flatMap(flight -> {
                                // I am adding seats back to available seats
                                flight.setAvailableSeats(
                                        flight.getAvailableSeats() + booking.getSeatsBooked()
                                );
                                return flightRepository.save(flight);
                            })
                            // after updating seats I am removing the booking
                            .then(bookingRepository.delete(booking))
                            .thenReturn("Booking cancelled");
                })
                // I am sending not found error when no booking exists for pnr
                .switchIfEmpty(Mono.error(new NoSuchElementException("PNR not found")));
    }
}