package com.learning.reactive.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.print.attribute.standard.Media;
import java.awt.*;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/flux")
    private Flux<Integer> getNumbers() {
        return Flux.just(1, 2, 3, 4, 5)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/flux/stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    private Flux<Integer> getNumberStream() {
        return Flux.just(1, 2, 3, 4, 5)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }
}
