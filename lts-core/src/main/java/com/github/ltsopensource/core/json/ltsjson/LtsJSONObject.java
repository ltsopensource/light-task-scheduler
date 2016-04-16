package com.github.ltsopensource.core.json.ltsjson;

import com.github.ltsopensource.core.json.JSONArray;
import com.github.ltsopensource.core.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class LtsJSONObject implements JSONObject {

    private com.github.ltsopensource.json.JSONObject jsonObject;

    public LtsJSONObject(com.github.ltsopensource.json.JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public int size() {
        return jsonObject.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return jsonObject.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return jsonObject.containsValue(value);
    }

    @Override
    public Object get(String key) {
        return jsonObject.get(key);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return new LtsJSONObject(jsonObject.getJSONObject(key));
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return new LtsJSONArray(jsonObject.getJSONArray(key));
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        return jsonObject.getObject(key, clazz);
    }

    @Override
    public Boolean getBoolean(String key) {
        return jsonObject.getBoolean(key);
    }

    @Override
    public byte[] getBytes(String key) {
        return jsonObject.getBytes(key);
    }

    @Override
    public boolean getBooleanValue(String key) {
        return jsonObject.getBooleanValue(key);
    }

    @Override
    public Byte getByte(String key) {
        return jsonObject.getByte(key);
    }

    @Override
    public byte getByteValue(String key) {
        return jsonObject.getByteValue(key);
    }

    @Override
    public Short getShort(String key) {
        return jsonObject.getShort(key);
    }

    @Override
    public short getShortValue(String key) {
        return jsonObject.getShortValue(key);
    }

    @Override
    public Integer getInteger(String key) {
        return jsonObject.getInteger(key);
    }

    @Override
    public int getIntValue(String key) {
        return jsonObject.getIntValue(key);
    }

    @Override
    public Long getLong(String key) {
        return jsonObject.getLong(key);
    }

    @Override
    public long getLongValue(String key) {
        return jsonObject.getLongValue(key);
    }

    @Override
    public Float getFloat(String key) {
        return jsonObject.getFloat(key);
    }

    @Override
    public float getFloatValue(String key) {
        return jsonObject.getFloatValue(key);
    }

    @Override
    public Double getDouble(String key) {
        return jsonObject.getDouble(key);
    }

    @Override
    public double getDoubleValue(String key) {
        return jsonObject.getDoubleValue(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return jsonObject.getBigDecimal(key);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return jsonObject.getBigInteger(key);
    }

    @Override
    public String getString(String key) {
        return jsonObject.getString(key);
    }

    @Override
    public Date getDate(String key) {
        return jsonObject.getDate(key);
    }

    @Override
    public java.sql.Date getSqlDate(String key) {
        return jsonObject.getSqlDate(key);
    }

    @Override
    public Timestamp getTimestamp(String key) {
        return jsonObject.getTimestamp(key);
    }

    @Override
    public Object put(String key, Object value) {
        return jsonObject.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        jsonObject.putAll(m);
    }

    @Override
    public void clear() {
        jsonObject.clear();
    }

    @Override
    public Object remove(String key) {
        return jsonObject.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return jsonObject.keySet();
    }

    @Override
    public Collection<Object> values() {
        return jsonObject.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return jsonObject.entrySet();
    }

    @Override
    public String toJSONString() {
        return jsonObject.toString();
    }

    public String toString() {
        return toJSONString();
    }
}
