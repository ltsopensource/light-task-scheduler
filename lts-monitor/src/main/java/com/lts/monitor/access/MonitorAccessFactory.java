package com.lts.monitor.access;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;
import com.lts.monitor.access.face.*;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
@SPI(key = SpiExtensionKey.ACCESS_DB, dftValue = "mysql")
public interface MonitorAccessFactory {

    JobTrackerMAccess getJobTrackerMAccess(Config config);

    TaskTrackerMAccess getTaskTrackerMAccess(Config config);

    JVMGCAccess getJVMGCAccess(Config config);

    JVMMemoryAccess getJVMMemoryAccess(Config config);

    JVMThreadAccess getJVMThreadAccess(Config config);

    JobClientMAccess getJobClientMAccess(Config config);
}
