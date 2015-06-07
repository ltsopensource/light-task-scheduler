package com.lts.job.web.controller.api;

import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.web.cluster.AdminApplication;
import com.lts.job.web.controller.AbstractController;
import com.lts.job.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by hugui on 6/6/15.
 */
@RestController
public class JobQueueApiController extends AbstractController {

    @Autowired
    AdminApplication application;

    @RequestMapping("/job-queue/cron-job-get")
    public RestfulResponse getCronJobList(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getCronJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executable-job-get")
    public RestfulResponse getExecutableJobList(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getExecutableJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executing-job-get")
    public RestfulResponse getExecutingJobList(JobQueueRequest request) {
        PageResponse<JobPo> pageResponse = application.getExecutingJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(pageResponse.getResults());
        response.setRows(pageResponse.getRows());
        return response;
    }
}
