package com.flightapp.controller;

import com.flightapp.GlobalErrorHandler;
import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = FlightController.class)
@AutoConfigureWebTestClient
@Import(GlobalErrorHandler.class)
class FlightControllerValidationTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    FlightService flightService;

    @Test
    void addFlight_withInvalidRequest_returns500_withServerMessage() {
        AddFlightRequest invalid = new AddFlightRequest(
                "AirlineX",
                "FN-1",
                "SFO",
                "LAX",
                "2025-12-01T10:00:00Z",
                "2025-12-01T12:00:00Z",
                -50.0f,
                0,
                "A320"
        );

        // Mock the service to throw; current GlobalErrorHandler returns a generic 500 message.
        Mockito.when(flightService.createFlight(Mockito.any()))
                .thenReturn(Mono.error(new IllegalArgumentException("invalid flight")));

        webTestClient.post()
                .uri("/api/flight/airline/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("something went wrong on server");
    }
}
