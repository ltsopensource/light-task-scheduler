package com.lts.json;

import com.lts.json.deserializer.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 12/29/15.
 */
public class JSONParser {

    protected static final ConcurrentMap<Type, Deserializer>
            deserializerMap = new ConcurrentHashMap<Type, Deserializer>();

    private final static Set<Class<?>> primitiveClasses = new HashSet<Class<?>>();

    static {
        primitiveClasses.add(boolean.class);
        primitiveClasses.add(byte.class);
        primitiveClasses.add(short.class);
        primitiveClasses.add(int.class);
        primitiveClasses.add(long.class);
        primitiveClasses.add(float.class);
        primitiveClasses.add(double.class);

        primitiveClasses.add(Boolean.class);
        primitiveClasses.add(Byte.class);
        primitiveClasses.add(Short.class);
        primitiveClasses.add(Integer.class);
        primitiveClasses.add(Long.class);
        primitiveClasses.add(Float.class);
        primitiveClasses.add(Double.class);

        primitiveClasses.add(BigInteger.class);
        primitiveClasses.add(BigDecimal.class);
        primitiveClasses.add(String.class);
    }

    public static <T> T parse(Object object, Type type) {

        Deserializer deserializer = getDeserializer(type);

        return deserializer.deserialize(object, type);
    }

    public static Deserializer getDeserializer(Type type) {

        Deserializer deserializer = deserializerMap.get(type);
        if (deserializer != null) {
            return deserializer;
        }

        if (type instanceof Class<?>) {
            return getDeserializer((Class<?>) type, type);
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                return getDeserializer((Class<?>) rawType, type);
            } else {
                return getDeserializer(rawType);
            }
        }

        if (type instanceof GenericArrayType) {
            return ArrayDeserializer.INSTANCE;
        }

        throw new IllegalArgumentException("can't get the Deserializer by " + type);
    }

    private static Deserializer getDeserializer(Class<?> clazz, Type type) {

        Deserializer deserializer;

        if (isPrimitiveClasses(clazz)) {
            deserializer = new PrimitiveTypeDeserializer();
        } else if (clazz.isEnum()) {
            deserializer = new EnumDeserializer(clazz);
        } else if (clazz.isArray()) {
            deserializer = ArrayDeserializer.INSTANCE;
        } else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class
                || clazz == ArrayList.class) {
            deserializer = CollectionDeserializer.INSTANCE;
        } else if (Collection.class.isAssignableFrom(clazz)) {
            deserializer = CollectionDeserializer.INSTANCE;
        } else if (Map.class.isAssignableFrom(clazz)) {
            deserializer = MapDeserializer.INSTANCE;
        } else {
            deserializer = new JavaBeanDeserializer(clazz);
        }

        deserializerMap.put(type, deserializer);

        return deserializer;
    }

    public static boolean isPrimitiveClasses(Class<?> clazz) {
        return primitiveClasses.contains(clazz);
    }

}
