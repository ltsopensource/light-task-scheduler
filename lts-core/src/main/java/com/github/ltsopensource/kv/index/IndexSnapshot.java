package com.github.ltsopensource.kv.index;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public interface IndexSnapshot<K, V> {

    /**
     * 初始化, 包括从磁盘中加载, 重放事务日志等
     */
    public void init() throws IOException;

    /**
     * 快照
     */
    public void snapshot() throws IOException;

}
