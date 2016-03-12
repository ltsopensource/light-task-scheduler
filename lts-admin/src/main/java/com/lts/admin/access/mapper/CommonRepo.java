package com.lts.admin.access.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public interface CommonRepo {

    void executeSQL(@Param("sql") String sql);

}
