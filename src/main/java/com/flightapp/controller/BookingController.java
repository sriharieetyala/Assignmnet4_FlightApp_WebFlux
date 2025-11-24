package com.flightapp.controller;

import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.dto.request.BookingRequest;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/*
 I keep controller small and do only common-sense validation here:
  - seats must be > 0
  - email/name basic non-empty etc come from @Valid on DTO
  - service still does authoritative checks (available seats etc)
*/
@RestController
@RequestMapping("/api/flight/airline/inventory")
@Validated
public class BookingController {

    @Autowired
    private FlightService flightService; // your service generates PNR and does booking

    @Autowired
    private BookingService bookingService; // kept for any read actions (not used here necessarily)

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookingResponse> bookTicket(@Valid @RequestBody BookingRequest req) {
        // common-sense check early to produce 400 for bad input (tests expect 400)
        if (req.getSeats() <= 0) {
            return Mono.error(new IllegalArgumentException("seats must be > 0"));
        }
        // delegate to FlightService. FlightService will throw IllegalStateException when seats insufficient,
        // which GlobalErrorHandler maps to 400.
        return flightService.bookTicket(
                req.getFlightId(),
                req.getSeats(),
                req.getName(),
                req.getEmail(),
                req.getGender(),
                req.getMealPreference()
        );
    }
}
