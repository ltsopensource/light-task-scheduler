package com.lts.job.task.tracker.runner;

import com.lts.job.common.domain.Job;
import com.lts.job.common.exception.JobInfoException;
import com.lts.job.task.tracker.domain.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 * Job Runner 的代理类,  要做一些错误处理之类的
 */
public class JobRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("JobRunner");
    private JobRunner jobRunner;
    private Job job;
    private RunnerCallback callback;

    public JobRunnerDelegate(JobRunner jobRunner, Job job, RunnerCallback callback) {
        this.jobRunner = jobRunner;
        this.job = job;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (job != null) {

            RunnerPool.RunningJobManager.in(job.getJobId());

            Response response = new Response();
            response.setJob(job);
            try {
                jobRunner.run(job);
                response.setSuccess(true);
                LOGGER.info("执行任务成功 : " + job);
            } catch (Throwable t) {
                response.setSuccess(false);

                if( t instanceof JobInfoException){
                    LOGGER.warn("任务执行失败: " + job + " " + t.getMessage());
                    response.setMsg(t.getMessage());
                }else{
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    response.setMsg(sw.toString());
                    LOGGER.info("任务执行失败: " + job + " " + t.getMessage(), t);
                }
            } finally {
                RunnerPool.RunningJobManager.out(job.getJobId());
            }
            job = callback.runComplete(response);

            if (job != null) {
                this.jobRunner = RunnerFactory.newRunner();
            }
        }
    }
}
