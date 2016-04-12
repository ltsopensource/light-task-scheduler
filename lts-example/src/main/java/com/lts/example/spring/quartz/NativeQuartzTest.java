package com.lts.example.spring.quartz;

import com.lts.core.support.AliveKeeping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class NativeQuartzTest {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("/spring/lts-quartz.xml");

        AliveKeeping.start();
    }

}
