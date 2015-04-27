package com.lts.job.core.remoting;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.loadbalance.LoadBalance;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.NettyRemotingClient;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         JobRemotingClient 包装了 NettyRemotingClient , 每次请求，都会随机连上一台JobTracker
 */
public class RemotingClientDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

    private NettyRemotingClient remotingClient;
    // 连JobTracker的负载均衡算法
    private LoadBalance loadBalance;
    private Application application;

    // JobTracker 是否可用
    private boolean serverEnable = false;
    private List<Node> jobTrackers;

    public RemotingClientDelegate(NettyRemotingClient remotingClient, Application application, LoadBalance loadBalance) {
        this.remotingClient = remotingClient;
        this.application = application;
        this.loadBalance = loadBalance;
        this.jobTrackers = new CopyOnWriteArrayList<Node>();
    }

    public Node getJobTrackerNode() throws JobTrackerNotFoundException {
        if (jobTrackers.size() == 0) {
            throw new JobTrackerNotFoundException("no available jobTracker!");
        }
        return loadBalance.select(jobTrackers, application.getConfig().getIdentity());
    }

    public void start() {
        remotingClient.start();
    }

    public boolean contains(Node jobTracker) {
        return jobTrackers.contains(jobTracker);
    }

    public void addJobTracker(Node jobTracker) {
        if (!contains(jobTracker)) {
            jobTrackers.add(jobTracker);
        }
    }

    public boolean removeJobTracker(Node jobTracker) {
        return jobTrackers.remove(jobTracker);
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

        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        }

        try {
            request.checkCommandBody();
            RemotingCommand response = remotingClient.invokeSync(jobTracker.getAddress(), request, application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
            return response;
        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Exception e) {
            // 将这个JobTracker移除
            jobTrackers.remove(jobTracker);
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

        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        }

        try {
            request.checkCommandBody();

            remotingClient.invokeAsync(jobTracker.getAddress(), request, application.getConfig().getInvokeTimeoutMillis(), invokeCallback);
            this.serverEnable = true;

        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Throwable e) {
            // 将这个JobTracker移除
            jobTrackers.remove(jobTracker);
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
        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        }

        try {
            request.checkCommandBody();
            remotingClient.invokeOneway(jobTracker.getAddress(), request, application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;

        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Throwable e) {
            // 将这个JobTracker移除
            jobTrackers.remove(jobTracker);
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
