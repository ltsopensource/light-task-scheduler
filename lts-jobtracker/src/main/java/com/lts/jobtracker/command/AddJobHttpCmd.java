package com.lts.jobtracker.command;

import com.lts.core.cmd.HttpCmdProcessor;
import com.lts.core.cmd.HttpCmdRequest;
import com.lts.core.cmd.HttpCmdResponse;
import com.lts.core.cmd.HttpCmds;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.jobtracker.domain.JobTrackerAppContext;

import java.util.Collections;

/**
 * 添加任务
 *
 * @author Robert HG (254963746@qq.com) on 10/27/15.
 */
public class AddJobHttpCmd implements HttpCmdProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(AddJobHttpCmd.class);

    private JobTrackerAppContext appContext;

    public AddJobHttpCmd(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String getCommand() {
        return HttpCmds.CMD_ADD_JOB;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(false);

        String jobJSON = request.getParam("job");
        if (StringUtils.isEmpty(jobJSON)) {
            response.setMsg("job can not be null");
            return response;
        }
        try {
            Job job = JSON.parse(jobJSON, Job.class);
            if (job == null) {
                response.setMsg("job can not be null");
                return response;
            }

            if (StringUtils.isEmpty(job.getSubmitNodeGroup())) {
                response.setMsg("job.SubmitNodeGroup can not be null");
                return response;
            }

            job.checkField();

            JobSubmitRequest jobSubmitRequest = new JobSubmitRequest();
            jobSubmitRequest.setJobs(Collections.singletonList(job));
            appContext.getJobReceiver().receive(jobSubmitRequest);

            LOGGER.info("add job succeed, {}", job);

            response.setSuccess(true);

        } catch (Exception e) {
            LOGGER.error("add job error, message:", e);
            response.setMsg("add job error, message:" + e.getMessage());
        }
        return response;
    }

}
