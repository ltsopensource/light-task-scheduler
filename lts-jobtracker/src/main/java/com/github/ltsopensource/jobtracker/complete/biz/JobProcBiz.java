package com.github.ltsopensource.jobtracker.complete.biz;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.jobtracker.complete.JobFinishHandler;
import com.github.ltsopensource.jobtracker.complete.JobRetryHandler;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.support.ClientNotifier;
import com.github.ltsopensource.jobtracker.support.ClientNotifyHandler;
import com.github.ltsopensource.queue.domain.JobFeedbackPo;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务完成
 *
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobProcBiz implements JobCompletedBiz {

    private ClientNotifier clientNotifier;
    private final JobRetryHandler retryHandler;
    private final JobFinishHandler jobFinishHandler;
    // 任务的最大重试次数
    private final Integer globalMaxRetryTimes;

    public JobProcBiz(final JobTrackerAppContext appContext) {
        this.retryHandler = new JobRetryHandler(appContext);
        this.jobFinishHandler = new JobFinishHandler(appContext);

        this.globalMaxRetryTimes = appContext.getConfig().getParameter(ExtConfig.JOB_MAX_RETRY_TIMES,
                Constants.DEFAULT_JOB_MAX_RETRY_TIMES);

        this.clientNotifier = new ClientNotifier(appContext, new ClientNotifyHandler<JobRunResult>() {
            @Override
            public void handleSuccess(List<JobRunResult> results) {
                jobFinishHandler.onComplete(results);
            }

            @Override
            public void handleFailed(List<JobRunResult> results) {
                if (CollectionUtils.isNotEmpty(results)) {
                    List<JobFeedbackPo> jobFeedbackPos = new ArrayList<JobFeedbackPo>(results.size());

                    for (JobRunResult result : results) {
                        JobFeedbackPo jobFeedbackPo = JobDomainConverter.convert(result);
                        jobFeedbackPos.add(jobFeedbackPo);
                    }
                    // 2. 失败的存储在反馈队列
                    appContext.getJobFeedbackQueue().add(jobFeedbackPos);
                    // 3. 完成任务 
                    jobFinishHandler.onComplete(results);
                }
            }
        });
    }

    @Override
    public RemotingCommand doBiz(JobCompletedRequest request) {

        List<JobRunResult> results = request.getJobRunResults();

        if (CollectionUtils.sizeOf(results) == 1) {
            singleResultsProcess(results);
        } else {
            multiResultsProcess(results);
        }
        return null;
    }

    private void singleResultsProcess(List<JobRunResult> results) {
        JobRunResult result = results.get(0);

        if (!needRetry(result)) {
            // 这种情况下，如果要反馈客户端的，直接反馈客户端，不进行重试
            if (isNeedFeedback(result.getJobMeta().getJob())) {
                clientNotifier.send(results);
            } else {
                jobFinishHandler.onComplete(results);
            }
        } else {
            // 需要retry
            retryHandler.onComplete(results);
        }
    }

    /**
     * 判断任务是否需要加入重试队列
     */
    private boolean needRetry(JobRunResult result) {
        // 判断类型
        if (!(Action.EXECUTE_LATER.equals(result.getAction())
                || Action.EXECUTE_EXCEPTION.equals(result.getAction()))) {
            return false;
        }

        // 判断重试次数
        Job job = result.getJobMeta().getJob();
        Integer retryTimes = result.getJobMeta().getRetryTimes();
        int jobMaxRetryTimes = job.getMaxRetryTimes();
        return !(retryTimes >= globalMaxRetryTimes || retryTimes >= jobMaxRetryTimes);
    }

    /**
     * 这里情况一般是发送失败，重新发送的
     */
    private void multiResultsProcess(List<JobRunResult> results) {

        List<JobRunResult> retryResults = null;
        // 过滤出来需要通知客户端的
        List<JobRunResult> feedbackResults = null;
        // 不需要反馈的
        List<JobRunResult> finishResults = null;

        for (JobRunResult result : results) {

            if (needRetry(result)) {
                // 需要加入到重试队列的
                retryResults = CollectionUtils.newArrayListOnNull(retryResults);
                retryResults.add(result);
            } else if (isNeedFeedback(result.getJobMeta().getJob())) {
                // 需要反馈给客户端
                feedbackResults = CollectionUtils.newArrayListOnNull(feedbackResults);
                feedbackResults.add(result);
            } else {
                // 不用反馈客户端，也不用重试，直接完成处理
                finishResults = CollectionUtils.newArrayListOnNull(finishResults);
                finishResults.add(result);
            }
        }

        // 通知客户端
        clientNotifier.send(feedbackResults);

        // 完成任务
        jobFinishHandler.onComplete(finishResults);

        // 将任务加入到重试队列
        retryHandler.onComplete(retryResults);
    }

    private boolean isNeedFeedback(Job job) {
        if (job == null) {
            return false;
        }
        // 容错,如果没有提交节点组,那么不反馈
        return !StringUtils.isEmpty(job.getSubmitNodeGroup()) && job.isNeedFeedback();
    }

}
