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
    private FlightService flightService; // I added this because booking flow already depends on flight checks

    @Autowired
    private BookingService bookingService; // I added this to fetch booking details and history

    @Autowired
    private Validator validator; // I added this to do simple validation before calling service

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookingResponse> bookTicket(@RequestBody BookingRequest req) {

        // I am checking request data first using validator so wrong data can be stopped early
        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            ConstraintViolation<BookingRequest> v = violations.iterator().next();
            String msg = v.getPropertyPath() + " " + v.getMessage();
            return Mono.error(new IllegalArgumentException(msg));
        }

        // I am sending request data to flightService where seat check and booking logic is already written
        return flightService.bookTicket(
                req.getFlightId(),
                req.getSeats(),
                req.getName(),
                req.getEmail(),
                req.getGender(),
                req.getMealPreference()
        );
    }

    // I wrote this to get one booking using pnr
    // if not found global error handler will send not found status
    @GetMapping("/booking/{pnr}")
    public Mono<Booking> getBookingByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("booking not found")));
    }

    // I added this to get all bookings done by one email id
    @GetMapping("/booking/email/{email}")
    public Flux<Booking> getBookingsByEmail(@PathVariable String email) {
        return bookingService.getBookingHistoryByEmail(email);
    }
}