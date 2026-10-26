package com.smartkitchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SmartKitchenApplication {

    public static void main(String[] args) {
        // Hosting servers usually run in UTC. Use one timezone everywhere so the
        // "Recent activity" times and the expiry "today" agree.
        String zone = System.getenv().getOrDefault("APP_TIMEZONE", "Asia/Kolkata");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        SpringApplication.run(SmartKitchenApplication.class, args);
    }
}
