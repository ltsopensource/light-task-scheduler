package com.lts.job.web.controller.api;

import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.core.exception.CronException;
import com.lts.job.core.support.CronExpression;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.StringUtils;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.web.cluster.AdminApplication;
import com.lts.job.web.controller.AbstractController;
import com.lts.job.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
            if (!CronExpression.isValidExpression(request.getCronExpression())) {
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

}
