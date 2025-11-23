package com.flightapp.repository;

import com.flightapp.model.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {
    Mono<Booking> findByPnr(String pnr);
}
