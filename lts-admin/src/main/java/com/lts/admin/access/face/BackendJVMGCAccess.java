package com.lts.admin.access.face;

import com.lts.admin.request.JvmDataReq;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.monitor.access.domain.JVMGCDataPo;
import com.lts.monitor.access.face.JVMGCAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMGCAccess extends JVMGCAccess {

    void delete(JvmDataReq request);

    List<JVMGCDataPo> queryAvg(MDataPaginationReq request);
}
