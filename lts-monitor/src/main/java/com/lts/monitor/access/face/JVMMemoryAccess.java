package com.lts.monitor.access.face;


import com.lts.monitor.access.domain.JVMMemoryDataPo;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMMemoryAccess {

    void insert(List<JVMMemoryDataPo> pos);

}
