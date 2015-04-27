package com.lts.job.task.tracker.processor;

import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.protocol.command.JobPushRequest;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.core.support.RetryScheduler;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.task.tracker.domain.Response;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.expcetion.NoAvailableJobRunnerException;
import com.lts.job.task.tracker.runner.RunnerCallback;
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
    private JobRunnerCallback jobRunnerCallback;
    private CommandBodyWrapper commandBodyWrapper;

    protected JobPushProcessor(final RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        super(remotingClient, application);
        this.commandBodyWrapper = application.getCommandBodyWrapper();
        retryScheduler = new RetryScheduler<JobResult>(application, 3) {
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

        // 线程安全的
        jobRunnerCallback = new JobRunnerCallback();
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, final RemotingCommand request) throws RemotingCommandException {

        JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final Job job = requestBody.getJob();

        try {
            application.getRunnerPool().execute(job, jobRunnerCallback);
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
            JobFinishedRequest requestBody = commandBodyWrapper.wrapper(new JobFinishedRequest());
            requestBody.addJobResult(jobResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.JOB_FINISHED.code();

            RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
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
                                try {
                                    retryScheduler.inSchedule(
                                            jobResult.getJob().getJobId().concat("_") + System.currentTimeMillis(),
                                            jobResult);
                                } catch (Exception e) {
                                    LOGGER.error("任务完成通知反馈存储文件失败", e);
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
        JobFinishedRequest requestBody = commandBodyWrapper.wrapper(new JobFinishedRequest());
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
                        } else {
                            LOGGER.warn("send job failed, {}", commandResponse);
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
