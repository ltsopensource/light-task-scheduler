package com.lts.web.support.spring;

import com.alibaba.fastjson.TypeReference;
import com.lts.core.commons.utils.JSONUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class MapEditor extends PropertyEditorSupport {

    public MapEditor() {
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(null);
        } else {
            setValue(JSONUtils.parse(text, new TypeReference<HashMap<String, String>>() {
            }));
        }
    }

    @Override
    public String getAsText() {
        Map value = (Map) getValue();

        if (value == null) {
            return "";
        }
        return JSONUtils.toJSONString(value);
    }
}
