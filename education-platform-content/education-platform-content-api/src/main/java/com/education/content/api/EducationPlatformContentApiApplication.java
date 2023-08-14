package com.education.content.api;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;


@EnableSwagger2Doc
@SpringBootApplication
//@MapperScan(basePackages = {"com.education.content.service.mapper"})
@EnableDiscoveryClient
@ComponentScan("com.education.content")
public class EducationPlatformContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationPlatformContentApiApplication.class, args);
    }

}
