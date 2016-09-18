package com.github.ltsopensource.ec;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件信息
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class EventInfo {

    private String topic;
    private Map<String, Object> params;

    public EventInfo(String topic) {
        this.topic = topic;
    }

    public void setParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(key, value);
    }

    public Object removeParam(String key) {
        if (params != null) {
            return params.remove(key);
        }
        return null;
    }

    public Object getParam(String key) {
        if (params != null) {
            return params.get(key);
        }
        return null;
    }

    public Map<String, Object> getParams() {
        return params == null ? new HashMap<String, Object>() : params;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
