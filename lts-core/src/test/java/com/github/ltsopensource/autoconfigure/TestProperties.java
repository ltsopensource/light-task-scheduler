package com.github.ltsopensource.autoconfigure;

import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;

import java.util.Map;

/**
 * Created by hugui.hg on 4/18/16.
 */
@ConfigurationProperties(prefix = "test.prop", locations = "autoconfigure.properties")
public class TestProperties {

    private String string;
    private Integer integer;
    private Config config;
    private Map<String, String> map;

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public static class Config {
        private String string;
        private boolean b;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public boolean isB() {
            return b;
        }

        public void setB(boolean b) {
            this.b = b;
        }
    }
}
