package com.lts.jobclient;

import com.lts.core.Application;
import com.lts.core.cluster.AbstractClientNode;
import com.lts.core.commons.utils.BatchUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.CommonUtils;
import com.lts.core.constant.Constants;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;
import com.lts.core.exception.JobTrackerNotFoundException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.core.protocol.command.JobSubmitResponse;
import com.lts.core.support.LoggerName;
import com.lts.jobclient.domain.JobClientApplication;
import com.lts.jobclient.domain.JobClientNode;
import com.lts.jobclient.domain.Response;
import com.lts.jobclient.domain.ResponseCode;
import com.lts.jobclient.processor.RemotingDispatcher;
import com.lts.jobclient.support.JobFinishedHandler;
import com.lts.jobclient.support.JobSubmitExecutor;
import com.lts.jobclient.support.JobSubmitProtector;
import com.lts.jobclient.support.SubmitCallback;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.remoting.netty.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         任务客户端
 */
public class JobClient<T extends JobClientNode, App extends Application> extends
        AbstractClientNode<JobClientNode, JobClientApplication> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.JobClient);

    private static final int BATCH_SIZE = 50;

    // 过载保护的提交者
    private JobSubmitProtector protector;

    private JobFinishedHandler jobFinishedHandler;

    @Override
    protected void preRemotingStart() {
        int concurrentSize = config.getParameter(Constants.JOB_SUBMIT_CONCURRENCY_SIZE,
                Constants.DEFAULT_JOB_SUBMIT_CONCURRENCY_SIZE);
        protector = new JobSubmitProtector(concurrentSize);
    }

    public Response submitJob(Job job) throws JobSubmitException {
        return protectSubmit(Collections.singletonList(job));
    }

    private Response protectSubmit(List<Job> jobs) throws JobSubmitException {
        return protector.execute(jobs, new JobSubmitExecutor<Response>() {
            @Override
            public Response execute(List<Job> jobs) throws JobSubmitException {
                return submitJob(jobs, SubmitType.ASYNC);
            }
        });
    }

    private void checkFields(List<Job> jobs) {
        // 参数验证
        if (CollectionUtils.isEmpty(jobs)) {
            throw new JobSubmitException("Job can not be null!");
        }
        for (Job job : jobs) {
            if (job == null) {
                throw new JobSubmitException("Job can not be null!");
            } else {
                job.checkField();
            }
        }
    }

    protected Response submitJob(final List<Job> jobs, SubmitType type) throws JobSubmitException {
        // 检查参数
        checkFields(jobs);

        final Response response = new Response();
        try {
            JobSubmitRequest jobSubmitRequest = CommandBodyWrapper.wrapper(application, new JobSubmitRequest());
            jobSubmitRequest.setJobs(jobs);

            RemotingCommand requestCommand = RemotingCommand.createRequestCommand(
                    JobProtos.RequestCode.SUBMIT_JOB.code(), jobSubmitRequest);

            SubmitCallback submitCallback = new SubmitCallback() {
                @Override
                public void call(RemotingCommand responseCommand) {
                    if (responseCommand == null) {
                        response.setFailedJobs(jobs);
                        response.setSuccess(false);
                        LOGGER.warn("Submit job failed: {}, {}",
                                jobs, "JobTracker is broken");
                        return;
                    }

                    if (JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code() == responseCommand.getCode()) {
                        LOGGER.info("Submit job success: {}", jobs);
                        response.setSuccess(true);
                        return;
                    }
                    // 失败的job
                    JobSubmitResponse jobSubmitResponse = responseCommand.getBody();
                    response.setFailedJobs(jobSubmitResponse.getFailedJobs());
                    response.setSuccess(false);
                    response.setCode(JobProtos.ResponseCode.valueOf(responseCommand.getCode()).name());
                    LOGGER.warn("Submit job failed: {}, {}, {}",
                            jobs,
                            responseCommand.getRemark(),
                            jobSubmitResponse.getMsg());
                }
            };

            if (SubmitType.ASYNC.equals(type)) {
                asyncSubmit(requestCommand, submitCallback);
            } else {
                syncSubmit(requestCommand, submitCallback);
            }
        } catch (JobTrackerNotFoundException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("Can not found JobTracker node!");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setMsg(CommonUtils.exceptionSimpleDesc(e));
        }

        return response;
    }

    private void asyncSubmit(RemotingCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        final CountDownLatch latch = new CountDownLatch(1);
        remotingClient.invokeAsync(requestCommand, new InvokeCallback() {
            @Override
            public void operationComplete(ResponseFuture responseFuture) {
                try {
                    submitCallback.call(responseFuture.getResponseCommand());
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new JobSubmitException("Submit job failed, async request timeout!", e);
        }
    }

    private void syncSubmit(RemotingCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        submitCallback.call(remotingClient.invokeSync(requestCommand));
    }

    public Response submitJob(List<Job> jobs) throws JobSubmitException {

        Response response = new Response();
        response.setSuccess(true);
        int size = jobs.size();
        for (int i = 0; i <= size / BATCH_SIZE; i++) {
            List<Job> subJobs = BatchUtils.getBatchList(i, BATCH_SIZE, jobs);

            if (CollectionUtils.isNotEmpty(subJobs)) {
                Response subResponse = protectSubmit(subJobs);
                if (!subResponse.isSuccess()) {
                    response.setSuccess(false);
                    response.addFailedJobs(subJobs);
                    response.setMsg(subResponse.getMsg());
                }
            }
        }

        return response;
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingClient, jobFinishedHandler);
    }

    /**
     * 设置任务完成接收器
     */
    public void setJobFinishedHandler(JobFinishedHandler jobFinishedHandler) {
        this.jobFinishedHandler = jobFinishedHandler;
    }

    enum SubmitType {
        SYNC,   // 同步
        ASYNC   // 异步
    }
}
