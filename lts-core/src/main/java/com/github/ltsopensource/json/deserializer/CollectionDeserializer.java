package com.github.ltsopensource.json.deserializer;

import com.github.ltsopensource.json.JSONArray;
import com.github.ltsopensource.json.JSONException;
import com.github.ltsopensource.json.JSONParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public class CollectionDeserializer implements Deserializer {

    public static final CollectionDeserializer INSTANCE = new CollectionDeserializer();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T deserialize(Object object, Type type) {

        if (object instanceof String) {
            object = new JSONArray(object);
        }

        if (!(object instanceof JSONArray || object instanceof Collection)) {
            throw new IllegalArgumentException("object=" + object);
        }

        Class<?> rawClass = getRawClass(type);

        Collection list;
        if (rawClass == AbstractCollection.class) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
            list = new HashSet();
        } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet();
        } else if (rawClass.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet();
        } else if (rawClass.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(EnumSet.class)) {
            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>) itemType);
        } else {
            try {
                list = (Collection) rawClass.newInstance();
            } catch (Exception e) {
                throw new JSONException("create instance error, class " + rawClass.getName());
            }
        }

        Type itemType;
        if (type instanceof ParameterizedType) {
            itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            itemType = Object.class;
        }

        Iterable<Object> it = (Iterable) object;
        for (Object o : it) {
            Deserializer itemDeserializer = JSONParser.getDeserializer(itemType);
            list.add(itemDeserializer.deserialize(o, itemType));
        }
        return (T) list;
    }

    public Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) type).getRawType());
        } else {
            throw new JSONException("can't resolve the rawClass by " + type);
        }
    }

}
