package com.flightapp.repository;

import com.flightapp.model.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

// simple reactive repo for flights
public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {
    Mono<Flight> findByFlightNumber(String flightNo);
}
