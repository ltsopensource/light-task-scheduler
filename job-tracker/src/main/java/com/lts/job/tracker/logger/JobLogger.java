package com.lts.job.tracker.logger;

import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.domain.LogType;
import com.lts.job.core.repository.JobLogMongoRepository;
import com.lts.job.core.repository.po.JobLogPo;
import com.lts.job.core.repository.po.JobPo;
import com.lts.job.core.support.SingletonBeanContext;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 */
public class JobLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogger.class);

    private JobLogger() {
    }

    private static JobLogMongoRepository jobLogMongoRepository;

    static {
        jobLogMongoRepository = SingletonBeanContext.getBean(JobLogMongoRepository.class);
    }

    public static void log(Job job, LogType logType) {
        try {
            JobLogPo jobLogPo = new JobLogPo();
            try {
                BeanUtils.copyProperties(jobLogPo, job);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jobLogPo.setLogType(logType);
            jobLogMongoRepository.save(jobLogPo);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    public static void log(JobPo jobPo, LogType logType) {
        try {
            JobLogPo jobLogPo = new JobLogPo();
            try {
                BeanUtils.copyProperties(jobLogPo, jobPo);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jobLogPo.setLogType(logType);
            jobLogMongoRepository.save(jobLogPo);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    public static void log(JobResult jobResult, LogType logType) {
        try {
            JobLogPo jobLogPo = new JobLogPo();
            try {
                BeanUtils.copyProperties(jobLogPo, jobResult.getJob());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jobLogPo.setMsg(jobResult.getMsg());
            jobLogPo.setLogType(logType);
            jobLogPo.setSuccess(jobResult.isSuccess());
            jobLogMongoRepository.save(jobLogPo);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    public static void log(List<JobResult> jobResults, LogType logType) {
        try {

            for (JobResult jobResult : jobResults) {
                log(jobResult, logType);
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

}
