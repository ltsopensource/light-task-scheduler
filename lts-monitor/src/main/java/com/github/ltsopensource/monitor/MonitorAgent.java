package com.github.ltsopensource.monitor;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public interface MonitorAgent {

    void start();

    void initRegistry();

    void stop();

}
