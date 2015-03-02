package com.lts.job.common.support;

import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.util.Md5Encrypt;
import com.lts.job.common.repository.po.JobPo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobDomainConverter {

    public static JobPo convert(Job job) {

        JobPo jobPo = new JobPo();
        jobPo.setPriority(job.getPriority());
        jobPo.setTaskId(job.getTaskId());
        jobPo.setGmtModify(System.currentTimeMillis());
        jobPo.setNodeGroup(job.getNodeGroup());
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
                    jobPo.setFinished(true);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            jobPo.setTriggerTime(System.currentTimeMillis());
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
        job.setNodeGroup(jobPo.getNodeGroup());
        job.setTaskId(jobPo.getTaskId());
        job.setTaskTrackerNodeGroup(jobPo.getTaskTrackerNodeGroup());
        job.setNeedFeedback(jobPo.isNeedFeedback());
        job.setJobId(jobPo.getJobId());
        return job;
    }

    public static List<JobResult> convert(List<JobPo> jobPos) {
        List<JobResult> jobResults = new ArrayList<JobResult>();
        for (JobPo jobPo : jobPos) {
            JobResult jobResult = new JobResult();
            jobResult.setJob(convert(jobPo));
            jobResult.setMsg(jobPo.getMsg());
            jobResult.setSuccess(jobPo.isSuccess());
            jobResults.add(jobResult);
        }
        return jobResults;
    }

    /**
     * 生成jobID 保证唯一
     *
     * @param jobPo
     * @return
     */
    public static String generateJobId(JobPo jobPo) {
        StringBuilder sb = new StringBuilder();
        sb.append(jobPo.getTaskId()).append(jobPo.getNodeGroup()).append(jobPo.getGmtCreate());
        return Md5Encrypt.md5(sb.toString());
    }
}
