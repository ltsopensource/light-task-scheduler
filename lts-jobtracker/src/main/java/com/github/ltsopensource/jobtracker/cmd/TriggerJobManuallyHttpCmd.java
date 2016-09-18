package com.github.ltsopensource.jobtracker.cmd;

import com.github.ltsopensource.cmd.HttpCmdProc;
import com.github.ltsopensource.cmd.HttpCmdRequest;
import com.github.ltsopensource.cmd.HttpCmdResponse;
import com.github.ltsopensource.core.cmd.HttpCmdNames;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;

/**
 * 用来手动触发任务
 * 内部做的事情就是将某个任务加载到内存中
 * @author Robert HG (254963746@qq.com) on 8/4/16.
 */
public class TriggerJobManuallyHttpCmd implements HttpCmdProc {

    private final Logger LOGGER = LoggerFactory.getLogger(TriggerJobManuallyHttpCmd.class);

    private JobTrackerAppContext appContext;

    public TriggerJobManuallyHttpCmd(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_TRIGGER_JOB_MANUALLY;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        String jobId = request.getParam("jobId");

        if (StringUtils.isEmpty(taskTrackerNodeGroup)) {
            return HttpCmdResponse.newResponse(true, "nodeGroup should not be empty");
        }

        if (StringUtils.isEmpty(jobId)) {
            return HttpCmdResponse.newResponse(true, "jobId should not be empty");
        }

        appContext.getPreLoader().loadOne2First(taskTrackerNodeGroup, jobId);

        LOGGER.info("Trigger Job jobId={} taskTrackerNodeGroup={}", jobId, taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "trigger job succeed");
    }
}
