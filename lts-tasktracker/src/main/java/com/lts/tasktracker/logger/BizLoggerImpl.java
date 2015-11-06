package com.lts.tasktracker.logger;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Level;
import com.lts.core.domain.BizLog;
import com.lts.core.exception.JobTrackerNotFoundException;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.BizLogSendRequest;
import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.core.support.RetryScheduler;
import com.lts.core.support.SystemClock;
import com.lts.remoting.AsyncCallback;
import com.lts.remoting.common.Pair;
import com.lts.remoting.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.tasktracker.domain.TaskTrackerApplication;

import java.util.Collections;
import java.util.List;

/**
 * 业务日志记录器实现
 * 1. 业务日志会发送给JobTracker
 * 2. 也会采取Fail And Store 的方式
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerImpl implements BizLogger {

    private Level level;
    private RemotingClientDelegate remotingClient;
    private TaskTrackerApplication application;
    private final ThreadLocal<Pair<String, String>> jobTL;
    private RetryScheduler<BizLog> retryScheduler;

    public BizLoggerImpl(Level level, final RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        this.level = level;
        if (this.level == null) {
            this.level = Level.INFO;
        }
        this.application = application;
        this.remotingClient = remotingClient;
        this.jobTL = new ThreadLocal<Pair<String, String>>();
        String storePath = getStorePath();
        this.retryScheduler = new RetryScheduler<BizLog>(application, storePath) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<BizLog> list) {
                return sendBizLog(list);
            }
        };
        retryScheduler.setName(BizLogger.class.getSimpleName());
        this.retryScheduler.start();
    }

    private String getStorePath() {
        return application.getConfig().getDataPath()
                + "/.lts" + "/" +
                application.getConfig().getNodeType() + "/" +
                application.getConfig().getNodeGroup() + "/bizlog/";
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

        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(application, new BizLogSendRequest());

        final BizLog bizLog = new BizLog();
        bizLog.setTaskTrackerIdentity(requestBody.getIdentity());
        bizLog.setTaskTrackerNodeGroup(requestBody.getNodeGroup());
        bizLog.setLogTime(SystemClock.now());
        bizLog.setJobId(jobTL.get().getObject1());
        bizLog.setTaskId(jobTL.get().getObject2());
        bizLog.setMsg(msg);
        bizLog.setLevel(level);

        requestBody.setBizLogs(Collections.singletonList(bizLog));

        if (!remotingClient.isServerEnable()) {
            retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
            return;
        }

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            // 有可能down机，日志丢失
            remotingClient.invokeAsync(request, new AsyncCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    RemotingCommand response = responseFuture.getResponseCommand();

                    if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                        // success
                    } else {
                        retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
                    }
                }
            });
        } catch (JobTrackerNotFoundException e) {
            retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
        }
    }

    private boolean sendBizLog(List<BizLog> bizLogs) {
        if (CollectionUtils.isEmpty(bizLogs)) {
            return true;
        }
        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(application, new BizLogSendRequest());
        requestBody.setBizLogs(bizLogs);

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            RemotingCommand response = remotingClient.invokeSync(request);
            if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                // success
                return true;
            }
        } catch (JobTrackerNotFoundException ignored) {
        }
        return false;
    }

}
