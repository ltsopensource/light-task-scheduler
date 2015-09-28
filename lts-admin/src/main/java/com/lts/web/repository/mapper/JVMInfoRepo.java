package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.JVMInfoDataPo;
import com.lts.web.request.JVMDataRequest;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMInfoRepo {

    void insert(JVMInfoDataPo po);

    JVMInfoDataPo select(JVMDataRequest request);

    void delete(JVMDataRequest request);
}
