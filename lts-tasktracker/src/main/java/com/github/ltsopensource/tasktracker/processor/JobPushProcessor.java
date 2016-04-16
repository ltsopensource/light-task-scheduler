package com.github.ltsopensource.tasktracker.processor;

import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.exception.JobTrackerNotFoundException;
import com.github.ltsopensource.core.exception.RequestTimeoutException;
import com.github.ltsopensource.core.failstore.FailStorePathBuilder;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.core.protocol.command.JobPushRequest;
import com.github.ltsopensource.core.remoting.RemotingClientDelegate;
import com.github.ltsopensource.core.support.NodeShutdownHook;
import com.github.ltsopensource.core.support.RetryScheduler;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.remoting.AsyncCallback;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.ResponseFuture;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;
import com.github.ltsopensource.tasktracker.domain.Response;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;
import com.github.ltsopensource.tasktracker.expcetion.NoAvailableJobRunnerException;
import com.github.ltsopensource.tasktracker.runner.RunnerCallback;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         接受任务并执行
 */
public class JobPushProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPushProcessor.class);

    private RetryScheduler<JobRunResult> retryScheduler;
    private JobRunnerCallback jobRunnerCallback;
    private RemotingClientDelegate remotingClient;

    protected JobPushProcessor(TaskTrackerAppContext appContext) {
        super(appContext);
        this.remotingClient = appContext.getRemotingClient();
        retryScheduler = new RetryScheduler<JobRunResult>(JobPushProcessor.class.getSimpleName(), appContext,
                FailStorePathBuilder.getJobFeedbackPath(appContext), 3) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<JobRunResult> results) {
                return retrySendJobResults(results);
            }
        };
        retryScheduler.start();

        // 线程安全的
        jobRunnerCallback = new JobRunnerCallback();

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryScheduler.stop();
            }
        });
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          final RemotingCommand request) throws RemotingCommandException {

        JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final JobMeta jobMeta = requestBody.getJobMeta();

        try {
            appContext.getRunnerPool().execute(jobMeta, jobRunnerCallback);
        } catch (NoAvailableJobRunnerException e) {
            // 任务推送失败
            return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code(),
                    "job push failure , no available job runner!");
        }

        // 任务推送成功
        return RemotingCommand.createResponseCommand(JobProtos
                .ResponseCode.JOB_PUSH_SUCCESS.code(), "job push success!");
    }

    /**
     * 任务执行的回调(任务执行完之后线程回调这个函数)
     */
    private class JobRunnerCallback implements RunnerCallback {
        @Override
        public JobMeta runComplete(Response response) {
            // 发送消息给 JobTracker
            final JobRunResult jobRunResult = new JobRunResult();
            jobRunResult.setTime(SystemClock.now());
            jobRunResult.setJobMeta(response.getJobMeta());
            jobRunResult.setAction(response.getAction());
            jobRunResult.setMsg(response.getMsg());
            JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
            requestBody.addJobResult(jobRunResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.JOB_COMPLETED.code();

            RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                remotingClient.invokeAsync(request, new AsyncCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        try {
                            RemotingCommand commandResponse = responseFuture.getResponseCommand();

                            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                                JobPushRequest jobPushRequest = commandResponse.getBody();
                                if (jobPushRequest != null) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Get new job :{}", jobPushRequest.getJobMeta());
                                    }
                                    returnResponse.setJobMeta(jobPushRequest.getJobMeta());
                                }
                            } else {
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info("Job feedback failed, save local files。{}", jobRunResult);
                                }
                                try {
                                    retryScheduler.inSchedule(
                                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                                            jobRunResult);
                                } catch (Exception e) {
                                    LOGGER.error("Job feedback failed", e);
                                }
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
            } catch (JobTrackerNotFoundException e) {
                try {
                    LOGGER.warn("No job tracker available! save local files.");
                    retryScheduler.inSchedule(
                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                            jobRunResult);
                } catch (Exception e1) {
                    LOGGER.error("Save files failed, {}", jobRunResult.getJobMeta(), e1);
                }
            }

            return returnResponse.getJobMeta();
        }
    }

    /**
     * 发送JobResults
     */
    private boolean retrySendJobResults(List<JobRunResult> results) {
        // 发送消息给 JobTracker
        JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
        requestBody.setJobRunResults(results);
        requestBody.setReSend(true);

        int requestCode = JobProtos.RequestCode.JOB_COMPLETED.code();
        RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

        try {
            // 这里一定要用同步，不然异步会发生文件锁，死锁
            RemotingCommand commandResponse = remotingClient.invokeSync(request);
            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                return true;
            } else {
                LOGGER.warn("Send job failed, {}", commandResponse);
                return false;
            }
        } catch (JobTrackerNotFoundException e) {
            LOGGER.error("Retry send job result failed! jobResults={}", results, e);
        }
        return false;
    }

}
