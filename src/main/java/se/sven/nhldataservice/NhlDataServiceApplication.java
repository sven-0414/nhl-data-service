package se.sven.nhldataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NHL Data Service Application
  * A Spring Boot application that provides data services for the NHL (National Hockey League).
 * This class serves as the main entry point for the application and automatically configures
 * the Spring Boot environment through the @SpringBootApplication annotation.
 *
 * @author Sven Eriksson
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class NhlDataServiceApplication {
    // Test passes if application context loads successfully
    public static void main(String[] args) {
        SpringApplication.run(NhlDataServiceApplication.class, args);
    }

}
