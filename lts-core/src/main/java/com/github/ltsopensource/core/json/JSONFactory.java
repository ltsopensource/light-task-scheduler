package com.github.ltsopensource.core.json;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.json.fastjson.FastJSONAdapter;
import com.github.ltsopensource.core.json.jackson.JacksonJSONAdapter;
import com.github.ltsopensource.core.json.ltsjson.LtsJSONAdapter;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.spi.ServiceLoader;

/**
 * @author Robert HG (254963746@qq.com) on 11/19/15.
 */
public class JSONFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONFactory.class);

    private static volatile JSONAdapter JSON_ADAPTER;

    static {
        String json = System.getProperty("lts.json");
        if ("fastjson".equals(json)) {
            setJSONAdapter(new FastJSONAdapter());
        } else if ("jackson".equals(json)) {
            setJSONAdapter(new JacksonJSONAdapter());
        } else {
            try {
                setJSONAdapter(new FastJSONAdapter());
            } catch (Throwable ignored) {
                try {
                    setJSONAdapter(new JacksonJSONAdapter());
                } catch (Throwable ignored2) {
                    try {
                        setJSONAdapter(new LtsJSONAdapter());
                    } catch (Throwable ignored3) {
                        throw new JSONException("Please check JSON lib");
                    }
                }
            }
        }
    }

    public static void setJSONAdapter(String jsonAdapter) {
        if (StringUtils.isNotEmpty(jsonAdapter)) {
            setJSONAdapter(ServiceLoader.load(JSONAdapter.class, jsonAdapter));
        }
    }

    public static JSONAdapter getJSONAdapter() {
        return JSONFactory.JSON_ADAPTER;
    }

    public static void setJSONAdapter(JSONAdapter jsonAdapter) {
        if (jsonAdapter != null) {
            LOGGER.info("Using JSON lib " + jsonAdapter.getName());
            JSONFactory.JSON_ADAPTER = jsonAdapter;
        }
    }
}
