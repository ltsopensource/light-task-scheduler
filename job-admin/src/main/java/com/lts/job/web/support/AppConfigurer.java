package com.lts.job.web.support;

import java.util.*;

/**
 * Created by hugui on 5/11/15.
 */
public class AppConfigurer {

    private static final Map<String, String> CONFIG = new HashMap<String, String>();

    static {
        try {
            List<String> configList = new ArrayList<String>(2);
            configList.add("config");
            Locale locale = Locale.getDefault();
            for (String config : configList) {
                try {
                    ResourceBundle localResource = ResourceBundle.getBundle(config, locale);
                    for (Object o : localResource.keySet()) {
                        String key = o.toString();
                        String value = localResource.getString(key);
                        CONFIG.put(key, value);
                    }
                } catch (MissingResourceException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
