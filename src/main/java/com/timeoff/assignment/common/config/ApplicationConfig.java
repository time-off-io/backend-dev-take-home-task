package com.timeoff.assignment.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    /**
     * A UTC clock injected wherever the current time is needed. Using an injected clock
     * (instead of {@code Instant.now()} / {@code LocalDate.now()}) keeps time-dependent
     * business logic deterministic and testable.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
