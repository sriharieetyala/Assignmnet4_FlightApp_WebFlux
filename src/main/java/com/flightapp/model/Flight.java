package com.flightapp.model;

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

    private String airline;
    private String flightNumber;
    private String fromPlace;
    private String toPlace;
    private String departureDateTime;
    private String arrivalDateTime;
    private float price;
    private int totalSeats;
    private int availableSeats;
    private String aircraft;
}
