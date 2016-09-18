package com.github.ltsopensource.autoconfigure;

import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;
import com.github.ltsopensource.core.cluster.AbstractJobNode;
import com.github.ltsopensource.core.cluster.Node;

import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/18/16.
 */
@ConfigurationProperties(prefix = "test.prop", locations = "autoconfigure.properties")
public class TestProperties {

    private String string;
    private Integer integer;
    private boolean bool;
    private Config config;
    private Map<String, String> map;
    private List<String> list;
    private Integer[] integers;
    private EnumValue enumValue;
    private Class<? extends AbstractJobNode> nodeClass;

    public EnumValue getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(EnumValue enumValue) {
        this.enumValue = enumValue;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public Integer[] getIntegers() {
        return integers;
    }

    public void setIntegers(Integer[] integers) {
        this.integers = integers;
    }

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

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Class<? extends AbstractJobNode> getNodeClass() {
        return nodeClass;
    }

    public void setNodeClass(Class<? extends AbstractJobNode> nodeClass) {
        this.nodeClass = nodeClass;
    }

    public static class Config {
        private String string;
        private Integer integer;
        private boolean bool;
        private Config config;
        private Map<String, String> map;
        private List<String> list;
        private Integer[] integers;
        private EnumValue enumValue;

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

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public Config getConfig() {
            return config;
        }

        public void setConfig(Config config) {
            this.config = config;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public Integer[] getIntegers() {
            return integers;
        }

        public void setIntegers(Integer[] integers) {
            this.integers = integers;
        }

        public EnumValue getEnumValue() {
            return enumValue;
        }

        public void setEnumValue(EnumValue enumValue) {
            this.enumValue = enumValue;
        }
    }

    public static enum EnumValue {
        ONE,
        TWO,
        THREE
    }
}
