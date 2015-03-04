package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 * 节点注册接口
 */
public interface Registry {

    /**
     * 节点注册
     *
     * @param node
     */
    void register(Node node);

    /**
     * 节点 取消注册
     *
     * @param node
     */
    void unregister(Node node);

    void destroy();

}
