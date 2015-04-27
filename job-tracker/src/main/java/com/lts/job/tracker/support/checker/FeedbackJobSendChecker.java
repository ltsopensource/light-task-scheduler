package com.lts.job.tracker.support.checker;

import com.lts.job.core.domain.JobResult;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.queue.JobFeedbackPo;
import com.lts.job.tracker.queue.JobFeedbackQueue;
import com.lts.job.tracker.support.ClientNotifier;
import com.lts.job.tracker.support.ClientNotifyHandler;
import com.lts.job.tracker.support.OldDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 *         用来检查 执行完成的任务, 发送给客户端失败的 由master节点来做
 *         单利
 */
public class FeedbackJobSendChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackJobSendChecker.class);

    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE;
    private volatile boolean start = false;
    private ClientNotifier clientNotifier;
    private JobFeedbackQueue jobFeedbackQueue;
    private OldDataHandler oldDataHandler;

    /**
     * 是否已经启动
     *
     * @return
     */
    private boolean isStart() {
        return start;
    }

    public FeedbackJobSendChecker(JobTrackerApplication application) {
        this.jobFeedbackQueue = application.getJobFeedbackQueue();
        clientNotifier = new ClientNotifier(application, new ClientNotifyHandler() {
            @Override
            public void handleSuccess(List<JobResult> jobResults) {
                for (JobResult jobResult : jobResults) {
                    jobFeedbackQueue.remove(((JobFeedbackPo) jobResult).getId());
                }
            }

            @Override
            public void handleFailed(List<JobResult> jobResults) {
                // do nothing
            }
        });
        this.oldDataHandler = application.getOldDataHandler();
    }

    /**
     * 启动
     */
    public void start() {
        if (!start) {
            RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
            RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runner()
                    , 60, 30, TimeUnit.SECONDS);
            start = true;
            LOGGER.info("完成任务重发发送定时器启动成功!");
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (start) {
            RETRY_EXECUTOR_SERVICE.shutdown();
            RETRY_EXECUTOR_SERVICE = null;
            start = false;
            LOGGER.info("完成任务重发发送定时器关闭成功!");
        }
    }

    private class Runner implements Runnable {
        @Override
        public void run() {
            try {
                long count = jobFeedbackQueue.count();
                if (count == 0) {
                    return;
                }
                LOGGER.info("一共有{}个完成的任务要通知客户端.", count);

                List<JobFeedbackPo> jobFeedbackPos;
                int limit = 5;
                int offset = 0;
                do {
                    jobFeedbackPos = jobFeedbackQueue.fetch(offset, limit);
                    if (CollectionUtils.isEmpty(jobFeedbackPos)) {
                        return;
                    }
                    List<JobResult> jobResults = new ArrayList<JobResult>(jobFeedbackPos.size());
                    for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
                        // 判断是否是过时的数据，如果是，那么移除
                        if (oldDataHandler == null ||
                                (oldDataHandler != null && !oldDataHandler.handleJobFeedbackPo(jobFeedbackQueue, jobFeedbackPo, jobFeedbackPo))) {
                            jobResults.add(jobFeedbackPo);
                        }
                    }
                    // 返回发送成功的个数
                    int sentSize = clientNotifier.send(jobResults);

                    LOGGER.info("发送客户端: {}个成功, {}个失败.", sentSize, jobResults.size() - sentSize);
                    offset += (jobResults.size() - sentSize);
                } while (jobFeedbackPos.size() > 0);

            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }
}
