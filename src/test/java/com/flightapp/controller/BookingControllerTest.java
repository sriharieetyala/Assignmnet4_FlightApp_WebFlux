package com.flightapp.controller;

import com.flightapp.dto.repsonse.BookingResponse;
import com.flightapp.dto.request.BookingRequest;
import com.flightapp.enums.Gender;
import com.flightapp.enums.MealType;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;
import com.flightapp.GlobalErrorHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

@WebFluxTest(controllers = BookingController.class)
@AutoConfigureWebTestClient
@Import(GlobalErrorHandler.class)
class BookingControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    FlightService flightService;

    @MockBean
    BookingService bookingService;

    @Test
    void bookTicket_success_returns201() {
        BookingRequest req = new BookingRequest(
                "FL123", 2, "John Doe", "john@example.com", Gender.MALE, MealType.VEG
        );

        BookingResponse resp = new BookingResponse("PNR-ABC-123");

        Mockito.when(flightService.bookTicket(
                Mockito.eq(req.getFlightId()),
                Mockito.eq(req.getSeats()),
                Mockito.eq(req.getName()),
                Mockito.eq(req.getEmail()),
                Mockito.eq(req.getGender()),
                Mockito.eq(req.getMealPreference())
        )).thenReturn(Mono.just(resp));

        webTestClient.post()
                .uri("/api/flight/airline/inventory/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("PNR-ABC-123");
    }

    @Test
    void bookTicket_notEnoughSeats_returns400() {
        BookingRequest req = new BookingRequest(
                "FL123", 5, "Jane Doe", "jane@example.com", Gender.FEMALE, MealType.NONVEG
        );

        Mockito.when(flightService.bookTicket(
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(Mono.error(new IllegalStateException("Not enough seats")));

        webTestClient.post()
                .uri("/api/flight/airline/inventory/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Not enough seats");
    }
}
