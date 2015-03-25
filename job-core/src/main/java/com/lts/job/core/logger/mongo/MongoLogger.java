package com.lts.job.core.logger.mongo;

import com.lts.job.core.logger.LtsLogger;
import com.lts.job.core.repository.JobLogMongoRepository;
import com.lts.job.core.repository.po.JobLogPo;
import com.lts.job.core.support.SingletonBeanContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 */
public class MongoLogger implements LtsLogger{

    private JobLogMongoRepository jobLogMongoRepository;

    public MongoLogger() {
        jobLogMongoRepository = SingletonBeanContext.getBean(JobLogMongoRepository.class);
    }

    @Override
    public void log(JobLogPo log) {
        jobLogMongoRepository.save(log);
    }

}
