package com.github.ltsopensource.monitor.access.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.monitor.access.MonitorAccessFactory;
import com.github.ltsopensource.monitor.access.face.*;

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
