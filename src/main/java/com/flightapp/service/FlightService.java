package com.flightapp.service;

import com.flightapp.model.Flight;
import com.flightapp.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class FlightService {
    @Autowired
    private FlightRepository flightRepository;

    public Mono<Flight> createFlight(Flight flight) {
        // set availableSeats from totalSeats if not set
        if (flight.getAvailableSeats() == 0) {
            flight.setAvailableSeats(flight.getTotalSeats());
        }
        return flightRepository.save(flight);
    }

    public Mono<Boolean> existsByFlightNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .map(f -> true)
                .defaultIfEmpty(false);
    }

    // return all flights
    public Flux<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    // find flight by id
    public Mono<Flight> getFlightById(String id) {
        return flightRepository.findById(id);
    }

    // find flight by flightNumber
    public Mono<Flight> findByFlightNumberMono(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }
}
