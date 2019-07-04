package com.github.ltsopensource.jobclient;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.domain.JobClientNode;
import com.github.ltsopensource.jobclient.domain.Response;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14. 重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public interface RetryJobClient extends JobClient<JobClientNode> {

    void beforeStart();

    void beforeStop();

    Response submitJob(Job job);

    Response submitJob(List<Job> jobs);

}
