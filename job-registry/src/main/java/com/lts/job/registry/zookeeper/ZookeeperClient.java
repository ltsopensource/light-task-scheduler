package com.lts.job.registry.zookeeper;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/8/14.
 */
public interface ZookeeperClient {

    String create(String path, boolean ephemeral, boolean sequential);

    String create(String path, Object data, boolean ephemeral, boolean sequential);

    void delete(String path);

    boolean exists(String path);

    <T> T getData(String path);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);

    void removeChildListener(String path, ChildListener listener);

    void addStateListener(StateListener listener);

    void removeStateListener(StateListener listener);

    boolean isConnected();

    void close();

}
