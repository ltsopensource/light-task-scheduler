package com.lts.job.tracker.support;

import com.lts.job.core.domain.JobResult;
import com.lts.job.core.exception.RemotingSendException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobClientNode;
import com.lts.job.tracker.domain.JobTrackerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Robert HG (254963746@qq.com) on 3/2/15.
 */
public class ClientNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNotifier.class.getSimpleName());
    private ClientNotifyHandler clientNotifyHandler;
    private JobClientManager jobClientManager;
    private CommandBodyWrapper commandBodyWrapper;
    private JobTrackerApplication application;

    public ClientNotifier(JobTrackerApplication application, ClientNotifyHandler clientNotifyHandler) {
        this.application = application;
        this.clientNotifyHandler = clientNotifyHandler;
        this.jobClientManager = application.getJobClientManager();
        this.commandBodyWrapper = application.getCommandBodyWrapper();
    }

    /**
     * 发送给客户端
     *
     * @param jobResults
     * @return 返回成功的个数
     */
    public int send(List<JobResult> jobResults) {
        if (jobResults == null || jobResults.size() == 0) {
            return 0;
        }

        // 单个 就不用 分组了
        if (jobResults.size() == 1) {

            JobResult jobResult = jobResults.get(0);
            if (!send0(jobResult.getJob().getSubmitNodeGroup(), jobResults)) {
                // 如果没有完成就返回
                clientNotifyHandler.handleFailed(jobResults);
                return 0;
            }
        } else if (jobResults.size() > 1) {

            List<JobResult> failedJobResult = new ArrayList<JobResult>();

            // 有多个要进行分组 (出现在 失败重发的时候)
            Map<String/*nodeGroup*/, List<JobResult>> groupMap = new HashMap<String, List<JobResult>>();

            for (JobResult jobResult : jobResults) {
                List<JobResult> jobResultList = groupMap.get(jobResult.getJob().getSubmitNodeGroup());
                if (jobResultList == null) {
                    jobResultList = new ArrayList<JobResult>();
                    groupMap.put(jobResult.getJob().getSubmitNodeGroup(), jobResultList);
                }
                jobResultList.add(jobResult);
            }
            for (Map.Entry<String, List<JobResult>> entry : groupMap.entrySet()) {

                if (!send0(entry.getKey(), entry.getValue())) {
                    failedJobResult.addAll(entry.getValue());
                }
            }
            clientNotifyHandler.handleFailed(failedJobResult);
            return jobResults.size() - failedJobResult.size();
        }
        return jobResults.size();
    }

    /**
     * 发送给客户端
     * 返回是否发送成功还是失败
     *
     * @param nodeGroup
     * @param jobResults
     * @return
     */
    private boolean send0(String nodeGroup, final List<JobResult> jobResults) {
        // 得到 可用的客户端节点
        JobClientNode jobClientNode = jobClientManager.getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }

        JobFinishedRequest requestBody = commandBodyWrapper.wrapper(new JobFinishedRequest());
        requestBody.setJobResults(jobResults);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_FINISHED.code(), requestBody);

        final boolean[] result = new boolean[1];
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            getRemotingServer().invokeAsync(jobClientNode.getChannel().getChannel(), commandRequest, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand commandResponse = responseFuture.getResponseCommand();

                        if (commandResponse != null && commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                            clientNotifyHandler.handleSuccess(jobResults);
                            result[0] = true;
                        } else {
                            result[0] = false;
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (RemotingSendException e) {
            LOGGER.error("通知客户端失败!", e);
        } catch (RemotingCommandFieldCheckException e) {
            LOGGER.error("通知客户端失败!", e);
        }
        return result[0];
    }

    private RemotingServerDelegate getRemotingServer() {
        return application.getRemotingServer();
    }

}
