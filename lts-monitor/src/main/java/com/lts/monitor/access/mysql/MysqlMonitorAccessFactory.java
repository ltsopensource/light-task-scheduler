package com.lts.monitor.access.mysql;

import com.lts.core.cluster.Config;
import com.lts.monitor.access.MonitorAccessFactory;
import com.lts.monitor.access.face.*;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlMonitorAccessFactory implements MonitorAccessFactory {

    @Override
    public JobTrackerMAccess getJobTrackerMAccess(Config config) {
        return new MysqlJobTrackerMAccess(config);
    }

    @Override
    public TaskTrackerMAccess getTaskTrackerMAccess(Config config) {
        return new MysqlTaskTrackerMAccess(config);
    }

    @Override
    public JVMGCAccess getJVMGCAccess(Config config) {
        return new MysqlJVMGCAccess(config);
    }

    @Override
    public JVMMemoryAccess getJVMMemoryAccess(Config config) {
        return new MysqlJVMMemoryAccess(config);
    }

    @Override
    public JVMThreadAccess getJVMThreadAccess(Config config) {
        return new MysqlJVMThreadAccess(config);
    }

    @Override
    public JobClientMAccess getJobClientMAccess(Config config) {
        return new MysqlJobClientMAccess(config);
    }

}
