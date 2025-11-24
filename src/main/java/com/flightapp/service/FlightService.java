package com.flightapp.service;

import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.flightapp.model.Booking;
import com.flightapp.model.Flight;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Random;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Mono<Flight> createFlight(Flight flight) {
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

    public Flux<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Mono<Flight> getFlightById(String id) {
        return flightRepository.findById(id);
    }

    public Mono<Flight> findByFlightNumberMono(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    public Mono<BookingResponse> bookTicket(String flightId,
                                            int seats,
                                            String name,
                                            String email,
                                            Gender gender,
                                            MealType mealPreference) {

        return flightRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("Flight not found")))
                .flatMap(flight -> {

                    if (seats <= 0) return Mono.error(new IllegalArgumentException("seats must be > 0"));
                    if (flight.getAvailableSeats() < seats) return Mono.error(new IllegalStateException("Not enough seats"));

                    flight.setAvailableSeats(flight.getAvailableSeats() - seats);

                    return flightRepository.save(flight)
                            .flatMap(savedFlight -> {

                                Booking booking = new Booking();
                                booking.setPnr(generatePnr());
                                booking.setFlightId(savedFlight.getId());
                                booking.setSeatsBooked(seats);
                                booking.setName(name);
                                booking.setEmail(email);
                                booking.setGender(gender);
                                booking.setMealPreference(mealPreference);
                                booking.setCreatedAt(Instant.now());
                                booking.setStatus(BookingStatus.BOOKED);

                                return bookingRepository.save(booking)
                                        .map(b -> new BookingResponse(b.getPnr()));
                            });
                });
    }

    private String generatePnr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
