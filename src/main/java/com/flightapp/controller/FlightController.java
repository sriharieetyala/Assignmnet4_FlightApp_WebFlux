package com.flightapp.controller;

import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.model.Flight;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/flight/airline/inventory")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private Validator validator;

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> addInventory(@RequestBody AddFlightRequest req) {
        // defensive: repository.existsByFlightNumber may be reactive; call and map
        return flightRepository.existsByFlightNumber(req.getFlightNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(ResponseEntity
                                .badRequest()
                                .body(Map.of("message", "flight already exists")));
                    }

                    // validation...
                    Set<ConstraintViolation<AddFlightRequest>> violations = validator == null ? null : validator.validate(req);
                    if (violations != null && !violations.isEmpty()) {
                        String msg = violations.iterator().next().getMessage();
                        return Mono.just(ResponseEntity
                                .badRequest()
                                .body(Map.of("message", msg)));
                    }

                    Flight flight = toFlight(req);

                    return flightService.createFlight(flight)
                            .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                                    .body(Map.of("id", saved.getId())));
                });

    }

    @GetMapping
    public Flux<Flight> listAll() {
        return flightService.getAllFlights();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Flight>> getById(@PathVariable String id) {
        return flightService.getFlightById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Flight>> searchByFlightNumberParam(@RequestParam String flightNumber) {
        return flightService.findByFlightNumberMono(flightNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private Flight toFlight(AddFlightRequest req) {
        Flight f = new Flight();
        f.setAirline(req.getAirline());
        f.setFlightNumber(req.getFlightNumber());
        f.setFromPlace(req.getFromPlace());
        f.setToPlace(req.getToPlace());
        f.setDepartureDateTime(req.getDepartureDateTime());
        f.setArrivalDateTime(req.getArrivalDateTime());
        f.setPrice(req.getPrice());
        f.setTotalSeats(req.getTotalSeats());
        f.setAircraft(req.getAircraft());
        return f;
    }
}
