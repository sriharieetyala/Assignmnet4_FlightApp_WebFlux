package com.flightapp.controller;

import com.flightapp.dto.repsonse.AddFlightResponse;
import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.dto.request.BookingRequest;
import com.flightapp.model.Booking;
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
    // field injection
    @Autowired
    private FlightService flightService;

    //Adding flight
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
    // list all flights
    @GetMapping("/inventory")
    public Flux<Flight> listAllFlights() {
        // returns empty list if none
        return flightService.getAllFlights();
    }

    // get flight by id
    @GetMapping("/inventory/{id}")
    public Mono<ResponseEntity<Flight>> getFlightById(@PathVariable String id) {
        // return 200 with flight or 404 if not found
        return flightService.getFlightById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // search by flight number
    @GetMapping("/inventory/search")
    public Mono<ResponseEntity<Flight>> searchByFlightNumber(@RequestParam("flightNumber") String flightNumber) {
        // returns 200 with flight or 404 if not found
        return flightService.findByFlightNumberMono(flightNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
}

    @PostMapping("/inventory/book")
    public Mono<ResponseEntity<Map<String,String>>> bookTicket(@RequestBody BookingRequest req) {
        return flightService.bookTicket(
                        req.getFlightId(),
                        req.getSeats(),
                        req.getName(),
                        req.getEmail(),
                        req.getGender(),
                        req.getMealPreference()
                )
                .flatMap(br -> {
                    Map<String,String> body = new HashMap<>();
                    body.put("pnr", br.getPnr());            // only pnr as requested
                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body));
                })
                .onErrorResume(err -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("message", err.getMessage());   // simple error message
                    return Mono.just(ResponseEntity.badRequest().body(body));
                });
    }

}



