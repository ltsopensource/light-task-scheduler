package com.github.ltsopensource.json.deserializer;

import com.github.ltsopensource.json.JSONException;
import com.github.ltsopensource.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public class MapDeserializer implements Deserializer {

    public static final MapDeserializer INSTANCE = new MapDeserializer();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T deserialize(Object object, Type type) {

        Map<Object, Object> map = createMap(type);

        if (object instanceof Map) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
            return (T) map;
        }

        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
            return (T) map;
        }
        throw new JSONException("illegal object class:" + object.getClass() + " type:" + type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Map<Object, Object> createMap(Type type) {
        if (type == Properties.class) {
            return new Properties();
        }

        if (type == Hashtable.class) {
            return new Hashtable();
        }

        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }

        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }

        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }

        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return createMap(parameterizedType.getRawType());
        }

        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
            throw new JSONException("unsupported type " + type);
        }

        try {
            return (Map<Object, Object>) clazz.newInstance();
        } catch (Exception e) {
            throw new JSONException("unsupported type " + type, e);
        }
    }

}
