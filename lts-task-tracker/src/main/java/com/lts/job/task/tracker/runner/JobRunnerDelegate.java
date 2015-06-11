package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.JobInfoException;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.task.tracker.domain.Response;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.logger.BizLoggerFactory;
import com.lts.job.task.tracker.logger.BizLoggerImpl;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 *         Job Runner 的代理类,  要做一些错误处理之类的
 */
public class JobRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("LTS.TaskTracker");
    private Job job;
    private RunnerCallback callback;
    private BizLoggerImpl logger;
    private TaskTrackerApplication application;

    public JobRunnerDelegate(TaskTrackerApplication application,
                             Job job, RunnerCallback callback) {
        this.job = job;
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

            while (job != null) {
                // 设置当前context中的jobId
                logger.setId(job.getJobId(), job.getTaskId());
                Response response = new Response();
                response.setJob(job);
                try {
                    application.getRunnerPool().getRunningJobManager().in(job.getJobId());

                    application.getRunnerPool().getRunnerFactory().newRunner().run(job);
                    response.setSuccess(true);
                    LOGGER.info("Job exec success : {}", job);
                } catch (Throwable t) {
                    response.setSuccess(false);

                    if (t instanceof JobInfoException) {
                        LOGGER.warn("Job exec failed : {} {}", job, t.getMessage());
                        response.setMsg(t.getMessage());
                    } else {
                        StringWriter sw = new StringWriter();
                        t.printStackTrace(new PrintWriter(sw));
                        response.setMsg(sw.toString());
                        LOGGER.info("Job exec error : {} {}", job, t.getMessage(), t);
                    }
                } finally {
                    logger.removeId();
                    application.getRunnerPool().getRunningJobManager().out(job.getJobId());
                }
                job = callback.runComplete(response);
            }
        } finally {
            LtsLoggerFactory.remove();
        }
    }

}
