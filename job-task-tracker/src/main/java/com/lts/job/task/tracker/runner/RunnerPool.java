package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.expcetion.NoAvailableJobRunnerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         线程池管理
 */
public class RunnerPool {

    private final Logger LOGGER = LoggerFactory.getLogger("RunnerPool");

    private ThreadPoolExecutor threadPoolExecutor = null;
    // 定时更新可用线程
    private ScheduledExecutorService REFRESH_EXECUTOR_SERVICE = null;

    private RunnerFactory runnerFactory;
    private TaskTrackerApplication application;
    private RunningJobManager runningJobManager;

    public RunnerPool(final TaskTrackerApplication application) {
        this.application = application;
        this.runningJobManager = new RunningJobManager();
        int maxSize = application.getConfig().getWorkThreads();
        int minSize = 4 > maxSize ? maxSize : 4;

        threadPoolExecutor = new ThreadPoolExecutor(minSize, maxSize, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),           // 直接提交给线程而不保持它们
                new ThreadPoolExecutor.AbortPolicy());      // A handler for rejected tasks that throws a

        REFRESH_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
        REFRESH_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                application.setAvailableThreads(getAvailablePoolSize());
            }
        }, 60, 30, TimeUnit.SECONDS);

        runnerFactory = application.getRunnerFactory();
        if (runnerFactory == null) {
            runnerFactory = new DefaultRunnerFactory(application);
        }

    }

    public void execute(Job job, RunnerCallback callback) throws NoAvailableJobRunnerException {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(application, job, callback));
            // 更新应用可用线程数
            application.setAvailableThreads(getAvailablePoolSize());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("receive job success ! " + job);
            }
        } catch (RejectedExecutionException e) {
            LOGGER.warn("there has no available thread to run job .");
            throw new NoAvailableJobRunnerException(e);
        }
    }

    /**
     * 得到当前可用的线程数
     *
     * @return
     */
    public int getAvailablePoolSize() {
        return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
    }

    /**
     * 得到最大线程数
     *
     * @return
     */
    public int getMaximumPoolSize() {
        return threadPoolExecutor.getMaximumPoolSize();
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    /**
     * 用来管理正在执行的任务
     */
    public class RunningJobManager {

        private final Set<String/*jobId*/> RUNNING_JOB_ID_SET = new ConcurrentHashSet<String>();

        public void in(String jobId) {
            RUNNING_JOB_ID_SET.add(jobId);
        }

        public void out(String jobId) {
            RUNNING_JOB_ID_SET.remove(jobId);
        }

        public boolean running(String jobId) {
            return RUNNING_JOB_ID_SET.contains(jobId);
        }

        /**
         * 返回给定list中不存在的jobId
         *
         * @param jobIds
         * @return
         */
        public List<String> getNotExists(List<String> jobIds) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("running jobs ：" + RUNNING_JOB_ID_SET);
                LOGGER.debug("ask jobs:" + jobIds);
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
