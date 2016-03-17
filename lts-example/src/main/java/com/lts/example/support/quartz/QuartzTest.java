package com.lts.example.support.quartz;

import com.lts.core.commons.utils.DateUtils;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class QuartzTest {

    public void autoRun() {
        System.out.println(DateUtils.formatYMD_HMS(new Date()) + " 我执行了...");
    }

}
