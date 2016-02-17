package com.lts.jobtracker.complete;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.DateUtils;
import com.lts.core.json.JSON;
import com.lts.core.domain.JobWrapper;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.LoggerName;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.jobtracker.support.CronExpressionUtils;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobRetryHandler implements JobCompleteHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.JobTracker);

    private JobTrackerAppContext appContext;

    public JobRetryHandler(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void onComplete(List<TaskTrackerJobResult> results) {

        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        for (TaskTrackerJobResult result : results) {

            JobWrapper jobWrapper = result.getJobWrapper();
            // 1. 加入到重试队列
            JobPo jobPo = appContext.getExecutingJobQueue().get(jobWrapper.getJobId());
            if (jobPo == null) {    // 表示已经被删除了
                continue;
            }

            // 重试次数+1
            jobPo.setRetryTimes((jobPo.getRetryTimes() == null ? 0 : jobPo.getRetryTimes()) + 1);
            Long nextRetryTriggerTime = DateUtils.addMinute(new Date(), jobPo.getRetryTimes()).getTime();

            if (jobPo.isSchedule()) {
                // 如果是 cron Job, 判断任务下一次执行时间和重试时间的比较
                JobPo cronJobPo = appContext.getCronJobQueue().finish(jobWrapper.getJobId());
                if (cronJobPo != null) {
                    Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronJobPo.getCronExpression());
                    if (nextTriggerTime != null && nextTriggerTime.getTime() < nextRetryTriggerTime) {
                        // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                        nextRetryTriggerTime = nextTriggerTime.getTime();
                        jobPo = cronJobPo;
                    }
                }
            }

            // 加入到队列, 重试
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setGmtModified(SystemClock.now());
            // 延迟重试时间就等于重试次数(分钟)
            jobPo.setTriggerTime(nextRetryTriggerTime);
            try {
                appContext.getExecutableJobQueue().add(jobPo);
            } catch (DuplicateJobException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
            }
            // 从正在执行的队列中移除
            appContext.getExecutingJobQueue().remove(jobPo.getJobId());
        }
    }
}
