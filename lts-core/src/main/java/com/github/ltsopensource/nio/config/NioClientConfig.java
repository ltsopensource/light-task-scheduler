package com.github.ltsopensource.nio.config;

/**
 * @author Robert HG (254963746@qq.com) on 2/3/16.
 */
public class NioClientConfig extends NioConfig {

    private int connectTimeout;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}

