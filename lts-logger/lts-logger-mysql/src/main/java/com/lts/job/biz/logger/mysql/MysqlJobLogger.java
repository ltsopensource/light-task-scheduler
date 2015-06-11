package com.lts.job.biz.logger.mysql;

import com.lts.job.biz.logger.JobLogException;
import com.lts.job.biz.logger.JobLogUtils;
import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.commons.file.FileUtils;
import com.lts.job.core.commons.utils.JSONUtils;
import com.lts.job.store.jdbc.JdbcRepository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class MysqlJobLogger extends JdbcRepository implements JobLogger {

    private String insertSQL;

    public MysqlJobLogger(Config config) {
        super(config);
        doCreateTable();

        insertSQL = "INSERT INTO `lts_job_log_po` (`timestamp`, `log_type`, `success`, `msg`" +
                ", `code`, `task_tracker_identity`, `level`, `task_id`, `job_id`" +
                ", `priority`, `submit_node_group`, `task_tracker_node_group`, `ext_params`, `needFeedback`" +
                ", `cron_expression`, `trigger_time`)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        try {
            getSqlTemplate().update(insertSQL,
                    jobLogPo.getTimestamp(),
                    jobLogPo.getLogType().name(),
                    jobLogPo.isSuccess(),
                    jobLogPo.getMsg(),
                    jobLogPo.getCode(),
                    jobLogPo.getTaskTrackerIdentity(),
                    jobLogPo.getLevel().name(),
                    jobLogPo.getTaskId(),
                    jobLogPo.getJobId(),
                    jobLogPo.getPriority(),
                    jobLogPo.getSubmitNodeGroup(),
                    jobLogPo.getTaskTrackerNodeGroup(),
                    JSONUtils.toJSONString(jobLogPo.getExtParams()),
                    jobLogPo.isNeedFeedback(),
                    jobLogPo.getCronExpression(),
                    jobLogPo.getTriggerTime()
            );
        } catch (SQLException e) {
            throw new JobLogException(e.getMessage(), e);
        }
    }

    @Override
    public void log(BizLogPo bizLogPo) {
        log(JobLogUtils.bizConvert(bizLogPo));
    }

    private void doCreateTable() {
        // 创建表
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_log_po.sql");
            getSqlTemplate().update(FileUtils.read(is));
        } catch (SQLException e) {
            throw new RuntimeException("create table error!", e);
        } catch (IOException e) {
            throw new RuntimeException("create table error!", e);
        }
    }
}
