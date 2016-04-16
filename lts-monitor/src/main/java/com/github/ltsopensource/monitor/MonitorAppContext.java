package com.github.ltsopensource.monitor;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.monitor.access.face.*;
import com.github.ltsopensource.monitor.cmd.MDataSrv;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorAppContext extends AppContext {

    private int httpCmdPort;

    private JobTrackerMAccess jobTrackerMAccess;
    private TaskTrackerMAccess taskTrackerMAccess;
    private JobClientMAccess jobClientMAccess;
    private JVMGCAccess jvmGCAccess;
    private JVMMemoryAccess jvmMemoryAccess;
    private JVMThreadAccess jvmThreadAccess;

    private MDataSrv mDataSrv;

    public int getHttpCmdPort() {
        return httpCmdPort;
    }

    public void setHttpCmdPort(int httpCmdPort) {
        this.httpCmdPort = httpCmdPort;
    }

    public JobTrackerMAccess getJobTrackerMAccess() {
        return jobTrackerMAccess;
    }

    public void setJobTrackerMAccess(JobTrackerMAccess jobTrackerMAccess) {
        this.jobTrackerMAccess = jobTrackerMAccess;
    }

    public TaskTrackerMAccess getTaskTrackerMAccess() {
        return taskTrackerMAccess;
    }

    public void setTaskTrackerMAccess(TaskTrackerMAccess taskTrackerMAccess) {
        this.taskTrackerMAccess = taskTrackerMAccess;
    }

    public JVMGCAccess getJvmGCAccess() {
        return jvmGCAccess;
    }

    public void setJvmGCAccess(JVMGCAccess jvmGCAccess) {
        this.jvmGCAccess = jvmGCAccess;
    }

    public JVMMemoryAccess getJvmMemoryAccess() {
        return jvmMemoryAccess;
    }

    public void setJvmMemoryAccess(JVMMemoryAccess jvmMemoryAccess) {
        this.jvmMemoryAccess = jvmMemoryAccess;
    }

    public JVMThreadAccess getJvmThreadAccess() {
        return jvmThreadAccess;
    }

    public void setJvmThreadAccess(JVMThreadAccess jvmThreadAccess) {
        this.jvmThreadAccess = jvmThreadAccess;
    }

    public MDataSrv getMDataSrv() {
        return mDataSrv;
    }

    public void setMDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }

    public JobClientMAccess getJobClientMAccess() {
        return jobClientMAccess;
    }

    public void setJobClientMAccess(JobClientMAccess jobClientMAccess) {
        this.jobClientMAccess = jobClientMAccess;
    }

    public MDataSrv getmDataSrv() {
        return mDataSrv;
    }

    public void setmDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }
}
