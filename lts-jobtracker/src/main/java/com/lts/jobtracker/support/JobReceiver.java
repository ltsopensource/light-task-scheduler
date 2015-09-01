package com.lts.jobtracker.support;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobReceiveException;
import com.lts.core.extension.ExtensionLoader;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.core.support.LoggerName;
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
        this.idGenerator = ExtensionLoader.getExtensionLoader(IdGenerator.class).getAdaptiveExtension();
    }

    /**
     * jobTracker 接受任务
     */
    public void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();

        JobReceiveException exception = null;

        if (jobs == null || jobs.size() == 0) {
            return;
        }

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
            jobPo.setJobId(idGenerator.generate(application.getConfig(), jobPo));

            if (job.isSchedule()) {
                addCronJob(jobPo);
                LOGGER.info("Receive cron job success. nodeGroup={}, CronExpression={}, {}",
                        request.getNodeGroup(), job.getCronExpression(), job);
            } else {
                application.getExecutableJobQueue().add(jobPo);
                LOGGER.info("Receive job success. nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } catch (DuplicateJobException e) {
            // already exist, ignore
            LOGGER.info("Job already exist. nodeGroup={}, {}", request.getNodeGroup(), job);
        } finally {
            monitor.incReceiveJobNum();
        }

        return jobPo;
    }

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

}
