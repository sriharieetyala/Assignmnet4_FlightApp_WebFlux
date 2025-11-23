package com.flightapp.dto;

import com.flightapp.dto.repsonse.BookingResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingResponseDtoTest {

    @Test
    void bookingResponse_getter_setter() {
        BookingResponse br = new BookingResponse("PNR1");
        assertEquals("PNR1", br.getPnr());
        br.setPnr("PNR2");
        assertEquals("PNR2", br.getPnr());
        assertTrue(br.toString().contains("PNR2"));
    }
}
