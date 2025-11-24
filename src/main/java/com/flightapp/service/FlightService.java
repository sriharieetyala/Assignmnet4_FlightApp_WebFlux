
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
    private FlightRepository flightRepository; // I added this to fetch or save flight data

    @Autowired
    private BookingRepository bookingRepository; // I added this to save booking info

    public Mono<Flight> createFlight(Flight flight) {
        // I am making sure available seats has proper value if someone passed zero
        if (flight.getAvailableSeats() == 0) {
            flight.setAvailableSeats(flight.getTotalSeats());
        }
        return flightRepository.save(flight);
    }

    public Mono<Boolean> existsByFlightNumber(String flightNumber) {
        // I am checking if flight number already exists
        return flightRepository.findByFlightNumber(flightNumber)
                .map(f -> true)
                .defaultIfEmpty(false);
    }

    public Flux<Flight> getAllFlights() {
        // I am fetching all flights from database
        return flightRepository.findAll();
    }

    public Mono<Flight> getFlightById(String id) {
        // I am fetching flight using id
        return flightRepository.findById(id);
    }

    public Mono<Flight> findByFlightNumberMono(String flightNumber) {
        // I am fetching one flight using flight number
        return flightRepository.findByFlightNumber(flightNumber);
    }

    public Mono<BookingResponse> bookTicket(String flightId,
                                            int seats,
                                            String name,
                                            String email,
                                            Gender gender,
                                            MealType mealPreference) {

        // I am checking if flight exists first
        return flightRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new java.util.NoSuchElementException("Flight not found")))
                .flatMap(flight -> {

                    // I am checking seat count rules
                    if (seats <= 0) return Mono.error(new IllegalArgumentException("seats must be > 0"));
                    if (flight.getAvailableSeats() < seats) return Mono.error(new IllegalStateException("Not enough seats"));

                    // I am reducing available seats after booking
                    flight.setAvailableSeats(flight.getAvailableSeats() - seats);

                    return flightRepository.save(flight)
                            .flatMap(savedFlight -> {

                                // I am creating new booking object here
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

                                // I am saving the booking and returning only the pnr
                                return bookingRepository.save(booking)
                                        .map(b -> new BookingResponse(b.getPnr()));
                            });
                });
    }

    private String generatePnr() {
        // I made this small helper to generate a simple 6 character pnr
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
