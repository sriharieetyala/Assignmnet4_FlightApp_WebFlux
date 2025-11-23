package com.flightapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalErrorHandler {

    @RestControllerAdvice
    public class ValidationAdvice {

        @ExceptionHandler(WebExchangeBindException.class)
        public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "validation failed");
            body.put("errors", ex.getFieldErrors().stream()
                    .map(e -> Map.of("field", e.getField(), "error", e.getDefaultMessage()))
                    .collect(Collectors.toList()));
            return Mono.just(ResponseEntity.badRequest().body(body));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public Mono<ResponseEntity<Map<String,String>>> handleIllegalArg(IllegalArgumentException ex) {
            Map<String,String> body = Map.of("message", ex.getMessage());
            return Mono.just(ResponseEntity.badRequest().body(body));
        }
    }
}
