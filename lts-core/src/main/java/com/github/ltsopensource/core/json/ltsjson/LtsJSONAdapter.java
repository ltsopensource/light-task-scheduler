package com.github.ltsopensource.core.json.ltsjson;

import com.github.ltsopensource.core.json.JSONAdapter;
import com.github.ltsopensource.core.json.JSONArray;
import com.github.ltsopensource.core.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class LtsJSONAdapter implements JSONAdapter {

    @Override
    public String getName() {
        return "ltsjson";
    }

    @Override
    public <T> T parse(String json, Type type) {
        return com.github.ltsopensource.json.JSONObject.parseObject(json, type);
    }

    @Override
    public String toJSONString(Object obj) {
        return com.github.ltsopensource.json.JSONObject.toJSONString(obj);
    }

    @Override
    public JSONObject toJSONObject(Object obj) {
        com.github.ltsopensource.json.JSONObject jsonObject = new com.github.ltsopensource.json.JSONObject(obj);
        return new LtsJSONObject(jsonObject);
    }

    @Override
    public JSONArray toJSONArray(Object obj) {
        com.github.ltsopensource.json.JSONArray jsonArray = new com.github.ltsopensource.json.JSONArray(obj);
        return new LtsJSONArray(jsonArray);
    }

    @Override
    public JSONArray parseArray(String json) {
        return new LtsJSONArray(new com.github.ltsopensource.json.JSONArray(json));
    }

    @Override
    public JSONObject parseObject(String json) {
        return new LtsJSONObject(new com.github.ltsopensource.json.JSONObject(json));
    }

    @Override
    public JSONObject newJSONObject() {
        return new LtsJSONObject(new com.github.ltsopensource.json.JSONObject());
    }

    @Override
    public JSONObject newJSONObject(Map<String, Object> map) {
        return new LtsJSONObject(new com.github.ltsopensource.json.JSONObject(map));
    }

    @Override
    public JSONObject newJSONObject(int initialCapacity) {
        return new LtsJSONObject(new com.github.ltsopensource.json.JSONObject(initialCapacity));
    }

    @Override
    public JSONArray newJSONArray() {
        return new LtsJSONArray(new com.github.ltsopensource.json.JSONArray());
    }

    @Override
    public JSONArray newJSONArray(List<Object> list) {
        return new LtsJSONArray(new com.github.ltsopensource.json.JSONArray(list));
    }

    @Override
    public JSONArray newJSONArray(int initialCapacity) {
        return new LtsJSONArray(new com.github.ltsopensource.json.JSONArray(initialCapacity));
    }
}
