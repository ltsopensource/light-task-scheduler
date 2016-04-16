package com.github.ltsopensource.monitor.access.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.monitor.access.domain.TaskTrackerMDataPo;
import com.github.ltsopensource.monitor.access.face.TaskTrackerMAccess;
import com.github.ltsopensource.store.jdbc.builder.InsertSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlTaskTrackerMAccess extends MysqlAbstractJdbcAccess implements TaskTrackerMAccess {

    public MysqlTaskTrackerMAccess(Config config) {
        super(config);
    }

    @Override
    protected String getTableName() {
        return "lts_admin_task_tracker_monitor_data";
    }

    @Override
    public void insert(List<TaskTrackerMDataPo> taskTrackerMDataPos) {

        if (CollectionUtils.isEmpty(taskTrackerMDataPos)) {
            return;
        }

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "node_group",
                        "identity",
                        "timestamp",
                        "exe_success_num",
                        "exe_failed_num",
                        "exe_later_num",
                        "exe_exception_num",
                        "total_running_time");

        for (TaskTrackerMDataPo taskTrackerMDataPo : taskTrackerMDataPos) {
            insertSql.values(
                    taskTrackerMDataPo.getGmtCreated(),
                    taskTrackerMDataPo.getNodeGroup(),
                    taskTrackerMDataPo.getIdentity(),
                    taskTrackerMDataPo.getTimestamp(),
                    taskTrackerMDataPo.getExeSuccessNum(),
                    taskTrackerMDataPo.getExeFailedNum(),
                    taskTrackerMDataPo.getExeLaterNum(),
                    taskTrackerMDataPo.getExeExceptionNum(),
                    taskTrackerMDataPo.getTotalRunningTime()
            );
        }

        insertSql.doBatchInsert();

    }

}
