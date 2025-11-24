package com.flightapp.controller;

import com.flightapp.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/*
 Human-first PNR check — returns Mono.error(new IllegalArgumentException(...))
 so GlobalErrorHandler will convert it to 400 + {"message": "..."}.
*/
@RestController
@RequestMapping("/api/flight/airline/inventory")
public class CancelController {

    @Autowired
    private CancelService cancelService;

    // Conservative PNR rule: uppercase letters, digits, hyphen allowed; length 3..20
    private boolean isValidPnr(String pnr) {
        if (pnr == null) return false;
        return pnr.matches("^[A-Z0-9\\-]{3,20}$");
    }

    @DeleteMapping("/booking/{pnr}")
    public Mono<Map<String, String>> cancelBooking(@PathVariable String pnr) {
        if (!isValidPnr(pnr)) {
            // return error that GlobalErrorHandler maps to HTTP 400 with { "message": "Invalid PNR" }
            return Mono.error(new IllegalArgumentException("Invalid PNR"));
        }

        return cancelService.cancelBooking(pnr)
                .map(msg -> {
                    Map<String, String> out = new HashMap<>();
                    out.put("message", msg);
                    return out;
                });
    }
}
