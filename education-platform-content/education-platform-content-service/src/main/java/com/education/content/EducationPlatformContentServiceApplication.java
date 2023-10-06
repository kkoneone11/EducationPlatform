package com.education.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.education")
//@MapperScan(basePackages = {"com.education.content.service.mapper"})
//@ComponentScan("com.education.content")
//@EnableDiscoveryClient
@EnableFeignClients(basePackages={"com.education.content.service.feignclient"})
public class EducationPlatformContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformContentServiceApplication.class, args);
    }

}
