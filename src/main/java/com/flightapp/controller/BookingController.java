package com.flightapp.controller;

import com.flightapp.dto.request.BookingRequest;
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
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    /**
     * Book ticket
     * - returns 201 + { "pnr": "ABC123" } on success
     * - validation errors and business errors are handled by GlobalErrorHandler
     */
    @PostMapping("/inventory/book")
    public Mono<ResponseEntity<Map<String, String>>> bookTicket(@Valid @RequestBody BookingRequest req) {
        return flightService.bookTicket(
                        req.getFlightId(),
                        req.getSeats(),
                        req.getName(),
                        req.getEmail(),
                        req.getGender(),
                        req.getMealPreference()
                )
                .map(br -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("pnr", br.getPnr());
                    return ResponseEntity.status(HttpStatus.CREATED).body(body);
                });
        // no onErrorResume here — let global handler convert exceptions to proper HTTP codes
    }

    /**
     * Get booking history by email
     * - returns 200 with list or 404 if no bookings found
     */
    @GetMapping("/inventory/bookings")
    public Mono<ResponseEntity<Object>> getBookingHistory(@RequestParam String email) {
        return bookingService.getBookingHistoryByEmail(email)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.ok((Object) list));
                });
        // again no local onErrorResume — global handler will manage errors
    }

    /**
     * (You already have a PNR lookup elsewhere; keep it there.)
     */
}
