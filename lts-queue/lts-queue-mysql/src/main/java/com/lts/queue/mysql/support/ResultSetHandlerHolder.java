package com.lts.queue.mysql.support;

import com.lts.core.json.JSON;
import com.lts.core.json.TypeReference;
import com.lts.queue.domain.JobPo;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class ResultSetHandlerHolder {

    public static final ResultSetHandler<JobPo> JOB_PO_RESULT_SET_HANDLER = new ResultSetHandler<JobPo>() {
        @Override
        public JobPo handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            return getJobPo(rs);
        }
    };

    public static final ResultSetHandler<List<JobPo>> JOB_PO_LIST_RESULT_SET_HANDLER = new ResultSetHandler<List<JobPo>>() {
        @Override
        public List<JobPo> handle(ResultSet rs) throws SQLException {
            List<JobPo> jobPos = new ArrayList<JobPo>();
            while (rs.next()) {
                jobPos.add(getJobPo(rs));
            }
            return jobPos;
        }
    };

    private static JobPo getJobPo(ResultSet rs) throws SQLException {
        JobPo jobPo = new JobPo();
        jobPo.setJobId(rs.getString("job_id"));
        jobPo.setPriority(rs.getInt("priority"));
        jobPo.setRetryTimes(rs.getInt("retry_times"));
        jobPo.setTaskId(rs.getString("task_id"));
        jobPo.setGmtCreated(rs.getLong("gmt_created"));
        jobPo.setGmtModified(rs.getLong("gmt_modified"));
        jobPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
        jobPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
        jobPo.setExtParams(JSON.parse(rs.getString("ext_params"), new TypeReference<HashMap<String, String>>(){}));
        jobPo.setIsRunning(rs.getBoolean("is_running"));
        jobPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
        jobPo.setCronExpression(rs.getString("cron_expression"));
        jobPo.setNeedFeedback(rs.getBoolean("need_feedback"));
        jobPo.setTriggerTime(rs.getLong("trigger_time"));
        return jobPo;
    }
}
