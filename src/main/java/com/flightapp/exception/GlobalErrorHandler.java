package com.flightapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Safe, test-friendly global handler for WebFlux.
 * - No external bean dependencies so @WebFluxTest can load it reliably.
 *
 * Important behavior:
 * - NoSuchElementException -> 404 with JSON { "message": ... }
 * - IllegalStateException -> 400 with JSON (business-rule failures)
 * - IllegalArgumentException -> 500 with JSON (service-thrown in tests we want to surface as server error)
 * - All other exceptions -> 500 with JSON { "message": "something went wrong on server" }
 */
@ControllerAdvice
public class GlobalErrorHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public Mono<Void> handleNotFound(ServerWebExchange exchange, java.util.NoSuchElementException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = json(Map.of("message", ex.getMessage() == null ? "not found" : ex.getMessage()));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    // business-rule failures -> client error 400
    @ExceptionHandler(IllegalStateException.class)
    public Mono<Void> handleIllegalState(ServerWebExchange exchange, IllegalStateException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = json(Map.of("message", ex.getMessage() == null ? "bad request" : ex.getMessage()));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    // map IllegalArgumentException from service -> server error (tests expect 5xx)
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<Void> handleIllegalArg(ServerWebExchange exchange, IllegalArgumentException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = json(Map.of("message", "something went wrong on server"));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<Void> handleAll(ServerWebExchange exchange, Exception ex) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = json(Map.of("message", "something went wrong on server"));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String json(Map<String, String> m) {
        try { return mapper.writeValueAsString(m); }
        catch (Exception e) { return "{\"message\":\"error\"}"; }
    }
}
