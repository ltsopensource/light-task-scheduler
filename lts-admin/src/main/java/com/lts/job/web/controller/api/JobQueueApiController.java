package com.lts.job.web.controller.api;

import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.JobLoggerRequest;
import com.lts.job.core.commons.utils.*;
import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.core.support.CronExpression;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.DuplicateJobException;
import com.lts.job.web.cluster.AdminApplication;
import com.lts.job.web.controller.AbstractController;
import com.lts.job.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@RestController
public class JobQueueApiController extends AbstractController {

    @Autowired
    AdminApplication application;

    @RequestMapping("/job-queue/cron-job-get")
    public RestfulResponse cronJobGet(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getCronJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executable-job-get")
    public RestfulResponse executableJobGet(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getExecutableJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executing-job-get")
    public RestfulResponse executingJobGet(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getExecutingJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }

    @RequestMapping("/job-queue/cron-job-update")
    public RestfulResponse cronJobUpdate(JobQueueRequest request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        // 1. 检测 cronExpression是否是正确的
        if (StringUtils.isNotEmpty(request.getCronExpression())) {
            try {
                CronExpression expression = new CronExpression(request.getCronExpression());
                if (expression.getTimeAfter(new Date()) == null) {
                    response.setSuccess(false);
                    response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                    return response;
                }
            } catch (ParseException e) {
                response.setSuccess(false);
                response.setMsg("请输入正确的 CronExpression!");
                return response;
            }
        }
        application.getCronJobQueue().selectiveUpdate(request);
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("/job-queue/cron-job-delete")
    public RestfulResponse cronJobDelete(JobQueueRequest request) {
        RestfulResponse response = new RestfulResponse();
        if (StringUtils.isEmpty(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("JobId 必须传!");
            return response;
        }
        application.getCronJobQueue().remove(request.getJobId());
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("/job-logger/job-logger-get")
    public RestfulResponse jobLoggerGet(JobLoggerRequest request) {
        RestfulResponse response = new RestfulResponse();

        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }

        PageResponse<JobLogPo> pageResponse = application.getJobLogger().search(request);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());

        response.setSuccess(true);
        return response;
    }

    @RequestMapping("/job-queue/job-add")
    public RestfulResponse jobAdd(JobQueueRequest request) {
        RestfulResponse response = new RestfulResponse();
        // 表单check

        Long triggerTime = null;
        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
            Assert.hasLength(request.getSubmitNodeGroup(), "submitNodeGroup不能为空!");

            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                try {
                    CronExpression expression = new CronExpression(request.getCronExpression());
                    Date nextTime = expression.getTimeAfter(new Date());
                    if (nextTime == null) {
                        response.setSuccess(false);
                        response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                        return response;
                    } else {
                        triggerTime = nextTime.getTime();
                    }
                } catch (ParseException e) {
                    response.setSuccess(false);
                    response.setMsg("请输入正确的 CronExpression!");
                    return response;
                }
            }

        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }

        addJob(request, triggerTime);

        response.setSuccess(true);
        return response;
    }

    private void addJob(JobQueueRequest request, Long triggerTime) {
        JobPo jobPo = new JobPo();
        // 这里暂时用UUID来代替
        jobPo.setJobId(StringUtils.generateUUID());
        jobPo.setCronExpression(request.getCronExpression());
        jobPo.setExtParams(request.getExtParams());
        jobPo.setGmtCreated(DateUtils.currentTimeMillis());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        jobPo.setNeedFeedback(request.getNeedFeedback());
        jobPo.setPriority(request.getPriority());
        jobPo.setTaskId(request.getTaskId());
        jobPo.setSubmitNodeGroup(request.getSubmitNodeGroup());
        jobPo.setTaskTrackerNodeGroup(request.getTaskTrackerNodeGroup());
        if (request.getTriggerTime() != null) {
            jobPo.setTriggerTime(request.getTriggerTime().getTime());
        }

        if (jobPo.isSchedule()) {
            application.getCronJobQueue().add(jobPo);
            if (triggerTime != null) {
                jobPo.setTriggerTime(triggerTime);
            }
        }
        if (jobPo.getTriggerTime() == null) {
            jobPo.setTriggerTime(DateUtils.currentTimeMillis());
        }

        application.getExecutableJobQueue().add(jobPo);
    }
}
