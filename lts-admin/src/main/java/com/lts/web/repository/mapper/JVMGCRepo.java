package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.JVMGCDataPo;
import com.lts.web.request.JVMDataRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMGCRepo{

    void insert(List<JVMGCDataPo> pos);

    void delete(JVMDataRequest request);

}
