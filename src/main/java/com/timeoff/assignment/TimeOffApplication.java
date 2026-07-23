package com.timeoff.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TimeOffApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeOffApplication.class, args);
    }
}
