package com.github.ltsopensource.monitor.access;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.monitor.access.face.*;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
@SPI(key = ExtConfig.ACCESS_DB, dftValue = "mysql")
public interface MonitorAccessFactory {

    JobTrackerMAccess getJobTrackerMAccess(Config config);

    TaskTrackerMAccess getTaskTrackerMAccess(Config config);

    JVMGCAccess getJVMGCAccess(Config config);

    JVMMemoryAccess getJVMMemoryAccess(Config config);

    JVMThreadAccess getJVMThreadAccess(Config config);

    JobClientMAccess getJobClientMAccess(Config config);
}
