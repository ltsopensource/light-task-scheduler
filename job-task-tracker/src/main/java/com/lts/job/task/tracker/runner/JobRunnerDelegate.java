package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.JobInfoException;
import com.lts.job.task.tracker.domain.Response;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.logger.BizLoggerFactory;
import com.lts.job.task.tracker.logger.BizLoggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 *         Job Runner 的代理类,  要做一些错误处理之类的
 */
public class JobRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("JobRunner");
    private Job job;
    private RunnerPool runnerPool;
    private RunnerCallback callback;
    private BizLoggerImpl logger;

    public JobRunnerDelegate(TaskTrackerApplication application,
                             Job job, RunnerCallback callback) {
        this.runnerPool = application.getRunnerPool();
        this.job = job;
        this.callback = callback;
        this.logger = (BizLoggerImpl) BizLoggerFactory.getLogger(
                application.getBizLogLevel(),
                application.getRemotingClient(), application);
    }

    @Override
    public void run() {

        try {
            LtsLogger.setLogger(logger);

            while (job != null) {
                // 设置当前context中的jobId
                logger.setJobId(job.getJobId());

                runnerPool.getRunningJobManager().in(job.getJobId());

                Response response = new Response();
                response.setJob(job);
                try {
                    runnerPool.getRunnerFactory().newRunner().run(job);
                    response.setSuccess(true);
                    LOGGER.info("执行任务成功 : {}", job);
                } catch (Throwable t) {
                    response.setSuccess(false);

                    if (t instanceof JobInfoException) {
                        LOGGER.warn("任务执行失败: {} {}", job, t.getMessage());
                        response.setMsg(t.getMessage());
                    } else {
                        StringWriter sw = new StringWriter();
                        t.printStackTrace(new PrintWriter(sw));
                        response.setMsg(sw.toString());
                        LOGGER.info("任务执行失败: {} {}", job, t.getMessage(), t);
                    }
                } finally {
                    runnerPool.getRunningJobManager().out(job.getJobId());
                }
                job = callback.runComplete(response);
            }
        } finally {
            LtsLogger.remove();
        }
    }

}
