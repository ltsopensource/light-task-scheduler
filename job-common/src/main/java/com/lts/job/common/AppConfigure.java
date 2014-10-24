package com.lts.job.common;

import com.lts.job.common.support.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class AppConfigure {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigure.class);

    private static Map<String, String> configMap = new HashMap<String, String>();

    static {
        try {
            List<String> configList = new ArrayList<String>(2);
            configList.add("job-config");
            configList.add("job-config-ext");

            Locale locale = Locale.getDefault();

            for (String config : configList) {
                try {
                    ResourceBundle localResource = ResourceBundle.getBundle(config, locale);
                    for (Object o : localResource.keySet()) {
                        String key = o.toString();
                        String value = localResource.getString(key);
                        configMap.put(key, value);
                    }
                } catch (Throwable t) {
                    LOGGER.warn(config + "属性文件加载失败!");
                }
            }
            if(LOGGER.isDebugEnabled()){
                for (Map.Entry<String, String> entry : configMap.entrySet()) {
                    LOGGER.debug(entry.getKey()+"=" + entry.getValue());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static String getString(String name) {
        return configMap.get(name);
    }

    public static String getString(String name, String defaultValue) {
        String returnValue = configMap.get(name);
        if (returnValue == null || returnValue.equals("")) {
            returnValue = defaultValue;
        }
        return returnValue;
    }

    public static int getInteger(String name, int defaultValue) {
        String returnValue = configMap.get(name);
        if (returnValue == null || returnValue.equals("")) {
            return defaultValue;
        }
        return Integer.parseInt(returnValue.trim());
    }

    public static int getInteger(String name) {
        return Integer.parseInt(configMap.get(name));
    }

    public static boolean getBoolean(String name, boolean defaultValue) {
        String returnValue = configMap.get(name);
        if (returnValue == null || returnValue.equals("")) {
            return defaultValue;
        }
        return Boolean.valueOf(configMap.get(name));
    }

    public static boolean getBoolean(String name) {
        return Boolean.valueOf(configMap.get(name));
    }

}
