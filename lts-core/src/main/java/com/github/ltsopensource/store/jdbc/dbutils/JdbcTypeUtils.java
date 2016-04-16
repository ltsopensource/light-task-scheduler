package com.github.ltsopensource.store.jdbc.dbutils;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class JdbcTypeUtils {

    public static Long toTimestamp(Date date) {

        if (date == null) {
            return null;
        }
        return date.getTime();
    }

    public static Date toDate(Long timestamp){
        if(timestamp == null){
            return null;
        }
        return new Date(timestamp);
    }
}
