package se.sven.nhldataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NhlDataServiceApplication {
    // Test passes if application context loads successfully
    public static void main(String[] args) {
        SpringApplication.run(NhlDataServiceApplication.class, args);
    }

}
