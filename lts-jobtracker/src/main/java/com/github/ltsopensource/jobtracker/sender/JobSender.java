package com.github.ltsopensource.jobtracker.sender;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobSender {

    private final Logger LOGGER = LoggerFactory.getLogger(JobSender.class);

    private JobTrackerAppContext appContext;

    public JobSender(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    public SendResult send(String taskTrackerNodeGroup, String taskTrackerIdentity, int size, SendInvoker invoker) {

        List<JobPo> jobPos = fetchJob(taskTrackerNodeGroup, taskTrackerIdentity, size);
        if (jobPos.size() == 0) {
            return new SendResult(false, JobPushResult.NO_JOB);
        }

        SendResult sendResult = invoker.invoke(jobPos);

        if (sendResult.isSuccess()) {
            List<JobLogPo> jobLogPos = new ArrayList<JobLogPo>(jobPos.size());
            for (JobPo jobPo : jobPos) {
                // 记录日志
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
                jobLogPo.setSuccess(true);
                jobLogPo.setLogType(LogType.SENT);
                jobLogPo.setLogTime(SystemClock.now());
                jobLogPo.setLevel(Level.INFO);
                jobLogPos.add(jobLogPo);
            }
            appContext.getJobLogger().log(jobLogPos);
        }
        return sendResult;
    }

    private List<JobPo> fetchJob(String taskTrackerNodeGroup, String taskTrackerIdentity, int size) {
        List<JobPo> jobPos = new ArrayList<JobPo>(size);

        for (int i = 0; i < size; i++) {
            // 从mongo 中取一个可运行的job
            final JobPo jobPo = appContext.getPreLoader().take(taskTrackerNodeGroup, taskTrackerIdentity);
            if (jobPo == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Job push failed: no job! nodeGroup=" + taskTrackerNodeGroup + ", identity=" + taskTrackerIdentity);
                }
                break;
            }

            // IMPORTANT: 这里要先切换队列
            try {
                appContext.getExecutingJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutingJobQueue already exist:" + JSON.toJSONString(jobPo));
                appContext.getExecutableJobQueue().resume(jobPo);
                continue;
            }
            appContext.getExecutableJobQueue().remove(jobPo.getTaskTrackerNodeGroup(), jobPo.getJobId());

            jobPos.add(jobPo);
        }
        return jobPos;
    }

    public interface SendInvoker {
        SendResult invoke(List<JobPo> jobPos);
    }

    public static class SendResult {
        private boolean success;
        private Object returnValue;

        public SendResult(boolean success, Object returnValue) {
            this.success = success;
            this.returnValue = returnValue;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }
    }

}
