package com.Flightapp.repository;

import com.Flightapp.model.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

public interface FlightRepository extends ReactiveMongoRepository<Flight, Integer> {

    Mono<Flight> findByFlightNumber(String flightNo);
}
