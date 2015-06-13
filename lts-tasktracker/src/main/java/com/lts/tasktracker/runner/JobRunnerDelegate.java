package com.lts.tasktracker.runner;

import com.lts.core.domain.JobWrapper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.domain.Response;
import com.lts.tasktracker.domain.TaskTrackerApplication;
import com.lts.tasktracker.logger.BizLoggerFactory;
import com.lts.tasktracker.logger.BizLoggerImpl;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 *         Job Runner 的代理类,  要做一些错误处理之类的
 */
public class JobRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("LTS.TaskTracker");
    private JobWrapper jobWrapper;
    private RunnerCallback callback;
    private BizLoggerImpl logger;
    private TaskTrackerApplication application;

    public JobRunnerDelegate(TaskTrackerApplication application,
                             JobWrapper jobWrapper, RunnerCallback callback) {
        this.jobWrapper = jobWrapper;
        this.callback = callback;
        this.application = application;
        this.logger = (BizLoggerImpl) BizLoggerFactory.getLogger(
                application.getBizLogLevel(),
                application.getRemotingClient(), application);
    }

    @Override
    public void run() {
        try {
            LtsLoggerFactory.setLogger(logger);

            while (jobWrapper != null) {
                // 设置当前context中的jobId
                logger.setId(jobWrapper.getJobId(), jobWrapper.getJob().getTaskId());
                Response response = new Response();
                response.setJobWrapper(jobWrapper);
                try {
                    application.getRunnerPool().getRunningJobManager().in(jobWrapper.getJobId());
                    Result result = application.getRunnerPool().getRunnerFactory().newRunner().run(jobWrapper.getJob());
                    if (result == null) {
                        response.setSuccess(true);
                    } else {
                        response.setSuccess(result.isSuccess());
                        response.setMsg(result.getMsg());
                    }
                    LOGGER.info("Job exec success : {}", jobWrapper);
                } catch (Throwable t) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    response.setSuccess(false);
                    response.setMsg(sw.toString());
                    LOGGER.info("Job exec error : {} {}", jobWrapper, t.getMessage(), t);
                } finally {
                    logger.removeId();
                    application.getRunnerPool().getRunningJobManager().out(jobWrapper.getJobId());
                }
                jobWrapper = callback.runComplete(response);
            }
        } finally {
            LtsLoggerFactory.remove();
        }
    }

}
