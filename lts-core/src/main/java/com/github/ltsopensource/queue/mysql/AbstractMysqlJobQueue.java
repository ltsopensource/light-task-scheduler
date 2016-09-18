package com.github.ltsopensource.queue.mysql;

import com.github.ltsopensource.admin.request.JobQueueReq;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.CharacterUtils;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.JobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.queue.mysql.support.RshHolder;
import com.github.ltsopensource.store.jdbc.JdbcAbstractAccess;
import com.github.ltsopensource.store.jdbc.builder.*;
import com.github.ltsopensource.store.jdbc.dbutils.JdbcTypeUtils;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public abstract class AbstractMysqlJobQueue extends JdbcAbstractAccess implements JobQueue {

    public AbstractMysqlJobQueue(Config config) {
        super(config);
    }

    protected boolean add(String tableName, JobPo jobPo) {
        return new InsertSql(getSqlTemplate())
                .insert(tableName)
                .columns("job_id",
                        "job_type",
                        "priority",
                        "retry_times",
                        "max_retry_times",
                        "rely_on_prev_cycle",
                        "task_id",
                        "real_task_id",
                        "gmt_created",
                        "gmt_modified",
                        "submit_node_group",
                        "task_tracker_node_group",
                        "ext_params",
                        "internal_ext_params",
                        "is_running",
                        "task_tracker_identity",
                        "need_feedback",
                        "cron_expression",
                        "trigger_time",
                        "repeat_count",
                        "repeated_count",
                        "repeat_interval")
                .values(jobPo.getJobId(),
                        jobPo.getJobType() == null ? null : jobPo.getJobType().name(),
                        jobPo.getPriority(),
                        jobPo.getRetryTimes(),
                        jobPo.getMaxRetryTimes(),
                        jobPo.getRelyOnPrevCycle(),
                        jobPo.getTaskId(),
                        jobPo.getRealTaskId(),
                        jobPo.getGmtCreated(),
                        jobPo.getGmtModified(),
                        jobPo.getSubmitNodeGroup(),
                        jobPo.getTaskTrackerNodeGroup(),
                        JSON.toJSONString(jobPo.getExtParams()),
                        JSON.toJSONString(jobPo.getInternalExtParams()),
                        jobPo.isRunning(),
                        jobPo.getTaskTrackerIdentity(),
                        jobPo.isNeedFeedback(),
                        jobPo.getCronExpression(),
                        jobPo.getTriggerTime(),
                        jobPo.getRepeatCount(),
                        jobPo.getRepeatedCount(),
                        jobPo.getRepeatInterval())
                .doInsert() == 1;
    }

    public PaginationRsp<JobPo> pageSelect(JobQueueReq request) {

        PaginationRsp<JobPo> response = new PaginationRsp<JobPo>();

        WhereSql whereSql = buildWhereSql(request);

        Long results = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName(request))
                .whereSql(whereSql)
                .single();
        response.setResults(results.intValue());

        if (results > 0) {

            List<JobPo> jobPos = new SelectSql(getSqlTemplate())
                    .select()
                    .all()
                    .from()
                    .table(getTableName(request))
                    .whereSql(whereSql)
                    .orderBy()
                    .column(CharacterUtils.camelCase2Underscore(request.getField()), OrderByType.convert(request.getDirection()))
                    .limit(request.getStart(), request.getLimit())
                    .list(RshHolder.JOB_PO_LIST_RSH);
            response.setRows(jobPos);
        }
        return response;
    }

    protected abstract String getTableName(JobQueueReq request);

    public boolean selectiveUpdateByJobId(JobQueueReq request) {
        Assert.hasLength(request.getJobId(), "Only allow update by jobId");

        UpdateSql sql = buildUpdateSqlPrefix(request);

        return sql.where("job_id=?", request.getJobId())
                .doUpdate() == 1;
    }

    @Override
    public boolean selectiveUpdateByTaskId(JobQueueReq request) {
        Assert.hasLength(request.getRealTaskId(), "Only allow update by realTaskId and taskTrackerNodeGroup");
        Assert.hasLength(request.getTaskTrackerNodeGroup(), "Only allow update by realTaskId and taskTrackerNodeGroup");

        UpdateSql sql = buildUpdateSqlPrefix(request);
        return sql.where("real_task_id = ?", request.getRealTaskId())
                .and("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup())
                .doUpdate() == 1;
    }

    private UpdateSql buildUpdateSqlPrefix(JobQueueReq request) {
        return new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName(request))
                .setOnNotNull("cron_expression", request.getCronExpression())
                .setOnNotNull("need_feedback", request.getNeedFeedback())
                .setOnNotNull("ext_params", JSON.toJSONString(request.getExtParams()))
                .setOnNotNull("trigger_time", JdbcTypeUtils.toTimestamp(request.getTriggerTime()))
                .setOnNotNull("priority", request.getPriority())
                .setOnNotNull("max_retry_times", request.getMaxRetryTimes())
                .setOnNotNull("rely_on_prev_cycle", request.getRelyOnPrevCycle() == null ? true : request.getRelyOnPrevCycle())
                .setOnNotNull("submit_node_group", request.getSubmitNodeGroup())
                .setOnNotNull("task_tracker_node_group", request.getTaskTrackerNodeGroup())
                .setOnNotNull("repeat_count", request.getRepeatCount())
                .setOnNotNull("repeat_interval", request.getRepeatInterval())
                .setOnNotNull("gmt_modified", SystemClock.now());
    }

    private WhereSql buildWhereSql(JobQueueReq request) {
        return new WhereSql()
                .andOnNotEmpty("job_id = ?", request.getJobId())
                .andOnNotEmpty("task_id = ?", request.getTaskId())
                .andOnNotEmpty("real_task_id = ?", request.getRealTaskId())
                .andOnNotEmpty("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup())
                .andOnNotEmpty("job_type = ?", request.getJobType())
                .andOnNotEmpty("submit_node_group = ?", request.getSubmitNodeGroup())
                .andOnNotNull("need_feedback = ?", request.getNeedFeedback())
                .andBetween("gmt_created", JdbcTypeUtils.toTimestamp(request.getStartGmtCreated()), JdbcTypeUtils.toTimestamp(request.getEndGmtCreated()))
                .andBetween("gmt_modified", JdbcTypeUtils.toTimestamp(request.getStartGmtModified()), JdbcTypeUtils.toTimestamp(request.getEndGmtModified()));
    }

}
