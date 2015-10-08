package com.lts.web.support;

import com.lts.core.commons.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 系统的配置信息（lts-admin.cfg）
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class AppConfigurer {

    private static final Map<String, String> CONFIG = new HashMap<String, String>();
    private static final String CONF_NAME = "lts-admin.cfg";

    private static AtomicBoolean load = new AtomicBoolean(false);

    public static void load(String confPath) {
        try {
            if (load.compareAndSet(false, true)) {
                Properties conf = new Properties();

                if (StringUtils.isNotEmpty(confPath)) {
                    InputStream is = new FileInputStream(new File(confPath + "/" + CONF_NAME));
                    conf.load(is);
                } else {
                    InputStream is = AppConfigurer.class.getClassLoader().getResourceAsStream(CONF_NAME);
                    conf.load(is);
                }

                for (Map.Entry<Object, Object> entry : conf.entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    CONFIG.put(key, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> allConfig() {
        return CONFIG;
    }

    public static String getProperties(String name) {
        return CONFIG.get(name);
    }

    public static String getProperties(String name, String defaultValue) {
        String returnValue = CONFIG.get(name);
        if (returnValue == null || returnValue.equals("")) {
            returnValue = defaultValue;
        }
        return returnValue;
    }

    public static int getInteger(String name, int defaultValue) {
        String returnValue = CONFIG.get(name);
        if (returnValue == null || returnValue.equals("")) {
            return defaultValue;
        }
        return Integer.parseInt(returnValue.trim());
    }

    public static int getInteger(String name) {
        return Integer.parseInt(CONFIG.get(name));
    }

    public static boolean getBoolean(String name, boolean defaultValue) {
        String returnValue = CONFIG.get(name);
        if (returnValue == null || returnValue.equals("")) {
            return defaultValue;
        }
        return Boolean.valueOf(CONFIG.get(name));
    }

    public static boolean getBoolean(String name) {
        return Boolean.valueOf(CONFIG.get(name));
    }
}
