package com.flightapp.controller;

import com.flightapp.service.CancelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CancelController.class)
class CancelControllerTest {

    @Autowired
    private WebTestClient web;

    @MockBean
    private CancelService cancelService;

    @Test
    void cancelBooking_success_returns200Message() {
        when(cancelService.cancelBooking("PNR1")).thenReturn(Mono.just("Cancelled"));

        web.delete().uri("/api/flight/airline/inventory/booking/PNR1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cancelled");
    }

    @Test
    void cancelBooking_pnrNotFound_returns404() {
        // <-- changed to NoSuchElementException so global handler returns 404
        when(cancelService.cancelBooking("NOPE"))
                .thenReturn(Mono.error(new NoSuchElementException("PNR not found")));

        web.delete().uri("/api/flight/airline/inventory/booking/NOPE")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("PNR not found");
    }

    @Test
    void cancelBooking_badBusinessRule_returns400() {
        when(cancelService.cancelBooking("LATE"))
                .thenReturn(Mono.error(new IllegalStateException("Cannot cancel within 24 hours")));

        web.delete().uri("/api/flight/airline/inventory/booking/LATE")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot cancel within 24 hours");
    }
}
