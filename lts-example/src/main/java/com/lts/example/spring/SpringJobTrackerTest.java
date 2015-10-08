package com.lts.example.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
public class SpringJobTrackerTest {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/lts-jobtracker.xml");
        System.in.read();
    }

}
