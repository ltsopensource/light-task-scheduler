package com.github.ltsopensource.example;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 4/18/16.
 */
public class Main {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("/lts-tasktracker-shard-annotaion.xml");
    }
}
