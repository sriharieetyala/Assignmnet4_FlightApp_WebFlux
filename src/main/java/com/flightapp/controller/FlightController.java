package com.flightapp.controller;

import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.dto.repsonse.AddFlightResponse;
import com.flightapp.model.Flight;
import com.flightapp.service.FlightService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/flight/airline/inventory")
public class FlightController {

    @Autowired
    private FlightService flightService; // I added this to call all flight related service work

    @Autowired
    private Validator validator; // I added this to check request fields before saving

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddFlightResponse> addInventory(@Valid @RequestBody AddFlightRequest req) {

        // I am first checking if flight number is already present
        return flightService.existsByFlightNumber(req.getFlightNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("flight already exists"));
                    }

                    // I am doing extra validation to show clear message if request has wrong data
                    Set<ConstraintViolation<AddFlightRequest>> violations = validator.validate(req);
                    if (!violations.isEmpty()) {
                        ConstraintViolation<AddFlightRequest> v = violations.iterator().next();
                        String msg = v.getPropertyPath() + " " + v.getMessage();
                        return Mono.error(new IllegalArgumentException(msg));
                    }

                    // I am checking that arrival time should be after departure time
                    try {
                        if (req.getDepartureDateTime() != null && req.getArrivalDateTime() != null) {
                            java.time.Instant dep = java.time.Instant.parse(req.getDepartureDateTime());
                            java.time.Instant arr = java.time.Instant.parse(req.getArrivalDateTime());
                            if (!arr.isAfter(dep)) {
                                return Mono.error(new IllegalArgumentException("arrival must be after departure"));
                            }
                        } else {
                            return Mono.error(new IllegalArgumentException("invalid departure/arrival datetime"));
                        }
                    } catch (Exception e) {
                        return Mono.error(new IllegalArgumentException("invalid departure/arrival datetime"));
                    }

                    // I am creating flight object and copying all fields into it
                    Flight f = new Flight();
                    f.setAirline(req.getAirline());
                    f.setFlightNumber(req.getFlightNumber());
                    f.setFromPlace(req.getFromPlace());
                    f.setToPlace(req.getToPlace());
                    f.setDepartureDateTime(req.getDepartureDateTime());
                    f.setArrivalDateTime(req.getArrivalDateTime());
                    f.setPrice(req.getPrice());
                    f.setTotalSeats(req.getTotalSeats());
                    f.setAvailableSeats(req.getTotalSeats()); // I set available seats same as total in start
                    f.setAircraft(req.getAircraft());

                    // I am saving the flight and returning only the id to client
                    return flightService.createFlight(f)
                            .map(saved -> new AddFlightResponse(saved.getId()));
                });
    }

    @GetMapping
    public Flux<Flight> listAll() {
        // I added this to show all flights in database
        return flightService.getAllFlights();
    }

    @GetMapping("/{id}")
    public Mono<Flight> getById(@PathVariable String id) {
        // I am finding one flight using id and returning error if not found
        return flightService.getFlightById(id)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

    @GetMapping(params = "flightNumber")
    public Mono<Flight> searchByFlightNumberParam(@RequestParam String flightNumber) {
        // I added this to search flight using flight number
        return flightService.findByFlightNumberMono(flightNumber)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

}
