package com.lts.jobtracker.support;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Level;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobReceiveException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.core.spi.ServiceLoader;
import com.lts.core.support.LoggerName;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.id.IdGenerator;
import com.lts.jobtracker.monitor.JobTrackerMonitor;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         任务处理器
 */
public class JobReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.JobTracker);

    private JobTrackerApplication application;
    private IdGenerator idGenerator;
    private JobTrackerMonitor monitor;

    public JobReceiver(JobTrackerApplication application) {
        this.application = application;
        this.monitor = (JobTrackerMonitor) application.getMonitor();
        this.idGenerator = ServiceLoader.load(IdGenerator.class, application.getConfig());
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
            } catch (Exception t) {
                if (exception == null) {
                    exception = new JobReceiveException(t);
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
            jobPo.setJobId(idGenerator.generate(jobPo));

            // 添加任务
            addJob(job, jobPo);

            success = true;
            code = BizLogCode.SUCCESS;

        } catch (DuplicateJobException e) {
            // 已经存在
            if (job.isReplaceOnExist()) {
                Assert.notNull(jobPo);
                success = replaceOnExist(job, jobPo);
                code = success ? BizLogCode.DUP_REPLACE : BizLogCode.DUP_FAILED;
            } else {
                code = BizLogCode.DUP_IGNORE;
                LOGGER.info("Job already exist. nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } finally {
            if (success) {
                monitor.incReceiveJobNum();
            }
        }

        // 记录日志
        jobBizLog(jobPo, code);

        return jobPo;
    }

    /**
     * 添加任务
     */
    private void addJob(Job job, JobPo jobPo) throws DuplicateJobException {
        if (job.isSchedule()) {
            addCronJob(jobPo);
            LOGGER.info("Receive Cron Job success. {}", job);
        } else {
            application.getExecutableJobQueue().add(jobPo);
            LOGGER.info("Receive Job success. {}", job);
        }
    }

    /**
     * 更新任务
     **/
    private boolean replaceOnExist(Job job, JobPo jobPo) {

        // 得到老的jobId
        JobPo oldJobPo = null;
        if (job.isSchedule()) {
            oldJobPo = application.getCronJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        } else {
            oldJobPo = application.getExecutableJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        }
        if (oldJobPo != null) {
            String jobId = oldJobPo.getJobId();
            // 1. 删除任务
            application.getExecutableJobQueue().remove(job.getTaskTrackerNodeGroup(), jobId);
            if (job.isSchedule()) {
                application.getCronJobQueue().remove(jobId);
            }
            jobPo.setJobId(jobId);
        }

        // 2. 重新添加任务
        try {
            addJob(job, jobPo);
        } catch (DuplicateJobException e) {
            // 一般不会走到这里
            LOGGER.error("Job already exist twice. {}", job);
            return false;
        }
        return true;
    }

    /**
     * 添加Cron 任务
     */
    private void addCronJob(JobPo jobPo) throws DuplicateJobException {
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime != null) {
            // 1.add to cron job queue
            application.getCronJobQueue().add(jobPo);

            // 2. add to executable queue
            jobPo.setTriggerTime(nextTriggerTime.getTime());
            application.getExecutableJobQueue().add(jobPo);
        }
    }

    /**
     * 记录任务日志
     */
    private void jobBizLog(JobPo jobPo, BizLogCode code) {
        if (jobPo != null) {
            try {
                // 记录日志
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
                jobLogPo.setSuccess(true);
                jobLogPo.setLogType(LogType.RECEIVE);
                jobLogPo.setLogTime(SystemClock.now());

                switch (code) {
                    case SUCCESS:
                        jobLogPo.setLevel(Level.INFO);
                        jobLogPo.setMsg("添加任务成功.");
                        break;
                    case DUP_IGNORE:
                        jobLogPo.setLevel(Level.WARN);
                        jobLogPo.setMsg("在任务队列中已经存在,忽略本次提交.");
                        break;
                    case DUP_FAILED:
                        jobLogPo.setLevel(Level.ERROR);
                        jobLogPo.setMsg("在任务队列中已经存在,更新时失败.");
                        break;
                    case DUP_REPLACE:
                        jobLogPo.setLevel(Level.INFO);
                        jobLogPo.setMsg("在任务队列中已经存在,更新成功.");
                        break;
                }

                application.getJobLogger().log(jobLogPo);
            } catch (Throwable t) {     // 日志记录失败不影响正常运行
                LOGGER.error("Receive Job Log error ", t);
            }
        }
    }

    private enum BizLogCode {
        DUP_IGNORE,     // 添加重复并忽略
        DUP_REPLACE,    // 添加时重复并覆盖更新
        DUP_FAILED,     // 添加时重复再次添加失败
        SUCCESS,     // 添加成功
    }

}
