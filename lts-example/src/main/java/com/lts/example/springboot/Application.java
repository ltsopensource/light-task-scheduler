package com.lts.example.springboot;

import com.lts.spring.boot.annotation.EnableJobClient;
import com.lts.spring.boot.annotation.EnableJobTracker;
import com.lts.spring.boot.annotation.EnableMonitor;
import com.lts.spring.boot.annotation.EnableTaskTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@SpringBootApplication
@EnableJobTracker
@EnableJobClient
@EnableTaskTracker
@EnableMonitor
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
