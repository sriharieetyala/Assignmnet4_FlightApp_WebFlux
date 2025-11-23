package com.flightapp.service;

import com.flightapp.model.Booking;
import com.flightapp.model.Flight;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class CancelService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    public Mono<String> cancelBooking(String pnr) {

        return bookingRepository.findByPnr(pnr)
                .flatMap(booking -> {

                    Instant created = booking.getCreatedAt();
                    Instant now = Instant.now();

                    // 24-hour cancellation rule
                    if (Duration.between(created, now).toHours() > 24) {
                        return Mono.error(new IllegalStateException("Cannot cancel after 24 hours"));
                    }

                    // Restore seats in flight
                    return flightRepository.findById(booking.getFlightId())
                            .flatMap(flight -> {
                                flight.setAvailableSeats(
                                        flight.getAvailableSeats() + booking.getSeatsBooked()
                                );
                                return flightRepository.save(flight);
                            })
                            // delete booking after restoring seats
                            .then(bookingRepository.delete(booking))
                            .thenReturn("Booking cancelled");
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("PNR not found")));
    }
}
