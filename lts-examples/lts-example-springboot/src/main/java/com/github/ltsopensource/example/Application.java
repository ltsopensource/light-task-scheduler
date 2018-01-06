package com.github.ltsopensource.example;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.codahale.metrics.ConsoleReporter;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	ApplicationContext ctx = SpringApplication.run(Application.class, args);
		// 启动Reporter
        ConsoleReporter reporter = ctx.getBean(ConsoleReporter.class);
        reporter.start(2, TimeUnit.SECONDS);
    }

}