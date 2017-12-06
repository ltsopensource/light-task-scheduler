package com.github.ltsopensource.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("/lts-quartz.xml");
    }

}
