package com.education.learning.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages={"com.education.learning.service.feignclient"})
@SpringBootApplication(scanBasePackages = "com.education.learning")
public class EducationPlatformLearningApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformLearningApiApplication.class, args);
    }

}
