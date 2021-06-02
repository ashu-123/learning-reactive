package com.learning.reactive.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class DemoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getNumbersTest() {

        Flux<Integer> responseBody = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(6))
                .build()
                .get()
                .uri("/api/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    void getNumberStreamTest() {

        Flux<Integer> responseBody = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(6))
                .build()
                .get()
                .uri("/api/flux/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_STREAM_JSON_VALUE)
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

}