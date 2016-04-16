package com.github.ltsopensource.store.jdbc.builder;

import com.github.ltsopensource.core.commons.utils.StringUtils;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public enum OrderByType {
    DESC, ASC;

    public static OrderByType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return OrderByType.valueOf(value);
    }

}
