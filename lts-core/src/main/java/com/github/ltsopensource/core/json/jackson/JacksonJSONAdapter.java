package com.github.ltsopensource.core.json.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ltsopensource.core.json.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 11/20/15.
 */
public class JacksonJSONAdapter implements JSONAdapter {

    private ObjectMapper mapper;

    public JacksonJSONAdapter() {
        mapper = new ObjectMapper();
        // 忽略多余的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 不输出为空的字段
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String getName() {
        return "jackson";
    }

    @Override
    public <T> T parse(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public String toJSONString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public JSONObject toJSONObject(Object obj) {
        Map<String, Object> map = mapper.convertValue(obj, new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, Object>>() {
        });
        return new JacksonJSONObject(map);
    }

    @Override
    public JSONArray toJSONArray(Object obj) {
        List<Object> list = mapper.convertValue(obj, new com.fasterxml.jackson.core.type.TypeReference<List<Object>>() {
        });
        return new JacksonJSONArray(list);
    }

    @Override
    public JSONArray parseArray(String json) {
        List<Object> list = parse(json, new TypeReference<List<Object>>() {
        }.getType());
        return new JacksonJSONArray(list);
    }

    @Override
    public JSONObject parseObject(String json) {
        Map<String, Object> map = parse(json, new TypeReference<Map<String, Object>>() {
        }.getType());
        return new JacksonJSONObject(map);
    }

    @Override
    public JSONObject newJSONObject() {
        return new JacksonJSONObject();
    }

    @Override
    public JSONObject newJSONObject(Map<String, Object> map) {
        return new JacksonJSONObject(map);
    }

    @Override
    public JSONObject newJSONObject(int initialCapacity) {
        return new JacksonJSONObject(initialCapacity);
    }

    @Override
    public JSONArray newJSONArray() {
        return new JacksonJSONArray();
    }

    @Override
    public JSONArray newJSONArray(List<Object> list) {
        return new JacksonJSONArray(list);
    }

    @Override
    public JSONArray newJSONArray(int initialCapacity) {
        return new JacksonJSONArray(initialCapacity);
    }

}
