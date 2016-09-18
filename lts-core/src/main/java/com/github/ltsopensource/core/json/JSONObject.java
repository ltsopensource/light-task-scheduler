package com.github.ltsopensource.core.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 11/19/15.
 */
public interface JSONObject {

    public int size();

    public boolean isEmpty();

    public boolean containsKey(String key);

    public boolean containsValue(Object value);

    public Object get(String key);

    public JSONObject getJSONObject(String key);

    public JSONArray getJSONArray(String key);

    public <T> T getObject(String key, Class<T> clazz);

    public Boolean getBoolean(String key);

    public byte[] getBytes(String key);

    public boolean getBooleanValue(String key);

    public Byte getByte(String key);

    public byte getByteValue(String key);

    public Short getShort(String key);

    public short getShortValue(String key);

    public Integer getInteger(String key);

    public int getIntValue(String key);

    public Long getLong(String key);

    public long getLongValue(String key);

    public Float getFloat(String key);

    public float getFloatValue(String key);

    public Double getDouble(String key);

    public double getDoubleValue(String key);

    public BigDecimal getBigDecimal(String key);

    public BigInteger getBigInteger(String key);

    public String getString(String key);

    public Date getDate(String key);

    public java.sql.Date getSqlDate(String key);

    public java.sql.Timestamp getTimestamp(String key);

    public Object put(String key, Object value);

    public void putAll(Map<? extends String, ? extends Object> m);

    public void clear();

    public Object remove(String key);

    public Set<String> keySet();

    public Collection<Object> values();

    public Set<Map.Entry<String, Object>> entrySet();

    public String toJSONString();

    public String toString();

}
