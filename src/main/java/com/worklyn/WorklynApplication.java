package com.worklyn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableMongoAuditing
@EnableCaching
public class WorklynApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorklynApplication.class, args);
    }
}
