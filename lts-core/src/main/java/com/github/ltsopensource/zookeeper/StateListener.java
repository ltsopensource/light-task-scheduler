package com.github.ltsopensource.zookeeper;

/**
 * @author Robert HG (254963746@qq.com) on 7/8/14.
 */
public interface StateListener {

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    void stateChanged(int connected);

}