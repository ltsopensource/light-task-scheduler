package com.lts.example.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by hugui on 8/4/15.
 */
public class SpringJobTrackerTest {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/lts-jobtracker.xml");
        System.in.read();
    }

}
