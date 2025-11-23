package com.Flightapp.controller;

import com.Flightapp.dto.repsonse.AddFlightResponse;
import com.Flightapp.dto.request.AddFlightRequest;
import com.Flightapp.model.Flight;
import com.Flightapp.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flight/airline")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @PostMapping(value = "/inventory")
    public Mono<ResponseEntity<?>> addInventory(@RequestBody AddFlightRequest req) {

        // Check duplicate flightNumber
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
                                return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(resp);
                            });
                });
    }
}