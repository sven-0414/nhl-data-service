package se.sven.nhldataservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NHL Data Service API")
                        .version("1.0.0")
                        .description("REST API for NHL game statistics and data")
                        .contact(new Contact()
                                .name("Sven Eriksson")
                                .email("svenpleriksson@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local development server")
                ));
    }
}