package com.lts.admin.access.face;

import com.lts.admin.request.JvmDataReq;
import com.lts.monitor.access.face.JVMGCAccess;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMGCAccess extends JVMGCAccess{

    void delete(JvmDataReq request);

}
