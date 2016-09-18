package com.github.ltsopensource.core.commons.utils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public class PrimitiveTypeUtils {

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

    public static boolean isPrimitiveClass(Class<?> clazz) {
        return primitiveClasses.contains(clazz);
    }

    public static boolean isPrimitiveType(Type type) {
        return primitiveClasses.contains(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Type type) {
        String valString = object.toString();
        if (type == Byte.class || type == byte.class) {
            return (T) Byte.valueOf(valString);
        } else if (type == Short.class || type == short.class) {
            return (T) Short.valueOf(valString);
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(valString);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(valString);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(valString);
        } else if (type == Float.class || type == float.class) {
            return (T) Float.valueOf(valString);
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(valString);
        } else if (type == BigInteger.class) {
            return (T) new BigInteger(valString);
        } else if (type == BigDecimal.class) {
            return (T) new BigDecimal(valString);
        } else if (type == String.class) {
            return (T) valString;
        }
        return (T) object;
    }

    public static Class<?> getUnBoxType(Class<?> boxType) {
        if (boxType == null) {
            return null;
        }
        if (boxType == Byte.class) {
            return byte.class;
        } else if (boxType == Short.class) {
            return short.class;
        } else if (boxType == Integer.class) {
            return int.class;
        } else if (boxType == Long.class) {
            return long.class;
        } else if (boxType == Boolean.class) {
            return boolean.class;
        } else if (boxType == Float.class) {
            return float.class;
        } else {
            return boxType;
        }
    }
}
