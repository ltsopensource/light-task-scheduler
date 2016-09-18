package com.github.ltsopensource.admin.access.face;

import com.github.ltsopensource.admin.request.JvmDataReq;
import com.github.ltsopensource.admin.request.MDataPaginationReq;
import com.github.ltsopensource.monitor.access.domain.JVMGCDataPo;
import com.github.ltsopensource.monitor.access.face.JVMGCAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMGCAccess extends JVMGCAccess {

    void delete(JvmDataReq request);

    List<JVMGCDataPo> queryAvg(MDataPaginationReq request);
}
