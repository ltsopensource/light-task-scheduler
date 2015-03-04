package com.lts.job.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class JsonUtils {

    private JsonUtils() {
    }

    public static <T> T jsonToObject(String json, Type type) {
        return (T) JSONObject.parseObject(json, type);
    }

    public static String objectToJsonString(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    public static JSONObject objectToJson(Object obj) {
        return (JSONObject) JSONObject.toJSON(obj);
    }

    public static JSONArray strToJsonArray(String obj) {
        return JSON.parseArray(obj);
    }

    public static JSONObject strToJson(String obj) {
        return JSON.parseObject(obj);
    }

}

