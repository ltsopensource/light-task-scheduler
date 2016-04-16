package com.github.ltsopensource.zookeeper;

/**
 * @author Robert HG (254963746@qq.com) on 2/24/16.
 */
public interface DataListener {

    void dataChange(String dataPath, Object data) throws Exception;

    void dataDeleted(String dataPath) throws Exception;
}
