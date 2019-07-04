package com.github.ltsopensource.admin.support;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang.StringUtils;

/**
 * 系统的配置信息（lts-admin.cfg）
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class AppConfigurer {

    private static final Map<String, String> CONFIG = new HashMap<String, String>();
    private static final String CONF_NAME = "lts-admin.cfg";

    private static AtomicBoolean load = new AtomicBoolean(false);

    public static void load(String confPath) {
        String path = "";
        try {
            if (load.compareAndSet(false, true)) {
                Properties conf = new Properties();

                if (StringUtils.isNotEmpty(confPath)) {
                    path = confPath + "/" + CONF_NAME;
                    InputStream is = new FileInputStream(new File(path));
                    conf.load(is);
                } else {
                    path = CONF_NAME;
                    InputStream is = AppConfigurer.class.getClassLoader().getResourceAsStream(path);
                    conf.load(is);
                }

                for (Map.Entry<Object, Object> entry : conf.entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    CONFIG.put(key, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load config[" + path + "] error ", e);
        }
    }

    public static Map<String, String> allConfig() {
        return CONFIG;
    }

    public static String getProperty(String name) {
        return CONFIG.get(name);
    }

    public static String getProperty(String name, String defaultValue) {
        String returnValue = CONFIG.get(name);
        if (returnValue == null || returnValue.equals("")) {
            returnValue = defaultValue;
        }
        return returnValue;
    }
}
