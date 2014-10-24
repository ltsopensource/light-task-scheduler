package com.lts.job.common.listener;

import com.lts.job.common.cluster.Node;

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
