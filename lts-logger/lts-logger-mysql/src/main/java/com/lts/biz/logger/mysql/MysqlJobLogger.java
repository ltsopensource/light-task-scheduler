package com.lts.biz.logger.mysql;

import com.lts.biz.logger.JobLogException;
import com.lts.biz.logger.JobLogger;
import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.json.JSON;
import com.lts.core.constant.Constants;
import com.lts.core.constant.Level;
import com.lts.core.json.TypeReference;
import com.lts.store.jdbc.JdbcRepository;
import com.lts.store.jdbc.SqlBuilder;
import com.lts.web.response.PageResponse;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class MysqlJobLogger extends JdbcRepository implements JobLogger {

    private String insertSQL;

    public MysqlJobLogger(Config config) {
        super(config);
        doCreateTable();

        insertSQL = "INSERT INTO `lts_job_log_po` (`log_time`,`gmt_created`, `log_type`, `success`, `msg`" +
                ",`task_tracker_identity`, `level`, `task_id`, `job_id`" +
                ", `priority`, `submit_node_group`, `task_tracker_node_group`, `ext_params`, `need_feedback`" +
                ", `cron_expression`, `trigger_time`, `retry_times`)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        try {
            getSqlTemplate().update(insertSQL,
                    jobLogPo.getLogTime(),
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
                    jobLogPo.isNeedFeedback(),
                    jobLogPo.getCronExpression(),
                    jobLogPo.getTriggerTime(),
                    jobLogPo.getRetryTimes()
            );
        } catch (SQLException e) {
            throw new JobLogException(e.getMessage(), e);
        }
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
        int size = jobLogPos.size();

        Object[][] params = new Object[size][17];
        int index = 0;
        for (JobLogPo jobLogPo : jobLogPos) {
            int i = index++;
            params[i][0] = jobLogPo.getLogTime();
            params[i][1] = jobLogPo.getGmtCreated();
            params[i][2] = jobLogPo.getLogType().name();
            params[i][3] = jobLogPo.isSuccess();
            params[i][4] = jobLogPo.getMsg();
            params[i][5] = jobLogPo.getTaskTrackerIdentity();
            params[i][6] = jobLogPo.getLevel().name();
            params[i][7] = jobLogPo.getTaskId();
            params[i][8] = jobLogPo.getJobId();
            params[i][9] = jobLogPo.getPriority();
            params[i][10] = jobLogPo.getSubmitNodeGroup();
            params[i][11] = jobLogPo.getTaskTrackerNodeGroup();
            params[i][12] = JSON.toJSONString(jobLogPo.getExtParams());
            params[i][13] = jobLogPo.isNeedFeedback();
            params[i][14] = jobLogPo.getCronExpression();
            params[i][15] = jobLogPo.getTriggerTime();
            params[i][16] = jobLogPo.getRetryTimes();
        }

        try {
            getSqlTemplate().batchUpdate(insertSQL, params);
        } catch (SQLException e) {
            throw new JobLogException(e);
        }
    }

    private ResultSetHandler<List<JobLogPo>> RESULT_SET_HANDLER = new ResultSetHandler<List<JobLogPo>>() {
        @Override
        public List<JobLogPo> handle(ResultSet rs) throws SQLException {
            List<JobLogPo> result = new ArrayList<JobLogPo>();
            while (rs.next()) {
                JobLogPo jobLogPo = new JobLogPo();
                jobLogPo.setLogTime(rs.getLong("log_time"));
                jobLogPo.setGmtCreated(rs.getLong("gmt_created"));
                jobLogPo.setLogType(LogType.valueOf(rs.getString("log_type")));
                jobLogPo.setSuccess(rs.getBoolean("success"));
                jobLogPo.setMsg(rs.getString("msg"));
                jobLogPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
                jobLogPo.setLevel(Level.valueOf(rs.getString("level")));
                jobLogPo.setTaskId(rs.getString("task_id"));
                jobLogPo.setJobId(rs.getString("job_id"));
                jobLogPo.setPriority(rs.getInt("priority"));
                jobLogPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
                jobLogPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
                jobLogPo.setExtParams(JSON.parse(rs.getString("ext_params"), new TypeReference<Map<String, String>>(){}));
                jobLogPo.setNeedFeedback(rs.getBoolean("need_feedback"));
                jobLogPo.setCronExpression(rs.getString("cron_expression"));
                jobLogPo.setTriggerTime(rs.getLong("trigger_time"));
                jobLogPo.setRetryTimes(rs.getInt("retry_times"));
                result.add(jobLogPo);
            }
            return result;
        }
    };


    private Long getTimestamp(Date timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

    @Override
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {

        PageResponse<JobLogPo> response = new PageResponse<JobLogPo>();

        try {
            // 查询count
            SqlBuilder countSQL = new SqlBuilder("SELECT count(1) FROM `lts_job_log_po` ");
            countSQL.addCondition("task_id", request.getTaskId());
            countSQL.addCondition("task_tracker_node_group", request.getTaskTrackerNodeGroup());
            countSQL.addCondition("log_time", getTimestamp(request.getStartLogTime()), ">=");
            countSQL.addCondition("log_time", getTimestamp(request.getEndLogTime()), "<=");
            Long results = getSqlTemplate().queryForValue(countSQL.getSQL(), countSQL.getParams().toArray());
            response.setResults(results.intValue());
            if (results == 0) {
                return response;
            }
            // 查询 rows
            SqlBuilder rowsSQL = new SqlBuilder("SELECT * FROM `lts_job_log_po` ");
            rowsSQL.addCondition("task_id", request.getTaskId());
            rowsSQL.addCondition("task_tracker_node_group", request.getTaskTrackerNodeGroup());
            rowsSQL.addCondition("log_time", getTimestamp(request.getStartLogTime()), ">=");
            rowsSQL.addCondition("log_time", getTimestamp(request.getEndLogTime()), "<=");

            rowsSQL.addOrderBy("log_time", "DESC");
            rowsSQL.addLimit(request.getStart(), request.getLimit());
            List<JobLogPo> rows = getSqlTemplate().query(rowsSQL.getSQL(), RESULT_SET_HANDLER, rowsSQL.getParams().toArray());
            response.setRows(rows);
        } catch (SQLException e) {
            throw new JobLogException(e);
        }
        return response;
    }

    private void doCreateTable() {
        // 创建表
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_log_po.sql");
            getSqlTemplate().update(FileUtils.read(is, Constants.CHARSET));
        } catch (SQLException e) {
            throw new JobLogException("create table error!", e);
        } catch (IOException e) {
            throw new JobLogException("create table error!", e);
        }
    }
}
