package com.github.ltsopensource.example.spring.quartz;

import com.github.ltsopensource.core.commons.utils.DateUtils;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class QuartzTest {

    public void autoRun() throws InterruptedException {
        System.out.println(DateUtils.formatYMD_HMS(new Date()) + " 我开始执行了...");
//        Thread.sleep(6000);
//        System.out.println(DateUtils.formatYMD_HMS(new Date()) + " 我执行完了...");

    }

}
