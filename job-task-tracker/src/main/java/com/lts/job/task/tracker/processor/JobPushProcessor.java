package com.lts.job.task.tracker.processor;

import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.exception.JobTrackerNotFoundException;
import com.lts.job.common.file.FileException;
import com.lts.job.common.file.Line;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.JobFinishedRequest;
import com.lts.job.common.protocol.command.JobPushRequest;
import com.lts.job.common.remoting.RemotingClientDelegate;
import com.lts.job.common.support.RetryScheduler;
import com.lts.job.common.support.SingletonBeanContext;
import com.lts.job.common.util.JsonUtils;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.task.tracker.domain.Response;
import com.lts.job.task.tracker.expcetion.NoAvailableJobRunnerException;
import com.lts.job.task.tracker.runner.RunnerCallback;
import com.lts.job.task.tracker.runner.RunnerPool;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         接受任务并执行
 */
public class JobPushProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPushProcessor.class);

    private RetryScheduler retryScheduler;

    protected JobPushProcessor(final RemotingClientDelegate remotingClient) {
        super(remotingClient);

        retryScheduler = new RetryScheduler<JobResult>(3) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<JobResult> jobResults) {
                return sendJobResults(jobResults);
            }
        };

        retryScheduler.start();
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, final RemotingCommand request) throws RemotingCommandException {

        JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final Job job = requestBody.getJob();

        try {
            RunnerPool runnerPool = SingletonBeanContext.getBean(RunnerPool.class);
            runnerPool.execute(job, new JobRunnerCallback());
        } catch (NoAvailableJobRunnerException e) {
            // 任务推送失败
            return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code(), "job push failure , no available job runner!");
        }

        // 任务推送成功
        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code(), "job push success!");
    }

    /**
     * 任务执行的回调(任务执行完之后线程回调这个函数)
     */
    private class JobRunnerCallback implements RunnerCallback {
        @Override
        public Job runComplete(Response response) {
            // 发送消息给 JobTracker
            final JobResult jobResult = new JobResult();
            jobResult.setTime(System.currentTimeMillis());
            jobResult.setJob(response.getJob());
            jobResult.setSuccess(response.isSuccess());
            jobResult.setMsg(response.getMsg());
            JobFinishedRequest requestBody = new JobFinishedRequest();
            requestBody.addJobResult(jobResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.JOB_FINISHED.code();

            RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                // 这里会容易超时, 造成JobTracker返回来的任务没有拿到, 但JobTracker 认为已经任务拿到了, 这个由JobTracker 定时去修复
                remotingClient.invokeAsync(request, new InvokeCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        try {
                            RemotingCommand commandResponse = responseFuture.getResponseCommand();

                            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                                JobPushRequest jobPushRequest = commandResponse.getBody();
                                if (jobPushRequest != null) {
                                    LOGGER.info("取到新任务:{}", jobPushRequest.getJob());
                                    returnResponse.setJob(jobPushRequest.getJob());
                                }
                            } else {
                                LOGGER.info("任务完成通知反馈失败, 存储文件。{}", jobResult);
                                // 通知失败, 存文件
                                Line line = new Line(JsonUtils.objectToJsonString(jobResult));
                                try {
                                    retryScheduler.getFileAccessor().addOneLine(line);
                                } catch (FileException e) {
                                    LOGGER.error("保存JobResult失败:{}", jobResult, e);
                                }
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
            } catch (RemotingCommandFieldCheckException e) {
                LOGGER.error("任务完成通知反馈失败, {}, {}", response.getJob(), e.getMessage(), e);
            } catch (JobTrackerNotFoundException e) {
                LOGGER.error("任务完成通知反馈失败, {},{}", response.getJob(), e.getMessage(), e);
            }

            return returnResponse.getJob();
        }
    }

    /**
     * 发送JobResults
     *
     * @param jobResults
     * @return
     */
    private boolean sendJobResults(List<JobResult> jobResults) {
        // 发送消息给 JobTracker
        JobFinishedRequest requestBody = new JobFinishedRequest();
        requestBody.setJobResults(jobResults);
        requestBody.setReSend(true);

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_FINISHED.code(), requestBody);

        final boolean[] result = new boolean[1];
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            remotingClient.invokeAsync(request, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand commandResponse = responseFuture.getResponseCommand();
                        if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                            result[0] = true;
                        }else{
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
        } catch (RemotingCommandFieldCheckException e) {
            LOGGER.error("任务完成通知失败, jobResults={}", jobResults, e);
        } catch (JobTrackerNotFoundException e) {
            LOGGER.error("任务完成通知失败, jobResults={}", jobResults, e);
        }

        return result[0];
    }

}
