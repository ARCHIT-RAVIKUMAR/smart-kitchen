package com.smartkitchen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /**
     * One shared Clock. Business code asks this clock for "today" instead of
     * calling LocalDate.now() directly, so tests can replace it with a fixed date.
     */
    @Bean
    public Clock clock(@Value("${kitchen.timezone:Asia/Kolkata}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
