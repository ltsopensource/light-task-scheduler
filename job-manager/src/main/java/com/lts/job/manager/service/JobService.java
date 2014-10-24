package com.lts.job.manager.service;

import com.lts.job.common.domain.Job;
import com.lts.job.common.repository.JobMongoRepository;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.common.support.JobDomainConverter;
import com.lts.job.common.support.SingletonBeanContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 */
@Service
public class JobService {

    private JobMongoRepository jobMongoRepository;

    public JobService() {
        this.jobMongoRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
    }

    /**
     * 添加Job
     *
     * @param job
     */
    public void addJob(Job job) {
        JobPo jobPo = JobDomainConverter.convert(job);
        jobMongoRepository.save(jobPo);
    }

    public List<JobPo> getAllJob() {
        return jobMongoRepository.getAllJob();
    }

    public JobPo delete(String jobId) {
        return jobMongoRepository.findAndDeleteJob(jobId);
    }
}
