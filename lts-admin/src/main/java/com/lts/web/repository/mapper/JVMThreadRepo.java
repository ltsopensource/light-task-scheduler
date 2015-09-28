package com.lts.web.repository.mapper;

import com.lts.web.repository.domain.JVMThreadDataPo;
import com.lts.web.request.JVMDataRequest;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public interface JVMThreadRepo {

    void insert(List<JVMThreadDataPo> pos);

    void delete(JVMDataRequest request);

}
