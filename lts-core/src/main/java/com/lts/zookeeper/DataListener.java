package com.lts.zookeeper;

/**
 * Created by hugui.hg on 2/24/16.
 */
public interface DataListener {

    void dataChange(String dataPath, Object data) throws Exception;

    void dataDeleted(String dataPath) throws Exception;
}
