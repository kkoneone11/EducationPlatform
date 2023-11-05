package com.education.learning.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.education.learning")
@EnableFeignClients(basePackages={"com.education.learning.service.feignclient"})
public class EducationPlatformLearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformLearningServiceApplication.class, args);
    }

}
