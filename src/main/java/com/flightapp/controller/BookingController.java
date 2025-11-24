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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping("/api/flight/airline/inventory")
public class BookingController {

    @Autowired
    private FlightService flightService; // booking logic delegated to FlightService

    @Autowired
    private BookingService bookingService; // to fetch booking details and history

    @Autowired
    private Validator validator; // bean validation

    @PostMapping("/book")
    public Mono<ResponseEntity<BookingResponse>> bookTicket(@RequestBody BookingRequest req) {

        // defensive: validator.validate can return null in test slices if not stubbed
        Set<ConstraintViolation<BookingRequest>> violations = validator == null ? null : validator.validate(req);
        if (violations != null && !violations.isEmpty()) {
            // return the annotation message only (tests expect the annotation message)
            String msg = violations.iterator().next().getMessage();
            return Mono.error(new IllegalArgumentException(msg));
        }

        // delegate booking to flightService which contains seat checks and booking persistence
        return flightService.bookTicket(
                        req.getFlightId(),
                        req.getSeats(),
                        req.getName(),
                        req.getEmail(),
                        req.getGender(),
                        req.getMealPreference()
                )
                // wrap successful response with 201 Created
                .map(br -> ResponseEntity.status(HttpStatus.CREATED).body(br))
                // map expected business exceptions to proper responses via global handler; keep stream semantics
                .onErrorResume(ex -> {
                    // let GlobalErrorHandler handle; rethrow to be handled globally
                    return Mono.error(ex);
                });
    }

    // get one booking by pnr - if not found we throw NoSuchElementException so GlobalErrorHandler returns 404+message
    @GetMapping("/booking/{pnr}")
    public Mono<Booking> getBookingByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("booking not found")));
    }

    // bookings for a user by email - returns empty flux if none
    @GetMapping("/booking/email/{email}")
    public Flux<Booking> getBookingsByEmail(@PathVariable String email) {
        return bookingService.getBookingHistoryByEmail(email);
    }
}
