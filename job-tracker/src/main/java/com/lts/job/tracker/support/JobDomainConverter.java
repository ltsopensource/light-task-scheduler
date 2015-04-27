package com.lts.job.tracker.support;

import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.support.CronExpression;
import com.lts.job.core.util.Md5Encrypt;
import com.lts.job.core.util.StringUtils;
import com.lts.job.tracker.logger.domain.JobLogPo;
import com.lts.job.tracker.queue.JobFeedbackPo;
import com.lts.job.tracker.queue.JobPo;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobDomainConverter {

    public static JobPo convert(Job job) {
        JobPo jobPo = new JobPo();
        jobPo.setPriority(job.getPriority());
        jobPo.setTaskId(job.getTaskId());
        jobPo.setGmtModify(System.currentTimeMillis());
        jobPo.setSubmitNodeGroup(job.getSubmitNodeGroup());
        jobPo.setTaskTrackerNodeGroup(job.getTaskTrackerNodeGroup());
        jobPo.setExtParams(job.getExtParams());
        jobPo.setNeedFeedback(job.isNeedFeedback());
        String jobId = generateJobId(jobPo);
        jobPo.setJobId(jobId);

        jobPo.setCronExpression(job.getCronExpression());

        if (jobPo.isSchedule()) {
            try {
                CronExpression cronExpression = new CronExpression(job.getCronExpression());
                Date nextTriggerTime = cronExpression.getTimeAfter(new Date());
                if (nextTriggerTime != null) {
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                } else {
                    // 如果没有下一次执行时间，那么直接忽略掉
                    return null;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (job.getTriggerTime() == null) {
                jobPo.setTriggerTime(System.currentTimeMillis());
            } else {
                jobPo.setTriggerTime(job.getTriggerTime());
            }
        }

        return jobPo;
    }

    /**
     * JobPo 转 Job
     *
     * @param jobPo
     * @return
     */
    public static Job convert(JobPo jobPo) {
        Job job = new Job();
        job.setPriority(jobPo.getPriority());
        job.setExtParams(jobPo.getExtParams());
        job.setSubmitNodeGroup(jobPo.getSubmitNodeGroup());
        job.setTaskId(jobPo.getTaskId());
        job.setTaskTrackerNodeGroup(jobPo.getTaskTrackerNodeGroup());
        job.setNeedFeedback(jobPo.isNeedFeedback());
        job.setJobId(jobPo.getJobId());
        job.setCronExpression(jobPo.getCronExpression());
        job.setTriggerTime(jobPo.getTriggerTime());
        return job;
    }

    public static JobLogPo convertJobLog(Job job) {
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setPriority(job.getPriority());
        jobLogPo.setExtParams(job.getExtParams());
        jobLogPo.setSubmitNodeGroup(job.getSubmitNodeGroup());
        jobLogPo.setTaskId(job.getTaskId());
        jobLogPo.setTaskTrackerNodeGroup(job.getTaskTrackerNodeGroup());
        jobLogPo.setNeedFeedback(job.isNeedFeedback());
        jobLogPo.setJobId(job.getJobId());
        jobLogPo.setCronExpression(job.getCronExpression());
        jobLogPo.setTriggerTime(job.getTriggerTime());
        return jobLogPo;
    }

    public static JobLogPo convertJobLog(JobPo jobPo) {
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setPriority(jobPo.getPriority());
        jobLogPo.setExtParams(jobPo.getExtParams());
        jobLogPo.setSubmitNodeGroup(jobPo.getSubmitNodeGroup());
        jobLogPo.setTaskId(jobPo.getTaskId());
        jobLogPo.setTaskTrackerNodeGroup(jobPo.getTaskTrackerNodeGroup());
        jobLogPo.setNeedFeedback(jobPo.isNeedFeedback());
        jobLogPo.setJobId(jobPo.getJobId());
        jobLogPo.setCronExpression(jobPo.getCronExpression());
        jobLogPo.setTriggerTime(jobPo.getTriggerTime());
        jobLogPo.setTaskTrackerIdentity(jobPo.getTaskTrackerIdentity());
        return jobLogPo;
    }

    public static JobFeedbackPo convert(JobResult jobResult) {
        JobFeedbackPo jobFeedbackPo = new JobFeedbackPo();
        jobFeedbackPo.setJob(jobResult.getJob());
        jobFeedbackPo.setSuccess(jobResult.isSuccess());
        jobFeedbackPo.setMsg(jobResult.getMsg());
        jobFeedbackPo.setTime(jobResult.getTime());
        jobFeedbackPo.setId(StringUtils.generateUUID());
        jobFeedbackPo.setGmtCreated(System.currentTimeMillis());
        return jobFeedbackPo;
    }

    /**
     * 生成jobID 保证唯一
     *
     * @param jobPo
     * @return
     */
    public static String generateJobId(JobPo jobPo) {
        StringBuilder sb = new StringBuilder();
        sb.append(jobPo.getTaskId()).append(jobPo.getSubmitNodeGroup()).append(jobPo.getGmtCreate());
        return Md5Encrypt.md5(sb.toString());
    }
}
