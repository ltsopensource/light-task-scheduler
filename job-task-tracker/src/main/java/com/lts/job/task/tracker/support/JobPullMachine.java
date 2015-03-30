package com.lts.job.task.tracker.support;

import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobPullRequest;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用来向JobTracker去取任务
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public class JobPullMachine {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPullMachine.class.getSimpleName());

    // 定时检查TaskTracker是否有空闲的线程，如果有，那么向JobTracker发起任务pull请求
    private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1);

    private RemotingClientDelegate remotingClient;
    private TaskTrackerApplication application;

    public JobPullMachine(TaskTrackerApplication application) {
        this.remotingClient = application.getRemotingClient();
        this.application = application;
    }

    public void start() {
        SCHEDULED_CHECKER.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendRequest();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                    /**
                     * 发送Job pull 请求
                     */
                    private void sendRequest() throws RemotingCommandFieldCheckException {
                        int availableThreads = application.getAvailableThreads();
                        if (availableThreads == 0) {
                            return;
                        }
                        JobPullRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobPullRequest());
                        requestBody.setAvailableThreads(availableThreads);
                        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_PULL.code(), requestBody);

                        try {
                            remotingClient.invokeAsync(request, new InvokeCallback() {
                                @Override
                                public void operationComplete(ResponseFuture responseFuture) {
                                    RemotingCommand responseCommand = responseFuture.getResponseCommand();
                                    if (responseCommand == null) {
                                        LOGGER.warn("job pull request failed! response command is null!");
                                        return;
                                    }
                                    if (JobProtos.ResponseCode.JOB_PULL_SUCCESS.code() == responseCommand.getCode()) {
                                        if (LOGGER.isDebugEnabled()) {
                                            LOGGER.debug("job pull request success!");
                                        }
                                        return;
                                    }
                                    LOGGER.warn("job pull request failed! response command is null!");
                                }
                            });
                        } catch (JobTrackerNotFoundException e) {
                            LOGGER.warn(e.getMessage());
                        }
                    }
                }, 10, 10, TimeUnit.SECONDS);        // 10s 检查一次是否有空余线程
    }

    public void stop() {
        SCHEDULED_CHECKER.shutdown();
    }
}
