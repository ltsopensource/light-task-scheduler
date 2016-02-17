package com.lts.jobtracker.command;

import com.lts.core.cmd.HttpCmdProcessor;
import com.lts.core.cmd.HttpCmdRequest;
import com.lts.core.cmd.HttpCmdResponse;
import com.lts.core.cmd.HttpCmds;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jobtracker.domain.JobTrackerAppContext;

/**
 * 给JobTracker发送信号，加载任务
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class LoadJobHttpCmd implements HttpCmdProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(LoadJobHttpCmd.class);

    private JobTrackerAppContext appContext;

    public LoadJobHttpCmd(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String getCommand() {
        return HttpCmds.CMD_LOAD_JOB;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        appContext.getPreLoader().load(taskTrackerNodeGroup);

        LOGGER.info("load job succeed : nodeGroup={}", taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "load job succeed");
    }
}
