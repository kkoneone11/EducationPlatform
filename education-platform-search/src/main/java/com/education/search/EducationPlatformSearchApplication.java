package com.education.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.education.search")
@EnableDiscoveryClient
public class EducationPlatformSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformSearchApplication.class, args);
    }

}
