package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.json.JSON;
import com.lts.core.constant.Constants;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.json.TypeReference;
import com.lts.core.support.JobQueueUtils;
import com.lts.queue.JobFeedbackQueue;
import com.lts.queue.domain.JobFeedbackPo;
import com.lts.queue.exception.JobQueueException;
import com.lts.store.jdbc.JdbcRepository;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public class MysqlJobFeedbackQueue extends JdbcRepository implements JobFeedbackQueue {


    private final ConcurrentHashMap<String, String> SQL_CACHE_MAP = new ConcurrentHashMap<String, String>();

    public MysqlJobFeedbackQueue(Config config) {
        super(config);
    }

    @Override
    public boolean createQueue(String jobClientNodeGroup) {
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_feedback_queue.sql");
            String sql = FileUtils.read(is, Constants.CHARSET);
            getSqlTemplate().update(getRealSql(sql, jobClientNodeGroup));
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
        return false;
    }


    private String delTable = "DROP TABLE IF EXISTS {tableName};";

    @Override
    public boolean removeQueue(String jobClientNodeGroup) {
        try {
            getSqlTemplate().update(delTable.replace("{tableName}", JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup)));
            return true;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String getTableName(String jobClientNodeGroup) {
        return JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup);
    }

    private String getRealSql(String sql, String jobClientNodeGroup) {
        String key = sql.concat(jobClientNodeGroup);
        String fineSQL = SQL_CACHE_MAP.get(key);
        // 这里可以不用锁，多生成一次也不会产生什么问题
        if (fineSQL == null) {
            fineSQL = sql.replace("{tableName}", getTableName(jobClientNodeGroup));
            SQL_CACHE_MAP.put(key, fineSQL);
        }
        return fineSQL;
    }

    private String insertSQL = "INSERT IGNORE INTO `{tableName}` (" +
            " `gmt_created`, `job_result`)" +
            " VALUES (?,?)";

    @Override
    public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return true;
        }
        // insert ignore duplicate record
        Object[] params = new Object[2];
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            params[0] = jobFeedbackPo.getGmtCreated();
            params[1] = JSON.toJSONString(jobFeedbackPo.getTaskTrackerJobResult());
            try {
                String jobClientNodeGroup = jobFeedbackPo.getTaskTrackerJobResult().getJobWrapper().getJob().getSubmitNodeGroup();
                getSqlTemplate().update(getRealSql(insertSQL, jobClientNodeGroup), params);
            } catch (SQLException e) {
                throw new JobQueueException(e);
            }
        }
        return true;
    }

    String removeSQL = "DELETE FROM `{tableName}` WHERE id = ? ";

    @Override
    public boolean remove(String jobClientNodeGroup, String jobId) {
        try {
            getSqlTemplate().update(getRealSql(removeSQL, jobClientNodeGroup), jobId);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
        return true;
    }

    String countSQL = "SELECT COUNT(1) FROM `{tableName}`";

    @Override
    public long getCount(String jobClientNodeGroup) {
        try {
            return getSqlTemplate().queryForValue(getRealSql(countSQL, jobClientNodeGroup));
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    String selectSQL = "SELECT * FROM `{tableName}` ORDER BY gmt_created ASC LIMIT 0, ?";

    @Override
    public List<JobFeedbackPo> fetchTop(String jobClientNodeGroup, int top) {
        try {
            return getSqlTemplate().query(getRealSql(selectSQL, jobClientNodeGroup), jobFeedbackPoListResultSetHandler, top);
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
                jobFeedbackPo.setTaskTrackerJobResult(JSON.parse(rs.getString("job_result"), new TypeReference<TaskTrackerJobResult>(){}));
                jobFeedbackPo.setGmtCreated(rs.getLong("gmt_created"));
                jobFeedbackPos.add(jobFeedbackPo);
            }
            return jobFeedbackPos;
        }
    };

}
