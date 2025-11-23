package com.flightapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Booking document stored in Mongo.
 * Keeps passenger details and reference to flight.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;        // internal Mongo id

    private String pnr;      // generated PNR (returned to client)
    private String flightId; // reference to flight.id
    private int seatsBooked;

    // passenger info captured at booking time
    private String name;
    private String email;
    private String gender;
    private String mealPreference;

    private Instant createdAt;
}
