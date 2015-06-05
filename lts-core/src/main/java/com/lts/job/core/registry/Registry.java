package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         节点注册接口
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

    /**
     * 监听节点
     *
     * @param listener
     */
    void subscribe(Node node, NotifyListener listener);

    /**
     * 取消监听节点
     *
     * @param node
     * @param listener
     */
    void unsubscribe(Node node, NotifyListener listener);

    /**
     * 销毁
     */
    void destroy();
}
