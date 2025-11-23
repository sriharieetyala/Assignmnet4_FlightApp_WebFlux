package com.flightapp.controller;

import com.flightapp.dto.request.BookingRequest;
import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.flightapp.service.FlightService;
import com.flightapp.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private WebTestClient web;

    @MockBean
    private FlightService flightService;

    @MockBean
    private BookingService bookingService;

    @Test
    void bookTicket_success_returns201WithPnr() {
        BookingResponse br = new BookingResponse("ABC123");

        // If FlightService.bookTicket now expects enums, pass enums here in Mockito stub
        when(flightService.bookTicket(anyString(), anyInt(),
                anyString(), anyString(), any(Gender.class), any(MealType.class)))
                .thenReturn(Mono.just(br));

        BookingRequest req = new BookingRequest();
        req.setFlightId("fid1");
        req.setSeats(1);
        req.setName("Sri");
        req.setEmail("sri@gmail.com");
        // If your controller expects strings and converts to enums, send strings. If controller already expects enums in DTO, set enums.
        req.setGender(Gender.MALE);
        req.setMealPreference(MealType.VEG);

        web.post()
                .uri("/api/flight/airline/inventory/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("ABC123");
    }

    @Test
    void bookTicket_notEnoughSeats_returns400() {
        when(flightService.bookTicket(anyString(), anyInt(),
                anyString(), anyString(), any(Gender.class), any(MealType.class)))
                .thenReturn(Mono.error(new IllegalStateException("Not enough seats")));

        BookingRequest req = new BookingRequest();
        req.setFlightId("fid1");
        req.setSeats(10);
        req.setName("A");
        req.setEmail("a@x.com");
        req.setGender(Gender.MALE);
        req.setMealPreference(MealType.NONVEG);

        web.post().uri("/api/flight/airline/inventory/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Not enough seats");
    }
}
