package com.github.ltsopensource.core.json.jackson;

import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.json.JSONArray;
import com.github.ltsopensource.core.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import static com.github.ltsopensource.core.json.TypeUtils.*;

/**
 * @author Robert HG (254963746@qq.com) on 11/20/15.
 */
public class JacksonJSONObject implements JSONObject {

    private final Map<String, Object> map;

    public JacksonJSONObject() {
        this(16);
    }

    public JacksonJSONObject(int initialCapacity) {
        map = new HashMap<String, Object>(initialCapacity);
    }

    public JacksonJSONObject(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        Object value = map.get(key);

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        return JSON.toJSONObject(value);
    }

    @Override
    public JSONArray getJSONArray(String key) {
        Object value = map.get(key);

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        return JSON.toJSONArray(value);
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        Object value = map.get(key);
        return castToJavaBean(value, clazz);
    }

    @Override
    public Boolean getBoolean(String key) {
        Object value = map.get(key);
        return castToBoolean(value);
    }

    @Override
    public byte[] getBytes(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return castToBytes(value);
    }

    @Override
    public boolean getBooleanValue(String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        return castToBoolean(key);
    }

    @Override
    public Byte getByte(String key) {
        Object value = get(key);
        return castToByte(value);
    }

    @Override
    public byte getByteValue(String key) {
        Object value = get(key);

        if (value == null) {
            return 0;
        }
        return castToByte(value);
    }

    @Override
    public Short getShort(String key) {
        Object value = get(key);

        return castToShort(value);
    }

    @Override
    public short getShortValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return castToShort(value);
    }

    @Override
    public Integer getInteger(String key) {
        Object value = get(key);
        return castToInt(value);
    }

    @Override
    public int getIntValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return castToInt(value);
    }

    @Override
    public Long getLong(String key) {
        Object value = get(key);

        return castToLong(value);
    }

    @Override
    public long getLongValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0L;
        }
        return castToLong(value);
    }

    @Override
    public Float getFloat(String key) {
        Object value = get(key);
        return castToFloat(value);
    }

    public float getFloatValue(String key) {
        Object value = get(key);

        if (value == null) {
            return 0F;
        }

        return castToFloat(value);
    }

    public Double getDouble(String key) {
        Object value = get(key);

        return castToDouble(value);
    }

    public double getDoubleValue(String key) {
        Object value = get(key);

        if (value == null) {
            return 0D;
        }

        return castToDouble(value);
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);

        return castToBigDecimal(value);
    }

    public BigInteger getBigInteger(String key) {
        Object value = get(key);

        return castToBigInteger(value);
    }

    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public Date getDate(String key) {
        Object value = get(key);

        return castToDate(value);
    }

    public java.sql.Date getSqlDate(String key) {
        Object value = get(key);

        return castToSqlDate(value);
    }

    public Timestamp getTimestamp(String key) {
        Object value = get(key);

        return castToTimestamp(value);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Object remove(String key) {
        return map.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toJSONString() {
        return JSON.toJSONString(map);
    }

    public String toString() {
        return toJSONString();
    }
}
