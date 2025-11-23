package com.flightapp.controller;

import com.flightapp.dto.request.BookingRequest;
import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.model.Booking;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flight/airline")
public class BookingController {

    @Autowired
    private FlightService flightService; // used for booking helper that lives in FlightService (if applicable)

    @Autowired
    private BookingService bookingService;

    // POST book ticket -> returns { "pnr": "ABC123" } with 201, or { "message": "..." } with 400
    @PostMapping("/inventory/book")
    public Mono<ResponseEntity<Map<String, String>>> bookTicket(@Valid @RequestBody BookingRequest req) {
        return flightService.bookTicket(
                        req.getFlightId(),
                        req.getSeats(),
                        req.getName(),
                        req.getEmail(),
                        req.getGender(),
                        req.getMealPreference())
                .flatMap(br -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("pnr", br.getPnr());
                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body));
                })
                .onErrorResume(err -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("message", err.getMessage() != null ? err.getMessage() : "error");
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
                });
    }

    // GET booking by PNR -> returns booking JSON or 404, 400 on error
    @GetMapping(value = "/inventory/booking/{pnr}", produces = "application/json")
    public Mono<ResponseEntity<Object>> getBookingByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .map(b -> ResponseEntity.ok((Object) b))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(err -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("message", err.getMessage() != null ? err.getMessage() : "error");
                    return Mono.just(ResponseEntity.badRequest().body((Object) body));
                });
    }



    @GetMapping("/inventory/bookings")
    public Mono<ResponseEntity<Object>> getBookingHistory(@RequestParam String email) {

        return bookingService.getBookingHistoryByEmail(email)
                .collectList()                                     // turn Flux → Mono<List>
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.ok((Object) list));
                })
                .onErrorResume(err -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("message", err.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body((Object) body));
                });
    }

}
