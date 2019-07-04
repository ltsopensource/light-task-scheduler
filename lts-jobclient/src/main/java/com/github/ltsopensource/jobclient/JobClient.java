package com.github.ltsopensource.jobclient;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.domain.JobClientNode;
import com.github.ltsopensource.jobclient.domain.Response;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14. 任务客户端
 */
public interface JobClient<T extends JobClientNode> {

    int BATCH_SIZE = 10;

    void start();

    void stop();

    void beforeStart();

    void afterStart();

    void afterStop();

    void beforeStop();

    Response submitJob(Job job);

    Response cancelJob(String taskId, String taskTrackerNodeGroup);

    Response submitJob(List<Job> jobs);

    enum SubmitType {
        SYNC,   // 同步
        ASYNC   // 异步
    }
}
