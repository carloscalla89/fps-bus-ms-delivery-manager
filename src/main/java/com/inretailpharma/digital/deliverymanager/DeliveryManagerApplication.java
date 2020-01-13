package com.inretailpharma.digital.deliverymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Clase principal, anotada como SpringBootApplication
 *
 * @author : Carlos calla
 */
@SpringBootApplication
@EnableSwagger2
public class DeliveryManagerApplication {

    public static void main( String[] args ){
        SpringApplication.run(DeliveryManagerApplication.class, args);
    }
}
