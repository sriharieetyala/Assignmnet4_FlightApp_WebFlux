package com.flightapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
@Component
public class GlobalErrorHandler {
    private ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return ResponseEntity.status(status).body(m);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleBindException(WebExchangeBindException ex) {
        FieldError fe = ex.getFieldErrors().isEmpty() ? null : ex.getFieldErrors().get(0);
        String msg = fe != null ? fe.getField() + " " + fe.getDefaultMessage() : "validation failed";
        return Mono.just(body(HttpStatus.BAD_REQUEST, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleIllegalArg(IllegalArgumentException ex) {
        return Mono.just(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleIllegalState(IllegalStateException ex) {
        return Mono.just(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleNotFound(NoSuchElementException ex) {
        return Mono.just(body(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleAny(Exception ex) {
        ex.printStackTrace();
        return Mono.just(body(HttpStatus.INTERNAL_SERVER_ERROR, "something went wrong on server"));
    }
}
