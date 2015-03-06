package com.lts.job.example.spring;

import com.lts.job.tracker.JobTracker;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("lts-spring-job-tacker.xml");
        JobTracker jobTracker = (JobTracker) context.getBean("jobTracker");
    }
}
