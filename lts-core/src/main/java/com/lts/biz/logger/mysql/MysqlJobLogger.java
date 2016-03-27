package com.lts.biz.logger.mysql;

import com.lts.biz.logger.JobLogger;
import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.json.JSON;
import com.lts.queue.mysql.support.RshHolder;
import com.lts.store.jdbc.JdbcAbstractAccess;
import com.lts.store.jdbc.builder.InsertSql;
import com.lts.store.jdbc.builder.OrderByType;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.store.jdbc.builder.WhereSql;
import com.lts.store.jdbc.dbutils.JdbcTypeUtils;
import com.lts.admin.response.PaginationRsp;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class MysqlJobLogger extends JdbcAbstractAccess implements JobLogger {

    public MysqlJobLogger(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_job_log_po.sql"));
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        InsertSql insertSql = buildInsertSql();

        setInsertSqlValues(insertSql, jobLogPo).doInsert();
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }

        InsertSql insertSql = buildInsertSql();

        for (JobLogPo jobLogPo : jobLogPos) {
            setInsertSqlValues(insertSql, jobLogPo);
        }
        insertSql.doBatchInsert();
    }

    private InsertSql buildInsertSql() {
        return new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("log_time",
                        "gmt_created",
                        "log_type",
                        "success",
                        "msg",
                        "task_tracker_identity",
                        "level",
                        "task_id",
                        "job_id",
                        "priority",
                        "submit_node_group",
                        "task_tracker_node_group",
                        "ext_params",
                        "internal_ext_params",
                        "need_feedback",
                        "cron_expression",
                        "trigger_time",
                        "retry_times",
                        "max_retry_times",
                        "repeat_count",
                        "repeated_count",
                        "repeat_interval"
                        );
    }

    private InsertSql setInsertSqlValues(InsertSql insertSql, JobLogPo jobLogPo) {
        return insertSql.values(jobLogPo.getLogTime(),
                jobLogPo.getGmtCreated(),
                jobLogPo.getLogType().name(),
                jobLogPo.isSuccess(),
                jobLogPo.getMsg(),
                jobLogPo.getTaskTrackerIdentity(),
                jobLogPo.getLevel().name(),
                jobLogPo.getTaskId(),
                jobLogPo.getJobId(),
                jobLogPo.getPriority(),
                jobLogPo.getSubmitNodeGroup(),
                jobLogPo.getTaskTrackerNodeGroup(),
                JSON.toJSONString(jobLogPo.getExtParams()),
                JSON.toJSONString(jobLogPo.getInternalExtParams()),
                jobLogPo.isNeedFeedback(),
                jobLogPo.getCronExpression(),
                jobLogPo.getTriggerTime(),
                jobLogPo.getRetryTimes(),
                jobLogPo.getMaxRetryTimes(),
                jobLogPo.getRepeatCount(),
                jobLogPo.getRepeatedCount(),
                jobLogPo.getRepeatInterval());
    }

    @Override
    public PaginationRsp<JobLogPo> search(JobLoggerRequest request) {

        PaginationRsp<JobLogPo> response = new PaginationRsp<JobLogPo>();

        Long results = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .single();
        response.setResults(results.intValue());
        if (results == 0) {
            return response;
        }
        // 查询 rows
        List<JobLogPo> rows = new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .orderBy()
                .column("log_time", OrderByType.DESC)
                .limit(request.getStart(), request.getLimit())
                .list(RshHolder.JOB_LOGGER_LIST_RSH);
        response.setRows(rows);

        return response;
    }

    private WhereSql buildWhereSql(JobLoggerRequest request) {
        return new WhereSql()
                .andOnNotEmpty("task_id = ?", request.getTaskId())
                .andOnNotEmpty("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup())
                .andBetween("log_time", JdbcTypeUtils.toTimestamp(request.getStartLogTime()), JdbcTypeUtils.toTimestamp(request.getEndLogTime()))
                ;
    }

    private String getTableName() {
        return "lts_job_log_po";
    }
}
