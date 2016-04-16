package com.github.ltsopensource.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.github.ltsopensource.core.json.TypeUtils.*;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class JSONArray implements Iterable<Object> {

    private final ArrayList<Object> list;

    public JSONArray() {
        this.list = new ArrayList<Object>();
    }

    public JSONArray(JSONTokenizer x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() != ']') {
            x.back();
            for (; ; ) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.list.add(JSONObject.NULL);
                } else {
                    x.back();
                    this.list.add(x.nextValue());
                }
                switch (x.nextClean()) {
                    case ',':
                        if (x.nextClean() == ']') {
                            return;
                        }
                        x.back();
                        break;
                    case ']':
                        return;
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    public JSONArray(String source) throws JSONException {
        this(new JSONTokenizer(source));
    }

    public JSONArray(Collection<?> collection) {
        this.list = new ArrayList<Object>();
        if (collection != null) {
            for (Object o : collection) {
                this.list.add(JSONObject.wrap(o));
            }
        }
    }

    public JSONArray(Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.put(JSONObject.wrap(Array.get(array, i)));
            }
        }
        if (Collection.class.isAssignableFrom(array.getClass())) {
            Collection<?> collection = (Collection<?>) array;
            for (Object o : collection) {
                this.put(JSONObject.wrap(o));
            }
        } else {
            throw new JSONException(
                    "JSONArray initial value should be a string or collection or array.");
        }
    }

    public Iterator<Object> iterator() {
        return list.iterator();
    }

    public Object get(int index) throws JSONException {
        return this.opt(index);
    }

    public Object opt(int index) {
        if (index < 0 || index >= this.length()) {
            return null;
        }
        Object value = list.get(index);
        if (value == JSONObject.NULL) {
            return null;
        }
        return value;
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object object = this.get(index);
        if (object == null) {
            return null;
        }
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        Object object = this.get(index);
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    public int length() {
        return this.list.size();
    }

    public JSONArray put(Object value) {
        this.list.add(value);
        return this;
    }

    public JSONArray put(int index, Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < this.length()) {
            this.list.set(index, value);
        } else {
            while (index != this.length()) {
                this.put(JSONObject.NULL);
            }
            this.put(value);
        }
        return this;
    }

    public Object remove(int index) {
        return index >= 0 && index < this.length()
                ? this.list.remove(index)
                : null;
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public String toString() {
        try {
            return this.write(new StringWriter()).toString();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    Writer write(Writer writer)
            throws JSONException {
        try {
            boolean commanate = false;
            int length = this.length();
            writer.write('[');

            if (length == 1) {
                JSONObject.writeValue(writer, this.list.get(0));
            } else if (length != 0) {
                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.write(',');
                    }
                    JSONObject.writeValue(writer, this.list.get(i));
                    commanate = true;
                }
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public boolean addAll(Collection<?> c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection<?> c) {
        return list.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public void clear() {
        list.clear();
    }

    public void add(int index, Object element) {
        list.add(index, element);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<Object> listIterator() {
        return list.listIterator();
    }

    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    public List<Object> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public <T> T getObject(int index, Class<T> clazz) {
        Object value = list.get(index);
        return castToJavaBean(value, clazz);
    }

    public Boolean getBoolean(int index) {
        Object value = get(index);

        if (value == null) {
            return null;
        }

        return castToBoolean(value);
    }

    public boolean getBooleanValue(int index) {
        Object value = get(index);

        if (value == null) {
            return false;
        }

        return castToBoolean(value);
    }

    public Byte getByte(int index) {
        Object value = get(index);

        return castToByte(value);
    }

    public byte getByteValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0;
        }

        return castToByte(value);
    }

    public Short getShort(int index) {
        Object value = get(index);

        return castToShort(value);
    }

    public short getShortValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0;
        }

        return castToShort(value);
    }

    public Integer getInteger(int index) {
        Object value = get(index);

        return castToInt(value);
    }

    public int getIntValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0;
        }

        return castToInt(value);
    }

    public Long getLong(int index) {
        Object value = get(index);

        return castToLong(value);
    }

    public long getLongValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0L;
        }

        return castToLong(value);
    }

    public Float getFloat(int index) {
        Object value = get(index);

        return castToFloat(value);
    }

    public float getFloatValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0F;
        }

        return castToFloat(value);
    }

    public Double getDouble(int index) {
        Object value = get(index);

        return castToDouble(value);
    }

    public double getDoubleValue(int index) {
        Object value = get(index);

        if (value == null) {
            return 0D;
        }

        return castToDouble(value);
    }

    public BigDecimal getBigDecimal(int index) {
        Object value = get(index);

        return castToBigDecimal(value);
    }

    public BigInteger getBigInteger(int index) {
        Object value = get(index);

        return castToBigInteger(value);
    }

    public String getString(int index) {
        Object value = get(index);

        return castToString(value);
    }

    public java.util.Date getDate(int index) {
        Object value = get(index);

        return castToDate(value);
    }

    public java.sql.Date getSqlDate(int index) {
        Object value = get(index);

        return castToSqlDate(value);
    }

    public java.sql.Timestamp getTimestamp(int index) {
        Object value = get(index);

        return castToTimestamp(value);
    }
}
