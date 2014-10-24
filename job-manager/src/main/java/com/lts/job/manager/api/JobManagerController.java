package com.lts.job.manager.api;

import com.lts.job.common.domain.Job;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.common.util.JsonUtils;
import com.lts.job.manager.domain.RestfulResponse;
import com.lts.job.manager.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 */
@RestController
@RequestMapping("/api/job/")
public class JobManagerController extends AbstractController {
    @Autowired
    private JobService jobService;

    @RequestMapping(value = "add", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public String add(@Valid final Job job, final BindingResult result) {

        return execute(new AbstractController.Delegate() {
            @Override
            public void delegate(RestfulResponse response) {
                if (result.hasErrors()) {
                    response.setSuccess(false);
                    response.setMsg(JsonUtils.objectToJsonString(result.getAllErrors()));
                    return;
                }
                jobService.addJob(job);
                response.setSuccess(true);
                response.setMsg("添加成功");
            }
        });
    }

    @RequestMapping(value = "getAll", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String getAllJob() {
        return execute(new AbstractController.Delegate() {
            @Override
            public void delegate(RestfulResponse response) {
                List<JobPo> jobPos = jobService.getAllJob();
                response.setSuccess(true);
                response.setBody(jobPos);
            }
        });
    }

    @RequestMapping(value = "delete/{jobId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public String deleteJob(@PathVariable("jobId") final String jobId) {
        return execute(new AbstractController.Delegate() {
            @Override
            public void delegate(RestfulResponse response) {
                JobPo jobPo = jobService.delete(jobId);
                response.setSuccess(true);
                response.setMsg("删除成功");
                response.setBody(jobPo);
            }
        });
    }
}
