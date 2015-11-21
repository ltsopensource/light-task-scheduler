package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CharacterUtils;
import com.lts.core.json.JSON;
import com.lts.core.commons.utils.StringUtils;
import com.lts.queue.JobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.lts.queue.exception.JobQueueException;
import com.lts.queue.mysql.support.ResultSetHandlerHolder;
import com.lts.store.jdbc.JdbcRepository;
import com.lts.store.jdbc.SqlBuilder;
import com.lts.web.request.JobQueueRequest;
import com.lts.web.response.PageResponse;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public abstract class AbstractMysqlJobQueue extends JdbcRepository implements JobQueue {

    public AbstractMysqlJobQueue(Config config) {
        super(config);
    }

    protected boolean add(String tableName, JobPo jobPo) {
        String sql = "INSERT INTO " +
                "`" + tableName + "` ( " +
                "`job_id`, " +
                "`priority`, " +
                "`retry_times`, " +
                "`task_id`, " +
                "`gmt_created`, " +
                "`gmt_modified`, " +
                "`submit_node_group`, " +
                "`task_tracker_node_group`, " +
                "`ext_params`, " +
                "`is_running`, " +
                "`task_tracker_identity`, " +
                "`need_feedback`, " +
                "`cron_expression`, " +
                "`trigger_time`)" +
                "VALUES " +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            getSqlTemplate().update(sql,
                    jobPo.getJobId(),
                    jobPo.getPriority(),
                    jobPo.getRetryTimes(),
                    jobPo.getTaskId(),
                    jobPo.getGmtCreated(),
                    jobPo.getGmtModified(),
                    jobPo.getSubmitNodeGroup(),
                    jobPo.getTaskTrackerNodeGroup(),
                    JSON.toJSONString(jobPo.getExtParams()),
                    jobPo.isRunning(),
                    jobPo.getTaskTrackerIdentity(),
                    jobPo.isNeedFeedback(),
                    jobPo.getCronExpression(),
                    jobPo.getTriggerTime());
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DuplicateJobException(e.getMessage(), e);
            } else {
                throw new JobQueueException(e.getMessage(), e);
            }
        }
    }

    public PageResponse<JobPo> pageSelect(JobQueueRequest request) {

        PageResponse<JobPo> response = new PageResponse<JobPo>();
        try {
            //  get count
            SqlBuilder selectCountSQL = new SqlBuilder("SELECT count(1) FROM " + getTableName(request));
            addCondition(selectCountSQL, request);
            Long results = getSqlTemplate().queryForValue(selectCountSQL.getSQL(), selectCountSQL.getParams().toArray());
            response.setResults(results.intValue());
            if (results > 0) {
                SqlBuilder selectSQL = new SqlBuilder("SELECT * FROM " + getTableName(request));
                addCondition(selectSQL, request);
                // 驼峰转为下划线
                selectSQL.addOrderBy(CharacterUtils.camelCase2Underscore(request.getField()), request.getDirection());
                selectSQL.addLimit(request.getStart(), request.getLimit());
                List<JobPo> jobPos = getSqlTemplate().query(selectSQL.getSQL(),
                        ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                        selectSQL.getParams().toArray()
                );
                response.setRows(jobPos);
            }
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
        return response;
    }

    protected abstract String getTableName(JobQueueRequest request);

    public boolean selectiveUpdate(JobQueueRequest request) {

        if (StringUtils.isEmpty(request.getJobId())) {
            throw new JobQueueException("Only allow by jobId");
        }
        SqlBuilder sql = new SqlBuilder("UPDATE " + getTableName(request));
        sql.addUpdateField("cron_expression", request.getCronExpression());
        sql.addUpdateField("need_feedback", request.getNeedFeedback());
        sql.addUpdateField("ext_params", JSON.toJSONString(request.getExtParams()));
        sql.addUpdateField("trigger_time", request.getTriggerTime() == null ? null : request.getTriggerTime().getTime());
        sql.addUpdateField("priority", request.getPriority());
        sql.addUpdateField("submit_node_group", request.getSubmitNodeGroup());
        sql.addUpdateField("task_tracker_node_group", request.getTaskTrackerNodeGroup());
        sql.addCondition("job_id", request.getJobId());

        try {
            int effectRows = getSqlTemplate().update(sql.getSQL(), sql.getParams().toArray());
            return effectRows == 1;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private SqlBuilder addCondition(SqlBuilder sql, JobQueueRequest request) {
        sql.addCondition("job_id", request.getJobId());
        sql.addCondition("task_id", request.getTaskId());
        sql.addCondition("task_tracker_node_group", request.getTaskTrackerNodeGroup());
        sql.addCondition("submit_node_group", request.getSubmitNodeGroup());
        sql.addCondition("need_feedback", request.getNeedFeedback());
        sql.addCondition("gmt_created", request.getStartGmtCreated() == null ? null : request.getStartGmtCreated(), ">=");
        sql.addCondition("gmt_created", request.getEndGmtCreated() == null ? null : request.getEndGmtCreated(), "<=");
        sql.addCondition("gmt_modified", request.getStartGmtModified() == null ? null : request.getStartGmtModified(), ">=");
        sql.addCondition("gmt_modified", request.getEndGmtModified() == null ? null : request.getEndGmtModified(), "<=");
        return sql;
    }

}
