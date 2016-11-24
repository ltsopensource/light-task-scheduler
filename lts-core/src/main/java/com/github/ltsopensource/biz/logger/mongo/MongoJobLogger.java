package com.github.ltsopensource.biz.logger.mongo;


import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.store.mongo.MongoRepository;
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
            template.ensureIndex("idx_realTaskId_taskTrackerNodeGroup", "realTaskId, taskTrackerNodeGroup");
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
    public PaginationRsp<JobLogPo> search(JobLoggerRequest request) {

        Query<JobLogPo> query = template.createQuery(JobLogPo.class);
        if(StringUtils.isNotEmpty(request.getTaskId())){
            query.field("taskId").equal(request.getTaskId());
        }
        if(StringUtils.isNotEmpty(request.getTaskTrackerNodeGroup())){
            query.field("taskTrackerNodeGroup").equal(request.getTaskTrackerNodeGroup());
        }
        if(StringUtils.isNotEmpty(request.getLogType())){
            query.field("logType").equal(request.getLogType());
        }
        if(StringUtils.isNotEmpty(request.getLevel())){
            query.field("level").equal(request.getLevel());
        }
        if(StringUtils.isNotEmpty(request.getSuccess())){
            query.field("success").equal(request.getSuccess());
        }
        if (request.getStartLogTime() != null) {
            query.filter("logTime >= ", getTimestamp(request.getStartLogTime()));
        }
        if (request.getEndLogTime() != null) {
            query.filter("logTime <= ", getTimestamp(request.getEndLogTime()));
        }
        PaginationRsp<JobLogPo> paginationRsp = new PaginationRsp<JobLogPo>();
        Long results = template.getCount(query);
        paginationRsp.setResults(results.intValue());
        if (results == 0) {
            return paginationRsp;
        }
        // 查询rows
        query.order("-logTime").offset(request.getStart()).limit(request.getLimit());

        paginationRsp.setRows(query.asList());

        return paginationRsp;
    }

    private Long getTimestamp(Date timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

}
