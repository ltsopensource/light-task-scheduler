package com.github.ltsopensource.monitor.access.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.monitor.access.domain.JobTrackerMDataPo;
import com.github.ltsopensource.monitor.access.face.JobTrackerMAccess;
import com.github.ltsopensource.store.jdbc.builder.InsertSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlJobTrackerMAccess extends MysqlAbstractJdbcAccess implements JobTrackerMAccess {

    public MysqlJobTrackerMAccess(Config config) {
        super(config);
    }

    @Override
    protected String getTableName() {
        return "lts_admin_job_tracker_monitor_data";
    }

    @Override
    public void insert(List<JobTrackerMDataPo> jobTrackerMDataPos) {

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "identity",
                        "timestamp",
                        "receive_job_num",
                        "push_job_num",
                        "exe_success_num",
                        "exe_failed_num",
                        "exe_later_num",
                        "exe_exception_num",
                        "fix_executing_job_num");

        for (JobTrackerMDataPo jobTrackerMDataPo : jobTrackerMDataPos) {
            insertSql.values(
                    jobTrackerMDataPo.getGmtCreated(),
                    jobTrackerMDataPo.getIdentity(),
                    jobTrackerMDataPo.getTimestamp(),
                    jobTrackerMDataPo.getReceiveJobNum(),
                    jobTrackerMDataPo.getPushJobNum(),
                    jobTrackerMDataPo.getExeSuccessNum(),
                    jobTrackerMDataPo.getExeFailedNum(),
                    jobTrackerMDataPo.getExeLaterNum(),
                    jobTrackerMDataPo.getExeExceptionNum(),
                    jobTrackerMDataPo.getFixExecutingJobNum()
            );
        }
        insertSql.doBatchInsert();
    }

}
