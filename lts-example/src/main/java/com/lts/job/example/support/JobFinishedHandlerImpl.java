package com.lts.job.example.support;

import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class JobFinishedHandlerImpl implements JobFinishedHandler {

    @Override
    public void handle(List<JobResult> jobResults) {
        // 任务执行反馈结果处理
        if (CollectionUtils.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 任务执行完成：" + jobResult);
            }
        }
    }
}
