package com.lts.jobtracker.command;

import com.lts.core.command.HttpCommandProcessor;
import com.lts.core.command.HttpCommandRequest;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jobtracker.domain.JobTrackerApplication;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * 给JobTracker发送信号，加载任务
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class LoadJobHttpCommand implements HttpCommandProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(LoadJobHttpCommand.class);

    private JobTrackerApplication application;

    public LoadJobHttpCommand(JobTrackerApplication application) {
        this.application = application;
    }

    @Override
    public void execute(OutputStream out, HttpCommandRequest request) throws Exception {
        PrintWriter writer = new PrintWriter(out);

        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        application.getPreLoader().load(taskTrackerNodeGroup);

        LOGGER.info("load job succeed : nodeGroup={}", taskTrackerNodeGroup);

        writer.println("true");

        writer.flush();
    }
}
