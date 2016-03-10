package com.lts.admin.access.mapper;

import com.lts.admin.access.domain.JVMGCDataPo;
import com.lts.admin.request.JVMDataRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMGCRepo{

    void insert(List<JVMGCDataPo> pos);

    void delete(JVMDataRequest request);

}
