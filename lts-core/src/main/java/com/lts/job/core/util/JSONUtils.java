package com.lts.job.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class JSONUtils {

    public static <T> T parse(String json, Type type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return (T) JSONObject.parseObject(json, type);
    }

    public static <T> T parse(String json, TypeReference<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return JSONObject.parseObject(json, type);
    }

    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSONObject.toJSONString(obj);
    }

    public static JSONObject toJSON(Object obj) {
        if (obj == null) {
            return null;
        }
        return (JSONObject) JSONObject.toJSON(obj);
    }

    public static JSONArray parseArray(String obj) {
        if (obj == null) {
            return null;
        }
        return JSON.parseArray(obj);
    }

    public static JSONObject parseObject(String obj) {
        if (obj == null) {
            return null;
        }
        return JSON.parseObject(obj);
    }

}

