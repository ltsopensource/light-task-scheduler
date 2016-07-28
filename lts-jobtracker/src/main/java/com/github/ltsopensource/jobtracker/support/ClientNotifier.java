package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.Holder;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.domain.JobResult;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.exception.RemotingSendException;
import com.github.ltsopensource.core.exception.RequestTimeoutException;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobFinishedRequest;
import com.github.ltsopensource.core.remoting.RemotingServerDelegate;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.jobtracker.domain.JobClientNode;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.AsyncCallback;
import com.github.ltsopensource.remoting.ResponseFuture;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

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
	public <T extends JobRunResult> int send(List<T> jobResults) {
        if (CollectionUtils.isEmpty(jobResults)) {
            return 0;
        }

        // 单个 就不用 分组了
        if (jobResults.size() == 1) {

            JobRunResult result = jobResults.get(0);
            if (!send0(result.getJobMeta().getJob().getSubmitNodeGroup(), Collections.singletonList(result))) {
                // 如果没有完成就返回
                clientNotifyHandler.handleFailed(jobResults);
                return 0;
            }
        } else if (jobResults.size() > 1) {

            List<JobRunResult> failedJobRunResult = new ArrayList<JobRunResult>();

            // 有多个要进行分组 (出现在 失败重发的时候)
            Map<String/*nodeGroup*/, List<JobRunResult>> groupMap = new HashMap<String, List<JobRunResult>>();

            for (T jobResult : jobResults) {
                List<JobRunResult> results = groupMap.get(jobResult.getJobMeta().getJob().getSubmitNodeGroup());
                if (results == null) {
                    results = new ArrayList<JobRunResult>();
                    groupMap.put(jobResult.getJobMeta().getJob().getSubmitNodeGroup(), results);
                }
                results.add(jobResult);
            }
            for (Map.Entry<String, List<JobRunResult>> entry : groupMap.entrySet()) {

                if (!send0(entry.getKey(), entry.getValue())) {
                    failedJobRunResult.addAll(entry.getValue());
                }
            }
            clientNotifyHandler.handleFailed(failedJobRunResult);
            return jobResults.size() - failedJobRunResult.size();
        }
        return jobResults.size();
    }

    /**
     * 发送给客户端
     * 返回是否发送成功还是失败
     */
    private boolean send0(String nodeGroup, final List<JobRunResult> results) {
        // 得到 可用的客户端节点
        JobClientNode jobClientNode = appContext.getJobClientManager().getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }
        List<JobResult> jobResults = new ArrayList<JobResult>(results.size());
        for (JobRunResult result : results) {
            JobResult jobResult = new JobResult();

            Job job = JobUtils.copy(result.getJobMeta().getJob());
            job.setTaskId(result.getJobMeta().getRealTaskId());
            jobResult.setJob(job);
            jobResult.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobResult.setMsg(result.getMsg());
            jobResult.setTime(result.getTime());
            jobResult.setExeSeqId(result.getJobMeta().getInternalExtParam(Constants.EXE_SEQ_ID));
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
        return result.get() == null ? false : result.get();
    }

    private RemotingServerDelegate getRemotingServer() {
        return appContext.getRemotingServer();
    }

}
