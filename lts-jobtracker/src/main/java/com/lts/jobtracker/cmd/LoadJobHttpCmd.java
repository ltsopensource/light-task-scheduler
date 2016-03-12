package com.lts.jobtracker.cmd;

import com.lts.cmd.HttpCmdProc;
import com.lts.cmd.HttpCmdRequest;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cmd.HttpCmdNames;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jobtracker.domain.JobTrackerAppContext;

/**
 * 给JobTracker发送信号，加载任务
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class LoadJobHttpCmd implements HttpCmdProc {

    private final Logger LOGGER = LoggerFactory.getLogger(LoadJobHttpCmd.class);

    private JobTrackerAppContext appContext;

    public LoadJobHttpCmd(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_LOAD_JOB;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        appContext.getPreLoader().load(taskTrackerNodeGroup);

        LOGGER.info("load job succeed : nodeGroup={}", taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "load job succeed");
    }
}
