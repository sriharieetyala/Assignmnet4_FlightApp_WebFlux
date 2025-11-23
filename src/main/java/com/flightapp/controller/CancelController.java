package com.flightapp.controller;

import com.flightapp.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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
                })
                .onErrorResume(err -> {
                    Map<String,String> body = new HashMap<>();
                    body.put("message", err.getMessage());

                    if (err.getMessage().equalsIgnoreCase("PNR not found")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)); // 404
                    }

                    return Mono.just(ResponseEntity.badRequest().body(body)); // 400
                });
    }
}
