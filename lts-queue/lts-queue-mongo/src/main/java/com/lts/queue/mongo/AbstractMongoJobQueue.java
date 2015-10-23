package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.web.request.JobQueueRequest;
import com.lts.web.response.PageResponse;
import com.lts.queue.JobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.JobQueueException;
import com.lts.store.mongo.MongoRepository;
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
    public PageResponse<JobPo> pageSelect(JobQueueRequest request) {
        Query<JobPo> query = template.createQuery(getTargetTable(request.getTaskTrackerNodeGroup()), JobPo.class);
        addCondition(query, "jobId", request.getJobId());
        addCondition(query, "taskId", request.getTaskId());
        addCondition(query, "taskTrackerNodeGroup", request.getTaskTrackerNodeGroup());
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
        PageResponse<JobPo> response = new PageResponse<JobPo>();
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
    public boolean selectiveUpdate(JobQueueRequest request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            throw new JobQueueException("Only allow by jobId");
        }
        Query<JobPo> query = template.createQuery(getTargetTable(request.getTaskTrackerNodeGroup()), JobPo.class);
        query.field("jobId").equal(request.getJobId());

        UpdateOperations<JobPo> operations = template.createUpdateOperations(JobPo.class);
        addUpdateField(operations, "cronExpression", request.getCronExpression());
        addUpdateField(operations, "needFeedback", request.getNeedFeedback());
        addUpdateField(operations, "extParams", request.getExtParams());
        addUpdateField(operations, "triggerTime", request.getTriggerTime() == null ? null : request.getTriggerTime().getTime());
        addUpdateField(operations, "priority", request.getPriority());
        addUpdateField(operations, "submitNodeGroup", request.getSubmitNodeGroup());
        addUpdateField(operations, "taskTrackerNodeGroup", request.getTaskTrackerNodeGroup());

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
