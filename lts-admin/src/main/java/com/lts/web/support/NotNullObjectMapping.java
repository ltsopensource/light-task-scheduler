package com.lts.web.support;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class NotNullObjectMapping extends ObjectMapper {

    public NotNullObjectMapping() {
        super();
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

