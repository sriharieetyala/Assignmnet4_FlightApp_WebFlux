package com.flightapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global error handler.
 * - Validation errors (from @Valid) -> 400 with field list
 * - IllegalArgumentException / IllegalStateException -> 400 with message
 * - NoSuchElementException -> 404 with message
 * - Fallback -> 500 generic message
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        List<Map<String, String>> errors = ex.getFieldErrors().stream()
                .map(e -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("field", e.getField());
                    m.put("error", e.getDefaultMessage());
                    return m;
                })
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("message", "validation failed");
        body.put("errors", errors);

        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Mono<ResponseEntity<Map<String, String>>> handleBadRequest(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleNotFound(java.util.NoSuchElementException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleGeneric(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "something went wrong on server");
        // ex.printStackTrace(); // enable for debugging locally
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
