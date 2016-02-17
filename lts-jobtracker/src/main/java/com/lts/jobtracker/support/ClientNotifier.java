package com.lts.jobtracker.support;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.Holder;
import com.lts.core.constant.Constants;
import com.lts.core.domain.Action;
import com.lts.core.domain.JobResult;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.exception.RemotingSendException;
import com.lts.core.exception.RequestTimeoutException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobFinishedRequest;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.jobtracker.domain.JobClientNode;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.remoting.AsyncCallback;
import com.lts.remoting.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 3/2/15.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ClientNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNotifier.class.getSimpleName());
	private ClientNotifyHandler clientNotifyHandler;
    private JobTrackerAppContext appContext;

    public ClientNotifier(JobTrackerAppContext appContext, ClientNotifyHandler clientNotifyHandler) {
        this.appContext = appContext;
        this.clientNotifyHandler = clientNotifyHandler;
    }

    /**
     * 发送给客户端
     * @return 返回成功的个数
     */
	public <T extends TaskTrackerJobResult> int send(List<T> jobResults) {
        if (CollectionUtils.isEmpty(jobResults)) {
            return 0;
        }

        // 单个 就不用 分组了
        if (jobResults.size() == 1) {

            TaskTrackerJobResult result = jobResults.get(0);
            if (!send0(result.getJobWrapper().getJob().getSubmitNodeGroup(), Arrays.asList(result))) {
                // 如果没有完成就返回
                clientNotifyHandler.handleFailed(jobResults);
                return 0;
            }
        } else if (jobResults.size() > 1) {

            List<TaskTrackerJobResult> failedTaskTrackerJobResult = new ArrayList<TaskTrackerJobResult>();

            // 有多个要进行分组 (出现在 失败重发的时候)
            Map<String/*nodeGroup*/, List<TaskTrackerJobResult>> groupMap = new HashMap<String, List<TaskTrackerJobResult>>();

            for (T jobResult : jobResults) {
                List<TaskTrackerJobResult> results = groupMap.get(jobResult.getJobWrapper().getJob().getSubmitNodeGroup());
                if (results == null) {
                    results = new ArrayList<TaskTrackerJobResult>();
                    groupMap.put(jobResult.getJobWrapper().getJob().getSubmitNodeGroup(), results);
                }
                results.add(jobResult);
            }
            for (Map.Entry<String, List<TaskTrackerJobResult>> entry : groupMap.entrySet()) {

                if (!send0(entry.getKey(), entry.getValue())) {
                    failedTaskTrackerJobResult.addAll(entry.getValue());
                }
            }
            clientNotifyHandler.handleFailed(failedTaskTrackerJobResult);
            return jobResults.size() - failedTaskTrackerJobResult.size();
        }
        return jobResults.size();
    }

    /**
     * 发送给客户端
     * 返回是否发送成功还是失败
     */
    private boolean send0(String nodeGroup, final List<TaskTrackerJobResult> results) {
        // 得到 可用的客户端节点
        JobClientNode jobClientNode = appContext.getJobClientManager().getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }
        List<JobResult> jobResults = new ArrayList<JobResult>(results.size());
        for (TaskTrackerJobResult result : results) {
            JobResult jobResult = new JobResult();
            jobResult.setJob(result.getJobWrapper().getJob());
            jobResult.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobResult.setMsg(result.getMsg());
            jobResult.setTime(result.getTime());
            jobResults.add(jobResult);
        }

        JobFinishedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobFinishedRequest());
        requestBody.setJobResults(jobResults);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_COMPLETED.code(), requestBody);

        final Holder<Boolean> result = new Holder<Boolean>();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            getRemotingServer().invokeAsync(jobClientNode.getChannel().getChannel(), commandRequest, new AsyncCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand commandResponse = responseFuture.getResponseCommand();

                        if (commandResponse != null && commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                            clientNotifyHandler.handleSuccess(results);
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
            LOGGER.error("Notify client failed!", e);
        }
        return result.get();
    }

    private RemotingServerDelegate getRemotingServer() {
        return appContext.getRemotingServer();
    }

}
