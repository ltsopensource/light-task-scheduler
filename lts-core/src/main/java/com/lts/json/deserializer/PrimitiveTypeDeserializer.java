package com.lts.json.deserializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public class PrimitiveTypeDeserializer implements Deserializer {

    @SuppressWarnings("unchecked")
    public <T> T deserialize(Object object, Type type) {

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
        }

        return (T) object;
    }
}
