package com.lts.tasktracker.runner;

import com.lts.core.commons.collect.ConcurrentHashSet;
import com.lts.core.constant.EcTopic;
import com.lts.core.domain.JobWrapper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;
import com.lts.tasktracker.domain.TaskTrackerAppContext;
import com.lts.tasktracker.expcetion.NoAvailableJobRunnerException;
import sun.nio.ch.Interruptible;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         线程池管理
 */
public class RunnerPool {

    private final Logger LOGGER = LoggerFactory.getLogger("LTS.RunnerPool");

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
                        setMaximumPoolSize(appContext.getConfig().getWorkThreads());
                    }
                }), EcTopic.WORK_THREAD_CHANGE);
    }

    private ThreadPoolExecutor initThreadPoolExecutor() {
        int maxSize = appContext.getConfig().getWorkThreads();
        int minSize = 4 > maxSize ? maxSize : 4;

        return new ThreadPoolExecutor(minSize, maxSize, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),           // 直接提交给线程而不保持它们
                new ThreadPoolExecutor.AbortPolicy());
    }

    public void execute(JobWrapper jobWrapper, RunnerCallback callback) throws NoAvailableJobRunnerException {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(appContext, jobWrapper, callback));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Receive job success ! " + jobWrapper);
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

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize == 0) {
            throw new IllegalArgumentException("maximumPoolSize can not be zero!");
        }

        int corePollSize = threadPoolExecutor.getCorePoolSize();
        if (maximumPoolSize < corePollSize) {
            threadPoolExecutor.setCorePoolSize(maximumPoolSize);
        }
        threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);

        LOGGER.info("maximumPoolSize update to {}", maximumPoolSize);
    }

    /**
     * 得到最大线程数
     */
    public int getMaximumPoolSize() {
        return threadPoolExecutor.getMaximumPoolSize();
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

    /**
     * 用来管理正在执行的任务
     */
    public class RunningJobManager {

        private final Set<String> JOB_SET = new ConcurrentHashSet<String>();

        public void in(String jobId) {
            JOB_SET.add(jobId);
        }

        public void out(String jobId) {
            JOB_SET.remove(jobId);
        }

        public boolean running(String jobId) {
            return JOB_SET.contains(jobId);
        }

        /**
         * 返回给定list中不存在的jobId
         */
        public List<String> getNotExists(List<String> jobIds) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Running jobs ：" + JOB_SET);
                LOGGER.debug("Ask jobs:" + jobIds);
            }
            List<String> notExistList = new ArrayList<String>();
            for (String jobId : jobIds) {
                if (!running(jobId)) {
                    notExistList.add(jobId);
                }
            }
            return notExistList;
        }
    }

    public RunningJobManager getRunningJobManager() {
        return runningJobManager;
    }
}
