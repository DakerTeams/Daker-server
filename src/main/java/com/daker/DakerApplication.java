package com.daker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DakerApplication.class, args);
    }
}
