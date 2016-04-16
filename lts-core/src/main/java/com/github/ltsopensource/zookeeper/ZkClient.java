package com.github.ltsopensource.zookeeper;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/8/14.
 */
public interface ZkClient {

    String create(String path, boolean ephemeral, boolean sequential);

    String create(String path, Object data, boolean ephemeral, boolean sequential);

    boolean delete(String path);

    boolean exists(String path);

    <T> T getData(String path);

    void setData(String path, Object data);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);

    void removeChildListener(String path, ChildListener listener);

    void addDataListener(String path, DataListener listener);

    void removeDataListener(String path, DataListener listener);

    void addStateListener(StateListener listener);

    void removeStateListener(StateListener listener);

    boolean isConnected();

    void close();

}
