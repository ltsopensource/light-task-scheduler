package com.lts.example.spring;

import com.lts.example.support.BaseJobClientTest;
import com.lts.jobclient.JobClient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@SuppressWarnings({"rawtypes","resource"})
public class SpringJobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws IOException {

        ApplicationContext context = new ClassPathXmlApplicationContext("/spring/lts-jobclient.xml");

        JobClient jobClient = (JobClient) context.getBean("jobClient");

        jobClient.start();

        SpringJobClientTest jobClientTest = new SpringJobClientTest();
        jobClientTest.jobClient = jobClient;
        jobClientTest.startConsole();

        System.in.read();
    }

}
