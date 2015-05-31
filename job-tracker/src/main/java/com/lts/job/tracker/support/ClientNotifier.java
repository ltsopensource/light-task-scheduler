package com.lts.job.tracker.support;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.exception.RemotingSendException;
import com.lts.job.core.exception.RequestTimeoutException;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.Holder;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobClientNode;
import com.lts.job.tracker.domain.JobTrackerApplication;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 3/2/15.
 */
public class ClientNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNotifier.class.getSimpleName());
    private ClientNotifyHandler clientNotifyHandler;
    private JobTrackerApplication application;

    public ClientNotifier(JobTrackerApplication application, ClientNotifyHandler clientNotifyHandler) {
        this.application = application;
        this.clientNotifyHandler = clientNotifyHandler;
    }

    /**
     * 发送给客户端
     *
     * @param jobResults
     * @return 返回成功的个数
     */
    public <T extends JobResult> int send(List<T> jobResults) {
        if (CollectionUtils.isEmpty(jobResults)) {
            return 0;
        }

        // 单个 就不用 分组了
        if (jobResults.size() == 1) {

            JobResult jobResult = jobResults.get(0);
            if (!send0(jobResult.getJob().getSubmitNodeGroup(), Arrays.asList(jobResult))) {
                // 如果没有完成就返回
                clientNotifyHandler.handleFailed(jobResults);
                return 0;
            }
        } else if (jobResults.size() > 1) {

            List<JobResult> failedJobResult = new ArrayList<JobResult>();

            // 有多个要进行分组 (出现在 失败重发的时候)
            Map<String/*nodeGroup*/, List<JobResult>> groupMap = new HashMap<String, List<JobResult>>();

            for (T jobResult : jobResults) {
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
        JobClientNode jobClientNode = application.getJobClientManager().getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }

        JobFinishedRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobFinishedRequest());
        requestBody.setJobResults(jobResults);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_FINISHED.code(), requestBody);

        final Holder<Boolean> result = new Holder<Boolean>();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            getRemotingServer().invokeAsync(jobClientNode.getChannel().getChannel(), commandRequest, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand commandResponse = responseFuture.getResponseCommand();

                        if (commandResponse != null && commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                            clientNotifyHandler.handleSuccess(jobResults);
                            result.set(true);
                        } else {
                            result.set(false);
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });

            try {
                latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RequestTimeoutException(e);
            }

        } catch (RemotingSendException e) {
            LOGGER.error("notify client failed!", e);
        }
        return result.get();
    }

    private RemotingServerDelegate getRemotingServer() {
        return application.getRemotingServer();
    }

}
