package com.github.ltsopensource.core.commons.utils;

import com.github.ltsopensource.core.exception.LtsRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BeanUtils {

    public static Object deepClone(Object object) {
        try {
            if (object == null) {
                throw new IllegalArgumentException("source object is null");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            throw new LtsRuntimeException(e.getMessage(), e);
        }
    }

    public static Map<String, String> copyMap(Map<String, String> source) {
        if (source == null) {
            return null;
        }
        int size = CollectionUtils.sizeOf(source);
        Map<String, String> map = new HashMap<String, String>(size);
        for (Map.Entry<String, String> entry : source.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

}
