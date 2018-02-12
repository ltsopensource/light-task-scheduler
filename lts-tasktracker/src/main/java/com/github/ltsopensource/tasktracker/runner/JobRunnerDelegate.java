package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.commons.utils.DotLogUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.domain.Response;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;
import com.github.ltsopensource.tasktracker.logger.BizLogger;
import com.github.ltsopensource.tasktracker.logger.BizLoggerAdapter;
import com.github.ltsopensource.tasktracker.logger.BizLoggerFactory;
import com.github.ltsopensource.tasktracker.monitor.TaskTrackerMStatReporter;
import sun.nio.ch.Interruptible;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Job Runner 的代理类,
 * 1. 做一些错误处理之类的
 * 2. 监控统计
 * 3. Context信息设置
 *
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public class JobRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDelegate.class);
    private JobMeta jobMeta;
    private RunnerCallback callback;
    private TaskTrackerAppContext appContext;
    private TaskTrackerMStatReporter stat;
    private Interruptible interruptor;
    private JobRunner curJobRunner;
    private AtomicBoolean interrupted = new AtomicBoolean(false);
    private Thread thread;

    public JobRunnerDelegate(TaskTrackerAppContext appContext,
                             JobMeta jobMeta, RunnerCallback callback) {
        this.appContext = appContext;
        this.callback = callback;
        this.jobMeta = jobMeta;

        stat = (TaskTrackerMStatReporter) appContext.getMStatReporter();

        this.interruptor = new InterruptibleAdapter() {
            public void interrupt() {
                JobRunnerDelegate.this.interrupt();
            }
        };
    }

    @Override
    public void run() {

        thread = Thread.currentThread();

        try {
            blockedOn(interruptor);
            if (Thread.currentThread().isInterrupted()) {
                ((InterruptibleAdapter) interruptor).interrupt();
            }

            while (jobMeta != null) {
                long startTime = SystemClock.now();
                // 设置当前context中的jobId
                Response response = new Response();
                response.setJobMeta(jobMeta);

                BizLoggerAdapter logger = (BizLoggerAdapter) BizLoggerFactory.getLogger(
                        appContext.getBizLogLevel(),
                        appContext.getRemotingClient(), appContext);

                try {
                    appContext.getRunnerPool().getRunningJobManager()
                            .in(jobMeta.getJobId(), this);
                    this.curJobRunner = appContext.getRunnerPool().getRunnerFactory().newRunner();
                    Result result = this.curJobRunner.run(buildJobContext(logger, jobMeta));

                    if (result == null) {
                        response.setAction(Action.EXECUTE_SUCCESS);
                    } else {
                        if (result.getAction() == null) {
                            response.setAction(Action.EXECUTE_SUCCESS);
                        } else {
                            response.setAction(result.getAction());
                        }
                        response.setMsg(result.getMsg());
                    }

                    long time = SystemClock.now() - startTime;
                    stat.addRunningTime(time);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job execute completed : {}, time:{} ms.", jobMeta.getJob(), time);
                    }
                } catch (Throwable t) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    response.setAction(Action.EXECUTE_EXCEPTION);
                    response.setMsg(sw.toString());
                    long time = SystemClock.now() - startTime;
                    stat.addRunningTime(time);
                    LOGGER.error("Job execute error : {}, time: {}, {}", jobMeta.getJob(), time, t.getMessage(), t);
                } finally {
                    checkInterrupted(logger);
                    appContext.getRunnerPool().getRunningJobManager()
                            .out(jobMeta.getJobId());
                }
                // 统计数据
                stat(response.getAction());

                if (isStopToGetNewJob()) {
                    response.setReceiveNewJob(false);
                }
                this.jobMeta = callback.runComplete(response);
                DotLogUtils.dot("JobRunnerDelegate.run get job " + (this.jobMeta == null ? "NULL" : "NOT_NULL"));
            }
        } finally {
            blockedOn(null);
        }
    }

    private JobContext buildJobContext(BizLoggerAdapter logger, JobMeta jobMeta) {
        JobContext jobContext = new JobContext();
        // 采用deepopy的方式 防止用户修改任务数据
        Job job = JobUtils.copy(jobMeta.getJob());
        job.setTaskId(jobMeta.getRealTaskId());     // 这个对于用户需要转换为用户提交的taskId
        jobContext.setJob(job);

        JobExtInfo jobExtInfo = new JobExtInfo();
        jobExtInfo.setRepeatedCount(jobMeta.getRepeatedCount());
        jobExtInfo.setRetryTimes(jobMeta.getRetryTimes());
        jobExtInfo.setRetry(Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(Constants.IS_RETRY_JOB)));
        jobExtInfo.setJobType(jobMeta.getJobType());
        jobExtInfo.setSeqId(jobMeta.getInternalExtParam(Constants.EXE_SEQ_ID));

        jobContext.setJobExtInfo(jobExtInfo);

        logger.setJobMeta(jobMeta);
        jobContext.setBizLogger(logger);
        return jobContext;
    }

    private void interrupt() {
        if (!interrupted.compareAndSet(false, true)) {
            return;
        }
        if (this.curJobRunner != null && this.curJobRunner instanceof InterruptibleJobRunner) {
            ((InterruptibleJobRunner) this.curJobRunner).interrupt();
        }
    }

    private boolean isInterrupted() {
        return this.interrupted.get();
    }

    private void stat(Action action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case EXECUTE_SUCCESS:
                stat.incSuccessNum();
                break;
            case EXECUTE_FAILED:
                stat.incFailedNum();
                break;
            case EXECUTE_LATER:
                stat.incExeLaterNum();
                break;
            case EXECUTE_EXCEPTION:
                stat.incExeExceptionNum();
                break;
        }
    }

    private static void blockedOn(Interruptible interruptible) {
        sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(), interruptible);
    }

    private abstract class InterruptibleAdapter implements Interruptible {
        // for > jdk7
        public void interrupt(Thread thread) {
            interrupt();
        }

        public abstract void interrupt();
    }

    private boolean isStopToGetNewJob() {
        if (isInterrupted()) {
            // 如果当前线程被阻断了,那么也就不接受新任务了
            return true;
        }
        // 机器资源是否充足
        return !appContext.getConfig().getInternalData(Constants.MACHINE_RES_ENOUGH, true);
    }

    private void checkInterrupted(BizLogger logger) {
        try {
            if (isInterrupted()) {
                logger.info("SYSTEM:Interrupted");
            }
        } catch (Throwable t) {
            LOGGER.warn("checkInterrupted error", t);
        }
    }

    public Thread currentThread() {
        return thread;
    }

    public JobMeta currentJob() {
        return jobMeta;
    }
}
