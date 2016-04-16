package com.github.ltsopensource.jobtracker.cmd;

import com.github.ltsopensource.cmd.HttpCmdProc;
import com.github.ltsopensource.cmd.HttpCmdRequest;
import com.github.ltsopensource.cmd.HttpCmdResponse;
import com.github.ltsopensource.core.cmd.HttpCmdNames;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.command.JobSubmitRequest;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;

import java.util.Collections;

/**
 * 添加任务
 *
 * @author Robert HG (254963746@qq.com) on 10/27/15.
 */
public class AddJobHttpCmd implements HttpCmdProc {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddJobHttpCmd.class);

    private JobTrackerAppContext appContext;

    public AddJobHttpCmd(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_ADD_JOB;
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

            if (job.isNeedFeedback() && StringUtils.isEmpty(job.getSubmitNodeGroup())) {
                response.setMsg("if needFeedback, job.SubmitNodeGroup can not be null");
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
