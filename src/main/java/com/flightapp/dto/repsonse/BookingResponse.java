package com.flightapp.dto.repsonse;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Minimal response that will be returned for a successful booking.
 * Matches the requirement: POST create -> return only the id-like field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String pnr;
}
