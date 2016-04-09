package com.lts.example.springboot;

import com.lts.spring.boot.annotation.EnableJobClient;
import com.lts.spring.boot.annotation.EnableJobTracker;
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
// 因为示例里面引入了mongo的包,所以要把这个排查, 自己的工程如果不用mongo做任务队列的时候,就不用写这个排除了
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
