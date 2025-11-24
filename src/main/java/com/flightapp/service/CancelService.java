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

/*
 I made this defensive and explicit so controller tests don't see weird 500s.
 - If booking.createdAt is missing, I return a clear IllegalStateException (so test can be adjusted if needed).
 - If the flight id from booking is missing in DB, I throw NoSuchElementException so GlobalErrorHandler returns 404.
 - Logic: check time window -> restore seats -> delete booking -> return message.
*/
@Service
public class CancelService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    public Mono<String> cancelBooking(String pnr) {

        return bookingRepository.findByPnr(pnr)
                .flatMap(booking -> {
                    // defensive: createdAt must exist for time-based cancellation rule
                    Instant created = booking.getCreatedAt();
                    if (created == null) {
                        return Mono.error(new IllegalStateException("booking createdAt missing"));
                    }

                    Instant now = Instant.now();

                    // 24-hour cancellation rule (cannot cancel after 24 hours)
                    if (Duration.between(created, now).toHours() > 24) {
                        return Mono.error(new IllegalStateException("Cannot cancel after 24 hours"));
                    }

                    // Restore seats in flight. If flight not found, surface 404.
                    return flightRepository.findById(booking.getFlightId())
                            .switchIfEmpty(Mono.error(new NoSuchElementException("Flight not found")))
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
                // IMPORTANT: if booking was not found, return NoSuchElementException so global handler returns 404
                .switchIfEmpty(Mono.error(new NoSuchElementException("PNR not found")));
    }
}
