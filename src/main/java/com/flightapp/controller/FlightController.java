package com.flightapp.controller;

import com.flightapp.dto.repsonse.AddFlightResponse;
import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.model.Flight;
import com.flightapp.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flight/airline")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @PostMapping(value = "/inventory")
    public Mono<ResponseEntity<?>> addInventory(@RequestBody AddFlightRequest req) {
        return flightService.existsByFlightNumber(req.getFlightNumber())
                .flatMap(exists -> {
                    if (exists) {
                        Map<String, String> body = new HashMap<>();
                        body.put("message", "flight already exists");
                        return Mono.just(ResponseEntity.badRequest().body(body));
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
                    f.setAvailableSeats(req.getTotalSeats());
                    f.setAircraft(req.getAircraft());

                    return flightService.createFlight(f)
                            .map(saved -> {
                                AddFlightResponse resp = new AddFlightResponse(saved.getId());
                                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
                            });
                });
    }

    @GetMapping("/inventory")
    public Flux<Flight> listAllFlights() {
        return flightService.getAllFlights();
    }

    @GetMapping("/inventory/{id}")
    public Mono<ResponseEntity<Flight>> getFlightById(@PathVariable String id) {
        return flightService.getFlightById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/inventory/search")
    public Mono<ResponseEntity<Flight>> searchByFlightNumber(@RequestParam("flightNumber") String flightNumber) {
        return flightService.findByFlightNumberMono(flightNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
