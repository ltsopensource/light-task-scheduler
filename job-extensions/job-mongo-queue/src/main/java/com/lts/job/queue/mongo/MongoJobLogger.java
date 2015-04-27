package com.lts.job.queue.mongo;

import com.lts.job.queue.mongo.store.AbstractMongoRepository;
import com.lts.job.queue.mongo.store.Config;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.logger.domain.BizLogPo;
import com.lts.job.tracker.logger.domain.JobLogPo;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobLogger extends AbstractMongoRepository<JobLogPo> implements JobLogger {

    public MongoJobLogger(Config config) {
        super(config);
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        super.save(jobLogPo);
    }

    @Override
    public void log(BizLogPo bizLogPo) {
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setTimestamp(bizLogPo.getTimestamp());
        jobLogPo.setTaskTrackerNodeGroup(bizLogPo.getTaskTrackerNodeGroup());
        jobLogPo.setTaskTrackerIdentity(bizLogPo.getTaskTrackerIdentity());
        jobLogPo.setJobId(bizLogPo.getJobId());
        jobLogPo.setMsg(bizLogPo.getMsg());
        super.save(jobLogPo);
    }
}
