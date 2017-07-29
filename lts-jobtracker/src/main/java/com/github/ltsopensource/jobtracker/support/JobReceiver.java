package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.exception.JobReceiveException;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.command.JobSubmitRequest;
import com.github.ltsopensource.core.support.CronExpressionUtils;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.monitor.JobTrackerMStatReporter;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         任务处理器
 */
public class JobReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobReceiver.class);

    private JobTrackerAppContext appContext;
    private JobTrackerMStatReporter stat;

    public JobReceiver(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.stat = (JobTrackerMStatReporter) appContext.getMStatReporter();
    }

    /**
     * jobTracker 接受任务
     */
    public void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        JobReceiveException exception = null;
        for (Job job : jobs) {
            try {
                addToQueue(job, request);
            } catch (Exception e) {
                if (exception == null) {
                    exception = new JobReceiveException(e);
                }
                exception.addJob(job);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private JobPo addToQueue(Job job, JobSubmitRequest request) {

        JobPo jobPo = null;
        boolean success = false;
        BizLogCode code = null;
        try {
            jobPo = JobDomainConverter.convert(job);
            if (jobPo == null) {
                LOGGER.warn("Job can not be null。{}", job);
                return null;
            }
            if (StringUtils.isEmpty(jobPo.getSubmitNodeGroup())) {
                jobPo.setSubmitNodeGroup(request.getNodeGroup());
            }
            // 设置 jobId
            jobPo.setJobId(JobUtils.generateJobId());

            // 添加任务
            addJob(job, jobPo);

            success = true;
            code = BizLogCode.SUCCESS;

        } catch (DupEntryException e) {
            // 已经存在
            if (job.isReplaceOnExist()) {
                Assert.notNull(jobPo);
                success = replaceOnExist(job, jobPo);
                code = success ? BizLogCode.DUP_REPLACE : BizLogCode.DUP_FAILED;
            } else {
                code = BizLogCode.DUP_IGNORE;
                LOGGER.info("Job already exist And ignore. nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } finally {
            if (success) {
                stat.incReceiveJobNum();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Receive Job success. {}", job);
                }
            }
        }

        // 记录日志
        jobBizLog(jobPo, code);

        return jobPo;
    }

    /**
     * 添加任务
     */
    private void addJob(Job job, JobPo jobPo) throws DupEntryException {
        if (job.isCron()) {
            addCronJob(jobPo);
        } else if (job.isRepeatable()) {
            addRepeatJob(jobPo);
        } else {
            addTriggerTimeJob(jobPo);
        }
    }

    private void addTriggerTimeJob(JobPo jobPo) {
        boolean needAdd2ExecutableJobQueue = true;
        String ignoreAddOnExecuting = CollectionUtils.getValue(jobPo.getInternalExtParams(), "__LTS_ignoreAddOnExecuting");
        if (ignoreAddOnExecuting != null && "true".equals(ignoreAddOnExecuting)) {
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
                needAdd2ExecutableJobQueue = false;
            }
        }
        if (needAdd2ExecutableJobQueue) {
            jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
            appContext.getExecutableJobQueue().add(jobPo);
        }
    }

    /**
     * 更新任务
     **/
    private boolean replaceOnExist(Job job, JobPo jobPo) {

        // 得到老的job
        JobPo existJobPo = appContext.getExecutableJobQueue().getJob(job.getTaskTrackerNodeGroup(), jobPo.getTaskId());
        if (existJobPo == null) {
            existJobPo = appContext.getCronJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
            if (existJobPo == null) {
                existJobPo = appContext.getRepeatJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
            }
        }
        if (existJobPo != null) {
            String jobId = existJobPo.getJobId();
            // 1. 3个都删除下
            appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            appContext.getCronJobQueue().remove(jobId);
            appContext.getRepeatJobQueue().remove(jobId);

            jobPo.setJobId(jobId);
        }

        // 2. 重新添加任务
        try {
            addJob(job, jobPo);
        } catch (DupEntryException e) {
            // 一般不会走到这里
            LOGGER.warn("Job already exist twice. {}", job);
            return false;
        }
        return true;
    }

    /**
     * 添加Cron 任务
     */
    private void addCronJob(JobPo jobPo) throws DupEntryException {
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime != null) {

            if (appContext.getRepeatJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
                //  这种情况是 由repeat 任务变为了 Cron任务
                throw new DupEntryException();
            }

            // 1.add to cron job queue
            appContext.getCronJobQueue().add(jobPo);

            if (JobUtils.isRelyOnPrevCycle(jobPo)) {
                // 没有正在执行, 则添加
                if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
                    // 2. add to executable queue
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                    try {
                        jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                        appContext.getExecutableJobQueue().add(jobPo);
                    } catch (DupEntryException e) {
                        appContext.getCronJobQueue().remove(jobPo.getJobId());
                        throw e;
                    }
                }
            } else {
                // 对于不需要依赖上一周期的,采取批量生成的方式
                appContext.getNonRelyOnPrevCycleJobScheduler().addScheduleJobForOneHour(jobPo);
            }
        }
    }

    /**
     * 添加Repeat 任务
     */
    private void addRepeatJob(JobPo jobPo) throws DupEntryException {

        if (appContext.getCronJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
            //  这种情况是 由cron 任务变为了 repeat 任务
            throw new DupEntryException();
        }

        // 1.add to repeat job queue
        appContext.getRepeatJobQueue().add(jobPo);

        if (JobUtils.isRelyOnPrevCycle(jobPo)) {
            // 没有正在执行, 则添加
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
                // 2. add to executable queue
                try {
                    jobPo.setRepeatedCount(1); //第一次job的repeatedCount为1
                    jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                    appContext.getExecutableJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    appContext.getRepeatJobQueue().remove(jobPo.getJobId());
                    throw e;
                }
            }
        } else {
            // 对于不需要依赖上一周期的,采取批量生成的方式
            appContext.getNonRelyOnPrevCycleJobScheduler().addScheduleJobForOneHour(jobPo);
        }
    }

    /**
     * 记录任务日志
     */
    private void jobBizLog(JobPo jobPo, BizLogCode code) {
        if (jobPo == null) {
            return;
        }

        try {
            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.RECEIVE);
            jobLogPo.setLogTime(SystemClock.now());

            switch (code) {
                case SUCCESS:
                    jobLogPo.setLevel(Level.INFO);
                    jobLogPo.setMsg("Receive Success");
                    break;
                case DUP_IGNORE:
                    jobLogPo.setLevel(Level.WARN);
                    jobLogPo.setMsg("Already Exist And Ignored");
                    break;
                case DUP_FAILED:
                    jobLogPo.setLevel(Level.ERROR);
                    jobLogPo.setMsg("Already Exist And Update Failed");
                    break;
                case DUP_REPLACE:
                    jobLogPo.setLevel(Level.INFO);
                    jobLogPo.setMsg("Already Exist And Update Success");
                    break;
            }

            appContext.getJobLogger().log(jobLogPo);
        } catch (Throwable t) {     // 日志记录失败不影响正常运行
            LOGGER.error("Receive Job Log error ", t);
        }
    }

    private enum BizLogCode {
        DUP_IGNORE,     // 添加重复并忽略
        DUP_REPLACE,    // 添加时重复并覆盖更新
        DUP_FAILED,     // 添加时重复再次添加失败
        SUCCESS,     // 添加成功
    }

}
