package com.lts.job.task.tracker.logger;

import com.lts.job.core.constant.Level;
import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.BizLogSendRequest;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.common.Pair;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerImpl implements BizLogger {

    private Level level;
    private RemotingClientDelegate remotingClient;
    private TaskTrackerApplication application;
    private final ThreadLocal<Pair<String, String>> jobTL;

    public BizLoggerImpl(Level level, RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        this.level = level;
        if (this.level == null) {
            this.level = Level.INFO;
        }
        this.application = application;
        this.remotingClient = remotingClient;
        this.jobTL = new ThreadLocal<Pair<String, String>>();
    }

    public void setId(String jobId, String taskId) {
        jobTL.set(new Pair<String, String>(jobId, taskId));
    }

    public void removeId() {
        jobTL.remove();
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            sendMsg(msg);
        }
    }

    private void sendMsg(String msg) {

        if (!remotingClient.isServerEnable()) {
            // TODO JobTracker不可用的时候
            return;
        }

        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(application, new BizLogSendRequest());
        requestBody.setJobId(jobTL.get().getObject1());
        requestBody.setTaskId(jobTL.get().getObject2());
        requestBody.setMsg(msg);
        requestBody.setLevel(level);

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            remotingClient.invokeAsync(request, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    RemotingCommand response = responseFuture.getResponseCommand();

                    if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                        // success
                        // TODO
                    }
                }
            });
        } catch (JobTrackerNotFoundException e) {
            e.printStackTrace();
        }
    }

}
