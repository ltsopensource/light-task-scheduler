package com.github.ltsopensource.json.deserializer;

import com.github.ltsopensource.core.commons.utils.PrimitiveTypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public class PrimitiveTypeDeserializer implements Deserializer {

    @SuppressWarnings("unchecked")
    public <T> T deserialize(Object object, Type type) {
        return PrimitiveTypeUtils.convert(object, type);
    }
}
