package com.lts.tasktracker.runner;

import com.lts.core.constant.Constants;
import com.lts.core.domain.Action;
import com.lts.core.domain.JobWrapper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.LoggerName;
import com.lts.core.support.SystemClock;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.domain.Response;
import com.lts.tasktracker.domain.TaskTrackerAppContext;
import com.lts.tasktracker.logger.BizLoggerAdapter;
import com.lts.tasktracker.logger.BizLoggerFactory;
import com.lts.tasktracker.monitor.TaskTrackerMStatReporter;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.TaskTracker);
    private JobWrapper jobWrapper;
    private RunnerCallback callback;
    private BizLoggerAdapter logger;
    private TaskTrackerAppContext appContext;
    private TaskTrackerMStatReporter stat;
    private Interruptible interruptor;
    private JobRunner curJobRunner;
    private AtomicBoolean interrupted = new AtomicBoolean(false);
    private Thread thread;

    public JobRunnerDelegate(TaskTrackerAppContext appContext,
                             JobWrapper jobWrapper, RunnerCallback callback) {
        this.appContext = appContext;
        this.callback = callback;
        this.jobWrapper = jobWrapper;

        this.logger = (BizLoggerAdapter) BizLoggerFactory.getLogger(
                appContext.getBizLogLevel(),
                appContext.getRemotingClient(), appContext);
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

            LtsLoggerFactory.setLogger(logger);

            while (jobWrapper != null) {
                long startTime = SystemClock.now();
                // 设置当前context中的jobId
                logger.setId(jobWrapper.getJobId(), jobWrapper.getJob().getTaskId());
                Response response = new Response();
                response.setJobWrapper(jobWrapper);
                try {
                    appContext.getRunnerPool().getRunningJobManager()
                            .in(jobWrapper.getJobId(), this);
                    this.curJobRunner = appContext.getRunnerPool().getRunnerFactory().newRunner();
                    Result result = this.curJobRunner.run(jobWrapper.getJob());

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
                    LOGGER.info("Job execute completed : {}, time:{} ms.", jobWrapper, time);
                } catch (Throwable t) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    response.setAction(Action.EXECUTE_EXCEPTION);
                    response.setMsg(sw.toString());
                    long time = SystemClock.now() - startTime;
                    stat.addRunningTime(time);
                    LOGGER.info("Job execute error : {}, time: {}, {}", jobWrapper, time, t.getMessage(), t);
                } finally {
                    checkInterrupted();
                    logger.removeId();
                    appContext.getRunnerPool().getRunningJobManager()
                            .out(jobWrapper.getJobId());
                }
                // 统计数据
                stat(response.getAction());

                if (isStopToGetNewJob()) {
                    response.setReceiveNewJob(false);
                }
                this.jobWrapper = callback.runComplete(response);

            }
        } finally {
            LtsLoggerFactory.remove();

            blockedOn(null);
        }
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

    private void checkInterrupted() {
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

    public JobWrapper currentJob() {
        return jobWrapper;
    }
}
