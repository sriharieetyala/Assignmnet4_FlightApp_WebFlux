package com.Flightapp.model;
// lombok imports
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="flights")
public class Flight {
    @Id
    private String id;

    private String airline;       // airline name
    private String flightNumber;  // unique flight number
    private String fromPlace;
    private String toPlace;
    private String departureDateTime; // ISO string for now
    private String arrivalDateTime;
    private float price; // use float as requested
    private int totalSeats;
    private int availableSeats;
    private String aircraft;
}
