package com.github.ltsopensource.core.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Robert HG (254963746@qq.com) on 11/19/15.
 */
public interface JSONArray {

    public int size();

    public boolean isEmpty();

    public boolean contains(Object o);

    public Iterator<Object> iterator();

    public Object[] toArray();

    public <T> T[] toArray(T[] a);

    public boolean add(Object e);

    public boolean remove(Object o);

    public boolean containsAll(Collection<?> c);

    public boolean addAll(Collection<? extends Object> c);

    public boolean addAll(int index, Collection<? extends Object> c);

    public boolean removeAll(Collection<?> c);

    public boolean retainAll(Collection<?> c);

    public void clear();

    public Object set(int index, Object element);

    public void add(int index, Object element);

    public Object remove(int index);

    public int indexOf(Object o);

    public int lastIndexOf(Object o);

    public ListIterator<Object> listIterator();

    public ListIterator<Object> listIterator(int index);

    public List<Object> subList(int fromIndex, int toIndex);

    public Object get(int index);

    public JSONObject getJSONObject(int index);

    public JSONArray getJSONArray(int index);

    public <T> T getObject(int index, Class<T> clazz);

    public Boolean getBoolean(int index);

    public boolean getBooleanValue(int index);

    public Byte getByte(int index);

    public byte getByteValue(int index);

    public Short getShort(int index);

    public short getShortValue(int index);

    public Integer getInteger(int index);

    public int getIntValue(int index);

    public Long getLong(int index);

    public long getLongValue(int index);

    public Float getFloat(int index);

    public float getFloatValue(int index);

    public Double getDouble(int index);

    public double getDoubleValue(int index);

    public BigDecimal getBigDecimal(int index);

    public BigInteger getBigInteger(int index);

    public String getString(int index);

    public java.util.Date getDate(int index);

    public java.sql.Date getSqlDate(int index);

    public java.sql.Timestamp getTimestamp(int index);
}
