package com.lts.job.tracker.support;

import com.lts.job.common.domain.JobResult;
import com.lts.job.common.exception.RemotingSendException;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.JobFinishedRequest;
import com.lts.job.common.support.JobDomainConverter;
import com.lts.job.common.support.SingletonBeanContext;
import com.lts.job.common.util.CollectionUtils;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobClientNode;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.common.repository.JobMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 * 用来检查 执行完成的任务, 发送给客户端失败的 由master节点来做
 * 单利
 */
public class FinishedJobSendChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinishedJobSendChecker.class);

    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE;
    private JobMongoRepository jobRepository;
    private volatile boolean start = false;

    /**
     * 是否已经启动
     *
     * @return
     */
    private boolean isStart() {
        return start;
    }

    public FinishedJobSendChecker() {
        jobRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
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
                // 从数据库 中取出所有已经完成的任务
                List<JobPo> jobPos = jobRepository.getFinishedJob();
                if (CollectionUtils.isEmpty(jobPos)) {
                    return;
                }
                LOGGER.info("一共有" + jobPos.size() +"个完成的任务要通知客户端.");

                List<JobResult> jobResults = JobDomainConverter.convert(jobPos);

                // 有多个要进行分组 (出现在 失败重发的时候)
                Map<String/*nodeGroup*/, List<JobResult>> groupMap = new HashMap<String, List<JobResult>>();

                for (JobResult jobResult : jobResults) {
                    List<JobResult> jobResultList = groupMap.get(jobResult.getJob().getNodeGroup());
                    if (jobResultList == null) {
                        jobResultList = new ArrayList<JobResult>();
                        groupMap.put(jobResult.getJob().getNodeGroup(), jobResultList);
                    }
                    jobResultList.add(jobResult);
                }
                for (Map.Entry<String, List<JobResult>> entry : groupMap.entrySet()) {
                    send0(entry.getKey(), entry.getValue());
                }
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    /**
     * 发送给客户端
     * 返回是否发送成功还是失败
     *
     * @param nodeGroup
     * @param jobResults
     * @return
     */
    private boolean send0(String nodeGroup, List<JobResult> jobResults) {
        // 得到 可用的客户端节点
        JobClientNode jobClientNode = JobClientManager.INSTANCE.getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }

        JobFinishedRequest requestBody = new JobFinishedRequest();
        requestBody.setJobResults(jobResults);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_FINISHED.code(), requestBody);
        try {
            RemotingCommand commandResponse = RemotingServerManager.getRemotingServer().invokeSync(jobClientNode.getChannel().getChannel(), commandRequest);

            if (commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                for (JobResult jobResult : jobResults) {
                    jobRepository.delJob(jobResult.getJob().getJobId());
                }
                return true;
            }
        } catch (RemotingSendException e) {
            LOGGER.error("通知客户端失败!", e);
        } catch (RemotingCommandFieldCheckException e) {
            LOGGER.error("通知客户端失败!", e);
        }
        return false;
    }

}
