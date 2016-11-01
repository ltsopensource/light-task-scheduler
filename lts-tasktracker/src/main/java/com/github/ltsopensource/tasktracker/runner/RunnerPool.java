package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.ec.EventInfo;
import com.github.ltsopensource.ec.EventSubscriber;
import com.github.ltsopensource.ec.Observer;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;
import com.github.ltsopensource.tasktracker.expcetion.NoAvailableJobRunnerException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         线程池管理
 */
public class RunnerPool {

    private final Logger LOGGER = LoggerFactory.getLogger(RunnerPool.class);

    private ThreadPoolExecutor threadPoolExecutor = null;

    private RunnerFactory runnerFactory;
    private TaskTrackerAppContext appContext;
    private RunningJobManager runningJobManager;

    public RunnerPool(final TaskTrackerAppContext appContext) {
        this.appContext = appContext;
        this.runningJobManager = new RunningJobManager();

        threadPoolExecutor = initThreadPoolExecutor();

        runnerFactory = appContext.getRunnerFactory();
        if (runnerFactory == null) {
            runnerFactory = new DefaultRunnerFactory(appContext);
        }
        // 向事件中心注册事件, 改变工作线程大小
        appContext.getEventCenter().subscribe(
                new EventSubscriber(appContext.getConfig().getIdentity(), new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        setWorkThread(appContext.getConfig().getWorkThreads());
                    }
                }), EcTopic.WORK_THREAD_CHANGE);
    }

    private ThreadPoolExecutor initThreadPoolExecutor() {
        int workThreads = appContext.getConfig().getWorkThreads();

        return new ThreadPoolExecutor(workThreads, workThreads, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),           // 直接提交给线程而不保持它们
                new NamedThreadFactory("JobRunnerPool"),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public void execute(JobMeta jobMeta, RunnerCallback callback) throws NoAvailableJobRunnerException {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(appContext, jobMeta, callback));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Receive job success ! " + jobMeta);
            }
        } catch (RejectedExecutionException e) {
            LOGGER.warn("No more thread to run job .");
            throw new NoAvailableJobRunnerException(e);
        }
    }

    /**
     * 得到当前可用的线程数
     */
    public int getAvailablePoolSize() {
        return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
    }

    public void setWorkThread(int workThread) {
        if (workThread == 0) {
            throw new IllegalArgumentException("workThread can not be zero!");
        }

        threadPoolExecutor.setMaximumPoolSize(workThread);
        threadPoolExecutor.setCorePoolSize(workThread);

        LOGGER.info("workThread update to {}", workThread);
    }

    /**
     * 得到最大线程数
     */
    public int getWorkThread() {
        return threadPoolExecutor.getCorePoolSize();
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    /**
     * 执行该方法，线程池的状态立刻变成STOP状态，并试图停止所有正在执行的线程，不再处理还在池队列中等待的任务，当然，它会返回那些未执行的任务。
     * 它试图终止线程的方法是通过调用Thread.interrupt()方法来实现的，但是大家知道，这种方法的作用有限，
     * 如果线程中没有sleep 、wait、Condition、定时锁等应用, interrupt()方法是无法中断当前的线程的。
     * 所以，ShutdownNow()并不代表线程池就一定立即就能退出，它可能必须要等待所有正在执行的任务都执行完成了才能退出。
     * 特殊的时候可以通过使用{@link InterruptibleJobRunner}来解决
     */
    public void stopWorking() {
        try {
            threadPoolExecutor.shutdownNow();
            Thread.sleep(1000);
            threadPoolExecutor = initThreadPoolExecutor();
            LOGGER.info("stop working succeed ");
        } catch (Throwable t) {
            LOGGER.error("stop working failed ", t);
        }
    }

    public void shutDown() {
        try {
            threadPoolExecutor.shutdownNow();
            LOGGER.info("stop working succeed ");
        } catch (Throwable t) {
            LOGGER.error("stop working failed ", t);
        }
    }

    /**
     * 用来管理正在执行的任务
     */
    public class RunningJobManager {

        private final ConcurrentMap<String/*jobId*/, JobRunnerDelegate> JOBS = new ConcurrentHashMap<String, JobRunnerDelegate>();

        public void in(String jobId, JobRunnerDelegate jobRunnerDelegate) {
            JOBS.putIfAbsent(jobId, jobRunnerDelegate);
        }

        public void out(String jobId) {
            JOBS.remove(jobId);
        }

        public boolean running(String jobId) {
            return JOBS.containsKey(jobId);
        }

        /**
         * 返回给定list中不存在的jobId
         */
        public List<String> getNotExists(List<String> jobIds) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ask jobs: " + jobIds + " Running jobs ：" + JOBS.keySet());
            }
            List<String> notExistList = new ArrayList<String>();
            for (String jobId : jobIds) {
                if (!running(jobId)) {
                    notExistList.add(jobId);
                }
            }
            return notExistList;
        }

        public void terminateJob(String jobId) {
            JobRunnerDelegate jobRunnerDelegate = JOBS.get(jobId);
            if (jobRunnerDelegate != null) {
                try {
                    jobRunnerDelegate.currentThread().interrupt();
                } catch (Throwable e) {
                    LOGGER.error("terminateJob [" + jobId + "]  error", e);
                }
            }
        }
    }

    public RunningJobManager getRunningJobManager() {
        return runningJobManager;
    }
}
