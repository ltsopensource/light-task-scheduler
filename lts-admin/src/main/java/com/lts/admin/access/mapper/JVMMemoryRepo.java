package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.JVMMemoryDataPo;
import com.lts.admin.request.JVMDataRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMMemoryRepo {

    void insert(List<JVMMemoryDataPo> pos);

    void delete(JVMDataRequest request);

}
