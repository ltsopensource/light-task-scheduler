package com.lts.admin.access.face;

import com.lts.admin.request.JvmDataReq;
import com.lts.monitor.access.face.JVMThreadAccess;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface BackendJVMThreadAccess extends JVMThreadAccess {

    void delete(JvmDataReq request);

}
