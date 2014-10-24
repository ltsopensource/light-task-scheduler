package com.lts.job.tracker.support;

import com.mongodb.MongoException;
import com.lts.job.common.domain.Job;
import com.lts.job.common.exception.JobReceiveException;
import com.lts.job.common.protocol.command.JobSubmitRequest;
import com.lts.job.common.support.JobDomainConverter;
import com.lts.job.common.support.SingletonBeanContext;
import com.lts.job.common.repository.JobMongoRepository;
import com.lts.job.common.repository.po.JobPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 * 任务处理器
 */
public class JobReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger("JobReceiver");
    private static JobMongoRepository jobRepository;

    static {
        jobRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
    }

    /**
     * jobTracker 接受任务
     *
     * @param request
     * @return
     */
    public static void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();

        JobReceiveException exception = null;

        if (jobs == null || jobs.size() == 0) {
            return;
        }

        for (Job job : jobs) {

            JobPo jobPo = JobDomainConverter.convert(job, request);

            try {
                jobRepository.save(jobPo);
                LOGGER.info("接受任务成功! nodeGroup=" + request.getNodeGroup() + "," + jobPo);
            } catch (MongoException.DuplicateKey e) {
                // 已经存在 ignore
                LOGGER.info("任务已经存在! nodeGroup=" + request.getNodeGroup() + "," + jobPo);
            } catch (Throwable t) {
                if (exception == null) {
                    exception = new JobReceiveException();
                }
                exception.addJob(job);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

}
