package com.lts.jobtracker.command;

import com.lts.command.CommandProcessor;
import com.lts.command.CommandRequest;
import com.lts.core.json.JSON;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.jobtracker.domain.JobTrackerApplication;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;

/**
 * 添加任务
 * @author Robert HG (254963746@qq.com) on 10/27/15.
 */
public class AddJobCommand implements CommandProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(AddJobCommand.class);

    private JobTrackerApplication application;

    public AddJobCommand(JobTrackerApplication application) {
        this.application = application;
    }

    @Override
    public void execute(OutputStream out, CommandRequest request) throws Exception {

        PrintWriter writer = new PrintWriter(out);

        String jobJSON = request.getParam("job");
        if (StringUtils.isEmpty(jobJSON)) {
            writer.println("job can not be null");
            return;
        }
        try {
            Job job = JSON.parse(jobJSON, Job.class);
            if (job == null) {
                writer.println("job can not be null");
                return;
            }

            if (StringUtils.isEmpty(job.getSubmitNodeGroup())) {
                writer.println("job.SubmitNodeGroup can not be null");
                return;
            }

            job.checkField();

            JobSubmitRequest jobSubmitRequest = new JobSubmitRequest();
            jobSubmitRequest.setJobs(Collections.singletonList(job));
            application.getJobReceiver().receive(jobSubmitRequest);

            LOGGER.info("add job succeed, {}", job);

            writer.println("true");

        } catch (Exception e) {
            writer.println("add job error, message:" + e.getMessage());
            return;
        }

        writer.flush();
    }

}
