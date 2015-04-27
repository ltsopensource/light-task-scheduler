package com.lts.job.tracker.support;

import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.JobReceiveException;
import com.lts.job.core.protocol.command.JobSubmitRequest;
import com.lts.job.core.util.StringUtils;
import com.lts.job.tracker.queue.DuplicateJobException;
import com.lts.job.tracker.queue.JobPo;
import com.lts.job.tracker.queue.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         任务处理器
 */
public class JobReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobReceiver.class.getSimpleName());

    private JobQueue jobQueue;

    public JobReceiver(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    /**
     * jobTracker 接受任务
     *
     * @param request
     * @return
     */
    public void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();

        JobReceiveException exception = null;

        if (jobs == null || jobs.size() == 0) {
            return;
        }

        for (Job job : jobs) {
            try {
                addToQueue(job, request);
            } catch (Exception t) {
                if (exception == null) {
                    exception = new JobReceiveException(t);
                }
                exception.addJob(job);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private JobPo addToQueue(Job job, JobSubmitRequest request) {

        JobPo jobPo = null;

        try {
            jobPo = JobDomainConverter.convert(job);
            if (jobPo == null) {
                LOGGER.warn("提交的任务节点不能被执行。{}", job);
                return null;
            }
            if (StringUtils.isEmpty(jobPo.getSubmitNodeGroup())) {
                jobPo.setSubmitNodeGroup(request.getNodeGroup());
            }
            jobQueue.add(jobPo);

            if (job.isSchedule()) {
                LOGGER.info("接受定时任务成功! nodeGroup={}, CronExpression={}, {}",
                        request.getNodeGroup(), job.getCronExpression(), job);
            } else {
                LOGGER.info("接受任务成功! nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } catch (DuplicateJobException e) {
            // 已经存在 ignore
            LOGGER.info("任务已经存在! nodeGroup={}, {}", request.getNodeGroup(), job);
        }
        return jobPo;
    }

}
