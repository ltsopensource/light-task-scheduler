package com.lts.job.queue.mongo;

import com.google.code.morphia.Datastore;
import com.lts.job.queue.mongo.store.mongo.AbstractMongoRepository;
import com.lts.job.tracker.logger.JobLogPo;
import com.lts.job.tracker.logger.JobLogger;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobLogger extends AbstractMongoRepository<JobLogPo> implements JobLogger {
    public MongoJobLogger(Datastore ds) {
        super(ds);
    }

    @Override
    public void log(JobLogPo log) {
        super.save(log);
    }
}
