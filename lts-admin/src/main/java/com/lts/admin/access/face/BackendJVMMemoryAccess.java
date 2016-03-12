package com.lts.admin.access.face;

import com.lts.admin.request.JvmDataReq;
import com.lts.monitor.access.face.JVMMemoryAccess;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMMemoryAccess extends JVMMemoryAccess{

    void delete(JvmDataReq request);

}
