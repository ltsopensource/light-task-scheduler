package com.lts.example.spring;

import com.lts.core.commons.utils.Assert;
import com.lts.jobclient.JobClient;
import com.lts.jobtracker.JobTracker;
import com.lts.tasktracker.TaskTracker;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/15.
 */
@SuppressWarnings({"rawtypes","resource"})
public class SpringAnnotationTest {

    public static void main(String[] args) throws IOException {

		ApplicationContext context = new ClassPathXmlApplicationContext("spring/lts-annotation.xml");

        JobTracker jobTracker = (JobTracker) context.getBean("jobTracker");
        Assert.notNull(jobTracker);
        jobTracker.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }

        TaskTracker taskTracker = (TaskTracker) context.getBean("taskTracker");
        Assert.notNull(taskTracker);
        taskTracker.start();

        JobClient jobClient = (JobClient) context.getBean("jobClient");
        Assert.notNull(jobClient);
        jobClient.start();
    }
}
