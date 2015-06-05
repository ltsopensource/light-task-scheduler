package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.DuplicateJobException;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.store.jdbc.JdbcRepository;

import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public abstract class AbstractMysqlJobQueue extends JdbcRepository {

    public AbstractMysqlJobQueue(Config config) {
        super(config);
    }

    protected boolean add(String tableName, JobPo jobPo) {
        String sql = "INSERT INTO " +
                "`" + tableName + "` ( " +
                "`job_id`, " +
                "`priority`, " +
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
                "`prev_exe_time`, " +
                "`trigger_time`)" +
                "VALUES " +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            getSqlTemplate().update(sql,
                    jobPo.getJobId(),
                    jobPo.getPriority(),
                    jobPo.getTaskId(),
                    jobPo.getGmtCreated(),
                    jobPo.getGmtModified(),
                    jobPo.getSubmitNodeGroup(),
                    jobPo.getTaskTrackerNodeGroup(),
                    JSONUtils.toJSONString(jobPo.getExtParams()),
                    jobPo.isRunning(),
                    jobPo.getTaskTrackerIdentity(),
                    jobPo.isNeedFeedback(),
                    jobPo.getCronExpression(),
                    jobPo.getPrevExeTime(),
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

}
