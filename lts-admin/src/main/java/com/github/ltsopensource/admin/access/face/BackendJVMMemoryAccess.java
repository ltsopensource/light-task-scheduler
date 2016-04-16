package com.github.ltsopensource.admin.access.face;

import com.github.ltsopensource.admin.request.JvmDataReq;
import com.github.ltsopensource.admin.request.MDataPaginationReq;
import com.github.ltsopensource.monitor.access.domain.JVMMemoryDataPo;
import com.github.ltsopensource.monitor.access.face.JVMMemoryAccess;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMMemoryAccess extends JVMMemoryAccess{

    void delete(JvmDataReq request);

    List<JVMMemoryDataPo> queryAvg(MDataPaginationReq request);
}
