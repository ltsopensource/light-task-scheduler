package com.lts.core.commons.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 */
@SuppressWarnings("rawtypes")
public class ReflectionUtils {

    /**
     * 得到所有field , 包括 父类
     */
	public static Field[] findFields(Class clazz) throws IllegalAccessException {
        final List<Field> fieldList = new ArrayList<Field>();

        doWithDeclaredFields(clazz, new FieldCallback() {
            public void doWith(Field field) {
                fieldList.add(field);
            }
        });
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    public static Field findField(Class clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            throw e;
        }
    }

    private static void doWithDeclaredFields(Class clazz, FieldCallback fieldCallback) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldCallback.doWith(field);
        }
        if (clazz.getSuperclass() != null) {
            doWithDeclaredFields(clazz.getSuperclass(), fieldCallback);
        }
    }

    protected interface FieldCallback {

        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }
}
