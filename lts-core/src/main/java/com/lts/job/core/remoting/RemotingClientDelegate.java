package com.lts.job.core.remoting;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.constant.EcTopic;
import com.lts.job.core.exception.JobTrackerNotFoundException;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.loadbalance.LoadBalance;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.ec.EventInfo;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.netty.NettyRemotingClient;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.protocol.RemotingCommand;

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
    private volatile boolean serverEnable = false;
    private List<Node> jobTrackers;

    public RemotingClientDelegate(NettyRemotingClient remotingClient, Application application) {
        this.remotingClient = remotingClient;
        this.application = application;
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getAdaptiveExtension();
        this.jobTrackers = new CopyOnWriteArrayList<Node>();
    }

    public Node getJobTrackerNode() throws JobTrackerNotFoundException {
        if (jobTrackers.size() == 0) {
            throw new JobTrackerNotFoundException("no available jobTracker!");
        }
        return loadBalance.select(application.getConfig(), jobTrackers,
                application.getConfig().getIdentity());
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
     */
    public RemotingCommand invokeSync(RemotingCommand request)
            throws JobTrackerNotFoundException {

        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            // publish msg
            EventInfo eventInfo = new EventInfo(EcTopic.NO_JOB_TRACKER_AVAILABLE);
            application.getEventCenter().publishAsync(eventInfo);
            throw e;
        }

        try {
            RemotingCommand response = remotingClient.invokeSync(jobTracker.getAddress(),
                    request, application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
            return response;
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
     */
    public void invokeAsync(RemotingCommand request, InvokeCallback invokeCallback)
            throws JobTrackerNotFoundException {

        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        }

        try {
            remotingClient.invokeAsync(jobTracker.getAddress(), request,
                    application.getConfig().getInvokeTimeoutMillis(), invokeCallback);
            this.serverEnable = true;

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
     */
    public void invokeOneway(RemotingCommand request)
            throws JobTrackerNotFoundException {
        Node jobTracker = null;
        try {
            jobTracker = getJobTrackerNode();
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            throw e;
        }

        try {
            remotingClient.invokeOneway(jobTracker.getAddress(), request,
                    application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;

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

    public void registerProcessor(int requestCode, NettyRequestProcessor processor,
                                  ExecutorService executor) {
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
