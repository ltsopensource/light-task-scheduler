package com.lts.job.example.spring;

import com.lts.job.task.tracker.TaskTracker;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("lts-spring-job-tasktracker.xml");
        TaskTracker taskTracker = (TaskTracker) context.getBean("taskTracker");
    }
}