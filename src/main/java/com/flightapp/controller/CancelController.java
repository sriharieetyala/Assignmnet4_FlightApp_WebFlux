package com.flightapp.controller;

import com.flightapp.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flight/airline/inventory")
public class CancelController {

    @Autowired
    private CancelService cancelService; // I added this to call the cancel logic from service layer

    // I wrote this small check to make sure pnr format is correct before going ahead
    private boolean isValidPnr(String pnr) {
        if (pnr == null) return false;
        return pnr.matches("^[A-Z0-9\\-]{3,20}$");
    }

    @DeleteMapping("/booking/{pnr}")
    public Mono<Map<String, String>> cancelBooking(@PathVariable String pnr) {

        // I am checking pnr format first so wrong pnr can be stopped early
        if (!isValidPnr(pnr)) {
            // I am throwing simple error that handler will convert into bad request message
            return Mono.error(new IllegalArgumentException("Invalid PNR"));
        }

        // I am calling service to cancel booking and then preparing simple output map
        return cancelService.cancelBooking(pnr)
                .map(msg -> {
                    Map<String, String> out = new HashMap<>();
                    out.put("message", msg);
                    return out;
                });
    }
}
