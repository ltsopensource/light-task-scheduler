package com.github.ltsopensource.example.springboot;

import com.github.ltsopensource.spring.boot.annotation.EnableJobClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@SpringBootApplication
@EnableJobClient
@ComponentScan("com.github.ltsopensource.example")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
