package com.github.ltsopensource.core.listener;

import com.github.ltsopensource.core.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 * Master 节点变化 监听器
 */
public interface MasterChangeListener {

    /**
     * 节点变化 监听
     * @param master master节点
     * @param isMaster 表示当前节点是不是master节点
     */
    public void change(Node master, boolean isMaster);

}
