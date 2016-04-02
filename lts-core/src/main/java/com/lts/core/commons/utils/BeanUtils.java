package com.lts.core.commons.utils;

import com.lts.core.exception.LtsRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BeanUtils {

    public static Object deepClone(Object object) {
        try {
            if (object == null) {
                return null;
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

}
