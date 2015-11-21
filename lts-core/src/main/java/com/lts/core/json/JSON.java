package com.lts.core.json;

import com.lts.core.commons.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class JSON {

    private static JSONAdapter aware = JSONFactory.getJSONAdapter();

    public static final <T> T parse(String json, Type type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return aware.parse(json, type);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final <T> T parse(String json, TypeReference<T> typeReference) {
        try {
            return aware.parse(json, typeReference.getType());
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final String toJSONString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return aware.toJSONString(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject toJSONObject(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return aware.toJSONObject(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray toJSONArray(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return aware.toJSONArray(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray parseArray(String obj) {
        try {
            return aware.parseArray(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject parseObject(String obj) {
        try {
            return aware.parseObject(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject() {
        try {
            return aware.newJSONObject();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray() {
        try {
            return aware.newJSONArray();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject(Map<String, Object> map) {
        try {
            return aware.newJSONObject(map);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject(int initialCapacity) {
        try {
            return aware.newJSONObject(initialCapacity);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray(List<Object> list) {
        try {
            return aware.newJSONArray(list);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray(int initialCapacity) {
        try {
            return aware.newJSONArray(initialCapacity);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

}

