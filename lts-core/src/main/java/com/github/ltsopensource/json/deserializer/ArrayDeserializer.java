package com.github.ltsopensource.json.deserializer;

import com.github.ltsopensource.json.JSONArray;
import com.github.ltsopensource.json.JSONParser;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public class ArrayDeserializer implements Deserializer {

    public static final ArrayDeserializer INSTANCE = new ArrayDeserializer();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T deserialize(Object object, Type type) {

        JSONArray jsonArray;
        if (object instanceof JSONArray) {
            jsonArray = (JSONArray) object;
        } else {
            jsonArray = new JSONArray(object);
        }

        Class componentClass = null;
        Type componentType = null;
        if (type instanceof GenericArrayType) {
            componentType = ((GenericArrayType) type).getGenericComponentType();
            if (componentType instanceof TypeVariable) {
                TypeVariable<?> componentVar = (TypeVariable<?>) componentType;
                componentType = componentVar.getBounds()[0];
            }
            if (componentType instanceof Class<?>) {
                componentClass = (Class<?>) componentType;
            }
        } else {
            Class clazz = (Class) type;
            componentType = componentClass = clazz.getComponentType();
        }

        int size = jsonArray.size();
        Object array = Array.newInstance(componentClass, size);

        for (int i = 0; i < size; i++) {
            Object value = jsonArray.get(i);

            Deserializer deserializer = JSONParser.getDeserializer(componentClass);
            Array.set(array, i, deserializer.deserialize(value, componentType));
        }

        return (T) array;
    }
}
