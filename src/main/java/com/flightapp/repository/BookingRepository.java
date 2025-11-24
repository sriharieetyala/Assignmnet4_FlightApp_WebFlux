
package com.flightapp.repository;

import com.flightapp.model.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {

    // find a booking by PNR (unique)
    Mono<Booking> findByPnr(String pnr);

    // find bookings by email (possibly many)
    Flux<Booking> findByEmail(String email);
}
