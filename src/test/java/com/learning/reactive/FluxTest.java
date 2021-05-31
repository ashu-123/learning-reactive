package com.learning.reactive;

import org.bson.io.BsonOutput;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FluxTest {

    private List<String> getList() {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        return list;
    }

    private List<String> getId() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        list.add("10");
        list.add("11");
        return list;
    }

    private Mono<String> findEmployee(String empId) {
        Map<String, String> employees = new HashMap<>();
        employees.put("1", "Ashutosh");
        employees.put("2", "Anurag");
        employees.put("3", "Kushagra");
        employees.put("4", "Prathamesh");
        employees.put("5", "Prathames");
        employees.put("6", "Prathame");
        employees.put("7", "Pratham");
        employees.put("8", "Pratha");
        employees.put("9", "Prath");
        employees.put("10", "Prat");
        employees.put("11", "Pra");

        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }
        return Mono.just(employees.computeIfAbsent(empId, (id) -> "Default")).delayElement(Duration.ofSeconds(1));
    }

    @Test
    void fluxTest0() {
        Flux<String> letters = Flux.just("A", "B", "C");
        letters.subscribe(System.out::println, System.out::println, () -> System.out.println("Completed"));
    }

    @Test
    void fluxTest1() {
        Flux<String> letters = Flux.just("A", "B", "C").concatWith(Flux.error(new RuntimeException("Some exception occured")));
        letters.onErrorReturn("ERROR").log().subscribe(System.out::println, System.out::println);
    }

    @Test
    void fluxTest2() {

        Flux<String> letters = Flux.fromIterable(getList()).concatWith(Flux.error(new RuntimeException("Some exception occured")));
        letters.log().subscribe(System.out::println, System.out::println);
    }


    @Test
    void fluxTest3() {
        Flux<String> letters = Flux.fromStream(() -> Stream.of("A", "B", "D"));
        letters.log().subscribe(System.out::println, System.out::println, () -> {
        });

        Duration duration = StepVerifier.create(letters)
                .expectNext("A", "B", "D")
                .expectComplete()
                .verify();

        System.out.println(duration);
    }

    @Test
    void fluxTest4() {
        Flux<String> letters = Flux.fromStream(Stream.of("A", "B", "D"));
        letters.log().subscribe(System.out::println, System.out::println);
    }

    @Test
    void fluxTest5() {

        Flux<String> letters = Flux.fromIterable(getList());
        Flux<String> filteredLetters = letters.filter(letter -> letter.equals("A") || letter.equals("B"));
        filteredLetters.doOnNext(System.out::println).subscribe();

    }

    @Test
    void fluxTest6() {

        Flux<String> employeeName = Flux.fromIterable(getId())
                .flatMap(this::findEmployee).log();

        StepVerifier.create(employeeName)
                .expectNextCount(11)
                .verifyComplete();

    }


    @Test
    void fluxTest6WithParallelSchedulers() {

        Flux<String> employeeName = Flux.fromIterable(getId())
                .window(2)
                .flatMap(ids -> ids.flatMap(this::findEmployee).subscribeOn(Schedulers.parallel())).log();

        StepVerifier.create(employeeName)
                .expectNextCount(11)
                .verifyComplete();

    }

    @Test
    void fluxTest7merge() {

        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("X", "Y", "Z");

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux)
                .expectNext("A", "B", "C", "X", "Y", "Z")
                .verifyComplete();

    }


    @Test
    void fluxTest7mergeWithDelay() {

        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("X", "Y", "Z").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.merge(flux1, flux2).log();

        StepVerifier.create(mergedFlux)
                .expectNextCount(6)
                .verifyComplete();

    }

    @Test
    void fluxTest8zip() {

        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("X", "Y", "Z").delayElements(Duration.ofSeconds(1));

        Flux<String> zippedFlux = Flux.zip(flux1, flux2).log().map(t -> t.getT1() + t.getT2());

        StepVerifier.create(zippedFlux).expectNext("AX", "BY", "CZ")
                .verifyComplete();

    }

    @Test
    void fluxTest8concatWith() {

        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("X", "Y", "Z").delayElements(Duration.ofSeconds(1));

        Flux<String> zippedFlux = Flux.concat(flux1, flux2).log();

        StepVerifier.create(zippedFlux).expectNext("A", "B", "C", "X", "Y", "Z")
                .verifyComplete();

    }

    @Test
    void fluxErrorTest() {
        Flux<String> flux = Flux.just("A", "B", "C").concatWith(Flux.error(new RuntimeException("Error occurred"))).log();

        StepVerifier.create(flux)
                .expectNext("A", "B", "C")
                .consumeErrorWith(err -> assertThat(err.getMessage()).isEqualTo("Error occurred"))
                .verify();
    }

    @Test
    void fluxErrorTestOnErrorReturn() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .onErrorReturn("Error Returned").log();

        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "Error Returned")
                .verifyComplete();
    }

    @Test
    void fluxErrorTestOnDoOnError() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .doOnError(err -> System.out.println("Some error occurred")).log();

        StepVerifier.create(flux)
                .expectNext("A", "B", "C")
                .expectError()
                .verify();
    }

    @Test
    void fluxErrorTestOnErrorResume() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .onErrorResume(err -> Flux.just("On error resume")).log();

        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "On error resume")
                .verifyComplete();
    }

    @Test
    void fluxErrorTestOnErrorMap() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .onErrorMap(err -> err.getMessage().equals("Error occurred") ? new CustomException("Translated exception") : err).log();

        StepVerifier.create(flux)
                .expectNext("A", "B", "C")
                .consumeErrorWith(err -> assertThat(err).isInstanceOf(CustomException.class))
                .verify();
    }

    @Test
    void fluxErrorTestOndoFinally() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error occurred")))
                .onErrorMap(err -> err.getMessage().equals("Error occurred") ? new CustomException("Translated exception") : err)
                .log().doFinally((x) -> {
                    if (x == SignalType.ON_ERROR) {
                        System.out.println("error occured");
                    }
                });

        StepVerifier.create(flux)
                .expectNext("A", "B", "C")
                .consumeErrorWith(err -> assertThat(err).isInstanceOf(CustomException.class))
                .verify();
    }


    @Test
    void fluxErrorTestFinallyWithCancel() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .doFinally((x) -> {
                    if (x == SignalType.ON_ERROR) {
                        System.out.println("error occured");
                    } else if (x == SignalType.CANCEL) {
                        System.out.println("cancelled");
                    }
                }).take(2).log();

        StepVerifier.create(flux)
                .expectNext("A", "B")

                .verifyComplete();
    }
}
