package com.github.ltsopensource.monitor.access.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.monitor.access.domain.JobClientMDataPo;
import com.github.ltsopensource.monitor.access.face.JobClientMAccess;
import com.github.ltsopensource.store.jdbc.builder.InsertSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class MysqlJobClientMAccess extends MysqlAbstractJdbcAccess implements JobClientMAccess {

    public MysqlJobClientMAccess(Config config) {
        super(config);
    }

    @Override
    public void insert(List<JobClientMDataPo> jobClientMDataPos) {
        if (CollectionUtils.isEmpty(jobClientMDataPos)) {
            return;
        }
        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "node_group",
                        "identity",
                        "timestamp",
                        "submit_success_num",
                        "submit_failed_num",
                        "fail_store_num",
                        "submit_fail_store_num",
                        "handle_feedback_num");

        for (JobClientMDataPo jobClientMDataPo : jobClientMDataPos) {
            insertSql.values(
                    jobClientMDataPo.getGmtCreated(),
                    jobClientMDataPo.getNodeGroup(),
                    jobClientMDataPo.getIdentity(),
                    jobClientMDataPo.getTimestamp(),
                    jobClientMDataPo.getSubmitSuccessNum(),
                    jobClientMDataPo.getSubmitFailedNum(),
                    jobClientMDataPo.getFailStoreNum(),
                    jobClientMDataPo.getSubmitFailStoreNum(),
                    jobClientMDataPo.getHandleFeedbackNum()
            );
        }
        insertSql.doBatchInsert();
    }

    @Override
    protected String getTableName() {
        return "lts_admin_job_client_monitor_data";
    }
}
