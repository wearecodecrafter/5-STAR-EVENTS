package com.backend;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
        info = @Info(
                title = "Five_Star_Events project REST API Documentation",
                description = "Payroll application REST API Documentation and developed by Tejas Patil",
                version="v1",
                contact = @Contact(
                        name = "Code Crafter Services",
                        email = "info@gmail.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Five Star Event's application REST API Documentation"
        )
)
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class FiveStarBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiveStarBackendApplication.class, args);
    }

}
