package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.admin.request.JobQueueReq;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.JobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.mongo.MongoRepository;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public abstract class AbstractMongoJobQueue extends MongoRepository implements JobQueue {

    public AbstractMongoJobQueue(Config config) {
        super(config);
    }

    @Override
    public PaginationRsp<JobPo> pageSelect(JobQueueReq request) {
        Query<JobPo> query = template.createQuery(getTargetTable(request.getTaskTrackerNodeGroup()), JobPo.class);
        addCondition(query, "jobId", request.getJobId());
        addCondition(query, "taskId", request.getTaskId());
        addCondition(query, "realTaskId", request.getRealTaskId());
        addCondition(query, "taskTrackerNodeGroup", request.getTaskTrackerNodeGroup());
        addCondition(query, "jobType", request.getJobType());
        addCondition(query, "submitNodeGroup", request.getSubmitNodeGroup());
        addCondition(query, "needFeedback", request.getNeedFeedback());
        if (request.getStartGmtCreated() != null) {
            query.filter("gmtCreated >= ", request.getStartGmtCreated().getTime());
        }
        if (request.getEndGmtCreated() != null) {
            query.filter("gmtCreated <= ", request.getEndGmtCreated().getTime());
        }
        if (request.getStartGmtModified() != null) {
            query.filter("gmtModified <= ", request.getStartGmtModified().getTime());
        }
        if (request.getEndGmtModified() != null) {
            query.filter("gmtModified >= ", request.getEndGmtModified().getTime());
        }
        PaginationRsp<JobPo> response = new PaginationRsp<JobPo>();
        Long results = template.getCount(query);
        response.setResults(results.intValue());
        if (results == 0) {
            return response;
        }

        if (StringUtils.isNotEmpty(request.getField()) && StringUtils.isNotEmpty(request.getDirection())) {
            query.order(("ASC".equalsIgnoreCase(request.getDirection()) ? "" : "-") + request.getField());
        }
        query.offset(request.getStart()).limit(request.getLimit());
        response.setRows(query.asList());
        return response;
    }

    @Override
    public boolean selectiveUpdateByJobId(JobQueueReq request) {
        Assert.hasLength(request.getJobId(), "Only allow update by jobId");

        Query<JobPo> query = template.createQuery(getTargetTable(request.getTaskTrackerNodeGroup()), JobPo.class);
        query.field("jobId").equal(request.getJobId());

        UpdateOperations<JobPo> operations = buildUpdateOperations(request);
        UpdateResults ur = template.update(query, operations);
        return ur.getUpdatedCount() == 1;
    }

    private UpdateOperations<JobPo> buildUpdateOperations(JobQueueReq request) {
        UpdateOperations<JobPo> operations = template.createUpdateOperations(JobPo.class);
        addUpdateField(operations, "cronExpression", request.getCronExpression());
        addUpdateField(operations, "needFeedback", request.getNeedFeedback());
        addUpdateField(operations, "extParams", request.getExtParams());
        addUpdateField(operations, "triggerTime", request.getTriggerTime() == null ? null : request.getTriggerTime().getTime());
        addUpdateField(operations, "priority", request.getPriority());
        addUpdateField(operations, "maxRetryTimes", request.getMaxRetryTimes());
        addUpdateField(operations, "relyOnPrevCycle", request.getRelyOnPrevCycle() == null ? true : request.getRelyOnPrevCycle());
        addUpdateField(operations, "submitNodeGroup", request.getSubmitNodeGroup());
        addUpdateField(operations, "taskTrackerNodeGroup", request.getTaskTrackerNodeGroup());
        addUpdateField(operations, "repeatCount", request.getRepeatCount());
        addUpdateField(operations, "repeatInterval", request.getRepeatInterval());
        addUpdateField(operations, "gmtModified", SystemClock.now());
        return operations;
    }

    @Override
    public boolean selectiveUpdateByTaskId(JobQueueReq request) {
        Assert.hasLength(request.getRealTaskId(), "Only allow update by realTaskId and taskTrackerNodeGroup");
        Assert.hasLength(request.getTaskTrackerNodeGroup(), "Only allow update by realTaskId and taskTrackerNodeGroup");

        Query<JobPo> query = template.createQuery(getTargetTable(request.getTaskTrackerNodeGroup()), JobPo.class);
        query.field("realTaskId").equal(request.getRealTaskId());
        query.field("taskTrackerNodeGroup").equal(request.getTaskTrackerNodeGroup());

        UpdateOperations<JobPo> operations = buildUpdateOperations(request);
        UpdateResults ur = template.update(query, operations);
        return ur.getUpdatedCount() == 1;
    }

    private Query<JobPo> addCondition(Query<JobPo> query, String field, Object o) {
        if (!checkCondition(o)) {
            return query;
        }
        query.field(field).equal(o);
        return query;
    }

    private UpdateOperations<JobPo> addUpdateField(UpdateOperations<JobPo> operations, String field, Object o) {
        if (!checkCondition(o)) {
            return operations;
        }
        operations.set(field, o);
        return operations;
    }

    private boolean checkCondition(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof String) {
            if (StringUtils.isEmpty((String) obj)) {
                return false;
            }
        } else if (
                obj instanceof Integer ||
                        obj instanceof Boolean ||
                        obj instanceof Long ||
                        obj instanceof Float ||
                        obj instanceof Date ||
                        obj instanceof HashMap) {
            return true;
        } else {
            throw new IllegalArgumentException("Can not support type " + obj.getClass());
        }

        return true;
    }

    protected abstract String getTargetTable(String taskTrackerNodeGroup);

}
