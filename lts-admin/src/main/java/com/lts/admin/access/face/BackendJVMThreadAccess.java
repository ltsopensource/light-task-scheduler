package com.lts.admin.access.face;

import com.lts.admin.request.JvmDataReq;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.monitor.access.domain.JVMThreadDataPo;
import com.lts.monitor.access.face.JVMThreadAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMThreadAccess extends JVMThreadAccess {

    void delete(JvmDataReq request);

    List<JVMThreadDataPo> queryAvg(MDataPaginationReq request);

}
