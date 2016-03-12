package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.JVMInfoDataPo;
import com.lts.admin.request.JVMDataRequest;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMInfoRepo {

    void insert(JVMInfoDataPo po);

    JVMInfoDataPo select(JVMDataRequest request);

    void delete(JVMDataRequest request);
}
