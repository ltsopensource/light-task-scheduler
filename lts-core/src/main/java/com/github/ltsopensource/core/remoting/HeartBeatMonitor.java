package com.github.ltsopensource.core.remoting;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.HeartBeatRequest;
import com.github.ltsopensource.ec.EventInfo;
import com.github.ltsopensource.ec.EventSubscriber;
import com.github.ltsopensource.ec.Observer;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 如果用来发送心跳包，当没有连接上JobTracker的时候，启动快速检测连接；连接后，采用慢周期检测来保持长连接
 *
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 */
public class HeartBeatMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatMonitor.class.getSimpleName());

    // 用来定时发送心跳
    private final ScheduledExecutorService PING_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-HeartBeat-Ping", true));
    private ScheduledFuture<?> pingScheduledFuture;
    // 当没有可用的JobTracker的时候，启动这个来快速的检查（小间隔）
    private final ScheduledExecutorService FAST_PING_EXECUTOR = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-HeartBeat-Fast-Ping", true));
    private ScheduledFuture<?> fastPingScheduledFuture;

    private RemotingClientDelegate remotingClient;
    private AppContext appContext;
    private EventSubscriber jobTrackerUnavailableEventSubscriber;

    public HeartBeatMonitor(RemotingClientDelegate remotingClient, AppContext appContext) {
        this.remotingClient = remotingClient;
        this.appContext = appContext;
        this.jobTrackerUnavailableEventSubscriber = new EventSubscriber(HeartBeatMonitor.class.getName()
                + "_PING_" + appContext.getConfig().getIdentity(),
                new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        startFastPing();
                        stopPing();
                    }
                });
        appContext.getEventCenter().subscribe(new EventSubscriber(HeartBeatMonitor.class.getName()
                + "_NODE_ADD_" + appContext.getConfig().getIdentity(), new Observer() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                Node node = (Node) eventInfo.getParam("node");
                if (node == null || NodeType.JOB_TRACKER != node.getNodeType()) {
                    return;
                }
                try {
                    check(node);
                } catch (Throwable ignore) {
                }
            }
        }), EcTopic.NODE_ADD);
    }

    private AtomicBoolean pingStart = new AtomicBoolean(false);
    private AtomicBoolean fastPingStart = new AtomicBoolean(false);

    public void start() {
        startFastPing();
    }

    public void stop() {
        stopPing();
        stopFastPing();
    }

    private void startPing() {
        try {
            if (pingStart.compareAndSet(false, true)) {
                // 用来监听 JobTracker不可用的消息，然后马上启动 快速检查定时器
                appContext.getEventCenter().subscribe(jobTrackerUnavailableEventSubscriber, EcTopic.NO_JOB_TRACKER_AVAILABLE);
                if (pingScheduledFuture == null) {
                    pingScheduledFuture = PING_EXECUTOR_SERVICE.scheduleWithFixedDelay(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (pingStart.get()) {
                                        ping();
                                    }
                                }
                            }, 30, 30, TimeUnit.SECONDS);      // 30s 一次心跳
                }
                LOGGER.debug("Start slow ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Start slow ping failed.", t);
        }
    }

    private void stopPing() {
        try {
            if (pingStart.compareAndSet(true, false)) {
//                pingScheduledFuture.cancel(true);
//                PING_EXECUTOR_SERVICE.shutdown();
                appContext.getEventCenter().unSubscribe(EcTopic.NO_JOB_TRACKER_AVAILABLE, jobTrackerUnavailableEventSubscriber);
                LOGGER.debug("Stop slow ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop slow ping failed.", t);
        }
    }

    private void startFastPing() {
        if (fastPingStart.compareAndSet(false, true)) {
            try {
                // 2s 一次进行检查
                if (fastPingScheduledFuture == null) {
                    fastPingScheduledFuture = FAST_PING_EXECUTOR.scheduleWithFixedDelay(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (fastPingStart.get()) {
                                        ping();
                                    }
                                }
                            }, 500, 500, TimeUnit.MILLISECONDS);
                }
                LOGGER.debug("Start fast ping success.");
            } catch (Throwable t) {
                LOGGER.error("Start fast ping failed.", t);
            }
        }
    }

    private void stopFastPing() {
        try {
            if (fastPingStart.compareAndSet(true, false)) {
//                fastPingScheduledFuture.cancel(true);
//                FAST_PING_EXECUTOR.shutdown();
                LOGGER.debug("Stop fast ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop fast ping failed.", t);
        }
    }

    private AtomicBoolean running = new AtomicBoolean(false);

    private void ping() {
        try {
            if (running.compareAndSet(false, true)) {
                // to ensure only one thread go there
                try {
                    check();
                } finally {
                    running.compareAndSet(true, false);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Ping JobTracker error", t);
        }
    }

    private void check() {
        List<Node> jobTrackers = appContext.getSubscribedNodeManager().getNodeList(NodeType.JOB_TRACKER);
        if (CollectionUtils.isEmpty(jobTrackers)) {
            return;
        }
        for (Node jobTracker : jobTrackers) {
            check(jobTracker);
        }
    }

    private void check(Node jobTracker) {
        // 每个JobTracker 都要发送心跳
        if (beat(remotingClient, jobTracker.getAddress())) {
            remotingClient.addJobTracker(jobTracker);
            if (!remotingClient.isServerEnable()) {
                remotingClient.setServerEnable(true);
                appContext.getEventCenter().publishAsync(new EventInfo(EcTopic.JOB_TRACKER_AVAILABLE));
            } else {
                remotingClient.setServerEnable(true);
            }
            stopFastPing();
            startPing();
        } else {
            remotingClient.removeJobTracker(jobTracker);
        }
    }

    /**
     * 发送心跳
     */
    private boolean beat(RemotingClientDelegate remotingClient, String addr) {

        HeartBeatRequest commandBody = appContext.getCommandBodyWrapper().wrapper(new HeartBeatRequest());

        RemotingCommand request = RemotingCommand.createRequestCommand(
                JobProtos.RequestCode.HEART_BEAT.code(), commandBody);
        try {
            RemotingCommand response = remotingClient.getRemotingClient().invokeSync(addr, request, 5000);
            if (response != null && JobProtos.ResponseCode.HEART_BEAT_SUCCESS ==
                    JobProtos.ResponseCode.valueOf(response.getCode())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("heart beat success. ");
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return false;
    }

}
