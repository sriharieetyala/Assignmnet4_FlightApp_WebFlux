package com.flightapp.controller;

import com.flightapp.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for cancel endpoints.
 * Let the GlobalErrorHandler handle exceptions and return correct HTTP statuses.
 */
@RestController
@RequestMapping("/api/flight/airline")
public class CancelController {

    @Autowired
    private CancelService cancelService;

    @DeleteMapping("/inventory/booking/{pnr}")
    public Mono<ResponseEntity<Map<String,String>>> cancelBooking(@PathVariable String pnr) {

        return cancelService.cancelBooking(pnr)
                .map(msg -> {
                    Map<String,String> body = new HashMap<>();
                    body.put("message", msg);
                    return ResponseEntity.ok(body);  // 200
                });
        // removed onErrorResume here: let GlobalErrorHandler map exceptions to 400/404/500
    }
}
