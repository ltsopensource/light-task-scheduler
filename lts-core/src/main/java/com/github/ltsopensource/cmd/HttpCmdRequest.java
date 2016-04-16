package com.github.ltsopensource.cmd;

import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCmdRequest {

    private String command;
    private String nodeIdentity;

    private Map<String, String> params;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getNodeIdentity() {
        return nodeIdentity;
    }

    public void setNodeIdentity(String nodeIdentity) {
        this.nodeIdentity = nodeIdentity;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
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
