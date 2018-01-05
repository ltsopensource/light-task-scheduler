package com.github.ltsopensource.jobtracker.complete;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.CronExpressionUtils;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobFinishHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishHandler.class);

    private JobTrackerAppContext appContext;

    public JobFinishHandler(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    public void onComplete(List<JobRunResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }

        for (JobRunResult result : results) {

            JobMeta jobMeta = result.getJobMeta();

            // 当前完成的job是否是重试的
            boolean isRetryForThisTime = Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(Constants.IS_RETRY_JOB));
            boolean isOnce = Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(Constants.ONCE));

            if (jobMeta.getJob().isCron()) {
                // 是 Cron任务
                if (isOnce) {
                    finishNoReplyPrevCronJob(jobMeta);
                } else {
                    finishCronJob(jobMeta.getJobId());
                }
            } else if (jobMeta.getJob().isRepeatable()) {
                if (isOnce) {
                    finishNoReplyPrevRepeatJob(jobMeta, isRetryForThisTime);
                } else {
                    finishRepeatJob(jobMeta.getJobId(), isRetryForThisTime);
                }
            }

            // 从正在执行的队列中移除
            appContext.getExecutingJobQueue().remove(jobMeta.getJobId());
        }
    }

    private void finishCronJob(String jobId) {
        JobPo jobPo = appContext.getCronJobQueue().getJob(jobId);
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime == null) {
            // 从CronJob队列中移除
            appContext.getCronJobQueue().remove(jobId);
            jobRemoveLog(jobPo, "Cron");
            return;
        }
        // 表示下次还要执行
        try {
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setTriggerTime(nextTriggerTime.getTime());
            jobPo.setGmtModified(SystemClock.now());
            jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
            appContext.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
        }
    }

    private void finishNoReplyPrevCronJob(JobMeta jobMeta) {
        JobPo jobPo = appContext.getCronJobQueue().getJob(jobMeta.getJob().getTaskTrackerNodeGroup(), jobMeta.getRealTaskId());
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime == null) {
            // 检查可执行队列中是否还有
            if (appContext.getExecutableJobQueue().countJob(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup()) == 0) {
                // TODO 检查执行中队列是否还有
                // 从CronJob队列中移除
                appContext.getCronJobQueue().remove(jobPo.getJobId());
                jobRemoveLog(jobPo, "Cron");
            }
        }
    }

    private void finishNoReplyPrevRepeatJob(JobMeta jobMeta, boolean isRetryForThisTime) {
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(jobMeta.getJob().getTaskTrackerNodeGroup(), jobMeta.getRealTaskId());
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        if (jobPo.getRepeatCount() != -1 && (jobPo.getRepeatedCount() + 1) >= jobPo.getRepeatCount()) {  //最后一次执行时 repeatedCount+1=repeatCount
            // 已经重试完成, 那么删除, 这里可以不用check可执行队列是否还有,因为这里依赖的是计数
            appContext.getRepeatJobQueue().remove(jobPo.getJobId());
            jobRemoveLog(jobPo, "Repeat");
            return;
        }

        // 如果当前完成的job是重试的,那么不要增加repeatedCount
        if (!isRetryForThisTime) {
            // 更新repeatJob的重复次数
            final int jobQueueRepeatedCount = appContext.getRepeatJobQueue().incRepeatedCount(jobPo.getJobId());
            if (jobQueueRepeatedCount >= jobPo.getRepeatCount()) {
                appContext.getRepeatJobQueue().remove(jobPo.getJobId());
                jobRemoveLog(jobPo, "Repeat");
            }
        }
    }

    private void finishRepeatJob(String jobId, boolean isRetryForThisTime) {
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(jobId);
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        if (jobPo.getRepeatCount() != -1 && (jobPo.getRepeatedCount() + 1) >= jobPo.getRepeatCount()) {  //最后一次执行时 repeatedCount+1=repeatCount
            // 已经重试完成, 那么删除
            appContext.getRepeatJobQueue().remove(jobId);
            jobRemoveLog(jobPo, "Repeat");
            return;
        }

        int repeatedCount = jobPo.getRepeatedCount();
        // 如果当前完成的job是重试的,那么不要增加repeatedCount
        if (!isRetryForThisTime) {
            // 更新repeatJob的重复次数
            repeatedCount = appContext.getRepeatJobQueue().incRepeatedCount(jobId);
        }
        if (repeatedCount == -1) {
            // 表示任务已经被删除了
            return;
        }
        long nexTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
        try {
            jobPo.setRepeatedCount(repeatedCount + 1); //再生成可执行job时，从RepeatJobQueue更新后的repeatedCount再加1
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setTriggerTime(nexTriggerTime);
            jobPo.setGmtModified(SystemClock.now());
            jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
            appContext.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
        }
    }

    private void jobRemoveLog(JobPo jobPo, String type) {
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.DEL);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogPo.setMsg(type + " Job Finished");
        appContext.getJobLogger().log(jobLogPo);
    }
}
