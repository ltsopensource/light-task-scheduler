package com.lts.web.repository.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public interface AbstractRepo {

    void executeSQL(@Param("sql") String sql);

}
