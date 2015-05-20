package com.lts.job.queue.mysql;

import com.alibaba.fastjson.TypeReference;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.domain.JobFeedbackPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.store.jdbc.JdbcRepository;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        doCreateTable();
    }

    private void doCreateTable() {
        // 创建表
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_feedback_po.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder createTableSql = new StringBuilder();
            String data = null;
            while ((data = br.readLine()) != null) {
                createTableSql.append(data);
            }
            getSqlTemplate().update(createTableSql.toString());
        } catch (SQLException e) {
            throw new RuntimeException("create table error!", e);
        } catch (IOException e) {
            throw new RuntimeException("create table error!", e);
        }
    }


    @Override
    public void add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return;
        }
        String sql = "INSERT INTO `lts_job_feedback_po` (" +
                " `gmt_created`, `job_result`)" +
                " VALUES (?,?)";
        Object[][] params = new Object[jobFeedbackPos.size()][2];
        int index = 0;
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            params[index][0] = jobFeedbackPo.getGmtCreated();
            params[index][1] = JSONUtils.toJSONString(jobFeedbackPo.getJobResult());
            index++;
        }
        try {
            getSqlTemplate().batchUpdate(sql, params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public void remove(String id) {
        String deleteSql = "DELETE FROM `lts_job_feedback_po` WHERE id = ? ";
        try {
            getSqlTemplate().update(deleteSql, id);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM `lts_job_feedback_po`";
        try {
            return getSqlTemplate().queryForValue(sql);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobFeedbackPo> fetch(int offset, int limit) {
        String selectSql = "SELECT * FROM `lts_job_feedback_po` ORDER BY gmt_created ASC LIMIT ?, ?";
        try {
            Object[] params = new Object[]{offset, limit};
            return getSqlTemplate().query(selectSql, jobFeedbackPoListResultSetHandler, params);
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
