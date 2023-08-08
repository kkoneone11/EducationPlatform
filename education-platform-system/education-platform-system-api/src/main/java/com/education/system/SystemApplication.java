package com.education.system;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p>
 *     系统管理启动类
 * </p>
 *
 * @Description:
 */
@EnableScheduling
@EnableSwagger2Doc
@SpringBootApplication
//@ComponentScan("com.education.system")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
    }
}