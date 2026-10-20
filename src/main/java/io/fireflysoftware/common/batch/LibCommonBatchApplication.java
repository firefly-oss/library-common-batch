package io.fireflysoftware.common.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo Spring Boot application entry point for local testing and Javadoc generation.
 * The library is primarily intended to be used as a dependency in other services.
 */
@SpringBootApplication
public class LibCommonBatchApplication {

    /**
     * Boots the application. Not required when using the library as a dependency,
     * but useful for local runs during development.
     */
    public static void main(String[] args) {
        SpringApplication.run(LibCommonBatchApplication.class, args);
    }

}
