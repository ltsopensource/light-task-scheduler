package com.lts.job.tracker.support.checker;

import com.lts.job.core.domain.JobResult;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.queue.domain.JobFeedbackPo;
import com.lts.job.tracker.domain.JobClientNode;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.support.ClientNotifier;
import com.lts.job.tracker.support.ClientNotifyHandler;
import com.lts.job.tracker.support.OldDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private OldDataHandler oldDataHandler;
    private JobTrackerApplication application;

    /**
     * 是否已经启动
     *
     * @return
     */
    private boolean isStart() {
        return start;
    }

    public FeedbackJobSendChecker(final JobTrackerApplication application) {
        this.application = application;

        clientNotifier = new ClientNotifier(application, new ClientNotifyHandler<JobResultWrapper>() {
            @Override
            public void handleSuccess(List<JobResultWrapper> jobResults) {
                for (JobResultWrapper jobResult : jobResults) {
                    String submitNodeGroup = jobResult.getJob().getSubmitNodeGroup();
                    application.getJobFeedbackQueue().remove(submitNodeGroup, jobResult.getId());
                }
            }

            @Override
            public void handleFailed(List<JobResultWrapper> jobResults) {
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
                    , 30, 30, TimeUnit.SECONDS);
            start = true;
            LOGGER.info("feedback job checker started!");
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
            LOGGER.info("feedback job checker stopped!");
        }
    }

    private volatile boolean isRunning = false;

    private class Runner implements Runnable {
        @Override
        public void run() {
            try {
                if (isRunning) {
                    return;
                }
                isRunning = true;

                Set<String> taskTrackerNodeGroups = application.getJobClientManager().getNodeGroups();
                if (CollectionUtils.isEmpty(taskTrackerNodeGroups)) {
                    return;
                }

                for (String taskTrackerNodeGroup : taskTrackerNodeGroups) {
                    check(taskTrackerNodeGroup);
                }

            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            } finally {
                isRunning = false;
            }
        }

        private void check(String jobClientNodeGroup) {

            // check that node group job client
            JobClientNode jobClientNode = application.getJobClientManager().getAvailableJobClient(jobClientNodeGroup);
            if (jobClientNode == null) {
                return;
            }

            long count = application.getJobFeedbackQueue().getCount(jobClientNodeGroup);
            if (count == 0) {
                return;
            }

            LOGGER.info("{} job need to feedback.", count);
            // 检测是否有可用的客户端

            List<JobFeedbackPo> jobFeedbackPos;
            int limit = 5;
            do {
                jobFeedbackPos = application.getJobFeedbackQueue().fetchTop(jobClientNodeGroup, limit);
                if (CollectionUtils.isEmpty(jobFeedbackPos)) {
                    return;
                }
                List<JobResultWrapper> jobResults = new ArrayList<JobResultWrapper>(jobFeedbackPos.size());
                for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
                    // 判断是否是过时的数据，如果是，那么移除
                    if (oldDataHandler == null ||
                            (oldDataHandler != null && !oldDataHandler.handle(application.getJobFeedbackQueue(), jobFeedbackPo, jobFeedbackPo))) {
                        jobResults.add(new JobResultWrapper(jobFeedbackPo.getId(), jobFeedbackPo.getJobResult()));
                    }
                }
                // 返回发送成功的个数
                int sentSize = clientNotifier.send(jobResults);

                LOGGER.info("send to client: {} success, {} failed.", sentSize, jobResults.size() - sentSize);
            } while (jobFeedbackPos.size() > 0);
        }
    }

    private class JobResultWrapper extends JobResult {
        private String id;

        public String getId() {
            return id;
        }

        public JobResultWrapper(String id, JobResult jobResult) {
            this.id = id;
            setJob(jobResult.getJob());
            setMsg(jobResult.getMsg());
            setSuccess(jobResult.isSuccess());
            setTime(jobResult.getTime());
        }
    }

}


