package com.lts.job.example.spring;

import com.lts.job.client.JobClient;
import com.lts.job.example.support.BaseJobClientTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */

public class JobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws ParseException, IOException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("lts-spring-job-client.xml");
        final JobClient jobClient = (JobClient) context.getBean("jobClient");

        JobClientTest jobClientTest = new JobClientTest();
        jobClientTest.jobClient = jobClient;
        jobClientTest.startConsole();
    }
}
