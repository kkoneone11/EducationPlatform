package com.education.platform.orders.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.education.platform.orders")
public class EducationPlatformOrdersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformOrdersApiApplication.class, args);
    }

}
