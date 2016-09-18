package com.github.ltsopensource.core.json.ltsjson;

import com.github.ltsopensource.core.json.JSONArray;
import com.github.ltsopensource.core.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class LtsJSONArray implements JSONArray {

    private com.github.ltsopensource.json.JSONArray jsonArray;

    public LtsJSONArray(com.github.ltsopensource.json.JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonArray.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return jsonArray.contains(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return jsonArray.iterator();
    }

    @Override
    public Object[] toArray() {
        return jsonArray.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return jsonArray.toArray(a);
    }

    @Override
    public boolean add(Object e) {
        jsonArray.put(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return jsonArray.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return jsonArray.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return jsonArray.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        return jsonArray.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return jsonArray.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return jsonArray.retainAll(c);
    }

    @Override
    public void clear() {
        jsonArray.clear();
    }

    @Override
    public Object set(int index, Object element) {
        return jsonArray.put(index, element);
    }

    @Override
    public void add(int index, Object element) {
        jsonArray.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return jsonArray.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return jsonArray.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return jsonArray.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator() {
        return jsonArray.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return jsonArray.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return jsonArray.subList(fromIndex, toIndex);
    }

    @Override
    public Object get(int index) {
        return jsonArray.get(index);
    }

    @Override
    public JSONObject getJSONObject(int index) {
        return new LtsJSONObject(jsonArray.getJSONObject(index));
    }

    @Override
    public JSONArray getJSONArray(int index) {
        return new LtsJSONArray(jsonArray.getJSONArray(index));
    }

    @Override
    public <T> T getObject(int index, Class<T> clazz) {
        return jsonArray.getObject(index, clazz);
    }

    @Override
    public Boolean getBoolean(int index) {
        return jsonArray.getBoolean(index);
    }

    @Override
    public boolean getBooleanValue(int index) {
        return jsonArray.getBooleanValue(index);
    }

    @Override
    public Byte getByte(int index) {
        return jsonArray.getByte(index);
    }

    @Override
    public byte getByteValue(int index) {
        return jsonArray.getByteValue(index);
    }

    @Override
    public Short getShort(int index) {
        return jsonArray.getShort(index);
    }

    @Override
    public short getShortValue(int index) {
        return jsonArray.getShortValue(index);
    }

    @Override
    public Integer getInteger(int index) {
        return jsonArray.getInteger(index);
    }

    @Override
    public int getIntValue(int index) {
        return jsonArray.getIntValue(index);
    }

    @Override
    public Long getLong(int index) {
        return jsonArray.getLong(index);
    }

    @Override
    public long getLongValue(int index) {
        return jsonArray.getLongValue(index);
    }

    @Override
    public Float getFloat(int index) {
        return jsonArray.getFloat(index);
    }

    @Override
    public float getFloatValue(int index) {
        return jsonArray.getFloatValue(index);
    }

    @Override
    public Double getDouble(int index) {
        return jsonArray.getDouble(index);
    }

    @Override
    public double getDoubleValue(int index) {
        return jsonArray.getDoubleValue(index);
    }

    @Override
    public BigDecimal getBigDecimal(int index) {
        return jsonArray.getBigDecimal(index);
    }

    @Override
    public BigInteger getBigInteger(int index) {
        return jsonArray.getBigInteger(index);
    }

    @Override
    public String getString(int index) {
        return jsonArray.getString(index);
    }

    @Override
    public Date getDate(int index) {
        return jsonArray.getDate(index);
    }

    @Override
    public java.sql.Date getSqlDate(int index) {
        return jsonArray.getSqlDate(index);
    }

    @Override
    public Timestamp getTimestamp(int index) {
        return jsonArray.getTimestamp(index);
    }
}
