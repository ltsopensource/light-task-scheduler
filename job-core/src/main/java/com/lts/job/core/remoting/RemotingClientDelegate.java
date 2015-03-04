package com.lts.job.core.remoting;

import com.lts.job.core.cluster.NodeManager;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.support.Application;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.*;
import com.lts.job.remoting.netty.NettyRemotingClient;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         JobRemotingClient 包装了 NettyRemotingClient , 并持有 sticky 的 jobTracker 信息的引用,
 *         用来管理client连接的 jobTracker, 让 client 始终连接同一个, 除非当前连接的JobTracker 不可用了，才会切换到另外一个
 */
public class RemotingClientDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

    private NettyRemotingClient remotingClient;
    // 连接的jobTracker node
    private Node stickyJobTrackerNode;

    // JobTracker 是否可用
    private boolean serverEnable = false;

    public RemotingClientDelegate(NettyRemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }

    public Node getStickyJobTrackerNode() throws JobTrackerNotFoundException {

        if (stickyJobTrackerNode == null) {
            changeStickyJobTrackerNode();
        }

        return stickyJobTrackerNode;
    }

    public synchronized void changeStickyJobTrackerNode() throws JobTrackerNotFoundException {
        if (stickyJobTrackerNode != null && HeartBeater.beat(this, stickyJobTrackerNode.getAddress())) {
            return;
        }

        List<Node> jobTrackerNodes = NodeManager.getNodeList(NodeType.JOB_TRACKER);
        if (CollectionUtils.isEmpty(jobTrackerNodes)) {
            throw new JobTrackerNotFoundException("没有找到可用的JobTracker节点!");
        }

        Node node = getJobTackerNodeByRandom(new ArrayList<Node>(jobTrackerNodes));

        if (node == null) {
            throw new JobTrackerNotFoundException("没有找到可用的JobTracker节点!");
        }
        stickyJobTrackerNode = node;
    }

    /**
     * 随机连接一个可用的JobTracker节点来实现负载均衡
     *
     * @return
     */
    private Node getJobTackerNodeByRandom(List<Node> jobTrackerNodes) {

        if(jobTrackerNodes.size() == 0){
            return null;
        }

        int min = 1;
        int max = jobTrackerNodes.size();
        Random random = new Random();
        int index = random.nextInt(max) % (max - min + 1) + min - 1;

        Node jobTackerNode = jobTrackerNodes.get(index);

        if (HeartBeater.beat(this, jobTackerNode.getAddress())) {
            return jobTackerNode;
        }else{
            // 如果这个节点不可用，那么移除
            jobTrackerNodes.remove(index);
        }
        return getJobTackerNodeByRandom(jobTrackerNodes);
    }

    public void start() {
        remotingClient.start();
    }

    /**
     * 同步调用
     *
     * @param request
     * @return
     * @throws RemotingCommandFieldCheckException
     * @throws JobTrackerNotFoundException
     */
    public RemotingCommand invokeSync(RemotingCommand request) throws RemotingCommandFieldCheckException, JobTrackerNotFoundException {

        try {
            request.checkCommandBody();

            String addr = getStickyJobTrackerNode().getAddress();

            RemotingCommand response = remotingClient.invokeSync(addr, request, Application.Config.getInvokeTimeoutMillis());
            this.serverEnable = true;
            return response;

        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        } catch (Throwable e) {
            try {
                changeStickyJobTrackerNode();
            } catch (JobTrackerNotFoundException e1) {
                this.serverEnable = false;
                throw e1;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // 只要不是节点 不可用, 轮询所有节点请求
            return invokeSync(request);
        }
    }

    /**
     * 异步调用
     *
     * @param request
     * @param invokeCallback
     * @throws RemotingCommandFieldCheckException
     * @throws JobTrackerNotFoundException
     */
    public void invokeAsync(RemotingCommand request, InvokeCallback invokeCallback) throws RemotingCommandFieldCheckException, JobTrackerNotFoundException {
        try {
            request.checkCommandBody();

            String addr = getStickyJobTrackerNode().getAddress();

            remotingClient.invokeAsync(addr, request, Application.Config.getInvokeTimeoutMillis(), invokeCallback);
            this.serverEnable = true;

        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        } catch (Throwable e) {
            try {
                changeStickyJobTrackerNode();
            } catch (JobTrackerNotFoundException e1) {
                this.serverEnable = false;
                throw e1;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // 只要不是节点 不可用, 轮询所有节点请求
            invokeAsync(request, invokeCallback);
        }
    }

    /**
     * 单向调用
     *
     * @param request
     * @throws RemotingCommandFieldCheckException
     * @throws JobTrackerNotFoundException
     */
    public void invokeOneway(RemotingCommand request) throws RemotingCommandFieldCheckException, JobTrackerNotFoundException {
        try {
            request.checkCommandBody();

            String addr = getStickyJobTrackerNode().getAddress();

            remotingClient.invokeOneway(addr, request, Application.Config.getInvokeTimeoutMillis());
            this.serverEnable = true;

        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        } catch (Throwable e) {
            try {
                changeStickyJobTrackerNode();
            } catch (JobTrackerNotFoundException e1) {
                this.serverEnable = false;
                throw e1;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // 只要不是节点 不可用, 轮询所有节点请求
            invokeOneway(request);
        }
    }

    public void registerProcessor(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        remotingClient.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        remotingClient.registerDefaultProcessor(processor, executor);
    }

    public boolean isServerEnable() {
        return serverEnable;
    }

    public void setServerEnable(boolean serverEnable) {
        this.serverEnable = serverEnable;
    }

    public void shutdown() {
        remotingClient.shutdown();
    }

    public NettyRemotingClient getNettyClient() {
        return remotingClient;
    }
}
