package com.github.ltsopensource.admin.access;

import com.github.ltsopensource.admin.access.face.*;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
@SPI(key = SpiExtensionKey.ACCESS_DB, dftValue = "mysql")
public interface BackendAccessFactory {

    BackendJobTrackerMAccess getJobTrackerMAccess(Config config);

    BackendJobClientMAccess getBackendJobClientMAccess(Config config);

    BackendJVMGCAccess getBackendJVMGCAccess(Config config);

    BackendJVMMemoryAccess getBackendJVMMemoryAccess(Config config);

    BackendJVMThreadAccess getBackendJVMThreadAccess(Config config);

    BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess(Config config);

    BackendTaskTrackerMAccess getBackendTaskTrackerMAccess(Config config);
}
