package com.flightapp.controller;

import com.flightapp.dto.request.BookingRequest;
import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.model.Booking;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping("/api/flight/airline/inventory")
public class BookingController {

    @Autowired
    private FlightService flightService; // used for booking flow you already have

    @Autowired
    private BookingService bookingService; // new service for lookups

    @Autowired
    private Validator validator;

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookingResponse> bookTicket(@RequestBody BookingRequest req) {

        // programmatic validation first
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            ConstraintViolation<BookingRequest> v = violations.iterator().next();
            String msg = v.getPropertyPath() + " " + v.getMessage();
            return Mono.error(new IllegalArgumentException(msg));
        }

        // Forward to FlightService (business checks like seat availability are there)
        return flightService.bookTicket(
                req.getFlightId(),
                req.getSeats(),
                req.getName(),
                req.getEmail(),
                req.getGender(),
                req.getMealPreference()
        );
    }

    /**
     * Lookup single booking by PNR.
     * returns 404 via GlobalErrorHandler when empty.
     */
    // GET booking by PNR
    @GetMapping("/booking/{pnr}")
    public Mono<Booking> getBookingByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("booking not found")));
    }

    // GET booking history by email
    @GetMapping("/booking/email/{email}")
    public Flux<Booking> getBookingsByEmail(@PathVariable String email) {
        return bookingService.getBookingHistoryByEmail(email);
    }
}
