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
    private CancelService cancelService;

    private boolean isValidPnr(String pnr) {
        if (pnr == null) return false;
        return pnr.matches("^[A-Z0-9\\-]{3,20}$");
    }

    @DeleteMapping("/booking/{pnr}")
    public Mono<Map<String, String>> cancelBooking(@PathVariable String pnr) {

        if (!isValidPnr(pnr)) {
            return Mono.error(new IllegalArgumentException("Invalid PNR"));
        }

        // call service and if nothing returned -> convert to NotFound so GlobalErrorHandler returns 404 with message
        return cancelService.cancelBooking(pnr)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("pnr not found")))
                .map(msg -> {
                    Map<String, String> out = new HashMap<>();
                    out.put("message", msg);
                    return out;
                });
    }
}
