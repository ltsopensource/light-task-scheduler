package com.github.ltsopensource.json;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.PrimitiveTypeUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.json.bean.MethodInfo;
import com.github.ltsopensource.json.deserializer.PrimitiveTypeDeserializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.ltsopensource.core.json.TypeUtils.*;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class JSONObject {

    private static final class Null {
        protected final Object clone() {
            return this;
        }

        public boolean equals(Object object) {
            return object == null || object == this;
        }

        public String toString() {
            return "null";
        }
    }

    private final Map<String, Object> map;
    private static final ConcurrentMap<Class<?>, Set<MethodInfo>> METHOD_MAP = new ConcurrentHashMap<Class<?>, Set<MethodInfo>>();

    public static final Object NULL = new Null();

    public JSONObject() {
        this.map = new HashMap<String, Object>();
    }

    public JSONObject(JSONTokenizer x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ; ) {
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, x.nextValue());

            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    public JSONObject(Map<?, ?> map) {
        this.map = new HashMap<String, Object>();
        if (map != null) {
            for (final Entry<?, ?> e : map.entrySet()) {
                final Object value = e.getValue();
                if (value != null) {
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }
    }

    public JSONObject(Object bean) {
        this();
        if (Map.class.isAssignableFrom(bean.getClass())) {
            for (Entry<?, ?> entry : ((Map<?, ?>) bean).entrySet()) {
                this.map.put(entry.getKey().toString(), wrap(entry.getValue()));
            }
        } else {
            this.populateMap(bean);
        }
    }

    public JSONObject(String source) throws JSONException {
        this(new JSONTokenizer(source));
    }

    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        return this.opt(key);
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JSONException("JSONObject[" + quote(key)
                + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException("JSONObject[" + quote(key)
                + "] is not a JSONObject.");
    }

    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public int size() {
        return this.map.size();
    }

    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator<String> keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    private static String numberToString(Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0
                && string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    public Object opt(String key) {
        if (key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == NULL) {
            return null;
        }
        return value;
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        }
        if (value != null) {
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }

    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
        }
        return this;
    }

    private String quote(String string) {
        StringWriter sw = new StringWriter();
        try {
            return quote(string, sw).toString();
        } catch (IOException ignored) {
            return "";
        }
    }

    private void populateMap(Object bean) {
        Class<?> clazz = bean.getClass();
        Set<MethodInfo> methodInfos = getGetterMethodInfo(clazz);

        if (CollectionUtils.isNotEmpty(methodInfos)) {
            for (MethodInfo methodInfo : methodInfos) {
                try {
                    Object result = methodInfo.getMethod().invoke(bean, (Object[]) null);
                    if (result != null) {
                        this.map.put(methodInfo.getFieldName(), wrap(result));
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static Set<MethodInfo> getGetterMethodInfo(Class<?> clazz) {

        Set<MethodInfo> methodInfos = METHOD_MAP.get(clazz);

        if (methodInfos == null) {

            methodInfos = new HashSet<MethodInfo>();

            boolean includeSuperClass = clazz.getClassLoader() != null;

            Method[] methods = includeSuperClass ? clazz.getMethods() : clazz
                    .getDeclaredMethods();

            for (Method method : methods) {
                try {
                    if (Modifier.isPublic(method.getModifiers())) {
                        String name = method.getName();
                        String key = "";
                        if (name.startsWith("get")) {
                            if ("getClass".equals(name)
                                    || "getDeclaringClass".equals(name)) {
                                key = "";
                            } else {
                                key = name.substring(3);
                            }
                        } else if (name.startsWith("is")) {
                            key = name.substring(2);
                        }
                        if (key.length() > 0
                                && Character.isUpperCase(key.charAt(0))
                                && method.getParameterTypes().length == 0) {
                            if (key.length() == 1) {
                                key = key.toLowerCase();
                            } else if (!Character.isUpperCase(key.charAt(1))) {
                                key = key.substring(0, 1).toLowerCase()
                                        + key.substring(1);
                            }
                            methodInfos.add(new MethodInfo(key, method));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            METHOD_MAP.putIfAbsent(clazz, methodInfos);
        }

        return methodInfos;
    }

    private static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return w;
        }
        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.write('\\');
                    w.write(c);
                    break;
                case '/':
                    if (b == '<') {
                        w.write('\\');
                    }
                    w.write(c);
                    break;
                case '\b':
                    w.write("\\b");
                    break;
                case '\t':
                    w.write("\\t");
                    break;
                case '\n':
                    w.write("\\n");
                    break;
                case '\f':
                    w.write("\\f");
                    break;
                case '\r':
                    w.write("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                            || (c >= '\u2000' && c < '\u2100')) {
                        w.write("\\u");
                        hhhh = Integer.toHexString(c);
                        w.write("0000", 0, 4 - hhhh.length());
                        w.write(hhhh);
                    } else {
                        w.write(c);
                    }
            }
        }
        w.write('"');
        return w;
    }

    public Object remove(String key) {
        return this.map.remove(key);
    }

    public String toString() {
        try {
            StringWriter w = new StringWriter();
            return this.write(w).toString();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    protected static Object wrap(Object object) {
        try {
            if (object == null) {
                return NULL;
            }
            if (object instanceof JSONObject || object instanceof JSONArray
                    || NULL.equals(object)
                    || object instanceof Byte || object instanceof Character
                    || object instanceof Short || object instanceof Integer
                    || object instanceof Long || object instanceof Boolean
                    || object instanceof Float || object instanceof Double
                    || object instanceof String || object instanceof BigInteger
                    || object instanceof BigDecimal) {
                return object;
            }

            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                return new JSONArray(coll);
            }
            if (object.getClass().isArray()) {
                return new JSONArray(object);
            }
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                return new JSONObject(map);
            }

            if (object.getClass().isEnum()) {
                return ((Enum<?>) object).name();
            }

            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage
                    .getName() : "";
            if (objectPackageName.startsWith("java.")
                    || objectPackageName.startsWith("javax.")
                    || object.getClass().getClassLoader() == null) {
                return object.toString();
            }
            return new JSONObject(object);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    static Writer writeValue(Writer writer, Object value) throws JSONException, IOException {
        if (value == null) {
            writer.write("null");
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).write(writer);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).write(writer);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            new JSONObject(map).write(writer);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            new JSONArray(coll).write(writer);
        } else if (value.getClass().isArray()) {
            new JSONArray(value).write(writer);
        } else if (value instanceof Number) {
            writer.write(numberToString((Number) value));
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    private Writer write(Writer writer)
            throws JSONException {
        try {
            boolean commanate = false;
            final int length = this.size();
            Iterator<String> keys = this.keys();
            writer.write('{');

            if (length == 1) {
                String key = keys.next();
                writer.write(quote(key));
                writer.write(':');
                writeValue(writer, this.map.get(key));
            } else if (length != 0) {
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (commanate) {
                        writer.write(',');
                    }
                    writer.write(quote(key));
                    writer.write(':');
                    writeValue(writer, this.map.get(key));
                    commanate = true;
                }
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        Object value = map.get(key);
        return castToJavaBean(value, clazz);
    }

    public Boolean getBoolean(String key) {
        Object value = map.get(key);
        return castToBoolean(value);
    }

    public byte[] getBytes(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return castToBytes(value);
    }


    public boolean getBooleanValue(String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        return castToBoolean(key);
    }

    public Byte getByte(String key) {
        Object value = get(key);
        return castToByte(value);
    }

    public byte getByteValue(String key) {
        Object value = get(key);

        if (value == null) {
            return 0;
        }
        return castToByte(value);
    }

    public Short getShort(String key) {
        Object value = get(key);

        return castToShort(value);
    }

    public short getShortValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return castToShort(value);
    }

    public Integer getInteger(String key) {
        Object value = get(key);
        return castToInt(value);
    }

    public int getIntValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return castToInt(value);
    }

    public Long getLong(String key) {
        Object value = get(key);
        return castToLong(value);
    }

    public long getLongValue(String key) {
        Object value = get(key);
        if (value == null) {
            return 0L;
        }
        return castToLong(value);
    }

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

    public void clear() {
        map.clear();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    public static <T> T parseObject(String json, Type type) {
        // 如果是原始类型
        if (PrimitiveTypeUtils.isPrimitiveType(type)) {
            return new PrimitiveTypeDeserializer().deserialize(json, type);
        }

        Object object = null;
        if (StringUtils.isEmpty(json)) {
            throw new JSONException("illegal json: json is empty");
        }
        json = json.trim();
        if (json.startsWith("{")) {
            object = new JSONObject(json);
        } else if (json.startsWith("[")) {
            object = new JSONArray(json);
        } else {
            throw new JSONException("illegal json:" + json);
        }
        return JSONParser.parse(object, type);
    }

    public static String toJSONString(Object obj) {
        if (isJSONArray(obj.getClass())) {
            return new com.github.ltsopensource.json.JSONArray(obj).toString();
        }
        return new com.github.ltsopensource.json.JSONObject(obj).toString();
    }

    private static boolean isJSONArray(Class<?> clazz) {
        if (clazz.isArray()) {
            return true;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }
}
