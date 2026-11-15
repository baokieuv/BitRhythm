package com.bit_rhythm.content_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// core provides ResponseFactory and the GlobalExceptionHandler: it must be scanned (remove it from
// scanBasePackages if core already registers itself the way your other services rely on).
@SpringBootApplication(scanBasePackages = {"com.bit_rhythm.content_service", "com.bit_rhythm.core"})
@ConfigurationPropertiesScan
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
