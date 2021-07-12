package com.inretailpharma.digital.deliverymanager.facade;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryManagerFacadeTest {


    @Test
    public void testOnErrorContinue() throws InterruptedException {

        Flux<Integer> fluxFromJust =
                Flux.just(1, 2,3,4,5)
                        .flatMap(val -> {
                            if (val == 3) {
                                return Flux.error(new RuntimeException("Test"));
                            }
                            return Flux.just(val*2);
                        })
                        .onErrorContinue((e,i)->{
                            System.out.println("Error For Item +" + i );
                        }).doOnNext(val -> System.out.println("final Item +" + val ))



                ;
        StepVerifier
                .create(fluxFromJust)
                .expectNext(2,4, 8,10)
                .verifyComplete();
    }
}