package com.Flightapp.service;

import com.Flightapp.model.Flight;
import com.Flightapp.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
}
