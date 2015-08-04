package com.lts.tasktracker.runner;

import com.lts.core.commons.collect.ConcurrentHashSet;
import com.lts.core.constant.EcTopic;
import com.lts.core.domain.JobWrapper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;
import com.lts.tasktracker.domain.TaskTrackerApplication;
import com.lts.tasktracker.expcetion.NoAvailableJobRunnerException;

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

        runnerFactory = application.getRunnerFactory();
        if (runnerFactory == null) {
            runnerFactory = new DefaultRunnerFactory(application);
        }
        // 向事件中心注册事件, 改变工作线程大小
        application.getEventCenter().subscribe(
                new EventSubscriber(application.getConfig().getIdentity(), new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        setMaximumPoolSize(application.getConfig().getWorkThreads());
                    }
                }), EcTopic.WORK_THREAD_CHANGE);
    }

    public void execute(JobWrapper jobWrapper, RunnerCallback callback) throws NoAvailableJobRunnerException {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(application, jobWrapper, callback));
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
     *
     * @return
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
        private final Set<String> RUNNING_JOB_ID_SET = new ConcurrentHashSet<String>();

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
                LOGGER.debug("Running jobs ：" + RUNNING_JOB_ID_SET);
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
