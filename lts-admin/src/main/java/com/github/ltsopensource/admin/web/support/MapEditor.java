package com.github.ltsopensource.admin.web.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyEditorSupport;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class MapEditor extends PropertyEditorSupport {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(null);
        } else {
            try {
                setValue(objectMapper.writeValueAsString(text));
            } catch (JsonProcessingException e) {
                // FIXME
            }
        }
    }

    @Override
    public String getAsText() {
        Map<?, ?> value = (Map<?, ?>) getValue();

        if (value == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // FIXME
            return null;
        }
    }
}
