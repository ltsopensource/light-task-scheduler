package com.lts.tasktracker.cmd;

import com.lts.cmd.HttpCmdProc;
import com.lts.cmd.HttpCmdRequest;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cmd.HttpCmdNames;
import com.lts.core.commons.utils.StringUtils;
import com.lts.tasktracker.domain.TaskTrackerAppContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/13/16.
 */
public class JobTerminateCmd implements HttpCmdProc {

    private TaskTrackerAppContext appContext;

    public JobTerminateCmd(TaskTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_JOB_TERMINATE;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String jobId = request.getParam("jobId");
        if (StringUtils.isEmpty(jobId)) {
            return HttpCmdResponse.newResponse(false, "jobId can't be empty");
        }

        if (!appContext.getRunnerPool().getRunningJobManager().running(jobId)) {
            return HttpCmdResponse.newResponse(false, "jobId dose not running in this TaskTracker now");
        }

        appContext.getRunnerPool().getRunningJobManager().terminateJob(jobId);

        return HttpCmdResponse.newResponse(true, "Execute terminate Command success");
    }
}
