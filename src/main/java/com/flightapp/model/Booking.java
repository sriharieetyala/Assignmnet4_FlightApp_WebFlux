package com.flightapp.model;

import com.flightapp.enums.BookingStatus;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
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
    private Gender gender;
    private MealType mealPreference;
    private BookingStatus status;   // new field


    private Instant createdAt;
}
