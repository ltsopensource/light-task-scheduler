package com.lts.job.queue.mysql;

import com.alibaba.fastjson.TypeReference;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.file.FileUtils;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.core.util.JobQueueUtils;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.domain.JobFeedbackPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.store.jdbc.JdbcRepository;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public class MysqlJobFeedbackQueue extends JdbcRepository implements JobFeedbackQueue {

    public MysqlJobFeedbackQueue(Config config) {
        super(config);
    }

    @Override
    public boolean createQueue(String taskTrackerNodeGroup) {
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_feedback_queue.sql");
            String sql = FileUtils.read(is);
            getSqlTemplate().update(getRealSql(sql, taskTrackerNodeGroup));
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
        return false;
    }

    private String getTableName(String taskTrackerNodeGroup) {
        return JobQueueUtils.getFeedbackQueueName(taskTrackerNodeGroup);
    }

    private String getRealSql(String sql, String taskTrackerNodeGroup) {
        return sql.replace("{tableName}", getTableName(taskTrackerNodeGroup));
    }

    @Override
    public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return true;
        }
        // insert ignore duplicate record
        String sql = "INSERT IGNORE INTO `{tableName}` (" +
                " `gmt_created`, `job_result`)" +
                " VALUES (?,?)";
        Object[] params = new Object[2];
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            params[0] = jobFeedbackPo.getGmtCreated();
            params[1] = JSONUtils.toJSONString(jobFeedbackPo.getJobResult());
            try {
                String taskTrackerNodeGroup = jobFeedbackPo.getJobResult().getJob().getTaskTrackerNodeGroup();
                getSqlTemplate().update(getRealSql(sql, taskTrackerNodeGroup), params);
            } catch (SQLException e) {
                throw new JobQueueException(e);
            }
        }
        return true;
    }

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        String deleteSql = "DELETE FROM `{tableName}` WHERE id = ? ";
        try {
            getSqlTemplate().update(getRealSql(deleteSql, taskTrackerNodeGroup), jobId);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
        return true;
    }

    @Override
    public long getCount(String taskTrackerNodeGroup) {
        String sql = "SELECT COUNT(1) FROM `{tableName}`";
        try {
            return getSqlTemplate().queryForValue(getRealSql(sql, taskTrackerNodeGroup));
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobFeedbackPo> fetchTop(String taskTrackerNodeGroup, int top) {
        String selectSql = "SELECT * FROM `{tableName}` ORDER BY gmt_created ASC LIMIT 0, ?";
        try {
            return getSqlTemplate().query(getRealSql(selectSql, taskTrackerNodeGroup), jobFeedbackPoListResultSetHandler, top);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    ResultSetHandler<List<JobFeedbackPo>> jobFeedbackPoListResultSetHandler = new ResultSetHandler<List<JobFeedbackPo>>() {
        @Override
        public List<JobFeedbackPo> handle(ResultSet rs) throws SQLException {
            List<JobFeedbackPo> jobFeedbackPos = new ArrayList<JobFeedbackPo>();
            while (rs.next()) {
                JobFeedbackPo jobFeedbackPo = new JobFeedbackPo();
                jobFeedbackPo.setId(rs.getString("id"));
                jobFeedbackPo.setJobResult(JSONUtils.parse(rs.getString("job_result"), new TypeReference<JobResult>() {
                }));
                jobFeedbackPo.setGmtCreated(rs.getLong("gmt_created"));
                jobFeedbackPos.add(jobFeedbackPo);
            }
            return jobFeedbackPos;
        }
    };

}
