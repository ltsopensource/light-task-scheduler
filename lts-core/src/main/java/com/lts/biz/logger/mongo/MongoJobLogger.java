package com.lts.biz.logger.mongo;


import com.lts.biz.logger.JobLogger;
import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.web.response.PageResponse;
import com.lts.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.mongodb.morphia.query.Query;

import java.util.Date;
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
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_logTime", "logTime");
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId,taskTrackerNodeGroup");
        }
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        template.save(jobLogPo);
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        template.save(jobLogPos);
    }

    @Override
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {

        Query<JobLogPo> query = template.createQuery(JobLogPo.class);
        if(StringUtils.isNotEmpty(request.getTaskId())){
            query.field("taskId").equal(request.getTaskId());
        }
        if(StringUtils.isNotEmpty(request.getTaskTrackerNodeGroup())){
            query.field("taskTrackerNodeGroup").equal(request.getTaskTrackerNodeGroup());
        }
        if (request.getStartLogTime() != null) {
            query.filter("logTime >= ", getTimestamp(request.getStartLogTime()));
        }
        if (request.getEndLogTime() != null) {
            query.filter("logTime <= ", getTimestamp(request.getEndLogTime()));
        }
        PageResponse<JobLogPo> pageResponse = new PageResponse<JobLogPo>();
        Long results = template.getCount(query);
        pageResponse.setResults(results.intValue());
        if (results == 0) {
            return pageResponse;
        }
        // 查询rows
        query.order("-logTime").offset(request.getStart()).limit(request.getLimit());

        pageResponse.setRows(query.asList());

        return pageResponse;
    }

    private Long getTimestamp(Date timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

}
