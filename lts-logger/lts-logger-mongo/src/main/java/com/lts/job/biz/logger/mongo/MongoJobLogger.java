package com.lts.job.biz.logger.mongo;


import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.JobLoggerRequest;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.commons.utils.CollectionUtils;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobLogger extends MongoRepository implements JobLogger {

    public MongoJobLogger(Config config) {
        super(config);
        setTableName("lts_job_log_po");

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.isEmpty(indexInfo)) {
            template.ensureIndex("idx_timestamp", "timestamp");
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId,taskTrackerNodeGroup");
        }
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        template.save(jobLogPo);
    }

    @Override
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {
        return null;
    }

}
