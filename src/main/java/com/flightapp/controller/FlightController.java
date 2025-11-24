package com.flightapp.controller;

import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.dto.repsonse.AddFlightResponse;
import com.flightapp.model.Flight;
import com.flightapp.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/*
 Controller: readable validation that returns Mono.error(...) with clear messages.
 GlobalErrorHandler will convert those to JSON { "message": "..." } and correct HTTP code.
 */
@RestController
@RequestMapping("/api/flight/airline/inventory")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Mono<AddFlightResponse> addInventory(@RequestBody AddFlightRequest req) {
        // existence check first (tests mock this)
        return flightService.existsByFlightNumber(req.getFlightNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("flight already exists"));
                    }

                    // basic checks
                    if (req.getTotalSeats() <= 0) {
                        return Mono.error(new IllegalArgumentException("totalSeats must be > 0"));
                    }
                    if (req.getPrice() <= 0) {
                        return Mono.error(new IllegalArgumentException("price must be > 0"));
                    }

                    // parse departure/arrival flexibly
                    Instant depInstant = null;
                    Instant arrInstant = null;
                    try {
                        if (req.getDepartureDateTime() != null) {
                            depInstant = parseToInstant(req.getDepartureDateTime());
                        }
                        if (req.getArrivalDateTime() != null) {
                            arrInstant = parseToInstant(req.getArrivalDateTime());
                        }
                        if (depInstant != null && arrInstant != null && !arrInstant.isAfter(depInstant)) {
                            return Mono.error(new IllegalArgumentException("arrival must be after departure"));
                        }
                    } catch (IllegalArgumentException iae) {
                        return Mono.error(iae);
                    } catch (Exception e) {
                        return Mono.error(new IllegalArgumentException("invalid departure/arrival datetime"));
                    }

                    Flight f = new Flight();
                    f.setAirline(req.getAirline());
                    f.setFlightNumber(req.getFlightNumber());
                    f.setFromPlace(req.getFromPlace());
                    f.setToPlace(req.getToPlace());
                    f.setDepartureDateTime(req.getDepartureDateTime());
                    f.setArrivalDateTime(req.getArrivalDateTime());
                    f.setPrice(req.getPrice());
                    f.setTotalSeats(req.getTotalSeats());

                    // set availableSeats from totalSeats for a new flight
                    f.setAvailableSeats(req.getTotalSeats());

                    f.setAircraft(req.getAircraft());

                    return flightService.createFlight(f)
                            .map(saved -> new AddFlightResponse(saved.getId()));
                });
    }

    @GetMapping
    public reactor.core.publisher.Flux<Flight> listAll() {
        return flightService.getAllFlights();
    }

    @GetMapping("/{id}")
    public Mono<Flight> getById(@PathVariable String id) {
        return flightService.getFlightById(id)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

    @GetMapping("/search")
    public Mono<Flight> searchByFlightNumber(@RequestParam String flightNumber) {
        return flightService.findByFlightNumberMono(flightNumber)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

    // Accepts either Instant.parse("...Z") or LocalDateTime like "2025-12-10T09:00:00"
    private Instant parseToInstant(String value) {
        try {
            // try strict instant first (with Z or offset)
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
        }

        try {
            // try local date-time, assume UTC (tests are not timezone-sensitive)
            LocalDateTime ldt = LocalDateTime.parse(value);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
        }

        // no known format
        throw new IllegalArgumentException("unparseable datetime: " + value);
    }
}
