package com.github.ltsopensource.core.json;

import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class JSON {

    private static JSONAdapter adapter = JSONFactory.getJSONAdapter();

    public static final <T> T parse(String json, Type type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return adapter.parse(json, type);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final <T> T parse(String json, TypeReference<T> typeReference) {
        try {
            if(StringUtils.isEmpty(json)){
                return null;
            }
            return adapter.parse(json, typeReference.getType());
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final String toJSONString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return adapter.toJSONString(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject toJSONObject(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return adapter.toJSONObject(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray toJSONArray(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return adapter.toJSONArray(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray parseArray(String obj) {
        try {
            return adapter.parseArray(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject parseObject(String obj) {
        try {
            return adapter.parseObject(obj);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject() {
        try {
            return adapter.newJSONObject();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray() {
        try {
            return adapter.newJSONArray();
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject(Map<String, Object> map) {
        try {
            return adapter.newJSONObject(map);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONObject newJSONObject(int initialCapacity) {
        try {
            return adapter.newJSONObject(initialCapacity);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray(List<Object> list) {
        try {
            return adapter.newJSONArray(list);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static final JSONArray newJSONArray(int initialCapacity) {
        try {
            return adapter.newJSONArray(initialCapacity);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

}

