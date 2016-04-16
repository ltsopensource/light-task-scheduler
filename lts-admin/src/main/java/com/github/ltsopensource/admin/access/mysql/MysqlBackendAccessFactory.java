package com.github.ltsopensource.admin.access.mysql;

import com.github.ltsopensource.admin.access.BackendAccessFactory;
import com.github.ltsopensource.admin.access.face.*;
import com.github.ltsopensource.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlBackendAccessFactory implements BackendAccessFactory {
    @Override
    public BackendJobTrackerMAccess getJobTrackerMAccess(Config config) {
        return new MysqlBackendJobTrackerMAccess(config);
    }

    @Override
    public BackendJobClientMAccess getBackendJobClientMAccess(Config config) {
        return new MysqlBackendJobClientMAccess(config);
    }

    @Override
    public BackendJVMGCAccess getBackendJVMGCAccess(Config config) {
        return new MysqlBackendJVMGCAccess(config);
    }

    @Override
    public BackendJVMMemoryAccess getBackendJVMMemoryAccess(Config config) {
        return new MysqlBackendJVMMemoryAccess(config);
    }

    @Override
    public BackendJVMThreadAccess getBackendJVMThreadAccess(Config config) {
        return new MysqlBackendJVMThreadAccess(config);
    }

    @Override
    public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess(Config config) {
        return new MysqlBackendNodeOnOfflineLogAccess(config);
    }

    @Override
    public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess(Config config) {
        return new MysqlBackendTaskTrackerMAccess(config);
    }
}
