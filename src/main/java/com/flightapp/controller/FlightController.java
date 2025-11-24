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
    private FlightService flightService;

    @Autowired
    private Validator validator; // injected by Spring Boot

    /**
     * Create flight inventory.
     * - Validates bean constraints (@Valid + programmatic check to ensure clear message)
     * - Checks duplicate flight number via service
     * - Business checks: positive seats/price, arrival after departure
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddFlightResponse> addInventory(@Valid @RequestBody AddFlightRequest req) {

        return flightService.existsByFlightNumber(req.getFlightNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("flight already exists"));
                    }

                    // programmatic bean validation (keeps message style consistent)
                    Set<ConstraintViolation<AddFlightRequest>> violations = validator.validate(req);
                    if (!violations.isEmpty()) {
                        ConstraintViolation<AddFlightRequest> v = violations.iterator().next();
                        String msg = v.getPropertyPath() + " " + v.getMessage();
                        return Mono.error(new IllegalArgumentException(msg));
                    }

                    // additional business checks (arrival after departure)
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

                    Flight f = new Flight();
                    f.setAirline(req.getAirline());
                    f.setFlightNumber(req.getFlightNumber());
                    f.setFromPlace(req.getFromPlace());
                    f.setToPlace(req.getToPlace());
                    f.setDepartureDateTime(req.getDepartureDateTime());
                    f.setArrivalDateTime(req.getArrivalDateTime());
                    f.setPrice(req.getPrice());
                    f.setTotalSeats(req.getTotalSeats());
                    f.setAvailableSeats(req.getTotalSeats()); // initial available seats = total
                    f.setAircraft(req.getAircraft());

                    return flightService.createFlight(f)
                            .map(saved -> new AddFlightResponse(saved.getId()));
                });
    }

    /**
     * List all flights
     * GET /api/flight/airline/inventory
     */
    @GetMapping
    public Flux<Flight> listAll() {
        return flightService.getAllFlights();
    }

    /**
     * Get flight by id
     * GET /api/flight/airline/inventory/{id}
     * returns 404 via GlobalErrorHandler if not found (service returns Mono.empty())
     */
    @GetMapping("/{id}")
    public Mono<Flight> getById(@PathVariable String id) {
        return flightService.getFlightById(id)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

    /**
     * Search by flight number
     * GET /api/flight/airline/inventory/search?flightNumber=XYZ
     */

    // add to FlightController
    @GetMapping(params = "flightNumber")
    public Mono<Flight> searchByFlightNumberParam(@RequestParam String flightNumber) {
        return flightService.findByFlightNumberMono(flightNumber)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("flight not found")));
    }

}
