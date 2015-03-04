package com.lts.job.core.listener;

import com.lts.job.core.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 * Master 节点变化 监听器
 */
public interface MasterNodeChangeListener {

    /**
     * master 为 master节点
     * isMaster 表示当前节点是不是master节点
     * @param master
     * @param isMaster
     */
    public void change(Node master, boolean isMaster);

}
