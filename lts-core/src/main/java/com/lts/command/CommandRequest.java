package com.lts.command;

import com.lts.core.commons.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class CommandRequest {

    private String command;

    private Map<String, String> params;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParam(String key) {
        if (params != null) {
            return params.get(key);
        }
        return null;
    }

    public String getParam(String key, String defaultValue) {
        if (params != null) {
            String value = params.get(key);
            if (StringUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        }
        return null;
    }

    public void addParam(String key, String value) {
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(key, value);
    }

    public Map<String, String> getParams() {
        return params;
    }
}
