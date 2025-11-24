package com.flightapp.controller;

import com.flightapp.exception.GlobalErrorHandler;
import com.flightapp.model.Flight;
import com.flightapp.dto.request.AddFlightRequest;
import com.flightapp.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import com.flightapp.exception.GlobalErrorHandler;


@WebFluxTest(controllers = FlightController.class)
@AutoConfigureWebTestClient
@Import(GlobalErrorHandler.class)
class FlightControllerTest {

    @Autowired
    private WebTestClient web;

    @MockBean
    private FlightService flightService;

    @Test
    void addInventory_duplicateFlight_returns400() {
        AddFlightRequest req = new AddFlightRequest();
        req.setAirline("Indigo");
        req.setFlightNumber("FHYD1");
        req.setFromPlace("Hyderabad");
        req.setToPlace("Chennai");
        req.setDepartureDateTime("2025-12-01T10:00:00");
        req.setArrivalDateTime("2025-12-01T12:00:00");
        req.setPrice(5000f);
        req.setTotalSeats(150);

        when(flightService.existsByFlightNumber("FHYD1")).thenReturn(Mono.just(true));

        web.post().uri("/api/flight/airline/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("flight already exists");
    }

    @Test
    void addInventory_success_returns201() {
        AddFlightRequest req = new AddFlightRequest();
        req.setAirline("AirIndia");
        req.setFlightNumber("FMUM1");
        req.setFromPlace("Mumbai");
        req.setToPlace("Bengaluru");
        req.setDepartureDateTime("2025-12-10T09:00:00");
        req.setArrivalDateTime("2025-12-10T10:30:00");
        req.setPrice(3000f);
        req.setTotalSeats(180);

        Flight saved = new Flight();
        saved.setId("id-mum-001");

        when(flightService.existsByFlightNumber("FMUM1")).thenReturn(Mono.just(false));
        when(flightService.createFlight(any(Flight.class))).thenReturn(Mono.just(saved));

        web.post().uri("/api/flight/airline/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("id-mum-001");
    }

    @Test
    void listAllFlights_returnsFlux() {
        Flight f1 = new Flight(); f1.setId("1"); f1.setFlightNumber("DEL-HYD-1");
        Flight f2 = new Flight(); f2.setId("2"); f2.setFlightNumber("MAA-BLR-2");

        when(flightService.getAllFlights()).thenReturn(Flux.just(f1, f2));

        web.get().uri("/api/flight/airline/inventory")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Flight.class)
                .hasSize(2);
    }

    @Test
    void getFlightById_found_returns200() {
        Flight f = new Flight(); f.setId("1"); f.setFlightNumber("DEL-101");

        when(flightService.getFlightById("1")).thenReturn(Mono.just(f));

        web.get().uri("/api/flight/airline/inventory/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    void getFlightById_notFound_returns404() {
        when(flightService.getFlightById("no")).thenReturn(Mono.empty());

        web.get().uri("/api/flight/airline/inventory/no")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void searchByFlightNumber_found_returns200() {
        Flight f = new Flight(); f.setId("x"); f.setFlightNumber("S1");

        when(flightService.findByFlightNumberMono("S1")).thenReturn(Mono.just(f));

        web.get().uri(uriBuilder -> uriBuilder.path("/api/flight/airline/inventory/search")
                        .queryParam("flightNumber", "S1").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.flightNumber").isEqualTo("S1");
    }

    @Test
    void searchByFlightNumber_notFound_returns404() {
        when(flightService.findByFlightNumberMono("xxx")).thenReturn(Mono.empty());

        web.get().uri(uriBuilder -> uriBuilder.path("/api/flight/airline/inventory/search")
                        .queryParam("flightNumber", "xxx").build())
                .exchange()
                .expectStatus().isNotFound();
    }
}
