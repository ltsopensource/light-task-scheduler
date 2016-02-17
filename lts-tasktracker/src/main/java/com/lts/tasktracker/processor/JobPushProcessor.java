package com.lts.tasktracker.processor;

import com.lts.core.constant.Constants;
import com.lts.core.domain.JobWrapper;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.exception.JobTrackerNotFoundException;
import com.lts.core.exception.RequestTimeoutException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.core.protocol.command.JobPushRequest;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.core.support.LoggerName;
import com.lts.core.support.RetryScheduler;
import com.lts.core.support.SystemClock;
import com.lts.remoting.AsyncCallback;
import com.lts.remoting.Channel;
import com.lts.remoting.ResponseFuture;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import com.lts.tasktracker.domain.Response;
import com.lts.tasktracker.domain.TaskTrackerAppContext;
import com.lts.tasktracker.expcetion.NoAvailableJobRunnerException;
import com.lts.tasktracker.runner.RunnerCallback;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         接受任务并执行
 */
public class JobPushProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.TaskTracker);

    private RetryScheduler<TaskTrackerJobResult> retryScheduler;
    private JobRunnerCallback jobRunnerCallback;
    private RemotingClientDelegate remotingClient;

    protected JobPushProcessor(TaskTrackerAppContext appContext) {
        super(appContext);
        this.remotingClient = appContext.getRemotingClient();
        retryScheduler = new RetryScheduler<TaskTrackerJobResult>(appContext, 3) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<TaskTrackerJobResult> results) {
                return retrySendJobResults(results);
            }
        };
        retryScheduler.setName("JobPush");
        retryScheduler.start();

        // 线程安全的
        jobRunnerCallback = new JobRunnerCallback();
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          final RemotingCommand request) throws RemotingCommandException {

        JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final JobWrapper jobWrapper = requestBody.getJobWrapper();

        try {
            appContext.getRunnerPool().execute(jobWrapper, jobRunnerCallback);
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
        public JobWrapper runComplete(Response response) {
            // 发送消息给 JobTracker
            final TaskTrackerJobResult taskTrackerJobResult = new TaskTrackerJobResult();
            taskTrackerJobResult.setTime(SystemClock.now());
            taskTrackerJobResult.setJobWrapper(response.getJobWrapper());
            taskTrackerJobResult.setAction(response.getAction());
            taskTrackerJobResult.setMsg(response.getMsg());
            JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
            requestBody.addJobResult(taskTrackerJobResult);
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
                                    LOGGER.info("Get new job :{}", jobPushRequest.getJobWrapper());
                                    returnResponse.setJobWrapper(jobPushRequest.getJobWrapper());
                                }
                            } else {
                                LOGGER.info("Job feedback failed, save local files。{}", taskTrackerJobResult);
                                try {
                                    retryScheduler.inSchedule(
                                            taskTrackerJobResult.getJobWrapper().getJobId().concat("_") + SystemClock.now(),
                                            taskTrackerJobResult);
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
                            taskTrackerJobResult.getJobWrapper().getJobId().concat("_") + SystemClock.now(),
                            taskTrackerJobResult);
                } catch (Exception e1) {
                    LOGGER.error("Save files failed, {}", taskTrackerJobResult.getJobWrapper(), e1);
                }
            }

            return returnResponse.getJobWrapper();
        }
    }

    /**
     * 发送JobResults
     */
    private boolean retrySendJobResults(List<TaskTrackerJobResult> results) {
        // 发送消息给 JobTracker
        JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
        requestBody.setTaskTrackerJobResults(results);
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
