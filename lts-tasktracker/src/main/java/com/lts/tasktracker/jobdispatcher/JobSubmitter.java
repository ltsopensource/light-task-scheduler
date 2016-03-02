package com.lts.tasktracker.jobdispatcher;

import java.util.Properties;
/**
 * 内嵌JobClient时，提交初始作业任务
 *
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
public interface JobSubmitter {
    void submitJob(Properties conf);
}