package com.lts.job.tracker.support;

import com.lts.job.core.domain.JobResult;
import com.lts.job.core.repository.JobFeedbackQueueMongoRepository;
import com.lts.job.core.repository.po.JobFeedbackQueuePo;
import com.lts.job.core.support.SingletonBeanContext;
import com.lts.job.core.util.CollectionUtils;
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
    private JobFeedbackQueueMongoRepository jobFeedbackQueueMongoRepository;
    private volatile boolean start = false;
    private ClientNotifier clientNotifier;

    /**
     * 是否已经启动
     *
     * @return
     */
    private boolean isStart() {
        return start;
    }

    public FeedbackJobSendChecker() {
        jobFeedbackQueueMongoRepository = SingletonBeanContext.getBean(JobFeedbackQueueMongoRepository.class);
        clientNotifier = new ClientNotifier(RemotingServerManager.getRemotingServer(), new ClientNotifyHandler() {
            @Override
            public void handleSuccess(List<JobResult> jobResults) {
                for (JobResult jobResult : jobResults) {
                    jobFeedbackQueueMongoRepository.delJobFeedback(((JobFeedbackQueuePo) jobResult).getId());
                }
            }

            @Override
            public void handleFailed(List<JobResult> jobResults) {
                // do nothing
            }
        });
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

                long count = jobFeedbackQueueMongoRepository.count();
                if (count == 0) {
                    return;
                }
                LOGGER.info("一共有{}个完成的任务要通知客户端.", count);

                List<JobFeedbackQueuePo> jobFeedbackQueuePos;
                int offset = 0;
                int limit = 5;
                do {
                    jobFeedbackQueuePos = jobFeedbackQueueMongoRepository.get(offset, limit);
                    if (CollectionUtils.isEmpty(jobFeedbackQueuePos)) {
                        return;
                    }
                    List<JobResult> jobResults = new ArrayList<JobResult>(jobFeedbackQueuePos.size());
                    for (JobFeedbackQueuePo jobFeedbackQueuePo : jobFeedbackQueuePos) {
                        jobResults.add(jobFeedbackQueuePo);
                    }
                    clientNotifier.send(jobResults);

                    offset += limit;

                } while (jobFeedbackQueuePos != null && jobFeedbackQueuePos.size() > 0);

            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }


}
