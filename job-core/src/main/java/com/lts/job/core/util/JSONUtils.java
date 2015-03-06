package com.lts.job.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class JSONUtils {

    public static <T> T parse(String json, Type type) {
        return (T) JSONObject.parseObject(json, type);
    }

    public static String toJSONString(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    public static JSONObject toJSON(Object obj) {
        return (JSONObject) JSONObject.toJSON(obj);
    }

    public static JSONArray parseArray(String obj) {
        return JSON.parseArray(obj);
    }

    public static JSONObject parseObject(String obj) {
        return JSON.parseObject(obj);
    }

}

