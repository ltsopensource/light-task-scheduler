package com.lts.jobtracker.command;

import com.lts.command.CommandProcessor;
import com.lts.command.CommandRequest;
import com.lts.jobtracker.domain.JobTrackerApplication;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class LoadJobCommand implements CommandProcessor {

    private JobTrackerApplication application;

    public LoadJobCommand(JobTrackerApplication application) {
        this.application = application;
    }

    @Override
    public void execute(OutputStream out, CommandRequest request) throws Exception {
        PrintWriter writer = new PrintWriter(out);

        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        application.getPreLoader().load(taskTrackerNodeGroup);

        writer.println("load success");
    }
}
